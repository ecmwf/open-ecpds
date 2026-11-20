#!/usr/bin/env python3
"""Render and operate the checked-in Kubernetes deployment."""

import json
import os
from pathlib import Path
import re
import subprocess
import sys
import tarfile
import tempfile
import uuid

import yaml


ROOT = Path(__file__).resolve().parent
OWNER = "app.kubernetes.io/part-of"
CONTROLLERS = {"StatefulSet", "Deployment"}
STORAGE = {"PersistentVolume", "PersistentVolumeClaim"}
SUPPORT = {"Service", "NetworkPolicy", "ConfigMap", "ServiceAccount"}
DNS_LABEL = r"[a-z0-9](?:[-a-z0-9]*[a-z0-9])?"
IMPORT_COMMAND = (
    'cd "$1"; mkdir .ecpds-import; '
    'existing=$(find . -mindepth 1 -maxdepth 1 ! -name lost+found ! -name .ecpds-import -print); '
    'if [ -n "$existing" ]; '
    'then rmdir .ecpds-import; echo "PVC is not empty; refusing import" >&2; exit 1; fi; '
    'tar -xpf -; rmdir .ecpds-import'
)


def setting(name, default=""):
    return os.environ.get(name) or default


def require(condition, message):
    if not condition:
        raise ValueError(message)


def run(args, *, data=None, capture=False, timeout=360, allowed=(0,)):
    result = subprocess.run(args, input=data, stdout=subprocess.PIPE if capture else None,
                            timeout=timeout)
    if result.returncode not in allowed:
        raise SystemExit(result.returncode)
    return result.stdout if capture else result.returncode


def render():
    profile = setting("PROFILE", "cluster")
    require(re.fullmatch(DNS_LABEL, profile), "PROFILE must be an overlay directory name")
    source = ROOT / "overlays" / profile
    require((source / "kustomization.yaml").is_file(), f"Unknown overlay: {profile}")
    repository = setting("ECPDS_REPOSITORY", "open-ecpds").rstrip("/")
    tag = setting("TAG", "latest")
    require(re.fullmatch(r"[a-zA-Z0-9_][a-zA-Z0-9_.-]{0,127}", tag), "Invalid image TAG")
    images = [{"name": f"open-ecpds/{role}", "newName": f"{repository}/{role}", "newTag": tag}
              for role in ("database", "master", "monitor", "mover", "hawtio")]
    vault = setting("OPENBAO_IMAGE")
    if vault:
        require(re.fullmatch(r"[^@\s]+(?::[^@\s:/]+|@sha256:[0-9a-f]{64})", vault),
                "OPENBAO_IMAGE must include a tag or sha256 digest")
        name, sep, version = vault.rpartition("@" if "@" in vault else ":")
        images.append({"name": "open-ecpds/openbao", "newName": name,
                       "digest" if sep == "@" else "newTag": version})
    node = setting("LOCAL_NODE")
    storage_class = setting("STORAGE_CLASS")
    patches = []
    if node:
        require(re.fullmatch(r"[A-Za-z0-9][A-Za-z0-9_.-]{0,62}", node), "Invalid LOCAL_NODE label")
        patches.append({"target": {"kind": "PersistentVolume"}, "patch": json.dumps([{
            "op": "replace", "path": "/spec/nodeAffinity/required/nodeSelectorTerms/0/matchExpressions/0/values",
            "value": [node],
        }])})
    if storage_class:
        require(profile in ("cluster", "cluster-agent"),
                "Use STORAGE_CLASS only on fresh cluster profiles; customize other overlays explicitly")
        for kind, prefix in (("PersistentVolumeClaim", "/spec"),
                             ("StatefulSet", "/spec/volumeClaimTemplates/0/spec")):
            target = {"kind": kind, **({"name": "openbao"} if kind == "StatefulSet" else {})}
            patches.append({"target": target, "patch": json.dumps([{
                "op": "add", "path": prefix + "/storageClassName", "value": storage_class,
            }])})
    with tempfile.TemporaryDirectory(prefix="ecpds-render-") as directory:
        config = {
            "apiVersion": "kustomize.config.k8s.io/v1beta1", "kind": "Kustomization",
            "resources": [os.path.relpath(source, directory)], "images": images, "patches": patches,
        }
        (Path(directory) / "kustomization.yaml").write_text(json.dumps(config), encoding="utf-8")
        output = run(["kubectl", "kustomize", directory], capture=True, timeout=60)
    return output, list(yaml.safe_load_all(output))


