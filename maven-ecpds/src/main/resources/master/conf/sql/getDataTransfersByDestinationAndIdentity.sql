##
## References
##
#menu "ECpdsBase"
#name "getDataTransfersByDestinationAndIdentity"
#group "query"

##
## Variable(s)
##
#prompt "destination;Which destination;%"
#prompt "identity;Which identity descriptor;%"

##
## Request(s)
##
SELECT DAT.* 
FROM 
  DATA_TRANSFER DAT
WHERE 
  DAT.DES_NAME = '$destination' AND
  DAT.DAT_IDENTITY = '$identity'
