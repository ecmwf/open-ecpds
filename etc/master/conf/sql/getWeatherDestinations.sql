##
## References
##
#menu "ECpdsBase"
#name "getWeatherDestinations"
#group "select"

##
## Variable(s)
##
#prompt "hosts;Quoted Host identifiers;%"
#prompt "cursor;Destination cursor;%"

##
## Request(s)
##
SELECT DISTINCT DES_NAME FROM ASSOCIATION FORCE INDEX (HOS_NAME)
WHERE HOS_NAME IN ($hosts) AND DES_NAME > '$cursor'
ORDER BY DES_NAME LIMIT 101
