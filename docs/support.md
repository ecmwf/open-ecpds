# Support Materials

A range of resources is available to help you work with OpenECPDS.

## Javadoc API documentation

You can access the Javadoc API documentation for OpenECPDS at the following link:
[Javadocs](https://ecmwf.github.io/open-ecpds/apidocs/). This comprehensive documentation
provides detailed information about the classes, methods, and functionalities available,
serving as a valuable resource for developers.

## Editor options

You can find the OpenECPDS options for various editors in the project's `Options.md`. This
documentation outlines the configurable options available in the OpenECPDS editors,
helping users customise their experience and optimise their workflow effectively.

The [Transfer Modules](transfer-modules/index.md) reference in this site documents the
per-module options (e.g. FTP, SFTP, S3) extracted from the in-app configuration guides.

## Presentation poster

A comprehensive **OpenECPDS Presentation Poster** is available in the project repository
(`Poster.pdf`). This poster provides an accessible, high-level overview of the system,
including its purpose, key features and architecture. It also includes visual diagrams,
usage statistics, and long-term trends to help new users, partners, and stakeholders
quickly understand what OpenECPDS does, who it serves, and how it supports large-scale
data acquisition and dissemination. It is an ideal entry point for anyone discovering the
platform or looking for a concise summary of its capabilities.

## Tooling & dependencies

OpenECPDS automates the downloading of specific tools and uses external APIs downloaded
via Maven. The versions used include:

| Tool | Version |
|------|---------|
| GraalVM | 25.0.3 |
| Maven | 3.9.9 |
| Docker | 29.2.1 |
| Kompose | 1.37.0 |
| Kubectl | 1.37.2 |
| GeoIP | GeoLite2-City |

For licenses and details on these dependencies, refer to their respective documentation.
You can retrieve the licenses from the development container using:

```bash
make get-licenses
```

If successful, the licenses will be available in the `target/generated-resources`
directory. These licenses are also included in the root directory of the container images,
along with the `AUTHORS`, `LICENSE.txt`, `NOTICE` and `VERSION` files.

## Related

- [Contributing](contributing.md)
- [Changelog](changelog.md)
- [Glossary](glossary.md)
