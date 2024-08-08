##
## References
##
#menu "ECpdsBase"
#name "getDataTransfersByDestinationAndTarget"
#group "query"

##
## Variable(s)
##
#prompt "destination;Destination name;%"
#prompt "target;Target name;%"
#prompt "currentTimeMillis;Current Time in Milliseconds;;long"
#prompt "runnable;Can this transfer be queueud?;%;java.lang.String"

##
## Request(s)
##
SELECT DAT.*
FROM
  DATA_TRANSFER DAT USE INDEX(listIndex), DATA_FILE DAF
WHERE
  DAT.DAF_ID = DAF.DAF_ID
  AND DAT.DES_NAME = '$destination'
#if ('$target' != '')
  AND (DAT.DAT_TARGET LIKE '$target' OR DAT.DAT_TARGET LIKE '/$target')
#fi
#if ('$runnable' == 'true')
  AND NOT DAT.STA_CODE = 'INIT'
  AND DAT.DAT_SCHEDULED_TIME < $currentTimeMillis
  AND DAT.DAT_DELETED = 0
#fi
ORDER BY
  DAT_ID DESC
