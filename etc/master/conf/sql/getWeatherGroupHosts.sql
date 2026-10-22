##
## References
##
#menu "ECpdsBase"
#name "getWeatherGroupHosts"
#group "select"

##
## Variable(s)
##
#prompt "group;Transfer group;%"
#prompt "limit;Maximum rows;%"

##
## Request(s)
##
SELECT HOS_NAME,HOS_HOST,TME_NAME,HOS_NICKNAME,TRG_NAME,HOS_TYPE
FROM HOST FORCE INDEX (TRG_NAME)
WHERE TRG_NAME='$group' AND HOS_ACTIVE=1 AND HOS_TYPE IN ('Dissemination','Acquisition')
ORDER BY HOS_NAME LIMIT $limit
