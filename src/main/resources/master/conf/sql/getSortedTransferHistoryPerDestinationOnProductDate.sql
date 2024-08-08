##
## References
##
#menu "ECpdsBase"
#name "getSortedTransferHistoryPerDestinationOnProductDate"
#group "select"

##
## Variable(s)
##
#prompt "destination;Destination name;%"
#prompt "fromDate;From date;%;java.sql.Timestamp"
#prompt "toDate;To date;%;java.sql.Timestamp"
#prompt "sort; Column;%"
#prompt "order; Ordering (Descending=2,Ascending=1);%"
#prompt "start; Start column;%"
#prompt "length; Column length;%"

##
## Request(s)
##
SELECT SQL_CALC_FOUND_ROWS TRH.*
FROM
  TRANSFER_HISTORY TRH, DATA_TRANSFER DAT, DATA_FILE DAF
WHERE
  TRH.DES_NAME='$destination' AND
  TRH.DAT_ID = DAT.DAT_ID AND
  DAT.DAF_ID = DAF.DAF_ID AND
  DAF.DAF_TIME_BASE >= '$fromDate' AND 
  DAF.DAF_TIME_BASE < '$toDate'
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
