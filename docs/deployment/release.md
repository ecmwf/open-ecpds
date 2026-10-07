# Releasing OpenECPDS to a Container Registry

All release commands are run from the **repository root** using the top-level `Makefile`.
There is no need to `cd` into the `docker/` directory.

## Configure credentials

Store your container registry credentials in `.settings/.cr-credentials`:

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

The source images are `open-ecpds/<service>:<tag>`. Published names are determined
by `CR_URL`, for example `ghcr.io/ecmwf/open-ecpds/master:<tag>`.

The development image is published only with `make push-dev`, independently of
application releases:

```bash
make push-dev             # push the dev image for the current architecture
make push-dev ARCH=amd64   # push the amd64 dev image
make push-dev ARCH=arm64   # push the arm64 dev image
```

This publishes `open-ecpds/dev:<arch>` as `<CR_URL>/dev:<arch>` (for example,
`ghcr.io/ecmwf/open-ecpds/dev:arm64`). Build it first on the host with
`make .dev-cntnr`, or `make .dev-cntnr ARCH=amd64` for another architecture.
A missing dev image causes `push-dev` to fail; it is not rebuilt automatically.
The `push`, `push-sa`, `push-cli` and all `-native` targets do not publish the dev image.
Dev images use architecture tags only, not `latest`.

AWS credentials are mounted at runtime, and registry credentials are used by
the container engine for login and push; neither is copied into the dev image.

**When to use these:**

- You have already built locally and want to push without rebuilding (saves time during
  iteration)
- Only one machine is available and a single-arch image is acceptable (e.g. a personal
  or development registry)
- You want to share a quick snapshot with a colleague for testing

**When NOT to use these:** for production releases, always use the multi-arch workflow
below so that the published images work on both `amd64` and `arm64` hosts.

## Multi-arch push (two machines)

Multi-arch publishing requires Docker with the Buildx plugin. Podman remains
supported for local builds and single-architecture pushes. If both engines are
installed, select Docker explicitly with `DOCKER=docker` on the release commands.
Native push targets reject an `ARCH` that differs from the executing environment's
native architecture, because the staged RPMs contain native binaries.

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
The manifest targets below combine application images only; dev images retain their
separate `amd64` and `arm64` tags.

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
Manifest targets can run on the host or inside the development container.

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

## Multi-arch push (single machine with QEMU emulation)

If only one machine is available, QEMU user-space emulation allows the other
architecture to be built and pushed from the same host. Docker runs a second development
container under emulation — `gcc` inside it produces binaries for the emulated
architecture, so `libsocketoptions.so` and the `ecpds` binary come out correctly.

This works symmetrically:

- **`amd64` host** — emulate `arm64` for the second container
- **`arm64` host** — emulate `amd64` for the second container

### One-time host setup

Install the QEMU `binfmt` handlers for the architecture you want to emulate:

=== "On an amd64 host (emulate arm64)"
    ```bash
    docker run --privileged --rm tonistiigi/binfmt --install arm64
    ```

=== "On an arm64 host (emulate amd64)"
    ```bash
    docker run --privileged --rm tonistiigi/binfmt --install amd64
    ```

### Build and push both architectures

Run the native dev container as usual (`make dev`), do the native push, then start a
second dev container with `--platform` set to the other architecture and repeat:

```bash
# Step 1a — native arch (fast)
make cr-login
make push-native      # (or push-sa-native / push-cli-native)

# Step 1b — emulated arch (slower — 3–5× due to QEMU)
# Start a second dev container with the opposite platform, then inside it:
make cr-login
make push-native      # (or push-sa-native / push-cli-native)

# Step 2 — manifest (from either container, once both pushes are done)
make manifest
make sa-manifest
make cli-manifest
```

!!! warning
    The emulated build is significantly slower than native. Maven compilation and `gcc`
    under QEMU typically take 3–5× longer. For frequent CI releases, two real machines
    are preferable. For occasional releases, single-machine QEMU is a practical option.

!!! note
    If a package download or JDK install fails inside the emulated container, retry —
    QEMU occasionally has transient issues with network-intensive setup steps.



| Scenario | Commands |
|---|---|
| Single-arch (already built) | `make cr-login` → `make push` / `make push-sa` / `make push-cli` |
| Multi-arch — two machines | `make push-native` on each → `make manifest` (and `sa-manifest` / `cli-manifest`) |
| Multi-arch — one machine + QEMU | native dev container + emulated dev container → `make push-native` in each → `make manifest` |

## Related

- [Installation](../getting-started/installation.md) — building the images
- [Standalone](../getting-started/standalone.md) — standalone all-in-one image
- [Getting the ecpds CLI](../getting-started/ecpds-cli.md) — downloading and using the CLI
- [Deploying on Kubernetes](kubernetes.md)
