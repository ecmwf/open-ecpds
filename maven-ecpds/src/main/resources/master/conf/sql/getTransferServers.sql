##
## References
##
#menu "ECpdsBase"
#name "getTransferServers"
#group "query"

##
## Variable(s)
##
#prompt "group;Which Transfer Group to monitor;%"

##
## Request(s)
##  
SELECT TRANSFER_SERVER.*
FROM
  TRANSFER_SERVER
WHERE
  TRG_NAME='$group'
  AND (TRS_ACTIVE<>0)
ORDER BY
  TRS_NAME
