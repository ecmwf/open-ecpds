##
## References
##
#menu "ECpdsBase"
#name "getDataTransfersPerTransferServer"
#group "select"

##
## Variable(s)
##
#prompt "server; TransferServer%"
#prompt "from; From which date;%;java.sql.Timestamp"
#prompt "to; To what date;%;java.sql.Timestamp"

##
## Request(s)
##
SELECT DAF_ID, DAT_SIZE, DAT_TIME_STEP, DAT_ID, DES_NAME, HOS_NAME, TRS_NAME, DAT_TARGET, STA_CODE,
  DAT_USER_STATUS, DAT_SENT, DAT_DURATION, DAT_PRIORITY, DAT_SCHEDULED_TIME, DAT_RETRY_TIME, DAT_QUEUE_TIME
FROM
  DATA_TRANSFER
WHERE
  TRS_NAME= '$server' AND
  DAT_TIME_BASE >= '$from' AND
  DAT_TIME_BASE < '$to'