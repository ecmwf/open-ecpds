##
## References
##
#menu "ECpdsBase"
#name "getDataTransfersByDataFile"
#group "query"

##
## Variable(s)
##
#prompt "datafile; DataFile%"
#prompt "includeDeleted;Include the deleted DataTransfers?;%;java.lang.String"

##
## Request(s)
##
SELECT DATA_TRANSFER.*
FROM 
  DATA_TRANSFER
WHERE
  DATA_TRANSFER.DAF_ID= '$datafile'
#if ('$includeDeleted' == 'false')
  AND DAT_DELETED = 0
#fi
