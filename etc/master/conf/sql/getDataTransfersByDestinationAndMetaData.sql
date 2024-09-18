##
## References
##
#menu "ECpdsBase"
#name "getDataTransfersByDestinationAndMetaData"
#group "select"

##
## Variable(s)
##
#prompt "from;From which date;%;java.sql.Timestamp"
#prompt "to;To which date;%;java.sql.Timestamp"
#prompt "destination;Destination;%"
#prompt "metaStream;MetaStream;%"
#prompt "metaTime;metaTime;%"

##
## Request(s)
##
SELECT DAF.DAF_ID, DAF_SIZE, DAF_TIME_STEP, DAF_ARRIVED_TIME, DAT.DAT_ID, DES_NAME, HOS_NAME, TRS_NAME, DAT_TARGET, STA_CODE, DAT_IDENTITY,
  DAT_USER_STATUS, DAT_SENT, DAT_DURATION, DAT_PRIORITY, DAT_SCHEDULED_TIME, DAT_FIRST_FINISH_TIME, DAT_FINISH_TIME, DAF_META_STREAM, DAF_META_TARGET,
  DAF_META_TIME, DAF_META_TYPE, DAF_TIME_BASE, DAF.MOV_ID AS DAF_MOV_ID, DAT.MOV_ID AS DAT_MOV_ID, DAT_RETRY_TIME, DAT_QUEUE_TIME
FROM
  DATA_TRANSFER DAT, DATA_FILE DAF
WHERE
  DAT.DAF_ID = DAF.DAF_ID
  AND (NOT (DAT_DELETED<>0))
  AND NOT (STA_CODE = 'DONE' OR STA_CODE = 'HOLD')
  AND DAT.DES_NAME = '$destination'
  AND DAF_TIME_BASE >= '$from'
  AND DAF_TIME_BASE < '$to'
  AND DAF_META_TIME = '$metaTime'
  AND DAF_META_STREAM = '$metaStream'
  AND NOT DAF.MOV_ID is NULL
  AND NOT DAT.MOV_ID is NULL
