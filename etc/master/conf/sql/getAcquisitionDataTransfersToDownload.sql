##
## References
##
#menu "ECpdsBase"
#name "getAcquisitionDataTransfersToDownload"
#group "query"

##
## Variable(s)
##
#prompt "currentTimeMillis;Current Time in Milliseconds;;long"
#prompt "limit;Limit;%;int"

##
## Request(s)
##
SELECT *
FROM (
  SELECT
    DT.*,
    DF.DAF_GROUP_BY,
    ROW_NUMBER() OVER (
      PARTITION BY DT.DES_NAME
      ORDER BY
        DF.DAF_GROUP_BY ASC,
        DT.DAT_PRIORITY ASC,
        DT.DAT_QUEUE_TIME ASC,
        DT.DAT_ID ASC
    ) AS ROW_NUM
  FROM DATA_TRANSFER DT
  USE INDEX (replicateRequest2)
  JOIN DATA_FILE DF
    ON DT.DAF_ID = DF.DAF_ID
  JOIN DESTINATION D
    ON D.DES_NAME = DT.DES_NAME
   AND D.STA_CODE <> 'STOP'
  WHERE
    DF.DAF_GROUP_BY IS NOT NULL
    AND DF.HOS_NAME_FOR_ACQUISITION IS NOT NULL
    AND DT.STA_CODE = 'SCHE'
    AND DF.DAF_DOWNLOADED = 0
    AND DT.DAT_EXPIRY_TIME > $currentTimeMillis
    AND (
          DT.DAT_REPLICATE_TIME IS NULL
          OR DT.DAT_REPLICATE_COUNT < 3
          OR DT.DAT_REPLICATE_TIME < ($currentTimeMillis - 180000)
        )
    AND DT.DAT_DELETED = 0
) T
WHERE T.ROW_NUM < 60;
