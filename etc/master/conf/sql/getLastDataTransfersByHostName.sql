##
## References
##
#menu "ECpdsBase"
#name "getLastDataTransfersByHostName"
#group "select"

##
## Variable(s)
##
#prompt "host;Host name;"
#prompt "limit;Maximum number of rows;50"

##
## Request(s)
##
SELECT DAF_ID, DAT_SIZE, DAT_TIME_STEP, DAT_ID, DES_NAME, HOS_NAME, TRS_NAME, DAT_TARGET, STA_CODE,
  DAT_USER_STATUS, DAT_SENT, DAT_DURATION, DAT_PRIORITY, DAT_SCHEDULED_TIME, DAT_RETRY_TIME, DAT_QUEUE_TIME
FROM
  DATA_TRANSFER
WHERE
  HOS_NAME = '$host'
ORDER BY DAT_ID DESC
LIMIT $limit
