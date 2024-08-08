##
## References
##
#menu "ECpdsBase"
#name "getDataTransfersByDestination"
#group "query"

##
## Variable(s)
##
#prompt "destination;Which destination;%"

##
## Request(s)
##
SELECT DAT.*
FROM
  DATA_TRANSFER DAT
WHERE
  DAT.DES_NAME = '$destination'
  AND (NOT (DAT_DELETED<>0))
