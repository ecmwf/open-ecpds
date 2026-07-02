# Changelog

All notable changes to OpenECPDS are listed here in **descending order** (most recent first).
Releases use a `MAJOR.MINOR.PATCH-DDMMYYYY` build identifier (e.g. `8.0.4-01072026`).

---

## 8.0.4-01072026 (2026-07-01)

- Added support for Python formatting and testing in the Host editors. Placeholder values can now be provided during testing, either by selecting an existing data transfer for the Host (if available) or by entering values manually.
- Added a badge and visual highlight to the Filter buttons to indicate active filters.
- Acquisition Hosts now include an **Acquisition Console** that centralises the Progress Console and listing output, providing better visibility into acquisition activities.
- Added data collection to track and display Data Mover availability periods and overall availability statistics.
- Added comprehensive configuration guides directly accessible from the Hosts pages in the Monitoring Interface, covering listing, authentication, connection, registration, acquisition, and dissemination engines. (These guides are the basis for the [Transfer Modules](transfer-modules/index.md) and [Host Directory](host-directory/index.md) sections of this site.)
- Added a new authentication mechanism supporting static Bearer/API tokens as well as automatic refresh of expiring tokens. For tokens with limited lifetimes (OAuth 2, NASA Earthdata, etc.), define a function in the JavaScript editor; the engine automatically calls it when the cached token is approaching expiry.
- Added support for single-file URL handling, allowing the Host Directory URL to point directly to a file to download. A HEAD (or GET) request is issued to retrieve the file size and last modification time, resulting in a single listing entry.
- Allowed filtering Data Transfers by Transfer Method for processed transfers that have already been assigned to a Host.
- Added additional icons to improve workflow in the Monitoring Interface (e.g. Sync Now, Requeue, and Stop icons on the Data Transfer page).
- Added access to TCP network statistics for completed data transfers. The new **Network Statistics** card on the Data Transfer page visualises connection activity and throughput, and exposes detailed per-connection metrics (RTT, congestion window, segment counts, etc.) to help troubleshoot transfer performance.

## 8.0.1-19062026 (2026-06-19)

- Added new dashboards to the Monitoring Interface for tracking Data Portal activity, including uploads and downloads, both globally and per data user.
- Added activity statistics to the Data Portal, helping users identify less busy periods and optimise transfer performance.
- The Data Portal now supports server-side **AWS S3 SDK v2** operations, including file upload and synchronisation.
- Upgraded the S3 transfer module client to the AWS SDK v2.
- Added warning badges to the Properties, Directory, JavaScript, and Python editor accordions to improve visibility of errors and warnings.
- When adding several related items, the Add panel now stays open automatically after each addition.
- In the Host editor, a warning is displayed when the Directory content does not appear to match the selected type (JavaScript or Python).
- Allowed all active sessions for a data user to be closed with a single click.
- Allowed the maximum number of connections for a data user to be scheduled (e.g. higher limits during off-peak hours).
- Added support for upload and download byte quotas in the Data Portal. Once a configured quota is reached, new transfer requests are automatically refused until usage falls back below the limit.
- Added support for similar quotas at the Destination level.
- Added a filter to select destinations with or without associated data users.
- Added a badge indicating whether a destination has associated data users, either directly or indirectly through a data policy.
- Improved the workflow for creating data users, web users, and other entities.
- Added presets and a "Remove All" option to association cards.
- Added a filter to identify problematic data users and web users, along with a "Delete All" button.
- Added feedback form to collect user input and satisfaction.
- Introduced properties for Web Users, similar to those available for Data Users.

## 8.0.1-29052026 (2026-05-29)

- Fixed packaging issue affecting the monitor RPM where the `ecpds.war` application was not included in the generated package.
- Release `8.0.0-26052026` has been withdrawn and should no longer be used.

## 8.0.0-26052026 (2026-05-26)

- Introduction of the new **Aegis** release following the initial Genesis release.
- Upgraded to the latest GraalVM release (25.0.3).
- Various external API upgrades addressing security-related issues.

---

## 7.7.0-18052026 (2026-05-18)

- Major UI improvements designed to enhance usability, accessibility, and overall user experience, based on end-user feedback.
- Introduced a fully **responsive interface** with improved support for mobile phones, tablets, and different screen sizes. Tables and layouts now adapt automatically to the available screen space.
- Better support for **dark themes**, particularly for map-based views and geographical visualisations.
- Added new visual graphs to monitor disk usage at both data mover and transfer group levels.
- Added a new **download monitoring page**, allowing administrators to track retrieval activity live at both the data mover and disk levels.

## 7.5.0-05042026 (2026-04-05)

