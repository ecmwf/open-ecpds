##
## References
##
#menu "ECpdsBase"
#name "getDataTransfersToBackup"
#group "query"

##
## Variable(s)
##
#prompt "currentTimeMillis;Current Time in Milliseconds;;long"
#prompt "limit;Limit;%;int"

##
## Request(s)
##
SELECT DT.* FROM
  DATA_TRANSFER DT, DATA_FILE DF
WHERE
  DT.DAF_ID = DF.DAF_ID
  AND DES_NAME IN (select DES_NAME from DESTINATION where DES_BACKUP)
  AND TRG_NAME IN (select distinct(TS1.TRG_NAME) from TRANSFER_GROUP TG,TRANSFER_SERVER TS1 where TRG_BACKUP
    AND (SELECT COUNT(*) FROM TRANSFER_SERVER TS2 WHERE TS1.TRG_NAME = TS2.TRG_NAME AND TRS_ACTIVE) > 0)
  AND DAT_REPLICATED
  AND DAT_EXPIRY_TIME > $currentTimeMillis
  AND DT.HOS_NAME_BACKUP is NULL
  AND DT.STA_CODE IN ('WAIT','RETR','HOLD','DONE')
  AND (DAT_BACKUP_TIME IS NULL OR ($currentTimeMillis - DAT_BACKUP_TIME) > 1800000)
  AND NOT DAF_DELETED
  AND NOT DAT_DELETED
ORDER BY
  DAF_GROUP_BY ASC, DAT_QUEUE_TIME ASC, DAT_ID ASC
LIMIT $limit
