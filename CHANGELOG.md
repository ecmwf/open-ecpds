# OpenECPDS Release Notes

This document summarizes the most significant changes in OpenECPDS across all major releases. The release notes are listed in **descending order**, with the most recent changes first.

---

## open-ecpds 8.0.4-01072026 (2026-07-01)

- Added support for Python formatting and testing in the Host editors. Placeholder values can now be provided during testing, either by selecting an existing data transfer for the Host (if available) or by entering values manually.
- Added a badge and visual highlight to the Filter buttons to indicate active filters.
- Acquisition Hosts now include an Acquisition Console that centralizes the Progress Console and listing output, providing better visibility into acquisition activities.
- Added data collection to track and display Data Mover availability periods and overall availability statistics.
- Added comprehensive configuration guides, directly accessible from the Hosts pages in the Monitoring Interface, providing detailed information on the various configuration options for listing, authentication, connection, registration, acquisition, and dissemination engines.
- Added a new authentication mechanism supporting static Bearer/API tokens as well as automatic refresh of expiring tokens. For tokens with limited lifetimes (OAuth 2, NASA Earthdata, etc.), define a function in the JavaScript editor; the engine automatically calls it when the cached token is approaching expiry.
- Added support for single-file URL handling, allowing the Host Directory URL to point directly to a file to download. A HEAD (or GET) request is issued to retrieve the file size and last modification time, resulting in a single listing entry.
- Allow filtering Data Transfers by Transfer Method for processed transfers that have already been assigned to a Host.
- Added additional icons to improve workflow in the Monitoring Interface (e.g. Sync Now, Requeue, and Stop icons on the Data Transfer page).
- Added access to TCP network statistics for completed data transfers. The new Network Statistics card on the Data Transfer page visualizes connection activity and throughput, and exposes detailed per-connection metrics (RTT, congestion window, segment counts, etc.) to help users better understand and troubleshoot transfer performance.

## open-ecpds 8.0.1-19062026 (2026-06-19)

- Added new dashboards to the Monitoring Interface for tracking Data Portal activity, including uploads and downloads, both globally and per data user.
- Added activity statistics to the Data Portal, helping users identify less busy periods and optimize transfer performance by selecting the most suitable time for uploads and downloads.
- The Data Portal now supports server-side AWS S3 SDK v2 operations, including file upload and synchronization. This improves compatibility with modern S3 environments and S3-compatible object storage platforms while benefiting from the latest AWS SDK enhancements.
- Upgrade the S3 transfer module client to the AWS SDK v2.
- Add warning badges to the Properties, Directory, JavaScript, and Python editor accordions to improve the visibility of errors and warnings.
- When adding several related items, the Add panel now stays open automatically after each addition. This means one can continue adding more items without having to reopen the panel every time, making the process quicker and more convenient.
- In the Host editor, display a warning message when the Directory content does not appear to match the selected type (JavaScript or Python), a common source of configuration errors.
- Allow all active sessions for a data user to be closed with a single click.
- Allow the maximum number of connections for a data user to be scheduled (e.g. higher limits during off-peak hours).
- Added support for upload and download byte quotas in the Data Portal. Administrators can now limit the amount of data a user may upload or download within a configurable rolling time window. Once the configured quota is reached, new transfer requests are automatically refused until usage falls back below the limit. This feature helps control bandwidth consumption, enforce usage policies, and prevent individual users from monopolising portal resources.
- Added support for similar quotas at the Destination level.
- Added a filter to select destinations with or without associated data users.
- Added a badge indicating whether a destination has associated data users, either directly or indirectly through a data policy.
- Improved the workflow for creating data users, web users, and other entities.
- Added presets and a "Remove All" option to association cards.
- Added a filter to identify problematic data users and web users, along with a "Delete All" button to remove them all at once.
- Add feedback form to collect user input and satisfaction.
- Introduce properties for Web Users, similar to those available for Data Users.

## open-ecpds 8.0.1-29052026 (2026-05-29)

- Fix packaging issue affecting the monitor RPM where the ecpds.war application was not included in the generated package.
- Release 8.0.0-26052026 has been withdrawn and should no longer be used.