- Major new release of the web interface. The previous stack (JSP + Apache Struts 1.x + jQuery) was fully modernised to incorporate **Bootstrap 5**, Bootstrap Icons, and DataTables.js. These upgrades deliver a sleeker, more responsive interface with significantly improved performance across all pages.

## 7.3.7-27022026 (2026-02-27)

- Improved DNF resilience in the Java container: added timeouts and retries.
- Allowed manual host latitude/longitude updates and display of location name.
- Allowed setting HTTP max redirects via the `http.maxRedirects` option, plus various HTTP module enhancements.
- Restricted visibility of Data Users for a destination to privileged users.
- Improved download error handling in browser (popup on failure) and hidden the download link when the file is not available.
- Improved compatibility in the HTTP transfer module with encoded and non-encoded URIs.

## 7.3.6-17022026 (2026-02-05)

- Added `ErrorMessage` field to the PRS Splunk event to report product status processing errors.
- Added `NetworkCode` field to the ERR Splunk event to allow tracking of dissemination failures per network type.
- Introduced a server-side check for destination existence, accessible via the `ecpds` command.
- Added an extra validation step in the monitoring interface when deleting critical entities (destinations, hosts, transfer groups, data movers) by requiring the entity name to be entered.
- Enhanced pop-up messages with a clearer design, replacing browser-native alert and confirmation boxes with a jQuery alternative.
- Enhanced the loading indicator shown after users confirm an action.
- Aligned the retrieval module to combine original and secondary errors on retrieval failure.
- Delete the stage file if the download fails during data retrieval on a data mover.
- Allowed a file to be purged on a data mover when its allocated data mover is not in the same group as its data file entry.
- Allowed separate SMTP and store hosts to be defined in the mail subsystem configuration, and added support for IMAPS.
- Allowed defining how directory listings are displayed in the ECAUTH module.
- Fixed incorrect load balancing in transfer group allocation when new files are registered.

## 7.3.5-20012026 (2026-01-20)

- Deferred transfer group and filesystem allocation until push/pull for new data files.
- Consolidated all transfer-server methods for data file allocation and processing into a common class.
- Allowed ordering of transfer servers based on live disk usage and free space statistics.
- Added ICO and PNG favicons to the data portal.
- Resolved HREFs relative to URL/path when parsing HTML files in the HTTP transfer module.
- Allowed skipping post-retrieval size checks for sources with inconsistent file size reporting.
- Fixed issue preventing pre-scheduled file downloads via the monitoring interface.
- Improved SQL query performance for data transfers used in retrieval, replication, and backup.
- Allowed unlinking uncompressed files on dissemination success if the compressed file already exists.
- Prevented `NotDirectoryException` when listing files if the underlying path is not a directory.
- Added timestamp field to all Splunk entries to ensure rotated files are correctly detected.

---

## 7.3.4-09122025 (2025-12-09)

- Refactored SQL queries for data-retrieval, replication, and filtering schedulers.
- Enabled filtering of data transfers by allocated data mover (`mover`) in the search box.
- Fixed replication issue where the original data mover could not always be found.

## 7.3.3-02122025 (2025-12-02)

- Retrieved total/free space and filesystem identifiers from movers for weighted allocation based on disk usage.
- Accounted for replication when disks are replaced, allowing empty volumes to grow gradually.

## 7.3.1-11112025 (2025-11-11)

- Added explanatory comments for certain entries in the destination form.
- Added REST endpoint to update host options, enabling automated token rotation for HTTPS authentication.
- Replaced `incoming.tmp` with `incoming.tmpDetect` and `incoming.tmpPattern` for simpler temporary file handling.
- Applied filter patterns to original filenames rather than target names via `ectrans.filterpattern`.

## 7.3.0-05112025 (2025-11-05)

- Clearly identified anonymous users in data user listings.
- Secured `s3.externalId` for cross-account trust.
- Ensured `ecpds -target` is handled correctly as a directory or file path.
- Improved text box validation for cut/paste/drag in transfer server, host, and destination editors.
- Allowed accessing associated data users directly from the destination page menu.
- Always display destinations alphabetically.
- Added flag to enable email notifications for destination changes.
- Enabled Proxy Hosts to use destination filter options during replication.
- Verified at least one active data mover per transfer group.
- Added `$dataTransfer[asap]` as a target directory option.
- Enabled file downloads with special characters across most browsers.

## 7.2.5-15102025 (2025-10-15)

- Enhanced SMTP, POP, and IMAP support with default ports and simplified configuration.
- Added `proxy.useDestinationFilter` to enforce destination-level compression.
- Fixed broken links to the Data Mover page in Transfer History events.
- Added warning prompt when updating a Proxy Host.

## 7.2.5-09102025 (2025-10-09)

