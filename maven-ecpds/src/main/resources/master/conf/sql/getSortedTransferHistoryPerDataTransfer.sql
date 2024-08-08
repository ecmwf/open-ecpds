##
## References
##
#menu "ECpdsBase"
#name "getSortedTransferHistoryPerDataTransfer"
#group "select"

##
## Variable(s)
##
#prompt "id;What Data Transfer?;%"
#prompt "afterScheduleTime; Privileged user?;%"
#prompt "sort; Column;%"
#prompt "order; Ordering (Descending=2,Ascending=1);%"
#prompt "start; Start column;%"
#prompt "length; Column length;%"

##
## Request(s)
##
SELECT SQL_CALC_FOUND_ROWS TRH.*
FROM
  TRANSFER_HISTORY TRH
#if ('$afterScheduleTime' == 'true')
  ,DATA_TRANSFER DAT
#fi
WHERE
  TRH.DAT_ID = '$id'
#if ('$afterScheduleTime' == 'true')
  AND TRH.DAT_ID = DAT.DAT_ID
  AND TRH_TIME > DAT_SCHEDULED_TIME
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
