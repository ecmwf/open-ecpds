##
## References
##
#menu "ECpdsBase"
#name "getScheduledDataTransfer"
#group "query"

##
## Variable(s)
##
#prompt "uniqueKey;Unique key;%"
#prompt "destination;Destination name;%"

##
## Request(s)
##
SELECT DATA_TRANSFER.*
FROM
  DATA_TRANSFER
WHERE
  DAT_UNIQUE_KEY = '$uniqueKey'
  AND (NOT (DAT_DELETED<>0))
  AND (
    DES_NAME = '$destination'
    OR DES_NAME IN (SELECT DES_NAME FROM ALIAS WHERE ALI_DES_NAME = '$destination'))
