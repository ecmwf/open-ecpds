##
## References
##
#menu "ECpdsBase"
#name "getInterruptedTransfers"
#group "query"

##
## Request(s)
##
SELECT DATA_TRANSFER.*
FROM
	DATA_TRANSFER
WHERE
  (STA_CODE = 'EXEC' OR STA_CODE = 'INTR' OR STA_CODE = 'INIT' OR STA_CODE = 'FETC')
  AND NOT (DAT_DELETED<>0)
