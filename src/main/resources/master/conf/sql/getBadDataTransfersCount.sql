##
## References
##
#menu "ECpdsBase"
#name "getBadDataTransfersCount"
#group "select"

##
## Request(s)
##
SELECT DES_NAME,COUNT(*) AS COUNT
FROM
  DATA_TRANSFER DAT USE INDEX(replicateRequest2)
WHERE
  (NOT (DAT_DELETED<>0)) AND
  ((DAT.STA_CODE = 'WAIT' AND DAT.DAT_START_COUNT > 0)
    OR (DAT.STA_CODE = 'RETR' AND DAT.DAT_USER_STATUS IS NULL AND (DAT.DAT_COMMENT LIKE 'Requeued by scheduler%' OR DAT.DAT_COMMENT LIKE 'Maximum retry limit reached%'))
    OR (DAT.STA_CODE = 'STOP' AND DAT.DAT_USER_STATUS IS NULL)
    OR DAT.STA_CODE = 'FAIL')
GROUP BY DES_NAME
