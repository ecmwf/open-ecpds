##
## References
##
#menu "ECpdsBase"
#name "getDestinationEcuser"
#group "query"

##
## Variable(s)
##
#prompt "name;Which destination to monitor;%"

##
## Request(s)
##
SELECT ECUSER.*
FROM 
  ECUSER, DESTINATION, DES_ECU
WHERE
  DESTINATION.DES_NAME = '$name'
  AND DESTINATION.DES_NAME = DES_ECU.DES_NAME
  AND DES_ECU.ECU_NAME = ECUSER.ECU_NAME
