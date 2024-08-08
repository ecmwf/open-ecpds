##
## References
##
#menu "ECpdsBase"
#name "getDataTransfersByDestinationAndTargetOnTransmissionDate"
#group "query"

##
## Variable(s)
##
#prompt "destination;Destination name;%"
#prompt "fromDate;From date;%;java.sql.Timestamp"
#prompt "toDate;To date;%;java.sql.Timestamp"
#prompt "target;Target name;%"
#prompt "currentTimeMillis;Current Time in Milliseconds;;long"

##
## Request(s)
##
SELECT DATA_TRANSFER.*
FROM
  DATA_TRANSFER
WHERE
  DES_NAME = '$destination'
#if ('$target' != '')
  AND DAT_TARGET LIKE '$target'
#fi
  AND NOT STA_CODE IN ('INIT','HOLD')
  AND DAT_START_TIME >= '$fromDate'
  AND DAT_START_TIME < '$toDate'
LIMIT 5000
