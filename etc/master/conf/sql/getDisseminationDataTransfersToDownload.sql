##
## References
##
#menu "ECpdsBase"
#name "getDisseminationDataTransfersToDownload"
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
WHERE
  DF.DAF_GROUP_BY IS NOT NULL
  AND DF.HOS_NAME_FOR_ACQUISITION IS NULL
  AND DT.STA_CODE = 'SCHE'
  AND DF.DAF_DOWNLOADED = 0
  AND DT.DAT_EXPIRY_TIME > $currentTimeMillis
  AND (
        DT.DAT_REPLICATE_TIME IS NULL
        OR DT.DAT_REPLICATE_COUNT < 50
        OR DT.DAT_REPLICATE_TIME < ($currentTimeMillis - 30000)
      )
  AND DT.DAT_DELETED = 0
ORDER BY
  DF.DAF_GROUP_BY ASC,
  DT.DAT_QUEUE_TIME ASC,
  DT.DAT_ID ASC
LIMIT $limit;