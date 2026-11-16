##
## References
##
#menu "ECpdsBase"
#name "getDataTransfersByDestinationAndTarget2"
#group "select"

##
## Variable(s)
##
#prompt "destination;Destination name;%"
#prompt "target;Target name;%"
#prompt "currentTimeMillis;Current Time in Milliseconds;;long"
#prompt "runnable;Can this transfer be queued?;%;java.lang.String"
#prompt "sort; Column (1=DAT_SIZE,2=DAT_TARGET,3=DAT_SCHEDULED_TIME);%"
#prompt "order; Ordering (Descending=2,Ascending=1);%"

##
## Request(s)
##
SELECT DAT_ID, DAT_SIZE, DAT_TARGET, DAT_SCHEDULED_TIME,
  DAF.DAF_ID AS FILE_VERSION_ID, DAF.DAF_FILE_INSTANCE
FROM
  DATA_TRANSFER DAT USE INDEX(listIndex)
  LEFT JOIN DATA_FILE DAF ON DAF.DAF_ID = DAT.DAF_ID
WHERE
  DAT.DES_NAME = '$destination'
#if ('$target' != '')
  AND (DAT.DAT_TARGET LIKE '$target' OR DAT.DAT_TARGET LIKE '/$target')
#fi
#if ('$runnable' == 'true')
  AND DAT.STA_CODE NOT IN ('INIT', 'SCHE', 'FETC')
  AND DAT.DAT_QUEUE_TIME < $currentTimeMillis
  AND DAT.DAT_DELETED = 0
#fi
ORDER BY
#if ('$sort' == '1')
  (DAT_SIZE IS NULL), DAT_SIZE
#fi
#if ('$sort' == '2')
  (DAT_TARGET IS NULL), DAT_TARGET
#fi
#if ('$sort' == '3')
  (DAT_SCHEDULED_TIME IS NULL), DAT_SCHEDULED_TIME
#fi
#if ('$order' == '1')
  ASC,
#fi
#if ('$order' == '2')
  DESC,
#fi
DAT_ID DESC
