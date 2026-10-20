##
## References
##
#menu "ECpdsBase"
#name "getWeatherTransferHistory"
#group "select"

##
## Variable(s)
##
#prompt "destination;Destination name;%"
#prompt "since;Start timestamp;%"
#prompt "until;End timestamp;%"
#prompt "limit;Maximum rows;%"

##
## Request(s)
##
SELECT HOS_NAME,TRH_TIME,TRH_ERROR FROM TRANSFER_HISTORY
FORCE INDEX (TRANSFER_HISTORY_DES_NAME_TRH_TIME)
WHERE DES_NAME='$destination' AND TRH_TIME >= $since AND TRH_TIME <= $until
ORDER BY TRH_TIME,TRH_ID LIMIT $limit
