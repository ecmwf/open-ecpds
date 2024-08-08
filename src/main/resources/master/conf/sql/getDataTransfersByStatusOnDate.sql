##
## References
##
#menu "ECpdsBase"
#name "getDataTransfersByStatusOnDate"
#group "select"

##
## Variable(s)
##
#prompt "id;Which status;%"
#prompt "search;Search string;%"
#prompt "fromDate;From date;%;java.sql.Timestamp"
#prompt "toDate;To date;%;java.sql.Timestamp"

##
## Request(s)
##
SELECT DAT_ID, DAF_ID, DES_NAME, HOS_NAME, TRS_NAME, DAT_SIZE, DAT_TIME_STEP, DAT_TARGET, STA_CODE,
  DAT_USER_STATUS, DAT_SENT, DAT_DURATION, DAT_DELETED, DAT_PRIORITY, DAT_SCHEDULED_TIME, DAT_RETRY_TIME, DAT_QUEUE_TIME
FROM
  DATA_TRANSFER
WHERE
  DAT_TIME_BASE >= '$fromDate'
  AND DAT_TIME_BASE < '$toDate'
#if ('$search' != '%')
  AND DAT_TARGET COLLATE latin1_general_cs like '%$search%'
#fi
#if ('$id' != 'All')
  AND STA_CODE = '$id'
#fi
ORDER BY
  DAT_SCHEDULED_TIME, DAT_PRIORITY
