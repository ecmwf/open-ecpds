##
## References
##
#menu "ECpdsBase"
#name "getBadDataTransfersByDestinationCount"
#group "count"

##
## Variable(s)
##
#prompt "destination;Destination name;%"

##
## Request(s)
##
SELECT COUNT(*)
FROM
  DATA_TRANSFER DAT
WHERE
  (NOT (DAT_DELETED<>0)) AND
  DAT.DES_NAME='$destination' AND (
    (DAT.STA_CODE = 'WAIT' AND DAT.DAT_START_COUNT > 0)
    OR (DAT.STA_CODE = 'RETR' AND DAT.DAT_USER_STATUS IS NULL AND (DAT.DAT_COMMENT LIKE 'Requeued by scheduler%' OR DAT.DAT_COMMENT LIKE 'Maximum retry limit reached%'))
    OR (DAT.STA_CODE = 'STOP' AND DAT.DAT_USER_STATUS IS NULL)
    OR DAT.STA_CODE = 'FAIL'
  )
