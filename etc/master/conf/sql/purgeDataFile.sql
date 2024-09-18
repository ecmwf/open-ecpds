##
## References
##
#menu "ECpdsBase"
#name "purgeDataFile"
#group "update"

##
## Variable(s)
##
#prompt "dataFileId;DataFile id;%"

##
## Request(s)
##
CHUNK 1 UPDATE DATA_FILE SET DAF_DELETED = '1', DAF_REMOVED = '1'
WHERE DAF_ID IN (
	SELECT DAF_ID FROM DATA_FILE
	WHERE DAF_ID = '$dataFileId'
)