def name(item):
    return item["metadata"]["name"]


def ref(item):
    return f'{item["kind"].lower()}/{name(item)}'


def pod_spec(item):
    return item["spec"]["template"]["spec"]


def mounted_claims(items):
    claims = set()
    for item in items:
        if item["kind"] not in CONTROLLERS:
            continue
        claims.update(v["persistentVolumeClaim"]["claimName"] for v in pod_spec(item).get("volumes", [])
                      if "persistentVolumeClaim" in v)
        templates = item["spec"].get("volumeClaimTemplates", [])
        if templates:
            require(item["spec"]["replicas"] == 1, "Storage operations require singleton StatefulSets")
        claims.update(f'{name(v)}-{name(item)}-0' for v in templates)
    return claims


def matches(selector, labels):
    return bool(selector) and all(labels.get(key) == value for key, value in selector.items())


def check(items):
    require(bool(items), "No resources rendered")
    indexed = {ref(item): item for item in items}
    require(len(indexed) == len(items), "Duplicate resource identity")
    workloads = [item for item in items if item["kind"] in CONTROLLERS]
    labels = [item["spec"]["template"]["metadata"]["labels"] for item in workloads]
    for item in items:
        kind = item["kind"]
        require(kind in CONTROLLERS | STORAGE | SUPPORT and not item["metadata"].get("namespace"),
                f"Unexpected kind or fixed namespace: {ref(item)}")
        if kind != "PersistentVolume":
            require(item["metadata"].get("labels", {}).get(OWNER) == "open-ecpds",
                    f"{ref(item)} is not marked as an OpenECPDS resource")
        if kind == "Service":
            require(any(matches(item["spec"]["selector"], pod) for pod in labels),
                    f"{ref(item)} does not select a workload")
        if kind == "StatefulSet":
            governing = indexed.get("service/" + item["spec"]["serviceName"])
            require(governing and governing["spec"].get("clusterIP") == "None"
                    and matches(governing["spec"]["selector"], item["spec"]["template"]["metadata"]["labels"]),
                    f"{ref(item)} is missing its governing headless Service")
            require(not item["spec"].get("volumeClaimTemplates") or
                    item["spec"].get("persistentVolumeClaimRetentionPolicy") ==
                    {"whenDeleted": "Retain", "whenScaled": "Retain"}, f"{ref(item)} must retain storage")
        if kind in CONTROLLERS:
            require(matches(item["spec"]["selector"]["matchLabels"],
                            item["spec"]["template"]["metadata"]["labels"]), f"{ref(item)} selector mismatch")
            require(item["spec"]["replicas"] == 1, f"{ref(item)} must remain a singleton")
            pod = pod_spec(item)
            volumes = {volume["name"] for volume in pod.get("volumes", [])}
            volumes.update(name(volume) for volume in item["spec"].get("volumeClaimTemplates", []))
            for container in pod["containers"]:
                for mount in container.get("volumeMounts", []):
                    require(mount["name"] in volumes, f"{ref(item)} mounts an undefined volume")
        if kind == "PersistentVolumeClaim" and item["spec"].get("volumeName"):
            volume = indexed.get("persistentvolume/" + item["spec"]["volumeName"])
            require(volume and volume["spec"]["persistentVolumeReclaimPolicy"] == "Retain"
                    and volume["spec"]["storageClassName"] == item["spec"]["storageClassName"],
                    f"{ref(item)} has no matching retained volume")
    claims = {name(item) for item in items if item["kind"] == "PersistentVolumeClaim"}
    expected = mounted_claims(items)
    require(claims <= expected, f"Unreferenced claims: {', '.join(sorted(claims - expected))}")
    if setting("PROFILE", "cluster") in ("cluster", "cluster-agent", "local", "local-agent"):
        expected = expected - {"data-openbao-0"} if not setting("PROFILE").startswith("local") else expected
        require(expected <= claims, f"Missing declarations: {', '.join(sorted(expected - claims))}")


