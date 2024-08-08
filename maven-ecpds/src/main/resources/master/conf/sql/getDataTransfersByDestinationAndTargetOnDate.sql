##
## References
##
#menu "ECpdsBase"
#name "getDataTransfersByDestinationAndTargetOnDate"
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
SELECT DAT.*
FROM
  DATA_TRANSFER DAT
WHERE
  DAT.DES_NAME = '$destination'
#if ('$target' != '')
  AND DAT.DAT_TARGET LIKE '$target'
#fi
  AND NOT DAT.STA_CODE = 'INIT'
  AND DAT.DAT_TIME_BASE >= '$fromDate'
  AND DAT.DAT_TIME_BASE < '$toDate'
  AND DAT.DAT_DELETED = 0
