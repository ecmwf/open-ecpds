##
## References
##
#menu "ECpdsBase"
#name "getDataFilesToFilter"
#group "select"

##
## Variable(s)
##
#prompt "currentTimeMillis;Current Time in Milliseconds;;long"
#prompt "limit;Limit;%;int"

##
## Request(s)
##
SELECT 
    df.DAF_ID,
    df.TRG_NAME,
    d.DES_FILTER_NAME,
    df.DAF_ARRIVED_TIME,
    df.DAF_GET_HOST,
    df.DAF_TIME_STEP,
    df.DAF_ORIGINAL,
    df.DAF_FILTER_TIME,
    df.DAF_CHECKSUM,
    df.DAF_FILE_SYSTEM,
    df.DAF_FILE_INSTANCE,
    df.DAF_SIZE,
    tg.TRG_MIN_FILTERING_COUNT,
    tg.TRG_MIN_REPLICATION_COUNT
FROM DATA_TRANSFER dt USE INDEX (replicateRequest2)
JOIN DESTINATION d 
    ON dt.DES_NAME = d.DES_NAME
JOIN DATA_FILE df
    ON dt.DAF_ID = df.DAF_ID
JOIN TRANSFER_GROUP tg
    ON df.TRG_NAME = tg.TRG_NAME
WHERE 
    d.DES_FILTER_NAME IS NOT NULL
    AND d.DES_FILTER_NAME NOT IN ('none', '')
    AND tg.TRG_FILTER <> 0
    AND dt.STA_CODE IN ('WAIT', 'RETR', 'HOLD')
    AND df.DAF_DELETED = 0
    AND dt.DAT_DELETED = 0
    AND NOT (dt.DAT_REPLICATED = 1 AND dt.DAT_REPLICATE_TIME IS NULL)
    AND (
        (
            df.DAF_FILTER_NAME IS NULL
            AND (
                df.DAF_FILTER_TIME IS NULL
                OR ($currentTimeMillis - df.DAF_FILTER_TIME) > 1800000
            )
        )
        OR df.DAF_FILTER_NAME <> d.DES_FILTER_NAME
    )
ORDER BY 
    CASE WHEN dt.STA_CODE = 'HOLD' THEN 1 ELSE 0 END ASC,
    dt.DAT_PRIORITY ASC,
    dt.DAT_QUEUE_TIME ASC,
    dt.DAT_ID ASC
LIMIT $limit;