class Cluster:
    def __init__(self):
        context, namespace = setting("CONTEXT"), setting("NAMESPACE")
        require(context and not context.startswith("-") and len(namespace) <= 63
                and re.fullmatch(DNS_LABEL, namespace), "Set CONTEXT and NAMESPACE explicitly")
        self.target = f"{context}/{namespace}"
        self.namespace = namespace
        self.args = ["kubectl", f"--context={context}", f"--namespace={namespace}", "--request-timeout=30s"]
        self.get("namespace", namespace)

    def call(self, *args, **kw):
        return run([*self.args, *args], **kw)

    def get(self, *args, optional=False):
        output = self.call("get", *args, "-o", "json", *(["--ignore-not-found"] if optional else []), capture=True)
        return json.loads(output) if output else None

    def apply(self, items, *, dry_run=False):
        if items:
            data = yaml.safe_dump_all(items).encode()
            self.call("apply", *(["--dry-run=server"] if dry_run else []), "-f", "-", data=data)


def storage(items, cluster):
    volumes = [item for item in items if item["kind"] == "PersistentVolume"]
    claims = [item for item in items if item["kind"] == "PersistentVolumeClaim"]
    require(claims, "This profile reuses existing storage and declares no new claims")
    if volumes:
        node = setting("LOCAL_NODE")
        require(node and len(cluster.get("nodes", "-l", f"kubernetes.io/hostname={node}")["items"]) == 1,
                "LOCAL_NODE must identify exactly one Kubernetes node by kubernetes.io/hostname")
        for volume in volumes:
            existing = cluster.get(ref(volume), optional=True)
            if existing:
                previous = existing["spec"].get("claimRef", {})
                require(not previous or (previous.get("namespace"), previous.get("name")) ==
                        (cluster.namespace, name(volume)),
                        f"{ref(volume)} belongs to another installation")
            volume["spec"]["claimRef"] = {"namespace": cluster.namespace, "name": name(volume)}
        for claim in claims:
            existing = cluster.get(ref(claim), optional=True)
            require(not existing or (existing["spec"].get("storageClassName") == ""
                    and existing["spec"].get("volumeName") == claim["spec"]["volumeName"]),
                    f"{ref(claim)} already uses different storage")
    cluster.apply(volumes + claims)


def prerequisites(items, cluster):
    declared = {ref(item) for item in items}
    claims = mounted_claims(items)
    secrets = {}
    migrating = setting("PROFILE").startswith("existing")
    retained_vault = ("persistentvolumeclaim/data-openbao-0" in declared
                      or migrating)

    def require_secret(secret_name, keys):
        if secret_name not in secrets:
            secrets[secret_name] = set(cluster.get("secret", secret_name).get("data", {}))
        missing = set(keys) - secrets[secret_name]
        require(not missing, f"Secret {secret_name} is missing keys: {', '.join(sorted(missing))}")

    for claim in sorted(claims - (set() if retained_vault else {"data-openbao-0"})):
        current = cluster.get("pvc", claim)
        require(not migrating or current.get("status", {}).get("phase") == "Bound",
                f"PVC {claim} must be bound before migrating")
    for item in items:
        if item["kind"] not in CONTROLLERS:
            continue
        legacy = cluster.get("pod", name(item), optional=True)
        require(not legacy, f"Legacy pod/{name(item)} still exists; follow the migration runbook")
        current = cluster.get(ref(item), optional=True)
        if current:
            require(current["spec"]["selector"] == item["spec"]["selector"],
                    f"{ref(item)} has a different immutable selector; follow the migration runbook")
        pod = pod_spec(item)
        for volume in pod.get("volumes", []):
            if "configMap" in volume:
                cm = volume["configMap"]["name"]
                if f"configmap/{cm}" not in declared:
                    cluster.get("configmap", cm)
            if "secret" in volume:
                secret = volume["secret"]["secretName"]
                require_secret(secret, ("tls.crt", "tls.key") if secret == "openbao-tls" else ())
        for container in pod["containers"]:
            for env in container.get("env", []):
                key = env.get("valueFrom", {}).get("secretKeyRef")
                if key:
                    require_secret(key["name"], (key["key"],))


def check_unused(cluster, claim):
    for kind in ("pod", "statefulset", "deployment", "replicaset", "daemonset", "job", "cronjob"):
        for item in cluster.get(kind)["items"]:
            if kind == "pod":
                spec = item["spec"]
            elif kind == "cronjob":
                spec = pod_spec(item["spec"]["jobTemplate"])
            else:
                spec = pod_spec(item)
            used = any(v.get("persistentVolumeClaim", {}).get("claimName") == claim
                       for v in spec.get("volumes", [])) or any(
                re.fullmatch(re.escape(f'{name(v)}-{name(item)}-') + r"\d+", claim)
                for v in item["spec"].get("volumeClaimTemplates", []))
            require(not used, f"{kind}/{name(item)} still references PVC {claim}; remove the workload first")


