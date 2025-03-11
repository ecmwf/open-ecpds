##
## References
##
#menu "ECpdsBase"
#name "getDataTransfersToReplicate"
#group "query"

##
## Variable(s)
##
#prompt "currentTimeMillis;Current Time in Milliseconds;;long"
#prompt "limit;Limit;%;int"

##
## Request(s)
##
SELECT DT1.* FROM
  TRANSFER_SERVER TS1,DATA_TRANSFER DT1 USE INDEX(replicateRequest2),DATA_FILE DF1,TRANSFER_GROUP TG
WHERE
  DAT_EXPIRY_TIME > $currentTimeMillis
  AND STA_CODE in ('WAIT','RETR','HOLD','DONE')
  AND (DAT_REPLICATE_TIME IS NULL OR ($currentTimeMillis - DAT_REPLICATE_TIME) > 1800000)
  AND DAT_REPLICATED = 0
  AND DT1.DAF_ID = DF1.DAF_ID
  AND DF1.TRG_NAME = TG.TRG_NAME
  AND TG.TRG_REPLICATE <> 0
  AND DT1.TRS_NAME_ORIGINAL = TS1.TRS_NAME
  AND DAF_DOWNLOADED <> 0
  AND (DAT_ASAP = 0 OR (DAT_ASAP <> 0 AND (DAF_GET_TIME IS NULL OR ($currentTimeMillis - (DAF_GET_TIME + DAF_GET_COMPLETE_DURATION)) > 180000)))
  AND DAF_DELETED = 0
  AND DAT_DELETED = 0
  AND (SELECT COUNT(*) FROM TRANSFER_SERVER TS2 WHERE TS1.TRG_NAME = TS2.TRG_NAME AND TRS_ACTIVE <> 0) > 1
LIMIT $limit
