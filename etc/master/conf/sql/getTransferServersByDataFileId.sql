##
## References
##
#menu "ECpdsBase"
#name "getTransferServersByDataFileId"
#group "query"

##
## Variable(s)
##
#prompt "dataFileId;Which DataFile;%"

##
## Request(s)
##
SELECT TRANSFER_SERVER.*
FROM
  TRANSFER_SERVER
WHERE
  (TRS_ACTIVE<>0)
  AND TRG_NAME IN(
    SELECT TRANSFER_SERVER.TRG_NAME
    FROM TRANSFER_SERVER,DATA_TRANSFER,DATA_FILE
    WHERE
      TRANSFER_SERVER.TRS_NAME = DATA_TRANSFER.TRS_NAME_ORIGINAL
      AND DATA_TRANSFER.DAF_ID = DATA_FILE.DAF_ID
      AND DATA_FILE.DAF_ID ='$dataFileId'
)
ORDER BY
  TRS_NAME
