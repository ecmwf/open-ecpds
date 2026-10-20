##
## References
##
#menu "ECpdsBase"
#name "getWeatherApprovedHosts"
#group "select"

##
## Variable(s)
##
#prompt "hosts;Quoted Host identifiers;%"

##
## Request(s)
##
SELECT HOS_NAME,HOS_HOST,TME_NAME,HOS_NICKNAME,TRG_NAME
FROM HOST
WHERE HOS_ACTIVE=1 AND HOS_TYPE='Dissemination' AND HOS_NAME IN ($hosts)
