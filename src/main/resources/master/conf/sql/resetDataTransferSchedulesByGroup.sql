##
## References
##
#menu "ECpdsBase"
#name "resetDataTransferSchedulesByGroup"
#group "update"

##
## Variable(s)
##
#prompt "currentTimeMillis;Current Time in Milliseconds;;long"
#prompt "groupBy;Group by;%"

##
## Request(s)
##
CHUNK 1000 UPDATE DATA_TRANSFER SET DAT_QUEUE_TIME = '$currentTimeMillis', DAT_RETRY_TIME = '$currentTimeMillis'
  WHERE DAT_ID IN (
    SELECT DAT_ID FROM DATA_TRANSFER, DATA_FILE
  		WHERE DATA_TRANSFER.DAF_ID = DATA_FILE.DAF_ID
  		AND DAF_GROUP_BY = '$groupBy'
  		AND DAT_ASAP
  		AND STA_CODE IN ('WAIT','HOLD')
  		AND NOT DAF_DELETED
  		AND DAF_DOWNLOADED
  		AND DAT_QUEUE_TIME > '$currentTimeMillis'
)