## open-ecpds 8.0.0-26052026 (2026-05-26)

- Introduction of the new Aegis release following the initial Genesis release.
- Upgrade to the latest GraalVM release (25.0.3).
- Various external API upgrades addressing security-related issues.

## open-ecpds 7.7.0-18052026 (2026-05-18)

- Introduces several user interface improvements designed to enhance usability, accessibility, and overall user experience, following the numerous feedbacks received from end users.
- A major enhancement is the introduction of a fully responsive interface, providing improved support for mobile phones, tablets, and different screen sizes. The interface can now automatically adapt tables and layouts to the available screen space, prioritising the most relevant information to improve readability and usability across various environments.
- In addition, better support for dark themes has been introduced, particularly for map-based views and geographical visualisations, improving visual comfort and integration with dark mode environments.
- On the administration side, additional monitoring capabilities have been introduced, including new visual graphs to monitor disk usage at both data mover and transfer group levels.
- A new download monitoring page is also now available, allowing administrators to track retrieval activity live at both the data mover and disk levels, helping to check that resource allocation and usage are fully optimised.

## open-ecpds 7.5.0-05042026 (2026-04-05)

- A major new release of the web interface, featuring significant updates that enhance both design and functionality. The previous stack (JSP + Apache Struts 1.x + jQuery) has been fully modernized to incorporate the latest technologies, including Bootstrap 5, Bootstrap Icons, and DataTables.js. These upgrades not only deliver a sleeker, more responsive interface, but also introduce advanced features and significantly improved performance across all pages. A broad range of UI improvements ensures a fresh, user-friendly, and highly interactive experience for all users.

## open-ecpds 7.3.7-27022026 (2026-02-27)

- Improve DNF resilience in Java container: add timeouts and retries, do not enable CRB.
- Allow manual host latitude/longitude updates and display location name.
- Allow setting HTTP max redirects by introducing the http.maxRedirects option and various HTTP module enhancements.
- Only show Data Users for a destination to privileged users.
- Improve download error handling in browser (show popup on failure) and do not show the download link if the file is not available.
- Improve compatibility in the HTTP transfer module with encoded and non-encoded URIs.

## open-ecpds 7.3.6-17022026 (2026-02-05)

- Add the ErrorMessage field to the PRS Splunk event to report product status processing errors, such as a missing required option, a status that is already expected, not expected, or already completed, in which case the notification is ignored.
- Add the NetworkCode field to the ERR Splunk event to allow tracking of dissemination failures or retries per network type (e.g. Internet or RMDCN).
- Introduce a server-side check for destination existence, accessible via the ecpds command.
- In the monitoring interface, add an extra validation step when deleting critical ECPDS entities (such as destinations, hosts, transfer groups, and data movers) by requiring the entity name to be entered to confirm deletion.
- Enhance pop-up messages with a clearer, more user-friendly design by replacing browser-native alert and confirmation boxes with a jQuery alternative.
- Enhanced the loading indicator shown after users confirm an action in the monitoring interface (e.g., deleting an item). Previously, the page could appear unresponsive while the request was being processed, which made it unclear whether the action was in progress.
- Align the retrieval module to combine original and secondary errors on retrieval failure, similarly to the push module (e.g. “Pipe Close” will no longer hide the underlying “Permission Denied” error in the transfer history).
- Delete the stage file if the download fails during data retrieval on a data mover.
- Allow a file to be purged on a data mover when its allocated data mover is not in the same group as its data file entry.
- Allow separate SMTP and store hosts to be defined in the mail subsystem configuration, and add support for IMAPS (in addition to IMAP + TLS).
- Allows defining how directory listings are displayed in the ECAUTH module.
- Fix incorrect load balancing in transfer group allocation when new files are registered in ECPDS.

## open-ecpds 7.3.5-20012026 (2026-01-20)