def bootstrap(cluster, claim):
    require(setting("SOURCE"), "Set SOURCE to a nonempty import directory")
    source = Path(setting("SOURCE")).resolve()
    require(source.is_dir() and any(source.iterdir()), "Set SOURCE to a nonempty import directory")
    require(claim != "data-openbao-0", "Initialize and restore OpenBao using its own procedures")
    cluster.get("pvc", claim)
    check_unused(cluster, claim)
    pod = "ecpds-import-" + uuid.uuid4().hex[:12]
    resource = {"apiVersion": "v1", "kind": "Pod", "metadata": {"name": pod, "labels": {OWNER: "open-ecpds"}},
                "spec": {"automountServiceAccountToken": False, "restartPolicy": "Never",
                         "activeDeadlineSeconds": 1800, "containers": [{
                             "name": "import", "image": "docker.io/library/busybox:1.37.0",
                             "command": ["sleep", "1800"],
                             "volumeMounts": [{"name": "data", "mountPath": "/data"}],
                         }], "volumes": [{"name": "data", "persistentVolumeClaim": {"claimName": claim}}]}}
    cluster.call("create", "-f", "-", data=json.dumps(resource).encode())
    try:
        cluster.call("wait", "--for=condition=Ready", "--timeout=120s", f"pod/{pod}")
        # A persistent marker prevents simultaneous or partial imports from being retried over data.
        with tempfile.TemporaryFile() as archive:
            with tarfile.open(fileobj=archive, mode="w") as tar:
                for path in source.iterdir():
                    require(path.name not in ("lost+found", ".ecpds-import"), f"Reserved import name: {path.name}")
                    tar.add(path, arcname=path.name)
            archive.seek(0)
            args = [*cluster.args, "exec", "-i", pod, "--", "sh", "-ec",
                    IMPORT_COMMAND, "ecpds-import", "/data"]
            subprocess.run(args, stdin=archive, check=True, timeout=1500)
    finally:
        cluster.call("delete", f"pod/{pod}", "--ignore-not-found", "--timeout=60s")


def main(command):
    output, items = render()
    if command == "render":
        sys.stdout.buffer.write(output)
        return 0
    check(items)
    if command == "check":
        print(f"Validated {len(items)} resources; no cluster contacted.")
        return 0
    cluster = Cluster()
    workloads = [ref(item) for item in items if item["kind"] in CONTROLLERS]
    support = [item for item in items if item["kind"] in SUPPORT]
    if command in ("apply", "validate"):
        prerequisites(items, cluster)
        cluster.apply([*support, *(item for item in items if item["kind"] in CONTROLLERS)],
                      dry_run=command == "validate")
    elif command == "storage":
        storage(items, cluster)
    elif command == "diff":
        data = yaml.safe_dump_all([item for item in items if item["kind"] not in STORAGE]).encode()
        return cluster.call("diff", "-f", "-", data=data, allowed=(0, 1))
    elif command == "status":
        cluster.call("get", *workloads, *[ref(item) for item in support if item["kind"] == "Service"],
                     *("pvc/" + claim for claim in sorted(mounted_claims(items))), "--ignore-not-found")
    elif command == "wait":
        for workload in workloads:
            cluster.call("rollout", "status", workload, "--timeout=180s")
    elif command == "ports":
        cluster.call("get", *[ref(item) for item in support if item["kind"] == "Service"], "-o", "wide")
    elif command == "delete-workloads":
        cluster.call("delete", *workloads, "--cascade=foreground", "--ignore-not-found", "--timeout=180s")
    elif command in ("bootstrap", "purge-data"):
        claim = setting("CLAIM")
        require(claim in mounted_claims(items), "Set CLAIM to exactly one claim from the selected profile")
        if command == "bootstrap":
            bootstrap(cluster, claim)
        else:
            require(setting("CONFIRM_DATA") == f"{cluster.target}/{claim}",
                    f"Set CONFIRM_DATA={cluster.target}/{claim} to confirm persistent data deletion")
            check_unused(cluster, claim)
            cluster.call("delete", "pvc", claim, "--timeout=120s")
    else:
        raise ValueError(f"Unknown operation: {command}")
    return 0


if __name__ == "__main__":
    try:
        sys.exit(main(sys.argv[1]))
    except (ValueError, KeyError, OSError, subprocess.SubprocessError, yaml.YAMLError) as exc:
        sys.exit(f"Deployment operation failed: {exc}")
