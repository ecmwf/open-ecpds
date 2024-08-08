##
## References
##
#menu "ECpdsBase"
#name "getDataTransferCountDestinationAndMetadataValueByMetadataName"
#group "select"

##
## Variable(s)
##
#prompt "name; Metadata name;%"

##
## Request(s)
##
SELECT COUNT(DAT.DAT_ID) AS COUNT, VAL.MEV_VALUE AS VALUE, DAT.DES_NAME AS DESTINATION
FROM 
  DATA_TRANSFER DAT, DATA_FILE DAF, METADATA_VALUE VAL
WHERE 
  DAT.DAF_ID=DAF.DAF_ID AND
  VAL.DAF_ID=DAT.DAF_ID AND
  VAL.MEA_NAME='$name'
GROUP BY VAL.MEV_VALUE, DAT.DES_NAME
