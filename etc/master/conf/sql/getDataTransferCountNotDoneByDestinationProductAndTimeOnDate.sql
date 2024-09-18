##
## References
##
#menu "ECpdsBase"
#name "getDataTransferCountNotDoneByDestinationProductAndTimeOnDate"
#group "count"

##
## Variable(s)
##
#prompt "destination;Destination;%"
#prompt "product;Product name;%"
#prompt "time;Time;%"
#prompt "lastPredicted;Only count transfer predicted before this date;%;java.sql.Timestamp"
#prompt "fromDate;From date;%;java.sql.Timestamp"
#prompt "toDate;To date;%;java.sql.Timestamp"

##
## Request(s)
##
SELECT COUNT(DAT_ID) AS COUNT
FROM 
  DATA_TRANSFER DAT, DATA_FILE DAF, MONITORING_VALUE MV
WHERE 
  STA_CODE!='DONE' AND
  STA_CODE!='HOLD' AND
  DAT.DES_NAME='$destination' AND
  DAT.DAF_ID=DAF.DAF_ID AND
  DAT.MOV_ID=MV.MOV_ID AND
  MV.MOV_PREDICTED_TIME < '$lastPredicted' AND
  DAF_META_STREAM='$product' AND
  DAF_META_TIME='$time' AND
  DAF_TIME_BASE >= '$fromDate' AND
  DAF_TIME_BASE < '$toDate'
