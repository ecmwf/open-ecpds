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
SELECT DT.*
FROM DATA_TRANSFER DT
JOIN DATA_FILE DF
  ON DT.DAF_ID = DF.DAF_ID
JOIN DESTINATION D
  ON D.DES_NAME = DT.DES_NAME
 AND D.DES_BACKUP = 1
JOIN TRANSFER_GROUP TG
  ON TG.TRG_NAME = DF.TRG_NAME
 AND TG.TRG_BACKUP = 1
WHERE
  EXISTS (
    SELECT 1
    FROM TRANSFER_SERVER TS
    WHERE TS.TRG_NAME = TG.TRG_NAME
      AND TS.TRS_ACTIVE = 1
  )
  AND DT.DAT_REPLICATED = 1
  AND DT.DAT_EXPIRY_TIME > $currentTimeMillis
  AND DT.HOS_NAME_BACKUP IS NULL
  AND DT.STA_CODE IN ('WAIT','RETR','HOLD','DONE')
  AND (
        DT.DAT_BACKUP_TIME IS NULL
        OR DT.DAT_BACKUP_TIME < ($currentTimeMillis - 1800000)
      )
  AND DF.DAF_FILE_SYSTEM IS NOT NULL
  AND DF.DAF_DELETED = 0
  AND DT.DAT_DELETED = 0
ORDER BY
  DF.DAF_GROUP_BY ASC,
  DT.DAT_QUEUE_TIME ASC,
  DT.DAT_ID ASC
LIMIT $limit;
