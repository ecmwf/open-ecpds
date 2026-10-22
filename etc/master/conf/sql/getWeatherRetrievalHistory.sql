##
## References
##
#menu "ECpdsBase"
#name "getWeatherRetrievalHistory"
#group "select"

##
## Variable(s)
##
#prompt "host;Acquisition Host identifier;%"
#prompt "since;Start timestamp;%"
#prompt "until;End timestamp;%"
#prompt "limit;Maximum rows;%"

##
## Request(s)
##
SELECT DAF_ID,DAF_GET_TIME,DAF_SIZE
FROM DATA_FILE FORCE INDEX (DATA_FILE_ibfk_3)
WHERE HOS_NAME_FOR_ACQUISITION='$host' AND DAF_DOWNLOADED<>0
  AND DAF_GET_TIME >= $since AND DAF_GET_TIME <= $until
ORDER BY DAF_GET_TIME DESC,DAF_ID DESC LIMIT $limit
