# System Requirements & Setup

OpenECPDS uses container technologies, which simplifies building and operating the
application across different environments. This allows for easy testing on laptops with
limited resources or scaling up for large deployments involving hundreds of systems and
petabytes of data.

## Docker

OpenECPDS requires **Docker** to be installed and fully functional, with the default
Docker socket enabled:

> Settings → Advanced → **"Allow the default Docker socket to be used"**

The build and run process has been tested on:

- **Linux**
- **macOS** (Intel / Apple Silicon)

…using **Docker Desktop v4.61.0**. It has also been reported to work on **Windows** with
the WSL 2 backend and the host networking option enabled.

## Kubernetes (optional)

To test the deployment of OpenECPDS containers to a Kubernetes cluster, Kubernetes must
be enabled in Docker:

> Settings → Kubernetes → **"Enable Kubernetes"**

!!! warning
    If Kubernetes is properly installed, your `$HOME/.kube/config` file should point to
    `https://kubernetes.docker.internal:6443`. If not, you can manually update the file.

See [Deploying on Kubernetes](../deployment/kubernetes.md) for the full deployment
workflow.

## Hardware resources

| Resource | Minimum |
|----------|---------|
| RAM | 3 GB available (default setup) |
| Disk | At least 15 GB for the development and application containers |

The disk space required depends on the size of the data you expect to handle, but at
least 15 GB is essential for the development and application containers.

## GitHub Copilot CLI (development container)

The GitHub Copilot CLI is installed in the development container. To use it, you must
configure a GitHub Personal Access Token (PAT).

Instructions on how to create the required PAT are available in the official
documentation: [https://github.com/github/copilot-cli](https://github.com/github/copilot-cli)

The token must be provided via the environment variable `GH_TOKEN` (preferred) or
`GITHUB_TOKEN` **before** creating or logging into the development container.

## Next steps

Continue to [Installation](installation.md) to download the distribution and build the
development container.
