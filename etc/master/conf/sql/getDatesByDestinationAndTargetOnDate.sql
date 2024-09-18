##
## References
##
#menu "ECpdsBase"
#name "getDatesByDestinationAndTargetOnDate"
#group "select"

##
## Variable(s)
##
#prompt "destination;Destination name;%"
#prompt "currentTimeMillis;Current Time in Milliseconds;;long"
#prompt "order; Ordering (Descending=2,Ascending=1);%"

##
## Request(s)
##
SELECT DISTINCT(UNIX_TIMESTAMP(DATE_FORMAT(FROM_UNIXTIME(DAF_TIME_BASE/1000), "%Y-%m-%d"))*1000) AS DAF_TIME_BASE
FROM
  DATA_TRANSFER DAT, DATA_FILE DAF
WHERE
  DAT.DAF_ID = DAF.DAF_ID
  AND DAT.DES_NAME = '$destination'
  AND NOT DAT.STA_CODE = 'INIT'
  AND DAT.DAT_SCHEDULED_TIME < $currentTimeMillis
  AND NOT DAT.DAT_DELETED
ORDER BY DAF_TIME_BASE
#if ('$order' == '1')
  ASC
#fi
#if ('$order' == '2')
  DESC
#fi
