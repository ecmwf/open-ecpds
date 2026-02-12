##
## References
##
#menu "ECpdsBase"
#name "getValidDataTransfersCount"
#group "count"

##
## Variable(s)
##
#prompt "isproxy;Is Proxy;%"
#prompt "datafile;DataFile ID;%"

##
## Request(s)
##
SELECT COUNT(*)
FROM DATA_FILE daf
JOIN DATA_TRANSFER dat
  ON dat.DAF_ID = daf.DAF_ID
WHERE
  daf.DAF_ID = '$datafile'
  AND dat.DAT_DELETED = 0
  AND daf.DAF_FILE_SYSTEM IS NOT NULL
#if ('$isproxy' == 'true')
  AND dat.STA_CODE <> 'DONE'
#fi
