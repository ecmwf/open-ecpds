##
## References
##
#menu "ECpdsBase"
#name "getDestinationsByHostName"
#group "query"

##
## Variable(s)
##
#prompt "host;Which host;%"

##
## Request(s)
##
SELECT D.*
FROM 
  DESTINATION D, ASSOCIATION A
WHERE 
  A.HOS_NAME = '$host'
  AND A.DES_NAME = D.DES_NAME
