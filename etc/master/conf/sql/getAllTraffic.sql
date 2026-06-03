##
## References
##
#menu "ECpdsBase"
#name "getAllTraffic"
#group "select"

##
## Request(s)
##
SELECT
  BAN_DATE AS DATE,
  SUM(BAN_BYTES) AS BYTES,
  SUM(BAN_DURATION) AS DURATION,
  SUM(BAN_FILES) AS FILES
FROM BANDWIDTH
GROUP BY DATE
ORDER BY DATE DESC
