# Changelog

This page summarises the most significant changes in OpenECPDS. The authoritative,
complete release notes are maintained in the
[`CHANGELOG.md`](https://github.com/ecmwf/open-ecpds/blob/master/CHANGELOG.md) file at the
root of the repository, listed in **descending order** (most recent first).

!!! note
    This page is a curated summary for the documentation site. For the full,
    line-by-line history across all releases, always refer to the repository
    `CHANGELOG.md`.

## Recent releases

### 8.0.3-26062026 (2026-06-26)

- Added comprehensive configuration guides, directly accessible from the Hosts pages in
  the Monitoring Interface, covering listing, authentication, connection, registration,
  acquisition, and dissemination engines. (These guides are the basis for the
  [Transfer Modules](transfer-modules/index.md) and
  [Host Directory](host-directory/index.md) sections of this site.)
- Added a new authentication mechanism supporting static Bearer/API tokens as well as
  automatic refresh of expiring tokens (OAuth 2, NASA Earthdata, etc.).
- Added support for single-file URL handling, allowing the Host Directory URL to point
  directly to a file to download.
- Allowed filtering Data Transfers by Transfer Method for processed transfers.
- Added access to TCP network statistics for completed data transfers.

### 8.0.1-19062026 (2026-06-19)

- New dashboards for tracking Data Portal activity (uploads and downloads, globally and
  per data user).
- Data Portal now supports server-side AWS S3 SDK v2 operations.
- Upgraded the S3 transfer module client to the AWS SDK v2.
- Added upload/download byte quotas in the Data Portal and at the Destination level.

### Earlier releases

The release history extends back through the 7.x and 6.x series. See the repository
[`CHANGELOG.md`](https://github.com/ecmwf/open-ecpds/blob/master/CHANGELOG.md) for the
complete list, including:

- 8.0.x series (2026)
- 7.x series (2025)
- 6.x series (2024–2025)

## Versioning

OpenECPDS releases use a `MAJOR.MINOR.PATCH-DDMMYYYY` style identifier (for example
`8.0.3-26062026`), combining a semantic-style version with a build date.

## Related

- [Support Materials](support.md)
- [Contributing](contributing.md)