- Defer transfer group and filesystem allocation until push/pull for new data files to better match actual disk usage.
- Consolidate all transfer-server methods for data file allocation and processing into a common class for maintainability.
- Allow ordering of transfer servers based on live disk usage and free space statistics.
- Add ICO and PNG favicons to the data portal for cross-browser compatibility.
- Resolve HREFs relative to URL/path when parsing HTML files in the HTTP transfer module.
- Allow skipping post-retrieval size checks when retrieving files from sources with inconsistent file size reporting.
- Fix issue preventing pre-scheduled file downloads via monitoring interface.
- Improve SQL query performance when getting data transfers for retrieval, replication, and backup.
- Allow unlinking uncompressed file on dissemination success if compressed file already exists on the data mover.
- Prevent NotDirectoryException when listing files if underlying path is not a directory in the data mover repository.
- Replace Gemini CLI with Copilot CLI inside the development container.
- Add timestamp field to all Splunk entries to ensure rotated files are correctly detected and prevent missing or duplicated events.

---

## open-ecpds 7.3.4-09122025 (2025-12-09)

- Refactor SQL queries for data-retrieval, replication, and filtering schedulers for clarity and performance.
- Enable filtering of data transfers by allocated data mover (`mover`) in the search box.
- Change `ss` command triggering to use direct execution instead of piping through `grep`.
- Fix replication issue where the original data mover could not always be found.

---

## open-ecpds 7.3.3-02122025 (2025-12-02)

- Retrieve total/free space and file system identifiers from movers for weighted allocation based on disk usage.
- Account for replication when disks are replaced, allowing empty volumes to grow gradually while awaiting data release.

---

## open-ecpds 7.3.1-11112025 (2025-11-11)

- Add explanatory comments for certain entries when filling out the destination form.
- Add REST endpoint to update host options, enabling automated token rotation for HTTPS authentication.
- Replace `incoming.tmp` with `incoming.tmpDetect` and `incoming.tmpPattern` for simpler temporary file handling.
- Apply filter patterns to original filenames rather than target names using `ectrans.filterpattern`.

---

## open-ecpds 7.3.0-05112025 (2025-11-05)

- Clearly identify anonymous users in data user listings.
- Secure `s3.externalId` for cross-account trust.
- Ensure `ecpds -target` is handled correctly as a directory or file path.
- Improve text box validation for cut/paste/drag in transfer server, host, and destination editors.
- Access associated data users directly from the destination page menu.
- Always display destinations alphabetically.
- Add flag to enable email notifications for destination changes; include sending service in mail subject tag.
- Enable Proxy Hosts to use destination filter options during replication.
- Verify at least one active data mover per transfer group.
- Add `$dataTransfer[asap]` as a target directory option.
- Enable file downloads with special characters across most browsers.

---

## open-ecpds 7.2.5-15102025 (2025-10-15)

- Enhance SMTP, POP, and IMAP support with default ports and simplified configuration.
- Add `proxy.useDestinationFilter` to enforce destination-level compression.
- Fix broken links to Data Mover page in Transfer History events.
- Add warning prompt when updating a Proxy Host.

---

## open-ecpds 7.2.5-09102025 (2025-09-10)

- Adjust ECPDS product submission to ecChart data layer.
- Reduce retention periods for obsolete statistics tables; improves database performance.
- Display Autonomous System (AS) numbers in `mtr` output.
- Explicit login-failure messages for exceeded connection limits.
- Report error if no destination name is specified in `ecpds` CLI.
- Add chunk size option in Azure transfer module for memory-limited movers.

---

## open-ecpds 7.2.5-30092025 (2025-09-30)

- Explicitly close multipart output before calling `complete()` on S3 uploads.
- Remove deprecated tables: `Q2DISS_REPORT`, `STATISTICS`, `DISK_USAGE`.
- Introduce `forceproxy` search criterion for Destinations.
- Ensure Destination is not stopped before starting Acquisition Thread.

---

## open-ecpds 7.2.4-26092025 (2025-09-26)

- Remove `CloseableClientResponse` for WAR context compatibility.

---

## open-ecpds 7.2.1-23092025 (2025-09-23)

- Restricted users can view full transfer history of pre-scheduled files.
- Transfer scheduler delays transmission when no server is available.
- Destinations can disseminate through proxies only.
- Rename Transfer History column from "S" to "Err"; adjust padding.
- Ensure proper closure of streams if replication fails.
- Pick alternative Transfer Group from same Cluster if no Mover available.
- Upgrade Transfer Modules:
  - GCS: configurable upload chunk size.
  - S3: improved role assumption and error extraction.
