##
## References
##
#menu "ECpdsBase"
#name "getDataTransfersByStatus"
#group "query"

##
## Variable(s)
##
#prompt "id;Which status;%"
#prompt "search;Search string;%"

##
## Request(s)
##
SELECT DATA_TRANSFER.* 
FROM 
  DATA_TRANSFER
WHERE 
  DAT_TARGET like '%$search%'
  AND STA_CODE = '$id'
