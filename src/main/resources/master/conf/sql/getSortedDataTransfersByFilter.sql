##
## References
##
#menu "ECpdsBase"
#name "getSortedDataTransfersByFilter"
#group "select"

##
## Variable(s)
##
#prompt "destination;Destination;%"
#prompt "target;Diss Stream;%"
#prompt "stream;Data Stream;%"
#prompt "time;Data Time;%"
#prompt "status;Status;%"
#prompt "privilegedUser;Can see everything?;%;java.lang.String"
#prompt "scheduledBefore;Count files scheduled before this date;%;java.sql.Timestamp"
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
#prompt "from;From which date;%;java.sql.Timestamp"
#prompt "to;To what date;%;java.sql.Timestamp"
#prompt "sort;Column;%"
#prompt "order;Ordering (Descending=2,Ascending=1);%"
#prompt "start;Start column;%"
#prompt "length;Column length;%"

##
## Request(s)
##
SELECT SQL_CALC_FOUND_ROWS DF.DAF_ID, DAF_ORIGINAL, DAT_SIZE, DAT_TIME_STEP, DAT_ID, DES_NAME, HOS_NAME, TRS_NAME, DAT_TARGET, STA_CODE,
  DAT_USER_STATUS, DAT_SENT, DAT_DURATION, DAT_PRIORITY, DAT_SCHEDULED_TIME, DAT_START_TIME, DAT_RETRY_TIME, DAT_QUEUE_TIME,
  DAT_DELETED,DAT_REPLICATED,HOS_NAME_BACKUP,HOS_NAME_PROXY,DAT_EXPIRY_TIME,DAT_FINISH_TIME,DAT_FAILED_TIME
FROM
	DATA_TRANSFER DT USE INDEX(destinationFilters), DATA_FILE DF
WHERE
	DES_NAME = '$destination'
#if ('$from' != '0')
	AND DAT_TIME_BASE >= '$from'
#fi
#if ('$to' != '9223372036854775807')
	AND DAT_TIME_BASE < '$to'
#fi
#if ('$status' != 'All')
	AND STA_CODE = '$status'
#fi
	AND DT.DAF_ID = DF.DAF_ID
#if ('$target' != 'All')
	AND DAF_META_TARGET = '$target'
#fi
#if ('$stream' != 'All')
	AND concat(DAF_META_TIME,'-',DAF_META_STREAM) = '$stream'
#fi
#if ('$time' != 'All')
	AND DAF_META_TIME = '$time'
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
#if ('$sort' == '0')
	ORDER BY DAT_FAILED_TIME
#fi
#if ('$sort' == '1')
	ORDER BY (HOS_NAME IS NULL), HOS_NAME
#fi
#if ('$sort' == '2')
	ORDER BY (DAT_SCHEDULED_TIME IS NULL), DAT_SCHEDULED_TIME
#fi
#if ('$sort' == '3')
	ORDER BY (DAT_START_TIME IS NULL), DAT_START_TIME
#fi
#if ('$sort' == '4')
	ORDER BY (DAT_FINISH_TIME IS NULL), DAT_FINISH_TIME
#fi
#if ('$sort' == '5')
	ORDER BY (DAT_TARGET IS NULL), DAT_TARGET
#fi
#if ('$sort' == '6')
	ORDER BY (DAT_TIME_STEP IS NULL), DAT_TIME_STEP
#fi
#if ('$sort' == '7')
	ORDER BY (DAT_SENT = '0'), (DAT_SENT/DAT_SIZE)
#fi
#if ('$sort' == '8')
	ORDER BY (DAT_SENT = '0'), (DAT_SENT/DAT_DURATION)
#fi
#if ('$sort' == '9')
	ORDER BY (STA_CODE IS NULL), STA_CODE
#fi
#if ('$sort' == '10')
	ORDER BY (DAT_PRIORITY IS NULL), DAT_PRIORITY
#fi
#if ('$order' == '1')
	ASC
#fi
#if ('$order' == '2')
	DESC
#fi
LIMIT $start,$length
