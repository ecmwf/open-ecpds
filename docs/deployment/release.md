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
make push-cli   # push the ecpds CLI image
```

These targets assume the images have already been built with `make build`, `make build-sa`,
or `make build-cli`. They do not trigger a Maven build — they just tag and push the
existing local images to the registry.

**When to use these:**

- You have already built locally and want to push without rebuilding (saves time during
  iteration)
- Only one machine is available and a single-arch image is acceptable (e.g. a personal
  or development registry)
- You want to share a quick snapshot with a colleague for testing

**When NOT to use these:** for production releases, always use the multi-arch workflow
below so that the published images work on both `amd64` and `arm64` hosts.

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

Once **both** step 1 runs have completed successfully:

```bash
make manifest      # combine service arch images into a multi-arch manifest
make sa-manifest   # combine standalone arch images into a multi-arch manifest
make cli-manifest  # combine CLI arch images into a multi-arch manifest
```

This step uses `docker buildx imagetools create` to merge the two arch-specific images
already in the registry into a single multi-arch manifest (`:tag` and `:latest`).
No local images are required, so it can be run from either machine.

!!! warning
    Each manifest target will fail if either architecture image is missing from the registry.
    Always ensure both step 1 runs have completed before running this step.

### Step 3 — Publish CLI binaries as GitHub Release assets (optional)

In addition to the CLI Docker image, standalone `ecpds` binaries can be published for
users who prefer a direct download. Run on **each machine** then upload both files to
the GitHub Release:

```bash
make release-tools   # produces release/ecpds-amd64 or release/ecpds-arm64
```

## Summary

| Scenario | Commands |
|---|---|
| Single-arch (already built) | `make cr-login` → `make push` / `make push-sa` / `make push-cli` |
| Multi-arch service images | `make cr-login` → `make push-native` (both machines) → `make manifest` |
| Multi-arch standalone image | `make cr-login` → `make push-sa-native` (both machines) → `make sa-manifest` |
| Multi-arch CLI image | `make cr-login` → `make push-cli-native` (both machines) → `make cli-manifest` |

## Related

- [Installation](../getting-started/installation.md) — building the images
- [Standalone](../getting-started/standalone.md) — standalone all-in-one image
- [Getting the ecpds CLI](../getting-started/ecpds-cli.md) — downloading and using the CLI
- [Deploying on Kubernetes](kubernetes.md)
