##
## References
##
#menu "ECpdsBase"
#name "resetDataTransferQueueAndRetryTimeToScheduledTime"
#group "update"

##
## Variable(s)
##
#prompt "destinationName;Destination Name;%"

##
## Request(s)
##
CHUNK 1000 UPDATE DATA_TRANSFER SET DAT_QUEUE_TIME = DAT_SCHEDULED_TIME, DAT_RETRY_TIME = DAT_SCHEDULED_TIME
  WHERE DAT_ID IN (
    SELECT DAT_ID FROM DATA_TRANSFER
    WHERE DES_NAME = '$destinationName'
  	AND (STA_CODE = 'WAIT' OR STA_CODE = 'RETR')
  	AND NOT (DAT_DELETED<>0)
  	AND (DAT_START_COUNT > 0 OR DAT_REQUEUE_HISTORY > 0)
)
