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
SELECT H.HOS_NAME,H.HOS_HOST,H.TME_NAME,H.HOS_NICKNAME,H.TRG_NAME,H.HOS_TYPE
FROM HOST H JOIN TRANSFER_METHOD M ON M.TME_NAME=H.TME_NAME
WHERE H.HOS_ACTIVE=1 AND H.HOS_TYPE IN ('Dissemination','Acquisition')
  AND ('$protocol'='' OR M.ECM_NAME='$protocol')
  AND ((H.HOS_TYPE='Dissemination' AND EXISTS (
    SELECT 1 FROM TRANSFER_HISTORY T FORCE INDEX (HOS_NAME)
    WHERE T.HOS_NAME=H.HOS_NAME AND T.STA_CODE='DONE'
      AND T.TRH_TIME >= $since AND T.TRH_TIME <= $until
  )) OR (H.HOS_TYPE='Acquisition' AND EXISTS (
    SELECT 1 FROM DATA_FILE F FORCE INDEX (DATA_FILE_ibfk_3)
    WHERE F.HOS_NAME_FOR_ACQUISITION=H.HOS_NAME AND F.DAF_DOWNLOADED<>0
      AND F.DAF_GET_TIME >= $since AND F.DAF_GET_TIME <= $until
  )))
ORDER BY H.HOS_NAME LIMIT $limit
