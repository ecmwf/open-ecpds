# Changelog

All notable changes to OpenECPDS are listed here in **descending order** (most recent first).
Releases use a `MAJOR.MINOR.PATCH-DDMMYYYY` build identifier (e.g. `8.0.4-01072026`).

---

## open-ecpds 8.0.6-16072026

- Added a new **Destination Metadata Framework** with web-based editing, automatic form generation, REST API support, and XML import for migrating existing metadata.
- Added a recursive destination alias diagram to the Monitor UI. The new visualization presents the complete destination alias hierarchy around the selected destination, including recursive alias chains and conditional aliases annotated with their matching conditions, providing a clear overview of complex routing relationships.
- Added portal session recovery across data movers. If a browser request is routed to a different data mover by the load balancer, the portal session is automatically located on the original data mover, transferred to the current one, and the request continues transparently without requiring the user to log in again. 
- Replaced the browser-based HTTP Basic Authentication login flow with a dedicated web login mechanism supporting proper logout. Browser users and command-line clients (`curl`, `wget`, etc.) now use separate authentication flows, avoiding conflicts between interactive sessions and automated access while preserving compatibility with existing clients.
- Introduced the **Portal Service** field on Data Users. Each data user can now be assigned one of three service modes: **Standard Login** (the default, requires credentials and supports TOTP), **Open Access** (no authentication required — the portal serves files directly without a login), or **Self-Service** (visitors self-register via email verification and receive personal credentials, all sharing the same data user configuration). The mode is visible and editable in the Data Users admin pages, and reflected in the user list alongside the other account properties.
- Added a self-service **registration page** (`/ecpds/register?user=<login>`) for data users configured with the Self-Service portal mode. Visitors fill in their name, email address, and country; a verification link is emailed to them; on confirmation their credentials are activated and emailed back. When enabled, a *Share registration link* item appears in the portal user menu for self-service data users. Failed login attempts on self-service users are automatically redirected to the registration page rather than showing an authentication error.
- Added two OPTIONS endpoints to the data portal to support CORS preflight requests. When CORS is configured at the data user level, the appropriate CORS headers are now returned in the preflight OPTIONS response.
- Enhanced the login page with an on-screen numeric keypad for OTP authentication. When OTP is the only available authentication method, users can now enter their one-time password using either the keyboard or the on-screen keypad.
- Improved the transfer status display by introducing a user icon to identify status changes initiated by end users (e.g. Stop or Requeue actions). The icon is displayed in both the Destination Transfers table and the Transfer History view, making it easier to distinguish user-initiated actions from scheduler-driven status changes.
- Improved the login page with keyboard/numpad input, a show/hide password toggle, a clear button, and an animated global network background (opt-in via `loginAnimatedBackground` in `ecmwf.properties`).
- Fixed and improved S3 multipart upload memory management: the `s3.queueCapacity` option (default: 4) is now correctly honoured. Part buffers are pre-allocated once as a fixed pool and handed off to upload threads without copying (zero-copy for full parts), bounding peak heap usage to `(numUploadThreads + queueCapacity) × partSize` MB per concurrent transfer and eliminating GC pressure from per-part allocations.

## open-ecpds 8.0.4-05072026

- Added a new configurable CORS option at the Data User level, allowing specific origins (or *) to be defined for browser access to download endpoints, with support for preflight requests and ranged downloads.
- Added the ability to filter Destinations, Hosts, Web Users, and Data Users by properties containing errors or warnings, making it easier to quickly identify items that require attention.
- In addition, Destinations and Hosts with errors or warnings are now highlighted with a red badge on their main page icons, providing an immediate visual indication that action may be required.
- Prevented data transfers from being requeued to destinations with no enabled dissemination hosts. This prevents transfers from being queued for destinations that cannot currently process them and avoids the destination switching to the `NoHosts` status.
- The *Test on Server* panel now includes a **File Content Preview**: fetches live file content directly from a remote acquisition host via the DataMover/ECtrans stack, making it straightforward to inspect file formats and tune CSV, JSON, or other parser configurations without leaving the UI.
- URLs detected in the directory output are shown in an **editable textarea**, so individual paths can be modified and re-fetched on the fly — no need to re-run the full acquisition cycle.
- A **line count selector** (default: 5) limits how much of each file is retrieved, keeping previews fast during iterative testing.
- The *Test on Server* button is now also available in **view mode** (read-only host page), restricted to users with edit permissions.
- On the destination *Hosts & Users* tab, each acquisition host now shows a live **running indicator**: a ▶ play button when idle (triggers an immediate acquisition run), and an animated ⏹ interrupt button when active — giving operators direct control without navigating to the host page.

