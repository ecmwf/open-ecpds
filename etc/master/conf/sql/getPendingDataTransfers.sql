##
## References
##
#menu "ECpdsBase"
#name "getPendingDataTransfers"
#group "select"

##
## Variable(s)
##
#prompt "currentTimeMillis;Current Time in Milliseconds;;long"
#prompt "destination;Destination name;%"
#prompt "before;Before date;%;java.sql.Timestamp"
#prompt "limit;Limit;%;int"

##
## Request(s)
##
SELECT 
    dat.DAT_ID,
    dat.STA_CODE,
    dat.DAT_RETRY_TIME,
    dat.DAT_QUEUE_TIME
FROM DATA_TRANSFER AS dat
JOIN DESTINATION AS des 
    ON dat.DES_NAME = des.DES_NAME
WHERE 
    dat.STA_CODE IN ('WAIT', 'RETR', 'INTR')
    AND dat.DES_NAME = '$destination'
    AND dat.DAT_QUEUE_TIME <= '$before'
    AND dat.DAT_EXPIRY_TIME >= $currentTimeMillis
    AND dat.DAT_DELETED = 0
    AND (
        des.DES_FORCE_PROXY = 0
        OR dat.HOS_NAME_PROXY IS NOT NULL
        OR NOT EXISTS (
            SELECT 1
            FROM ASSOCIATION AS a
            JOIN HOST AS h 
                ON a.HOS_NAME = h.HOS_NAME
            WHERE 
                a.DES_NAME = dat.DES_NAME
                AND h.HOS_TYPE = 'Proxy'
                AND h.HOS_ACTIVE <> 0
        )
    )
ORDER BY 
    dat.DAT_PRIORITY ASC,
    dat.DAT_QUEUE_TIME ASC,
    dat.DAT_ID ASC
LIMIT $limit;
