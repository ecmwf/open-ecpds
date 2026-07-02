# Releasing OpenECPDS to a Container Registry

To deploy the built container images to a container registry, go to the `docker`
directory, which contains a dedicated `Makefile` for building and publishing images:

```bash
cd docker
```

## Configure credentials

This is an example configuration for pushing OpenECPDS container images to a container
registry (in this case the GitHub Container Registry, GHCR). The credentials are stored
in the `.settings/.cr-credentials` file. The same configuration can be adapted for other
container registries by updating `CR_URL` and, if necessary, the authentication
credentials (`CR_UID` and `CR_PWD`).

The file should follow this format:

```bash
CR_UID=<GITHUB_USERNAME>
CR_PWD=<GITHUB_PERSONAL_ACCESS_TOKEN>
CR_URL=ghcr.io/ecmwf/open-ecpds
```

!!! warning
    `CR_PWD` must be a GitHub Personal Access Token (PAT), not your GitHub password.

## Push the images

Once the credentials are set, you can push the images to the container registry with:

```bash
make push
```

## Related

- [Installation](../getting-started/installation.md) — building the images
- [Deploying on Kubernetes](kubernetes.md)
