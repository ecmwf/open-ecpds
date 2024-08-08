##
## References
##
#menu "ECpdsBase"
#name "getDataFilesByMetaData"
#group "select"

##
## Variable(s)
##
#prompt "name;MetaData Name;%"
#prompt "value;MetaData Value;%"
#prompt "fromDate;From date;%;java.sql.Timestamp"
#prompt "toDate;To date;%;java.sql.Timestamp"
#prompt "sort;Column;%"
#prompt "order;Ordering (Descending=2,Ascending=1);%"
#prompt "start;Start column;%"
#prompt "length;Number of columns;%"

##
## Request(s)
##
SELECT SQL_CALC_FOUND_ROWS DF.DAF_ID, DAF_ORIGINAL, DAF_TIME_BASE, DAF_SIZE, DAF_TIME_STEP
FROM
  DATA_FILE DF, METADATA_VALUE MV
WHERE
  DF.DAF_ID = MV.DAF_ID
  AND MV.MEA_NAME = '$name'
  AND MV.MEV_VALUE = '$value'
  AND DF.DAF_TIME_BASE >= '$fromDate'
  AND DF.DAF_TIME_BASE < '$toDate'
#if ('$sort' == '0')
	ORDER BY DF.DAF_ID
#fi
#if ('$sort' == '1')
	ORDER BY (DAF_TIME_BASE IS NULL), DAF_TIME_BASE
#fi
#if ('$sort' == '2')
	ORDER BY (DAF_SIZE IS NULL), DAF_SIZE
#fi
#if ('$sort' == '3')
	ORDER BY (DAF_TIME_STEP IS NULL), DAF_TIME_STEP
#fi
#if ('$order' == '1')
	ASC
#fi
#if ('$order' == '2')
	DESC
#fi
LIMIT $start,$length
