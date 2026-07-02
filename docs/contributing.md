# Contributing

OpenECPDS is an open-source project, and contributions from the community are welcome.
The launch of OpenECPDS signifies a commitment to ongoing maintenance and updates,
focusing on long-term sustainability and continuous improvement to meet evolving user
needs.

!!! note
    Parts of this page are a starting point for contributors. Project-specific policies
    (such as a formal Contributor License Agreement or detailed coding standards) should
    be confirmed against the repository's own governance files where available.

## Where development happens

The main development of OpenECPDS is done at the **European Centre for Medium-Range
Weather Forecasts (ECMWF)**. The project is hosted on GitHub:

- Repository: [github.com/ecmwf/open-ecpds](https://github.com/ecmwf/open-ecpds)

OpenECPDS was launched to encourage collaboration with other organisations, strengthen
integration efforts, and enhance data service capabilities through community
contributions.

## Setting up a development environment

1. Review the [System Requirements](getting-started/requirements.md).
2. Follow the [Installation](getting-started/installation.md) guide to download the
   distribution and build the development container (`make dev`, `make build`).
3. Configure your IDE using the [IDE Setup](getting-started/ide-setup.md) guide
   (VS Code Dev Containers or Eclipse).
4. Start the database and services and verify your build with the
   [First Run](getting-started/first-run.md) guide.

## Building and testing

Use the provided `Makefile` targets to build and run the application:

```bash
make dev      # create / log into the development container
make build    # compile, package RPMs, build Docker images
make start-db # create the Docker network and start the database
make up       # start master, monitor, mover and database
make ps       # check running containers
make logs     # view container stdout/stderr
make down     # stop the application
make clean    # clean logs and data
```

## Suggested contribution workflow

*To be completed with the project's confirmed process.* A typical open-source workflow is:

1. **Fork** the repository and create a feature branch.
2. Make your changes, keeping them focused and well-described.
3. Ensure the project still builds (`make build`) and runs (`make up`).
4. Open a **pull request** against the `master` branch with a clear description of the
   change and its motivation.
5. Respond to review feedback.

## Reporting issues

*To be completed.* Use the GitHub issue tracker on the
[OpenECPDS repository](https://github.com/ecmwf/open-ecpds) to report bugs or request
features. When reporting a bug, include:

- The OpenECPDS version (see [Changelog](changelog.md) for version format).
- Steps to reproduce.
- Relevant log output (see [Event Logging](event-logging/overview.md) and the log
  directories described in [First Run](getting-started/first-run.md#checking-the-containers-and-logs)).

## Licensing

OpenECPDS is distributed under the **Apache License, Version 2.0**. By contributing, you
agree that your contributions will be licensed under the same terms. See the `LICENSE.txt`,
`AUTHORS`, and `NOTICE` files in the repository.

## Related

- [Support Materials](support.md)
- [Changelog](changelog.md)
- [Glossary](glossary.md)
