# OpenECPDS Release Notes

This document summarizes the most significant changes in OpenECPDS across all releases. The release notes are listed in **descending order**, with the most recent changes first.

---

## open-ecpds 7.3.5-15122026 (2026-12-15)

- Defer transfer group and filesystem allocation until push/pull for new data files to better match actual disk usage.
- Consolidate all transfer-server methods for data file allocation and processing into a common class for maintainability.
- Allow ordering of transfer servers based on live disk usage and free space statistics.
- Add ICO and PNG favicons to the data portal for cross-browser compatibility.
- Resolve HREFs relative to URL/path when parsing HTML files in the HTTP transfer module.
- Allow skipping post-retrieval size checks when retrieving files from sources with inconsistent file size reporting.

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