## open-ecpds 8.0.4-01072026

- Added support for Python formatting and testing in the Host editors. Placeholder values can now be provided during testing, either by selecting an existing data transfer for the Host (if available) or by entering values manually.
- Added a badge and visual highlight to the Filter buttons to indicate active filters.
- Acquisition Hosts now include an **Acquisition Console** that centralises the Progress Console and listing output, providing better visibility into acquisition activities.
- Added data collection to track and display Data Mover availability periods and overall availability statistics.
- Added comprehensive configuration guides directly accessible from the Hosts pages in the Monitoring Interface, covering listing, authentication, connection, registration, acquisition, and dissemination engines. (These guides are the basis for the [Transfer Modules](transfer-modules/index.md) and [Host Directory](host-directory/index.md) sections of this site.)
- Added a new authentication mechanism supporting static `Bearer`/`API` tokens as well as automatic refresh of expiring tokens. For tokens with limited lifetimes (OAuth 2, NASA Earthdata, etc.), define a function in the JavaScript editor; the engine automatically calls it when the cached token is approaching expiry.
- Added support for single-file URL handling, allowing the Host Directory URL to point directly to a file to download. A HEAD (or GET) request is issued to retrieve the file size and last modification time, resulting in a single listing entry.
- Allowed filtering Data Transfers by Transfer Method for processed transfers that have already been assigned to a Host.
- Added additional icons to improve workflow in the Monitoring Interface (e.g. Sync Now, Requeue, and Stop icons on the Data Transfer page).
- Added access to TCP network statistics for completed data transfers. The new **Network Statistics** card on the Data Transfer page visualises connection activity and throughput, and exposes detailed per-connection metrics (RTT, congestion window, segment counts, etc.) to help troubleshoot transfer performance.

## open-ecpds 8.0.1-19062026

- Added new dashboards to the Monitoring Interface for tracking Data Portal activity, including uploads and downloads, both globally and per data user.
- Added activity statistics to the Data Portal, helping users identify less busy periods and optimise transfer performance.
- The Data Portal now supports server-side **AWS S3 SDK v2** operations, including file upload and synchronisation.
- Upgraded the S3 transfer module client to the **AWS SDK v2**.
- Added warning badges to the *Properties*, *Directory*, *JavaScript*, and *Python* editor accordions to improve visibility of errors and warnings.
- When adding several related items, the *Add panel* now stays open automatically after each addition.
- In the Host editor, a warning is displayed when the Directory content does not appear to match the selected type (JavaScript or Python).
- Allowed all active sessions for a data user to be closed with a single click.
- Allowed the maximum number of connections for a data user to be scheduled (e.g. higher limits during off-peak hours).
- Added support for upload and download byte quotas in the Data Portal. Once a configured quota is reached, new transfer requests are automatically refused until usage falls back below the limit.
- Added support for similar quotas at the Destination level.
- Added a filter to select destinations with or without associated data users.
- Added a badge indicating whether a destination has associated data users, either directly or indirectly through a data policy.
- Improved the workflow for creating data users, web users, and other entities.
- Added presets and a *Remove All* option to association cards.
- Added a filter to identify problematic data users and web users, along with a *Delete All* button.
- Added feedback form to collect user input and satisfaction.
- Introduced properties for Web Users, similar to those available for Data Users.

## open-ecpds 8.0.1-29052026

- Fixed packaging issue affecting the monitor RPM where the `ecpds.war` application was not included in the generated package.
- Release `8.0.0-26052026` has been withdrawn and should no longer be used.

## open-ecpds 8.0.0-26052026

- Introduction of the new **Aegis** release following the initial Genesis release.
- Upgraded to the latest GraalVM release (25.0.3).
- Various external API upgrades addressing security-related issues.

---

## open-ecpds 7.7.0-18052026

