##
## References
##
#menu "ECpdsBase"
#name "getIncomingHistory"
#group "select"

##
## Variable(s)
##
#prompt "user;Which data user to monitor;%"
#prompt "fromDate;From date;%;java.sql.Timestamp"
#prompt "toDate;To date;%;java.sql.Timestamp"
#prompt "search;Search string;%"
#prompt "sort; Column;%"
#prompt "order; Ordering (Descending=2,Ascending=1);%"
#prompt "start; Start column;%"
#prompt "length; Column length;%"

##
## Request(s)
##
SELECT
	SQL_CALC_FOUND_ROWS INH_USER_NAME,DAT_ID,INH_DESTINATION,INH_FILE_NAME,INH_FILE_SIZE,INH_START_TIME,INH_DURATION,INH_SENT,
	INH_PROTOCOL,INH_TRANSFER_SERVER,INH_HOST_ADDRESS,INH_UPLOAD
FROM
	INCOMING_HISTORY
WHERE
#if ('$user' != '%')
	INH_USER_NAME LIKE '$user' AND
#fi
	INH_START_TIME >= $fromDate AND
	INH_START_TIME <= $toDate
#if ('$search' != '%')
  AND INH_FILE_NAME COLLATE latin1_general_cs like '%$search%'
#fi
#if ('$sort' == '0')
	ORDER BY (INH_USER_NAME IS NULL), INH_USER_NAME
#fi
#if ('$sort' == '1')
	ORDER BY (INH_DESTINATION IS NULL), INH_DESTINATION
#fi
#if ('$sort' == '2')
	ORDER BY (INH_TRANSFER_SERVER IS NULL), INH_TRANSFER_SERVER
#fi
#if ('$sort' == '3')
	ORDER BY (INH_PROTOCOL IS NULL), INH_PROTOCOL
#fi
#if ('$sort' == '4')
	ORDER BY (INH_FILE_NAME IS NULL), INH_FILE_NAME
#fi
#if ('$sort' == '5')
	ORDER BY (INH_START_TIME IS NULL), INH_START_TIME
#fi
#if ('$sort' == '8')
	ORDER BY (INH_UPLOAD IS NULL), INH_UPLOAD
#fi
#if ('$order' == '1')
	ASC
#fi
#if ('$order' == '2')
	DESC
#fi
LIMIT $start,$length
