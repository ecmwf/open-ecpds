##
## References
##
#menu "ECpdsBase"
#name "getDataTransfersByStatusAndDestinationType"
#group "query"

##
## Variable(s)
##
#prompt "id;Which status;%"
#prompt "search;Search string;%"
#prompt "type;Destination type;%"

##
## Request(s)
##
SELECT DATA_TRANSFER.*
FROM
  DATA_TRANSFER,DESTINATION
WHERE
  DAT_TARGET COLLATE latin1_general_cs like '%$search%'
  AND DATA_TRANSFER.STA_CODE = '$id'
  and DATA_TRANSFER.DES_NAME = DESTINATION.DES_NAME
  and DES_TYPE = '$type'
