##
## References
##
#menu "ECpdsBase"
#name "getDestinationsByCountry"
#group "query"

##
## Variable(s)
##
#prompt "name;Get Destinations from a given Country;%"

##
## Request(s)
##
SELECT DESTINATION.*
FROM 
  DESTINATION
WHERE 
  DESTINATION.COU_ISO = '$name'