- Dependencies upgraded:
  - GraalVM 24.2.1 → 25.0.0
  - JDK 24.0.1 → 25.0.0
  - jsch 0.2.24 → 2.27.3
  - s3-stream-upload 2.0.3 → 2.2.4
  - httpclient5 5.3 → 5.3.1

---

## open-ecpds 7.2.0-02092025 (2025-09-02)

- Improve HTTP headers and response handling for portal compliance (GitHub issues #1–#5).
- Use try-with-resources to ensure proper closure of HTTP/S connections.
- Log and validate metaDate/metaTime during submissions.
- Upgrade base image: `rockylinux/rockylinux:9.6`.
- Dependencies upgraded:
  - GraalVM 24.2.1 → 24.2.2
  - JDK 24.0.1 → 24.0.2
  - s3-stream-upload 2.0.3 → 2.2.4

---

## open-ecpds 7.1.7 (2025-08-06 → 2025-08-20)

- Ensure RMI stream objects are properly unexported when streams close.
- Fix long-term memory leak for expired RMI scheduled tasks.
- Cache RestClient to reduce object creation overhead for Opsview notifications.
- Fix batch file selection and race conditions causing stuck transfers.

---

## open-ecpds 7.1.0-21072025 (2025-07-21)

- Major release with dependency updates, bug fixes, and new features.
- Scripts now require explicit return statements to avoid global context pollution.
- Remove `wmoLikeFormat` from FTP, FTPS, SFTP modules.
- Switch from MariaDB JDBC to Oracle MySQL JDBC; upgrade to Hibernate 7.
- Use virtual threads where possible.
- Aggressive purging of expired files on movers and proxies.

---

## open-ecpds 6.8.8 → 6.8.0 (2025-06-06 → 2025-03-01)

- Remove deprecated `finalize()` and replace with `java.lang.ref.Cleaner`.
- Clean thread-locals, MDC, and context class loader to prevent leaks.
- Remove bzip2a support; prefer lbzip2.
- Add `$${context:...}` placeholders in host/destination editors.
- Sandboxed Polyglot context execution; prevent global scope pollution.
- Pool Polyglot contexts; enforce statement limits.
- Deactivate Hibernate statistics.
- Dependencies upgraded:
  - GraalVM 24.2.0 → 24.2.1
  - JDK 24.0.0 → 24.0.1
  - Azure 1.24.1 → 1.53.0
  - HiveMQ MQTT 2024.7 → 2025.3
  - Jetty 10.0.18 → 10.0.25
  - MariaDB Java Client 3.5.2 → 3.5.3
  - Commons IO 2.13.0 → 2.16.1
  - Guava 33.2.1 → 33.4.8
  - Jackson JSON 2.13.1 → 2.18.0
  - Apache Sling 3.2.22 → 3.3.0
  - Log4j 2.20.0 → 2.24.0
  - Disruptor 3.4.2 → 4.0.0

---

## open-ecpds 6.8.2 → 6.8.0 (2025-04-28 → 2025-02-16)

- Enable native access for Polyglot JDK support.
- Activate virtual threads within Polyglot context.
- Finer JVM heap memory control; switch to ZGC.
- Introduce DirectBuffer pool for I/O performance.
- Show MQTT file retrieval progress.
- Prevent double synchronization after file writing.
- Allow custom `User-Agent` and `Accept` headers in HTTP/S transfer module.
- Increase concurrent data transfer deletions to 50.

---

## open-ecpds 6.8.0-10022025 → 6.8.0-01032025

- Add libsocketoption.so to timeout `ss` processes after 2s (Java-native implementation).
- Introduce new Splunk events for denial monitoring.
- Default sorting for product page; search by email and quick access to destination type.
- Display retrieval progress on transfer pages.

---

## open-ecpds 6.7.9-09112024

- Add additional fields for improved Splunk monitoring.
- Configure data portal for RFC3986 compliance (allow ambiguous URIs).

---
