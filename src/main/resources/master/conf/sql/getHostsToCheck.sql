##
## References
##
#menu "ECpdsBase"
#name "getHostsToCheck"
#group "query"

##
## Request(s)
##
SELECT HOST.*
FROM 
  HOST
WHERE 
  (HOS_ACTIVE<>0)
  AND (HOS_CHECK<>0)
