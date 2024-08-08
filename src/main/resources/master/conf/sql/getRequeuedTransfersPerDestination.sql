##
## References
##
#menu "ECpdsBase"
#name "getRequeuedTransfersPerDestination"
#group "query"

##
## Variable(s)
##
#prompt "destination;Which destination;%"

##
## Request(s)
##
SELECT DATA_TRANSFER.*
FROM
	DATA_TRANSFER
WHERE
  DES_NAME = '$destination'
  AND (STA_CODE = 'WAIT' OR STA_CODE = 'RETR')
  AND NOT (DAT_DELETED<>0)
  AND (DAT_START_COUNT > 0 OR DAT_REQUEUE_HISTORY > 0)
