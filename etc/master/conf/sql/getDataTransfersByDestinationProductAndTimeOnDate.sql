##
## References
##
#menu "ECpdsBase"
#name "getDataTransfersByDestinationProductAndTimeOnDate"
#group "query"

##
## Variable(s)
##
#prompt "destination;Destination name;%"
#prompt "stream;Data Stream;%"
#prompt "time;Time base;%"
#prompt "fromDate;From date;%;java.sql.Timestamp"
#prompt "toDate;To date;%;java.sql.Timestamp"

##
## Request(s)
##
SELECT DAT.*
FROM
  DATA_TRANSFER DAT, DATA_FILE DAF
WHERE
  STA_CODE != 'HOLD' AND
  DAT.DES_NAME='$destination' AND
  DAT.DAF_ID=DAF.DAF_ID AND
  DAF.DAF_META_STREAM='$stream' AND
  DAF.DAF_META_TIME='$time' AND
  DAF_TIME_BASE >= '$fromDate' AND
  DAF_TIME_BASE < '$toDate'
ORDER BY
  DAF.DAF_TIME_STEP
