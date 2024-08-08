##
## References
##
#menu "ECpdsBase"
#name "getDataTransfersByDestinationOnDate"
#group "query"

##
## Variable(s)
##
#prompt "destination;Destination name;%"
#prompt "fromDate;From date;%;java.sql.Timestamp"
#prompt "toDate;To date;%;java.sql.Timestamp"

##
## Request(s)
##
SELECT DAT.*
FROM 
  DATA_TRANSFER DAT, DATA_FILE DAF
WHERE
  DAT.DAF_ID = DAF.DAF_ID AND
  DAT.DES_NAME='$destination' AND
  DAF.DAF_TIME_BASE >= '$fromDate' AND 
  DAF.DAF_TIME_BASE < '$toDate'
