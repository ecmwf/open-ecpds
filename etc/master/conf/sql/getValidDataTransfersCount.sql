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
FROM
  DATA_TRANSFER DAT, DATA_FILE DAF
WHERE
  DAF.DAF_ID = '$datafile'
  AND DAF.DAF_ID = DAT.DAF_ID
  AND NOT DAT_DELETED<>0
#if ('$isproxy' == 'true')
  AND NOT STA_CODE = 'DONE'
#fi
