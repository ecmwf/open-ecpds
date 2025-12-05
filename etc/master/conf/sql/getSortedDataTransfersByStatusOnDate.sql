##
## References
##
#menu "ECpdsBase"
#name "getSortedDataTransfersByStatusOnDate"
#group "select"

##
## Variable(s)
##
#prompt "status;Which status;%"
#prompt "datafile;Datafile;no"
#prompt "fileName;File Name;%"
#prompt "source;Source;%"
#prompt "ts;Time Step;%"
#prompt "priority;Priority;%"
#prompt "checksum;Checksum;%"
#prompt "groupby;Group By;%"
#prompt "identity;Identity;%"
#prompt "size;Size;%"
#prompt "replicated;Replicated;%"
#prompt "asap;Asap;%"
#prompt "event;Event;%"
#prompt "deleted;Deleted;%"
#prompt "expired;Expired;%"
#prompt "proxy;Proxy;%"
#prompt "mover;Mover;%"
#prompt "fromDate;From date;%;java.sql.Timestamp"
#prompt "toDate;To date;%;java.sql.Timestamp"
#prompt "sort;Column;%"
#prompt "order;Ordering (Descending=2,Ascending=1);%"
#prompt "start;Start column;%"
#prompt "length;Column length;%"
#prompt "type;Type;%"

##
## Request(s)
##
SELECT SQL_CALC_FOUND_ROWS DAT_ID, DT.DAF_ID, DES_NAME, HOS_NAME, TRS_NAME, DAT_SIZE, DAT_TIME_STEP, DAT_TARGET, STA_CODE, DAT_FAILED_TIME,
  DAT_USER_STATUS, DAT_SENT, DAT_DURATION, DAT_DELETED, DAT_PRIORITY, DAT_SCHEDULED_TIME, DAT_RETRY_TIME, DAT_QUEUE_TIME, DAT_START_TIME,
  DAT_FINISH_TIME
FROM
  DATA_TRANSFER DT USE INDEX (DATA_TRANSFER_DAT_TIME_BASE_IDX)
#if $("$datafile" == "yes")
	, DATA_FILE DF
#fi
WHERE
  DAT_TIME_BASE >= '$fromDate'
  AND DAT_TIME_BASE < '$toDate'
#if $("$datafile" == "yes")
	AND DT.DAF_ID = DF.DAF_ID
#fi
	$fileName
	$source
	$ts
	$priority
	$checksum
	$groupby
	$identity
	$size
	$replicated
	$asap
	$event
	$deleted
	$expired
	$proxy
	$mover
#if ('$status' != 'All')
  AND STA_CODE = '$status'
#fi
#if ('$type' != '')
  AND DES_NAME IN (SELECT DES_NAME FROM DESTINATION WHERE DES_TYPE in ($type))
#fi
#if ('$sort' == '0')
	ORDER BY DES_NAME
#fi
#if ('$sort' == '1')
	ORDER BY (HOS_NAME IS NULL), HOS_NAME
#fi
#if ('$sort' == '2')
	ORDER BY (DAT_SCHEDULED_TIME IS NULL), DAT_SCHEDULED_TIME
#fi
#if ('$sort' == '3')
	ORDER BY (DAT_TARGET IS NULL), DAT_TARGET
#fi
#if ('$sort' == '4')
	ORDER BY (DAT_SENT = '0'), (DAT_SENT/DAT_SIZE)
#fi
#if ('$sort' == '5')
	ORDER BY (DAT_SENT = '0'), (DAT_SENT/DAT_DURATION)
#fi
#if ('$sort' == '6')
	ORDER BY (DAT_PRIORITY IS NULL), DAT_PRIORITY
#fi
#if ('$order' == '1')
	ASC
#fi
#if ('$order' == '2')
	DESC
#fi
LIMIT $start,$length