- Major UI improvements designed to enhance usability, accessibility, and overall user experience, based on end-user feedback.
- Introduced a fully **responsive interface** with improved support for mobile phones, tablets, and different screen sizes. Tables and layouts now adapt automatically to the available screen space.
- Better support for **dark themes**, particularly for map-based views and geographical visualisations.
- Added new visual graphs to monitor disk usage at both data mover and transfer group levels.
- Added a new **download monitoring page**, allowing administrators to track retrieval activity live at both the data mover and disk levels.

## open-ecpds 7.5.0-05042026

- Major new release of the web interface. The previous stack (JSP + Apache Struts 1.x + jQuery) was fully modernised to incorporate **Bootstrap 5**, **Bootstrap Icons**, and **DataTables.js**. These upgrades deliver a sleeker, more responsive interface with significantly improved performance across all pages.

## open-ecpds 7.3.7-27022026

- Improved DNF resilience in the Java container: added timeouts and retries.
- Allowed manual host latitude/longitude updates and display of location name.
- Allowed setting HTTP max redirects via the `http.maxRedirects` option, plus various HTTP module enhancements.
- Restricted visibility of Data Users for a destination to privileged users.
- Improved download error handling in browser (popup on failure) and hidden the download link when the file is not available.
- Improved compatibility in the HTTP transfer module with encoded and non-encoded URIs.

## open-ecpds 7.3.6-17022026

- Added `ErrorMessage` field to the `PRS` Splunk event to report product status processing errors.
- Added `NetworkCode` field to the `ERR` Splunk event to allow tracking of dissemination failures per network type.
- Introduced a server-side check for destination existence, accessible via the `ecpds` command.
- Added an extra validation step in the monitoring interface when deleting critical entities (destinations, hosts, transfer groups, data movers) by requiring the entity name to be entered.
- Enhanced pop-up messages with a clearer design, replacing browser-native alert and confirmation boxes with a jQuery alternative.
- Enhanced the loading indicator shown after users confirm an action.
- Aligned the retrieval module to combine original and secondary errors on retrieval failure.
- Delete the stage file if the download fails during data retrieval on a data mover.
- Allowed a file to be purged on a data mover when its allocated data mover is not in the same group as its data file entry.
- Allowed separate SMTP and store hosts to be defined in the mail subsystem configuration, and added support for IMAPS.
- Allowed defining how directory listings are displayed in the `ECAUTH` module.
- Fixed incorrect load balancing in transfer group allocation when new files are registered.

## open-ecpds 7.3.5-20012026

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

## open-ecpds 7.3.4-09122025

- Refactored SQL queries for data-retrieval, replication, and filtering schedulers.
- Enabled filtering of data transfers by allocated data mover (`mover`) in the search box.
- Fixed replication issue where the original data mover could not always be found.

## open-ecpds 7.3.3-02122025

- Retrieved total/free space and filesystem identifiers from movers for weighted allocation based on disk usage.
- Accounted for replication when disks are replaced, allowing empty volumes to grow gradually.

## open-ecpds 7.3.1-11112025

- Added explanatory comments for certain entries in the destination form.
- Added REST endpoint to update host options, enabling automated token rotation for HTTPS authentication.
- Replaced `incoming.tmp` with `incoming.tmpDetect` and `incoming.tmpPattern` for simpler temporary file handling.
- Applied filter patterns to original filenames rather than target names via `ectrans.filterpattern`.

## open-ecpds 7.3.0-05112025

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

## open-ecpds 7.2.5-15102025

- Enhanced SMTP, POP, and IMAP support with default ports and simplified configuration.
- Added `proxy.useDestinationFilter` to enforce destination-level compression.
- Fixed broken links to the Data Mover page in Transfer History events.
- Added warning prompt when updating a Proxy Host.

## open-ecpds 7.2.5-09102025

- Adjusted ECPDS product submission to the ecChart data layer.
- Reduced retention periods for obsolete statistics tables.
- Displayed Autonomous System (AS) numbers in `mtr` output.
- Explicit login-failure messages for exceeded connection limits.
- Reported error if no destination name is specified in the `ecpds` CLI.
- Added chunk size option in the Azure transfer module for memory-limited movers.

## open-ecpds 7.2.5-30092025

