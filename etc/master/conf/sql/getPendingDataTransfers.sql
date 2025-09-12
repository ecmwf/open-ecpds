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
SELECT DAT.DAT_ID, DAT.STA_CODE, DAT.DAT_RETRY_TIME, DAT.DAT_QUEUE_TIME
FROM
  DATA_TRANSFER DAT
JOIN DESTINATION DES
  ON DAT.DES_NAME = DES.DES_NAME
WHERE
  (DAT.STA_CODE IN ('WAIT','RETR','INTR'))
  AND DAT.DES_NAME = '$destination'
  AND DAT.DAT_QUEUE_TIME <= '$before'
  AND (NOT DAT.DAT_EXPIRY_TIME < $currentTimeMillis)
  AND (NOT (DAT.DAT_DELETED<>0))
  AND (
    DES.DES_FORCE_PROXY = 0
    OR DAT.DES_NAME NOT IN (
      SELECT A.DES_NAME
      FROM ASSOCIATION A
      JOIN HOST H ON A.HOS_NAME = H.HOS_NAME
      WHERE H.HOS_TYPE = 'Proxy' AND H.HOS_ACTIVE <> 0)
    OR DAT.HOS_NAME_PROXY IS NOT NULL
  )
ORDER BY
  DAT.DAT_PRIORITY ASC, DAT.DAT_QUEUE_TIME ASC, DAT.DAT_ID ASC
LIMIT $limit
