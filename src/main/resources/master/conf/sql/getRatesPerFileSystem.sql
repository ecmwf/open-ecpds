##
## References
##
#menu "ECpdsBase"
#name "getRatesPerFileSystem"
#group "select"

##
## Variable(s)
##
#prompt "fromDate;From date;%;java.sql.Timestamp"
#prompt "toDate;To date;%;java.sql.Timestamp"
#prompt "transferServer;Which transfer server;%"
#prompt "caller;Which caller;%"
#prompt "sourceHost;Which source host;%"

##
## Request(s)
##
SELECT
  DATE_FORMAT(FROM_UNIXTIME(DAF_GET_TIME/1000),'%Y-%m-%d') AS DATE,
  TRG_NAME,
  DAF_GET_HOST,
  DAF_FILE_SYSTEM,
  COUNT(*) AS COUNT,
  SUM(DAF_SIZE) AS SIZE,
  SUM(DAF_GET_DURATION) AS DURATION
FROM
  DATA_FILE
WHERE
  DAF_GET_HOST = '$transferServer'
#if ('$caller' != '%')
  AND DAF_CALLER LIKE '$caller'
#fi
#if ('$sourceHost' != '%')
  AND DAF_ECAUTH_HOST LIKE '$sourceHost'
#fi
  AND DAF_GET_TIME  >= $fromDate
  AND DAF_GET_TIME  <= $toDate
  AND TIME(FROM_UNIXTIME(DAF_GET_TIME/1000)) BETWEEN TIME(FROM_UNIXTIME($fromDate/1000)) AND TIME(FROM_UNIXTIME($toDate/1000))
GROUP BY
  DATE,TRG_NAME,DAF_FILE_SYSTEM
ORDER BY
  TRG_NAME,DAF_FILE_SYSTEM,DATE DESC
