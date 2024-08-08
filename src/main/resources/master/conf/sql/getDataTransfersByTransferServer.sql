##
## References
##
#menu "ECpdsBase"
#name "getDataTransfersByTransferServer"
#group "query"

##
## Variable(s)
##
#prompt "transferServer;Which transfer server;%"

##
## Request(s)
##
SELECT DAT.* 
FROM
  DATA_TRANSFER DAT
WHERE
  TRS_NAME = '$transferServer'
