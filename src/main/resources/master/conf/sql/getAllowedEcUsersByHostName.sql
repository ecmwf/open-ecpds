##
## References
##
#menu "ECpdsBase"
#name "getAllowedEcUsersByHostName"
#group "query"

##
## Variable(s)
##
#prompt "host;Which host;%"

##
## Request(s)
##
SELECT U.*
FROM
  ECUSER U, HOS_ECU H
WHERE
  H.HOS_NAME = '$host'
  AND H.ECU_NAME = U.ECU_NAME
