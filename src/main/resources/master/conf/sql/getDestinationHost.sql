##
## References
##
#menu "ECpdsBase"
#name "getDestinationHost"
#group "query"

##
## Variable(s)
##
#prompt "destination;Which destination to monitor;%"

##
## Request(s)
##
SELECT H.*
FROM
  HOST H, ASSOCIATION A
WHERE
  A.DES_NAME = '$destination'
  AND A.HOS_NAME = H.HOS_NAME
ORDER BY A.ASO_PRIORITY ASC
