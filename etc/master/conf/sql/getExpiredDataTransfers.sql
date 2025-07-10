##
## References
##
#menu "ECpdsBase"
#name "getExpiredDataTransfers"
#group "select"

##
## Request(s)
##
SET SESSION net_write_timeout=180 FOR SELECT
  DAT_ID,DAT_IDENTITY,DAT_EXPIRY_TIME
FROM
  DATA_TRANSFER
WHERE
  (DAT_DELETED<>0)
ORDER BY
  DAT_IDENTITY,
  DAT_SCHEDULED_TIME DESC
