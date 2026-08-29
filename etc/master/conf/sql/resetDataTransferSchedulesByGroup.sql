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
CHUNK 1000 UPDATE DATA_TRANSFER DT
  JOIN DATA_FILE DF ON DT.DAF_ID = DF.DAF_ID
  SET DT.DAT_QUEUE_TIME = '$currentTimeMillis', DT.DAT_RETRY_TIME = '$currentTimeMillis'
  WHERE DT.DAT_GROUP_BY = '$groupBy'
    AND DT.DAT_ASAP
    AND DT.STA_CODE IN ('WAIT','HOLD')
    AND NOT DF.DAF_DELETED
    AND DF.DAF_DOWNLOADED
    AND DT.DAT_QUEUE_TIME > '$currentTimeMillis'
