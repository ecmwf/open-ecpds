##
## References
##
#menu "ECpdsBase"
#name "getTransferMethodsByEcTransModuleName"
#group "query"

##
## Variable(s)
##
#prompt "module;What Module?;%"

##
## Request(s)
##
SELECT TRANSFER_METHOD.*
FROM 
  TRANSFER_METHOD
WHERE 
  ECM_NAME='$module'