- Adjusted ECPDS product submission to the ecChart data layer.
- Reduced retention periods for obsolete statistics tables.
- Displayed Autonomous System (AS) numbers in `mtr` output.
- Explicit login-failure messages for exceeded connection limits.
- Reported error if no destination name is specified in the `ecpds` CLI.
- Added chunk size option in the Azure transfer module for memory-limited movers.

## 7.2.5-30092025 (2025-09-30)

- Explicitly closed multipart output before calling `complete()` on S3 uploads.
- Removed deprecated tables: `Q2DISS_REPORT`, `STATISTICS`, `DISK_USAGE`.
- Introduced `forceproxy` search criterion for Destinations.
- Ensured Destination is not stopped before starting an Acquisition Thread.

## 7.2.4-26092025 (2025-09-26)

- Removed `CloseableClientResponse` for WAR context compatibility.

## 7.2.1-23092025 (2025-09-23)

- Restricted users can now view the full transfer history of pre-scheduled files.
- Transfer scheduler delays transmission when no server is available.
- Destinations can disseminate through proxies only.
- Ensured proper closure of streams if replication fails.
- Transfer Group fall-back: pick alternative group from same Cluster if no Mover available.
- Transfer Module upgrades:
    - **GCS**: configurable upload chunk size.
    - **S3**: improved role assumption and error extraction.
- Dependency upgrades: GraalVM 24.2.1 → 25.0.0, JDK 24.0.1 → 25.0.0, jsch 0.2.24 → 2.27.3, s3-stream-upload 2.0.3 → 2.2.4, httpclient5 5.3 → 5.3.1.

## 7.2.0-02092025 (2025-09-02)

- Improved HTTP headers and response handling for portal compliance (GitHub issues #1–#5).
- Used try-with-resources to ensure proper closure of HTTP/S connections.
- Logged and validated metaDate/metaTime during submissions.
- Upgraded base image to `rockylinux/rockylinux:9.6`.
- Dependency upgrades: GraalVM 24.2.1 → 24.2.2, JDK 24.0.1 → 24.0.2.

## 7.1.7 (2025-08-06 — 2025-08-20)

- Ensured RMI stream objects are properly unexported when streams close.
- Fixed long-term memory leak for expired RMI scheduled tasks.
- Cached RestClient to reduce object creation overhead for Opsview notifications.
- Fixed batch file selection and race conditions causing stuck transfers.

## 7.1.0-21072025 (2025-07-21)

- Major release with dependency updates, bug fixes, and new features.
- Scripts now require explicit return statements to avoid global context pollution.
- Removed `wmoLikeFormat` from FTP, FTPS, SFTP modules.
- Switched from MariaDB JDBC to Oracle MySQL JDBC; upgraded to Hibernate 7.
- Used virtual threads where possible.
- Aggressive purging of expired files on movers and proxies.

---

## 6.8.8 — 6.8.0 (2025-06-06 — 2025-03-01)

- Removed deprecated `finalize()` and replaced with `java.lang.ref.Cleaner`.
- Cleaned thread-locals, MDC, and context class loader to prevent leaks.
- Removed bzip2a support; preferred lbzip2.
- Added `$${context:...}` placeholders in host/destination editors.
- Sandboxed Polyglot context execution; prevented global scope pollution.
- Pooled Polyglot contexts; enforced statement limits.
- Deactivated Hibernate statistics.
- Dependency upgrades: GraalVM 24.2.0 → 24.2.1, JDK 24.0.0 → 24.0.1, Azure 1.24.1 → 1.53.0, HiveMQ MQTT 2024.7 → 2025.3, Jetty 10.0.18 → 10.0.25, Log4j 2.20.0 → 2.24.0, Disruptor 3.4.2 → 4.0.0, and others.

## 6.8.2 — 6.8.0 (2025-04-28 — 2025-02-16)

- Enabled native access for Polyglot JDK support.
- Activated virtual threads within Polyglot context.
- Finer JVM heap memory control; switched to ZGC.
- Introduced DirectBuffer pool for I/O performance.
- Showed MQTT file retrieval progress.
- Prevented double synchronisation after file writing.
- Allowed custom `User-Agent` and `Accept` headers in the HTTP/S transfer module.
- Increased concurrent data transfer deletions to 50.

## 6.8.0 (2025-02-10 — 2025-03-01)

- Added `libsocketoption.so` to timeout `ss` processes after 2 s (Java-native implementation).
- Introduced new Splunk events for denial monitoring.
- Default sorting for product page; search by email and quick access to destination type.
- Displayed retrieval progress on transfer pages.

## 6.7.9-09112024 (2024-11-09)

- Added additional fields for improved Splunk monitoring.
- Configured data portal for RFC 3986 compliance (allow ambiguous URIs).

---

## Related

- [Support Materials](support.md)
- [Contributing](contributing.md)

