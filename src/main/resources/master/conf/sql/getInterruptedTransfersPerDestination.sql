##
## References
##
#menu "ECpdsBase"
#name "getInterruptedTransfersPerDestination"
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
  AND (STA_CODE = 'EXEC' OR STA_CODE = 'INTR')
  AND NOT (DAT_DELETED<>0)
