##
## References
##
#menu "ECpdsBase"
#name "getDataTransfersByDestinationAndTargetOnDate2"
#group "select"

##
## Variable(s)
##
#prompt "destination;Destination name;%"
#prompt "fromDate;From date;%;java.sql.Timestamp"
#prompt "toDate;To date;%;java.sql.Timestamp"
#prompt "target;Target name;%"
#prompt "currentTimeMillis;Current Time in Milliseconds;;long"
#prompt "sort; Column (1=DAT_SIZE,2=DAT_TARGET,3=DAT_SCHEDULED_TIME);%"
#prompt "order; Ordering (Descending=2,Ascending=1);%"

##
## Request(s)
##
SELECT DAT_ID, DAT_SIZE, DAT_TARGET, DAT_SCHEDULED_TIME, DAT_QUEUE_TIME
FROM
  DATA_TRANSFER DAT
WHERE
  DAT.DES_NAME = '$destination'
#if ('$target' != '')
  AND DAT.DAT_TARGET LIKE '$target'
#fi
  AND NOT DAT.STA_CODE = 'INIT'
  AND NOT DAT.STA_CODE = 'SCHE'
  AND DAT.DAT_TIME_BASE >= '$fromDate'
  AND DAT.DAT_TIME_BASE < '$toDate'
  AND DAT.DAT_QUEUE_TIME < $currentTimeMillis
  AND DAT.DAT_DELETED = 0
ORDER BY
#if ('$sort' == '1')
  (DAT_SIZE IS NULL), DAT_SIZE
#fi
#if ('$sort' == '2')
  (DAT_TARGET IS NULL), DAT_TARGET
#fi
#if ('$sort' == '3')
  DAT_SCHEDULED_TIME
#fi
#if ('$order' == '1')
  ASC,
#fi
#if ('$order' == '2')
  DESC,
#fi
DAT_ID DESC
