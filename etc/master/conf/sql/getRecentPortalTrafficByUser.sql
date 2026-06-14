##
## References
##
#menu "ECpdsBase"
#name "getRecentPortalTrafficByUser"
#group "select"

##
## Variable(s)
##
#prompt "userId;Incoming user id;%"
#prompt "hours;Number of hours to look back;24"

##
## Request(s)
##
SELECT
  PTR_TIME        AS TIME,
  PTR_CONNECTIONS AS CONNECTIONS,
  PTR_BYTES_IN    AS BYTES_IN,
  PTR_BYTES_OUT   AS BYTES_OUT,
  PTR_DURATION_IN  AS DURATION_IN,
  PTR_DURATION_OUT AS DURATION_OUT
FROM PORTAL_TRAFFIC
WHERE PTR_USER = '$userId'
  AND PTR_TIME >= DATE_SUB(NOW(), INTERVAL '$hours' HOUR)
ORDER BY PTR_TIME ASC
