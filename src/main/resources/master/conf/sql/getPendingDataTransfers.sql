##
## References
##
#menu "ECpdsBase"
#name "getPendingDataTransfers"
#group "select"

##
## Variable(s)
##
#prompt "currentTimeMillis;Current Time in Milliseconds;;long"
#prompt "destination;Destination name;%"
#prompt "before;Before date;%;java.sql.Timestamp"
#prompt "limit;Limit;%;int"

##
## Request(s)
##
SELECT DAT_ID, STA_CODE, DAT_RETRY_TIME, DAT_QUEUE_TIME
FROM
  DATA_TRANSFER
WHERE
  (STA_CODE = 'WAIT' OR STA_CODE = 'RETR' OR STA_CODE = 'INTR')
  AND DES_NAME = '$destination'
  AND DAT_QUEUE_TIME <= '$before'
  AND (NOT DAT_EXPIRY_TIME < $currentTimeMillis)
  AND (NOT (DAT_DELETED<>0))
ORDER BY
  DAT_PRIORITY ASC, DAT_QUEUE_TIME ASC, DAT_ID ASC
LIMIT $limit
