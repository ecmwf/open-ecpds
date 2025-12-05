##
## References
##
#menu "ECpdsBase"
#name "getDataTransferCountAndMetaDataByFilter"
#group "select"

##
## Variable(s)
##
#prompt "destination;Destination Name;%"
#prompt "countBy;The attribute of which to show the count;%"
#prompt "target;Diss Stream;%"
#prompt "stream;Data Stream;%"
#prompt "time;Time;%"
#prompt "status; Status;%"
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
#prompt "from;From which date;%;java.sql.Timestamp"
#prompt "to;To what date;%;java.sql.Timestamp"
#prompt "privilegedUser;Can see everything?;%;java.lang.String"
#prompt "scheduledBefore;Count files scheduled before this date;%;java.sql.Timestamp"

##
## Request(s)
##
#if ('$countBy' == 'status')
SELECT COUNT(DAT_ID) AS COUNT, STA_CODE AS NAME
#fi
#if ('$countBy' == 'status2')
SELECT COUNT(DAT_ID) AS COUNT, STA_CODE AS NAME, SUM(DAT_SIZE) AS SIZE
#fi
#if ('$countBy' == 'target')
SELECT COUNT(DAT_ID) AS COUNT, DAF_META_TARGET AS NAME
#fi
#if ('$countBy' == 'target2')
SELECT COUNT(DAT_ID) AS COUNT, DAF_META_TARGET AS NAME, SUM(DAT_SIZE) AS SIZE
#fi
#if ('$countBy' == 'stream')
SELECT COUNT(DAT_ID) AS COUNT, concat(DAF_META_TIME,'-',DAF_META_STREAM) AS NAME
#fi
#if ('$countBy' == 'stream2')
SELECT COUNT(DAT_ID) AS COUNT, concat(DAF_META_TIME,'-',DAF_META_STREAM) AS NAME, SUM(DAT_SIZE) AS SIZE
#fi
FROM
	DATA_TRANSFER DT USE INDEX(destinationFilters)
#if $("$countBy".startsWith("target") || "$countBy".startsWith("stream") || "$target" != "All" || "$stream" != "All" || "$time" != "All" || "$datafile" == "yes")
	, DATA_FILE DF
#fi
WHERE
	DES_NAME ='$destination'
#if ('$from' != '0')
	AND DAT_TIME_BASE >= '$from'
#fi
#if ('$to' != '9223372036854775807')
	AND DAT_TIME_BASE < '$to'
#fi
#if ('$status' != 'All')
	AND STA_CODE = '$status'
#fi
#if $("$countBy".startsWith("target") || "$countBy".startsWith("stream") || "$target" != "All" || "$stream" != "All" || "$time" != "All" || "$datafile" == "yes")
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
#fi
#if ('$target' != 'All')
	AND DAF_META_TARGET = '$target'
#fi
#if ('$stream' != 'All')
	AND concat(DAF_META_TIME,'-',DAF_META_STREAM) = '$stream'
#fi
#if ('$time' != 'All')
	AND DAF_META_TIME = '$time'
#fi
#if ('$countBy' == 'status')
GROUP BY
	STA_CODE
ORDER BY
	STA_CODE;
#fi
#if ('$countBy' == 'status2')
GROUP BY
	STA_CODE
ORDER BY
	STA_CODE;
#fi
#if ('$countBy' == 'stream')
GROUP BY
	DAF_META_TIME,DAF_META_STREAM
ORDER BY
	DAF_META_STREAM,DAF_META_TIME
#fi
#if ('$countBy' == 'stream2')
GROUP BY
	DAF_META_TIME,DAF_META_STREAM
ORDER BY
	DAF_META_STREAM,DAF_META_TIME
#fi
#if ('$countBy' == 'target')
GROUP BY
	DAF_META_TARGET
ORDER BY
	DAF_META_TARGET
#fi
#if ('$countBy' == 'target2')
GROUP BY
	DAF_META_TARGET
ORDER BY
	DAF_META_TARGET
#fi
