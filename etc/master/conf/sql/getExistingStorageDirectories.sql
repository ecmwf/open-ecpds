##
## References
##
#menu "ECpdsBase"
#name "getExistingStorageDirectories"
#group "select"

##
## Request(s)
##
SELECT
    DATE(FROM_UNIXTIME(df.DAF_ARRIVED_TIME / 1000)) AS DAF_ARRIVED_DATE,
    df.TRG_NAME,
    df.DAF_FILE_SYSTEM,
    COUNT(*) AS FILES_COUNT,
    SUM(df.DAF_SIZE) AS FILES_SIZE
FROM
    DATA_TRANSFER dt
JOIN
    DATA_FILE df
    ON dt.DAF_ID = df.DAF_ID
WHERE
    df.TRG_NAME IS NOT NULL
    AND df.DAF_FILE_SYSTEM IS NOT NULL
    AND dt.DAT_DELETED = 0
GROUP BY
    df.TRG_NAME,
    DAF_ARRIVED_DATE,
    df.DAF_FILE_SYSTEM
ORDER BY
    df.TRG_NAME,
    DAF_ARRIVED_DATE,
    df.DAF_FILE_SYSTEM
