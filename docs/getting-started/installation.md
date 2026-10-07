# Installation

This page covers downloading the OpenECPDS distribution, creating the development
container, and building & configuring the application. Make sure you have met the
[System Requirements](requirements.md) first.

## Download the distribution

To download the latest distribution, run:

```bash
curl -L -o master.zip https://github.com/ecmwf/open-ecpds/archive/refs/heads/master.zip && unzip master.zip
```

A `Makefile` located in the `open-ecpds-master` directory is used to create the
development container that installs all the necessary tools for building the
application. The Java classes are compiled, packaged into RPM files, and used to build
Docker images for each OpenECPDS component.

## Create and log into the development container

The development container includes all tools for compiling source code, building RPM
files, creating container images, and deploying the application.

```bash
make dev
```

If successful, you should be logged into the development container.

If a container already exists, `make dev` offers to reuse it (starting it if
stopped), replace it with a locally-built image, or cancel. Reusing skips the build.
Replacement builds successfully before removing the old container; bind-mounted
host files are preserved, but files stored only inside the old container are lost.

By default, the container targets your host's native architecture. To build/run a
second, side-by-side dev container for another architecture (e.g. for multi-arch
image testing), pass `ARCH`:

```bash
make dev ARCH=arm64   # or ARCH=amd64
```

Each `ARCH` gets its own image (`open-ecpds/dev:<arch>`) and container
(`open-ecpds-dev-<arch>`), so both can coexist and run at the same time.
The docs-preview port is also kept separate automatically: the native-arch
container publishes it on the usual `8000`, while any other `ARCH` is offset to
`8001` (override with `HOST_DOCS_PORT=...` if you need a different port).
Cross-arch builds/runs require QEMU/binfmt emulation (or Docker Desktop's built-in
support) when `ARCH` differs from the host's native architecture.

### Download the dev image instead of building it

When a dev image has been published, you can pull it from GHCR and use it without
running the image build. From the repository root on your host:

```bash
make dev-pull
```

This host-only target detects your native architecture, pulls and tags the image
locally, starts the container, and opens a shell. Docker or Podman is selected
automatically. Override the architecture with `make dev-pull ARCH=amd64` or
`make dev-pull ARCH=arm64` if needed.

If the container already exists, you are prompted to reuse it (starting it if
stopped), delete it and download a fresh image, or cancel. Replacement discards
files stored only inside the container, but preserves bind-mounted host directories.
The new image is downloaded successfully before the existing container is removed.
Do not use `make dev` for this workflow: it builds the image before starting it.

The source distribution is still required: it is mounted into the container.
AWS credentials are mounted from the host's `~/.aws`, not supplied by the image.
If `~/.aws/credentials` is missing, `make .run` initializes it with the repository's
local test credentials without overwriting an existing file.

`make rm-dev` removes a running or stopped dev container and its local image.
Application cleanup with `make clean` leaves dev images untouched.

Public GHCR images can be pulled without authentication. For a private package,
log in on the host with `docker login ghcr.io` using a token with `read:packages`
and access to the package. Registry credentials are not included in the image.
If the requested architecture tag has not been published yet, use `make dev`
to build locally instead.

## Build and configure OpenECPDS

Once inside the development container, compile the Java classes, package the RPM files,
and build the OpenECPDS Docker images:

```bash
make build
```

Local images are tagged `open-ecpds/<service>:<version>-<build>`; the shared Java
base is `open-ecpds/java:graalvm`. Standalone, CLI and Hawtio images use the same
namespace. Registry images are published under `ghcr.io/ecmwf/open-ecpds/*`.

!!! warning
    In a production environment, `ENV` should be avoided in Dockerfiles for sensitive
    data like `MYSQL_ROOT_PASSWORD` for the Database, or `KEYSTORE_PASSWORD` for the
    Monitor and Mover. Docker secrets or environment variable files should be used
    instead.

Once the build process is complete, navigate to the directory where another `Makefile`
is available:

```bash
cd run/bin/ecpds
```

The services are started using **Docker Compose**. The `docker-compose.yml` file
contains all the necessary configurations to launch and manage the different components
of OpenECPDS. You can find this file in the appropriate directory for your OS:

- `run/bin/ecpds/Darwin-ecpds/docker-compose.yml` — macOS
- `run/bin/ecpds/Linux-ecpds/docker-compose.yml` — Linux and Windows

Both files use container and service names such as `master`, `mover` and `monitor`;
use them in commands such as `make up svc=master`. The macOS network is
`ecpds-backbone`.
Internal hostnames use names such as `ecpds-master`; data and configuration are
bind-mounted from the project's `run/` directory.
To use registry images, override `ECPDS_REPOSITORY`, for example:

```bash
make up ECPDS_REPOSITORY=ghcr.io/ecmwf/open-ecpds
```

!!! warning
    Do not run multiple database containers against the same data directory.
    Applications sharing a host must use distinct host ports; Linux services use
    host networking. Avoid volume deletion or pruning commands when preserving data.

To verify the configuration and understand how Docker Compose interprets the settings
before running the services:

```bash
make config
```

For advanced configurations, you can fine-tune the options by modifying the default
values in the Compose file. Each parameter is documented within the file itself to
provide a better understanding of its function and how it impacts the system's
behaviour. By reviewing the Compose file, you can tailor the setup to your environment's
specific requirements.

## Next steps

Continue to [First Run](first-run.md) to start the services and access the interfaces.
For working inside an IDE, see [IDE Setup](ide-setup.md).
