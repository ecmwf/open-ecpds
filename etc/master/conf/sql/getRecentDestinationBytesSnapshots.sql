##
## References
##
#menu "ECpdsBase"
#name "getRecentDestinationBytesSnapshots"
#group "select"

##
## Variable(s)
##
#prompt "retentionHours;Number of hours to look back;168"

##
## Request(s)
##
SELECT
  DBS_DESTINATION    AS DBS_DESTINATION,
  DBS_MINUTE         AS DBS_MINUTE,
  DBS_UPLOAD_BYTES   AS DBS_UPLOAD_BYTES,
  DBS_DOWNLOAD_BYTES AS DBS_DOWNLOAD_BYTES
FROM DESTINATION_BYTES_SNAPSHOT
WHERE DBS_MINUTE >= DATE_SUB(NOW(), INTERVAL '$retentionHours' HOUR)
ORDER BY DBS_DESTINATION ASC, DBS_MINUTE ASC
