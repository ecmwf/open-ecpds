##
## References
##
#menu "ECpdsBase"
#name "getTransferHistoryPerDataTransfer"
#group "select"

##
## Variable(s)
##
#prompt "id;What Data Transfer?;%"

##
## Request(s)
##
SELECT *
FROM
  TRANSFER_HISTORY
WHERE
  DAT_ID = '$id'
ORDER BY TRH_TIME ASC, TRH_ID ASC
