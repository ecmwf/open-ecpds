##
## References
##
#menu "ECpdsBase"
#name "getDestinationExts"
#group "select"

##
## Request(s)
##
SELECT 
    des.DES_NAME,
    des.DES_RESET_FREQUENCY,
    des.DES_STOP_IF_DIRTY,
    des.DES_UPDATE,
    COUNT(dat.DAT_ID) AS PENDING_COUNT,
    scv.SCV_ID,
    MIN(dat.DAT_QUEUE_TIME) AS MIN_QUEUE_TIME,
    scv.HOS_NAME,
    scv.SCV_START_COUNT,
    scv.SCV_RESET_TIME,
    des.STA_CODE
FROM DATA_TRANSFER AS dat
USE INDEX (schedulerRequest)
JOIN DESTINATION AS des
    ON dat.DES_NAME = des.DES_NAME
JOIN SCHEDULER_VALUE AS scv
    ON des.SCV_ID = scv.SCV_ID
WHERE 
    dat.STA_CODE IN ('WAIT', 'RETR')
    AND dat.DAT_DELETED = 0
    AND des.DES_ACTIVE <> 0
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
GROUP BY 
    des.DES_NAME,
    des.DES_RESET_FREQUENCY,
    des.DES_STOP_IF_DIRTY,
    des.DES_UPDATE,
    scv.SCV_ID,
    scv.HOS_NAME,
    scv.SCV_START_COUNT,
    scv.SCV_RESET_TIME,
    des.STA_CODE;
