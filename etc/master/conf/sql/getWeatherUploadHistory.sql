##
## References
##
#menu "ECpdsBase"
#name "getWeatherUploadHistory"
#group "select"

##
## Variable(s)
##
#prompt "since;Start timestamp;%"
#prompt "until;End timestamp;%"
#prompt "cursorTime;Upload cursor timestamp;%"
#prompt "cursorId;Upload cursor identifier;%"

##
## Request(s)
##
SELECT UPH_QUEUE_TIME,UPH_ID,UPH_HOST_ADDRESS,UPH_FILE_SIZE
FROM UPLOAD_HISTORY FORCE INDEX (UPLOAD_HISTORY_UPH_QUEUE_TIME_IDX)
WHERE UPH_QUEUE_TIME >= $since AND UPH_QUEUE_TIME <= $until
  AND (UPH_QUEUE_TIME > $cursorTime OR (UPH_QUEUE_TIME=$cursorTime AND UPH_ID > $cursorId))
ORDER BY UPH_QUEUE_TIME,UPH_ID LIMIT 500
