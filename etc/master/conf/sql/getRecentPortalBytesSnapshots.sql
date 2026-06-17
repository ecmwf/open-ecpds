##
## References
##
#menu "ECpdsBase"
#name "getRecentPortalBytesSnapshots"
#group "select"

##
## Variable(s)
##
#prompt "retentionHours;Number of hours to look back;168"

##
## Request(s)
##
SELECT
  PBS_USER           AS PBS_USER,
  PBS_MINUTE         AS PBS_MINUTE,
  PBS_UPLOAD_BYTES   AS PBS_UPLOAD_BYTES,
  PBS_DOWNLOAD_BYTES AS PBS_DOWNLOAD_BYTES
FROM PORTAL_BYTES_SNAPSHOT
WHERE PBS_MINUTE >= DATE_SUB(NOW(), INTERVAL '$retentionHours' HOUR)
ORDER BY PBS_USER ASC, PBS_MINUTE ASC
