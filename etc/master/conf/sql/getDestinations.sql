##
## References
##
#menu "ECpdsBase"
#name "getDestinations"
#group "query"

##
## Variable(s)
##
#prompt "name;Destination name;%"

##
## Request(s)
##
SELECT DESTINATION.*
FROM 
  DESTINATION
WHERE
  DES_NAME = '$name'
  OR DES_NAME IN (SELECT DISTINCT(DES_NAME) FROM ALIAS WHERE ALI_DES_NAME = '$name')
