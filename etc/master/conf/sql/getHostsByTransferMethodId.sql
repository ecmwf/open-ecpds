##
## References
##
#menu "ECpdsBase"
#name "getHostsByTransferMethodId"
#group "query"

##
## Variable(s)
##
#prompt "method;Which method;%"

##
## Request(s)
##
SELECT H.*
FROM 
  HOST H
WHERE 
  H.TME_NAME = '$method'
