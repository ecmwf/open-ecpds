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
SELECT dt.*
FROM DATA_TRANSFER dt USE INDEX (replicateRequest2)
JOIN DATA_FILE df 
    ON dt.DAF_ID = df.DAF_ID
JOIN TRANSFER_GROUP tg
    ON df.TRG_NAME = tg.TRG_NAME
JOIN TRANSFER_SERVER ts1
    ON dt.TRS_NAME_ORIGINAL = ts1.TRS_NAME
WHERE 
    dt.DAT_EXPIRY_TIME > $currentTimeMillis
    AND dt.STA_CODE IN ('WAIT', 'RETR', 'HOLD', 'DONE')
    AND NOT (dt.DAT_REPLICATED = 1 AND dt.DAT_REPLICATE_TIME IS NULL)
    AND (
        dt.DAT_REPLICATE_TIME IS NULL 
        OR ($currentTimeMillis - dt.DAT_REPLICATE_TIME) > 1800000
    )
    AND dt.DAT_REPLICATED = 0
    AND tg.TRG_REPLICATE <> 0
    AND df.DAF_DOWNLOADED <> 0
    AND (
        -- CASE 1: Filtering is required - ensure filtering is complete and fresh
        EXISTS (
            SELECT 1
            FROM DESTINATION d
            WHERE d.DES_NAME = dt.DES_NAME
              AND d.DES_FILTER_NAME IS NOT NULL
              AND d.DES_FILTER_NAME NOT IN ('none', '')
        )
        AND tg.TRG_FILTER <> 0
        AND df.DAF_FILTER_NAME IS NOT NULL
        AND df.DAF_FILTER_TIME IS NOT NULL
        AND ($currentTimeMillis - df.DAF_FILTER_TIME) <= 1800000
        -- CASE 2: Filtering NOT required - skip filter checks
        OR (
            NOT EXISTS (
                SELECT 1
                FROM DESTINATION d2
                WHERE d2.DES_NAME = dt.DES_NAME
                  AND d2.DES_FILTER_NAME IS NOT NULL
                  AND d2.DES_FILTER_NAME NOT IN ('none', '')
            )
            OR tg.TRG_FILTER = 0
        )
    )
    AND (
        dt.DAT_ASAP = 0
        OR (
            dt.DAT_ASAP <> 0
            AND (
                df.DAF_GET_TIME IS NULL
                OR (
                    $currentTimeMillis 
                    - (df.DAF_GET_TIME + df.DAF_GET_COMPLETE_DURATION)
                ) > 180000
            )
        )
    )
    AND df.DAF_DELETED = 0
    AND dt.DAT_DELETED = 0
    AND (
        SELECT COUNT(*)
        FROM TRANSFER_SERVER ts2
        WHERE ts2.TRG_NAME = ts1.TRG_NAME
          AND ts2.TRS_ACTIVE <> 0
    ) > 1
ORDER BY 
    CASE WHEN dt.STA_CODE = 'HOLD' THEN 1 ELSE 0 END ASC,
    dt.DAT_PRIORITY ASC,
    dt.DAT_QUEUE_TIME ASC,
    dt.DAT_ID ASC
LIMIT $limit;
