# Deploying OpenECPDS on Kubernetes

If you have successfully built the OpenECPDS containers (see
[Installation](../getting-started/installation.md)) and enabled Kubernetes in Docker (see
[System Requirements](../getting-started/requirements.md)), you can deploy OpenECPDS to a
Kubernetes cluster.

Services and containers use names such as `ecpds-master`.
Images default to `open-ecpds/*`. Persistent volumes and claims use names such as
`var-lib-ecpds-database`. Application paths inside the containers use `/etc/ecpds`,
`/var/lib/ecpds`, `/var/log/ecpds` and `/var/tmp/ecpds`.

!!! warning "Persistent data"
    Back up persistent data before changing workloads or volume claims.
    The deployment Makefile populates volumes from the local `run/` directories.
    Ensure `k8s-configs` contains only the manifests you intend to deploy:
    `make start` applies every manifest in that directory.
    `make delete` removes persistent volumes and claims as well as workloads.

Navigate to the directory where the Kubernetes `Makefile` is available:

```bash
cd deploy/kubernetes
```

## Build and start the pods

To convert the `deploy/kubernetes/docker-compose.yml` file into Kubernetes YAML files in
the `k8s-configs` directory and start the pods, run:

```bash
make build
```

## Find the external ports

If successful, use the following command to get the port mappings needed to connect from
outside the cluster:

```bash
make ports
```

If the default port for the monitoring interface has not been updated in the
`docker-compose.yml` file, you can find the external port by checking the redirection for
port 3443, for example:

```text
3443 -> 32034/TCP
```

You can then use your browser to access the monitoring interface at:

```text
https://127.0.0.1:32034
```

## Export the full configuration

To get the YAML files for all pods, PVs, and PVCs (you can redirect the output to capture
the full configuration), run:

```bash
make yaml
```

## Tear down

To delete all Kubernetes resources and stop the pods:

```bash
make delete
```

## Related

- [Illustrative Physical Infrastructure](infrastructure.md)
- [Releasing to a Container Registry](release.md)
