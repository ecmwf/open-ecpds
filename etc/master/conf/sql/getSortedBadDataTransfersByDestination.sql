##
## References
##
#menu "ECpdsBase"
#name "getSortedBadDataTransfersByDestination"
#group "select"

##
## Variable(s)
##
#prompt "destination;Destination name;%"
#prompt "sort;Column;%"
#prompt "order;Ordering (Descending=2,Ascending=1);%"
#prompt "start;Start column;%"
#prompt "length;Column length;%"
#prompt "search;Search term;%"

##
## Request(s)
##
SELECT
  SQL_CALC_FOUND_ROWS DAT_ID,DES_NAME,HOS_NAME,DAT_TARGET,STA_CODE,DAT_USER_STATUS,DAT_SENT,DAT_SIZE,DAT_DURATION,DAT_PRIORITY,DAT_COMMENT,DAT_SCHEDULED_TIME,DAT_FAILED_TIME,DAT_START_COUNT
FROM
  DATA_TRANSFER
WHERE
  (NOT (DAT_DELETED<>0)) AND
  DES_NAME='$destination' AND (
    (STA_CODE = 'WAIT' AND DAT_START_COUNT > 0)
    OR (STA_CODE = 'RETR' AND DAT_USER_STATUS IS NULL AND
      (DAT_COMMENT LIKE 'Requeued by scheduler%' OR
        DAT_COMMENT LIKE 'Maximum requeue limit reached%' OR
        DAT_COMMENT LIKE 'Maximum start limit reached%'))
    OR (STA_CODE = 'STOP' AND DAT_USER_STATUS IS NULL)
    OR STA_CODE = 'FAIL'
  )
#if ('$search' != '')
  AND (HOS_NAME LIKE '%$search%' OR DAT_TARGET LIKE '%$search%' OR STA_CODE LIKE '%$search%' OR DAT_COMMENT LIKE '%$search%')
#fi
#if ('$sort' == '0')
ORDER BY (HOS_NAME IS NULL), HOS_NAME
#fi
#if ('$sort' == '1')
ORDER BY (DAT_TARGET IS NULL), DAT_TARGET
#fi
#if ('$sort' == '2')
ORDER BY (STA_CODE IS NULL), STA_CODE
#fi
#if ('$sort' == '3')
ORDER BY (DAT_PRIORITY IS NULL), DAT_PRIORITY
#fi
#if ('$sort' == '4')
ORDER BY (DAT_COMMENT IS NULL), DAT_COMMENT
#fi
#if ('$order' == '1')
ASC
#fi
#if ('$order' == '2')
DESC
#fi
#if ('$start' != '-1')
LIMIT $start,$length
#fi
