##
## References
##
#menu "ECpdsBase"
#name "getSortedTransferHistoryPerDestinationOnHistoryDate"
#group "select"

##
## Variable(s)
##
#prompt "destination;Destination name;%"
#prompt "fromDate;From date;%;java.sql.Timestamp"
#prompt "toDate;To date;%;java.sql.Timestamp"
#prompt "afterScheduleTime;After schedule time;%"
#prompt "sort; Column;%"
#prompt "order; Ordering (Descending=2,Ascending=1);%"
#prompt "start; Start column;%"
#prompt "length; Column length;%"

##
## Request(s)
##
SELECT SQL_CALC_FOUND_ROWS TRH.*
FROM
  TRANSFER_HISTORY TRH, DATA_TRANSFER DAT
WHERE
  TRH.DES_NAME='$destination' AND
  TRH.DAT_ID = DAT.DAT_ID AND
  TRH.TRH_TIME >= '$fromDate' AND 
  TRH.TRH_TIME < '$toDate'
#if ('$afterScheduleTime' == 'true')
	AND TRH.TRH_TIME > DAT.DAT_SCHEDULED_TIME
#fi
#if ('$sort' == '0')
	ORDER BY TRH_ERROR
#fi
#if ('$sort' == '1')
	ORDER BY TRH_TIME
#fi
#if ('$sort' == '2')
	ORDER BY TRH.STA_CODE
#fi
#if ('$sort' == '3')
	ORDER BY (TRH.HOS_NAME IS NULL), TRH.HOS_NAME
#fi
#if ('$order' == '1')
	ASC, TRH_ID ASC
#fi
#if ('$order' == '2')
	DESC, TRH_ID DESC
#fi
LIMIT $start,$length
