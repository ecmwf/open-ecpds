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
SELECT DT1.*
FROM
  DATA_TRANSFER DT1,DATA_FILE DF1
WHERE
  NOT DAF_GROUP_BY IS NULL
  AND HOS_NAME_FOR_ACQUISITION IS NULL
  AND DT1.DAF_ID = DF1.DAF_ID
  AND DT1.STA_CODE = 'SCHE'
  AND DF1.DAF_DOWNLOADED = 0
  AND DT1.DAT_EXPIRY_TIME > $currentTimeMillis
  AND (DT1.DAT_REPLICATE_TIME IS NULL OR DAT_REPLICATE_COUNT < 50 OR ($currentTimeMillis - DT1.DAT_REPLICATE_TIME) > 30000)
  AND DT1.DAT_DELETED = 0
ORDER BY
  DAF_GROUP_BY ASC, DAT_QUEUE_TIME ASC, DAT_ID ASC
LIMIT $limit