- Explicitly closed multipart output before calling `complete()` on S3 uploads.
- Removed deprecated tables: `Q2DISS_REPORT`, `STATISTICS`, `DISK_USAGE`.
- Introduced `forceproxy` search criterion for Destinations.
- Ensured Destination is not stopped before starting an Acquisition Thread.

## open-ecpds 7.2.4-26092025

- Removed `CloseableClientResponse` for WAR context compatibility.

## open-ecpds 7.2.1-23092025

- Restricted users can now view the full transfer history of pre-scheduled files.
- Transfer scheduler delays transmission when no server is available.
- Destinations can disseminate through proxies only.
- Ensured proper closure of streams if replication fails.
- Transfer Group fall-back: pick alternative group from same Cluster if no Mover available.
- Transfer Module upgrades:
    - **GCS**: configurable upload chunk size.
    - **S3**: improved role assumption and error extraction.
- Dependency upgrades: GraalVM 24.2.1 → 25.0.0, JDK 24.0.1 → 25.0.0, jsch 0.2.24 → 2.27.3, s3-stream-upload 2.0.3 → 2.2.4, httpclient5 5.3 → 5.3.1.

## open-ecpds 7.2.0-02092025

- Improved HTTP headers and response handling for portal compliance (GitHub issues #1–#5).
- Used try-with-resources to ensure proper closure of HTTP/S connections.
- Logged and validated metaDate/metaTime during submissions.
- Upgraded base image to `rockylinux/rockylinux:9.6`.
- Dependency upgrades: GraalVM 24.2.1 → 24.2.2, JDK 24.0.1 → 24.0.2.

## open-ecpds 7.1.7-20082025

- Ensured RMI stream objects are properly unexported when streams close.
- Fixed long-term memory leak for expired RMI scheduled tasks.
- Cached `RestClient` to reduce object creation overhead for Opsview notifications.
- Fixed batch file selection and race conditions causing stuck transfers.

## open-ecpds 7.1.0-21072025

- Major release with dependency updates, bug fixes, and new features.
- Scripts now require explicit return statements to avoid global context pollution.
- Removed `wmoLikeFormat` from FTP, FTPS, SFTP modules.
- Switched from MariaDB JDBC to Oracle MySQL JDBC; upgraded to **Hibernate 7**.
- Used virtual threads where possible.
- Aggressive purging of expired files on movers and proxies.

## open-ecpds 6.8.8-06062025

- Removed deprecated `finalize()` and replaced with `java.lang.ref.Cleaner`.
- Cleaned thread-locals, MDC, and context class loader to prevent leaks.
- Removed `bzip2a` support; preferred `lbzip2`.
- Added `$${context:...}` placeholders in host/destination editors.
- Sandboxed Polyglot context execution; prevented global scope pollution.
- Pooled Polyglot contexts; enforced statement limits.
- Deactivated Hibernate statistics.
- Dependency upgrades: GraalVM 24.2.0 → 24.2.1, JDK 24.0.0 → 24.0.1, Azure 1.24.1 → 1.53.0, HiveMQ MQTT 2024.7 → 2025.3, Jetty 10.0.18 → 10.0.25, Log4j 2.20.0 → 2.24.0, Disruptor 3.4.2 → 4.0.0, and others.

## open-ecpds 6.8.2-16022025

- Enabled native access for Polyglot JDK support.
- Activated virtual threads within Polyglot context.
- Finer JVM heap memory control; switched to **ZGC**.
- Introduced **DirectBuffer** pool for I/O performance.
- Showed MQTT file retrieval progress.
- Prevented double synchronisation after file writing.
- Allowed custom `User-Agent` and `Accept` headers in the HTTP/S transfer module.
- Increased concurrent data transfer deletions to 50.

## open-ecpds 6.8.0-01032025

- Added `libsocketoption.so` to timeout `ss` processes after 2 s (**Java-native** implementation).
- Introduced new Splunk events for denial monitoring.
- Default sorting for product page; search by email and quick access to destination type.
- Displayed retrieval progress on transfer pages.

## open-ecpds 6.7.9-09112024

- Added additional fields for improved Splunk monitoring.
- Configured data portal for **RFC 3986** compliance (allow ambiguous URIs).

---

## Related

- [Support Materials](support.md)
- [Contributing](contributing.md)

