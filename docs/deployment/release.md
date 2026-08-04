# Releasing OpenECPDS to a Container Registry

All release commands are run from the **repository root** using the top-level `Makefile`.
There is no need to `cd` into the `docker/` directory.

## Configure credentials

Store your container registry credentials in `.settings/.cr-credential`:

```bash
CR_UID=<USERNAME>
CR_PWD=<PERSONAL_ACCESS_TOKEN>
CR_URL=ghcr.io/ecmwf/open-ecpds
```

This example targets the GitHub Container Registry (GHCR). The same format works for
any OCI-compatible registry — just update `CR_URL` and the credentials accordingly.

!!! warning
    Use a Personal Access Token (PAT) for `CR_PWD`, not your account password.

## Log in to the registry

Before pushing, authenticate with the registry:

```bash
make cr-login
```

## Single-arch push

If you are pushing from a single machine (images are already built locally), use:

```bash
make push       # push service images (master, mover, monitor, …)
make push-sa    # push the standalone all-in-one image
```

These targets assume the images have already been built with `make build` or
`make build-sa`. They do not trigger a Maven build.

## Multi-arch push (two machines)

Because the `ecpds-mover` image contains a native shared library
(`libsocketoptions.so`) compiled for the host architecture, true multi-arch images
require building on each target platform separately.

### Step 1 — Build and push from each machine (run in parallel)

Run the following on **each machine** (x86\_64 and aarch64). The two runs can proceed
concurrently — they are fully independent:

```bash
make push-native      # service images
make push-sa-native   # standalone image (if needed)
make push-cli-native  # CLI image (if needed)
```

Each machine builds the RPMs via Maven, constructs the Docker images, and pushes them
to the registry with an architecture-specific tag (e.g. `:tag-amd64`, `:tag-arm64`).

!!! note
    These targets must be run **inside the development container** (i.e. after
    `make dev`) because they invoke `mvn package`, which compiles the native library.

### Step 2 — Create the multi-arch manifest (run once, on either machine)

Once **both** `push-native` (or `push-sa-native`) runs have completed successfully:

```bash
make manifest      # combine service arch images into a multi-arch manifest
make sa-manifest   # combine standalone arch images into a multi-arch manifest
```

This step uses `docker buildx imagetools create` to merge the two arch-specific images
already in the registry into a single multi-arch manifest (`:tag` and `:latest`).
No local images are required, so it can be run from either machine.

!!! warning
    `make manifest` will fail if either architecture image is missing from the registry.
    Always ensure both `push-native` runs have completed before running this step.

## Summary

| Scenario | Commands |
|---|---|
| Single-arch (already built) | `make cr-login` → `make push` / `make push-sa` / `make push-cli` |
| Multi-arch service images | `make cr-login` → `make push-native` (both machines) → `make manifest` |
| Multi-arch standalone image | `make cr-login` → `make push-sa-native` (both machines) → `make sa-manifest` |
| Multi-arch CLI image | `make cr-login` → `make push-cli-native` (both machines) → `make cli-manifest` |

## CLI binary release assets

In addition to Docker images, the `ecpds` binary is published as a plain file on the
GitHub Releases page (`ecpds-amd64` / `ecpds-arm64`) so that users can download and run
it without Docker. To produce the correctly named binary for the current machine:

```bash
make release-tools   # produces release/ecpds-amd64 or release/ecpds-arm64
```

Run this on each machine and upload both files as assets to the GitHub Release.

## Related

- [Installation](../getting-started/installation.md) — building the images
- [Standalone](../getting-started/standalone.md) — standalone all-in-one image
- [Getting the ecpds CLI](../getting-started/ecpds-cli.md) — downloading and using the CLI
- [Deploying on Kubernetes](kubernetes.md)
