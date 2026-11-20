import contextlib
from copy import deepcopy
import io
import os
from pathlib import Path
import subprocess
import tarfile
import tempfile
import unittest
from unittest.mock import patch

import deploy


class DeploymentTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.profiles = {}
        path = os.environ.get("PATH", os.defpath)
        for profile in ("cluster", "cluster-agent", "existing", "existing-agent", "local", "local-agent"):
            with patch.dict(os.environ, {"PROFILE": profile, "PATH": path}, clear=True):
                _, items = deploy.render()
                deploy.check(items)
                cls.profiles[profile] = items

    def test_existing_reuses_claims_and_legacy_service_ports(self):
        items = self.profiles["existing"]
        self.assertFalse(any(item["kind"] in deploy.STORAGE for item in items))
        services = {deploy.name(item): item["spec"] for item in items if item["kind"] == "Service"}
        for service in ("database", "master", "monitor", "mover"):
            self.assertEqual(services[f"ecpds-{service}"]["type"], "NodePort")
        for service, first in (("master", 60000), ("mover", 60010)):
            self.assertTrue(set(range(first, first + 10)) <=
                            {item["port"] for item in services[f"ecpds-{service}"]["ports"]})
            self.assertNotIn("type", services[f"ecpds-{service}-jolokia"])
        self.assertIn("data-openbao-0", deploy.mounted_claims(items))

    def test_local_provisioning_is_namespace_pinned_and_retained(self):
        items = deepcopy(self.profiles["local"])
        with patch.dict(os.environ, {"LOCAL_NODE": "my-node"}), patch.object(deploy, "Cluster") as cluster:
            target = cluster.return_value
            target.namespace = "verification"
            target.get.side_effect = lambda *args, **kw: {"items": [{}]} if args[0] == "nodes" else None
            deploy.storage(items, target)
            resources = target.apply.call_args.args[0]
            for volume in (item for item in resources if item["kind"] == "PersistentVolume"):
                name = deploy.name(volume)
                self.assertEqual(volume["spec"]["persistentVolumeReclaimPolicy"], "Retain")
                self.assertEqual(volume["spec"]["claimRef"], {"name": name, "namespace": "verification"})
            self.assertEqual({deploy.name(v) for v in resources if v["kind"] == "PersistentVolumeClaim"},
                             deploy.mounted_claims(items))

    def test_agent_identity_and_token_are_not_mounted_on_other_containers(self):
        for profile, items in self.profiles.items():
            master = next(item for item in items if deploy.ref(item) == "statefulset/ecpds-master")
            pod = deploy.pod_spec(master)
            self.assertFalse(pod["automountServiceAccountToken"])
            containers = {container["name"]: container for container in pod["containers"]}
            app_mounts = {m["name"]: m for m in containers["ecpds-master"]["volumeMounts"]}
            self.assertNotIn("openbao-identity", app_mounts)
            if profile.endswith("-agent"):
                self.assertTrue(app_mounts["openbao-token"]["readOnly"])
                self.assertIn("openbao-identity", {v["name"] for v in containers["openbao-agent"]["volumeMounts"]})
                self.assertTrue(any(deploy.name(item).startswith("ecpds-master-openbao-agent-")
                                    for item in items if item["kind"] == "ConfigMap"))
            else:
                self.assertNotIn("openbao-agent", containers)

    def test_apply_and_teardown_do_not_mutate_storage(self):
        items = self.profiles["local"]
        with patch.dict(os.environ, {"PROFILE": "local"}), patch.object(deploy, "render", return_value=(b"", items)), \
                patch.object(deploy, "Cluster") as cluster, patch.object(deploy, "prerequisites") as prereqs:
            deploy.main("apply")
            prereqs.assert_called_once()
            target = cluster.return_value
            self.assertFalse(any(item["kind"] in deploy.STORAGE for item in target.apply.call_args.args[0]))
            deploy.main("delete-workloads")
            args = target.call.call_args.args
            self.assertEqual(args[0], "delete")
            self.assertIn("--cascade=foreground", args)
            self.assertTrue(all(arg.startswith(("deployment/", "statefulset/", "--")) for arg in args[1:]))

    def test_apply_refuses_legacy_pod(self):
        items = self.profiles["existing"]
        with patch.dict(os.environ, {"PROFILE": "existing"}), patch.object(deploy, "Cluster") as cluster:
            target = cluster.return_value
            target.get.side_effect = lambda *args, **kw: (
                {"status": {"phase": "Bound"}} if args[0] == "pvc" else {"metadata": {"name": args[1]}})
            with self.assertRaisesRegex(ValueError, "Legacy pod"):
                deploy.prerequisites(items, target)

    def test_data_purge_requires_exact_confirmation(self):
        items = self.profiles["existing"]
        with patch.dict(os.environ, {"PROFILE": "existing", "CLAIM": "data-openbao-0", "CONFIRM_DATA": "wrong"}), \
                patch.object(deploy, "render", return_value=(b"", items)), patch.object(deploy, "Cluster") as cluster:
            cluster.return_value.target = "test/ecpds"
            with self.assertRaisesRegex(ValueError, "CONFIRM_DATA=test/ecpds/data-openbao-0"):
                deploy.main("purge-data")
            cluster.return_value.call.assert_not_called()

    def test_import_never_overwrites_a_populated_or_partial_volume(self):
        archive = io.BytesIO()
        with tarfile.open(fileobj=archive, mode="w") as tar:
            item = tarfile.TarInfo("config")
            item.size = len(b"seed data")
            tar.addfile(item, io.BytesIO(b"seed data"))
        with tempfile.TemporaryDirectory() as directory:
            volume = Path(directory)
            (volume / "lost+found").mkdir()
            command = ["sh", "-ec", deploy.IMPORT_COMMAND, "ecpds-import", directory]
            subprocess.run(command, input=archive.getvalue(), capture_output=True, check=True)
            (volume / "config").write_text("existing data")
            result = subprocess.run(command, input=archive.getvalue(), capture_output=True)
            self.assertNotEqual(result.returncode, 0)
            self.assertIn(b"PVC is not empty", result.stderr)
            self.assertEqual((volume / "config").read_text(), "existing data")
            (volume / "config").unlink()
            (volume / ".ecpds-import").mkdir()
            result = subprocess.run(command, input=archive.getvalue(), capture_output=True)
            self.assertNotEqual(result.returncode, 0)
            self.assertFalse((volume / "config").exists())

    def test_cluster_target_is_required_but_offline_check_does_not_use_it(self):
        with patch.dict(os.environ, {"CONTEXT": "", "NAMESPACE": ""}), patch.object(deploy, "run") as run:
            with self.assertRaisesRegex(ValueError, "Set CONTEXT and NAMESPACE"):
                deploy.Cluster()
            run.assert_not_called()
        with patch.object(deploy, "Cluster") as cluster, patch.dict(os.environ, {"PROFILE": "existing"}), \
                patch.object(deploy, "render", return_value=(b"", self.profiles["existing"])), \
                contextlib.redirect_stdout(io.StringIO()):
            self.assertEqual(deploy.main("check"), 0)
            cluster.assert_not_called()


if __name__ == "__main__":
    unittest.main()
