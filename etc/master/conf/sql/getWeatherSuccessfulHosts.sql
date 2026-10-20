##
## References
##
#menu "ECpdsBase"
#name "getWeatherSuccessfulHosts"
#group "select"

##
## Variable(s)
##
#prompt "protocol;ECtrans module;%"
#prompt "since;Start timestamp;%"
#prompt "until;End timestamp;%"
#prompt "limit;Maximum rows;%"

##
## Request(s)
##
SELECT H.HOS_NAME,H.HOS_HOST,H.TME_NAME,H.HOS_NICKNAME,H.TRG_NAME
FROM HOST H JOIN TRANSFER_METHOD M ON M.TME_NAME=H.TME_NAME
WHERE H.HOS_ACTIVE=1 AND H.HOS_TYPE='Dissemination'
  AND ('$protocol'='' OR M.ECM_NAME='$protocol')
  AND EXISTS (
    SELECT 1 FROM TRANSFER_HISTORY T FORCE INDEX (HOS_NAME)
    WHERE T.HOS_NAME=H.HOS_NAME AND T.STA_CODE='DONE'
      AND T.TRH_TIME >= $since AND T.TRH_TIME <= $until
  )
ORDER BY H.HOS_NAME LIMIT $limit
