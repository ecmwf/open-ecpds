##
## References
##
#menu "ECpdsBase"
#name "getExistingStorageDirectoriesPerProxyHost"
#group "select"

##
## Request(s)
##
SELECT
	DISTINCT(DATE(FROM_UNIXTIME(DAF_ARRIVED_TIME/1000))) AS DAF_ARRIVED_DATE,
	HOS_NAME_PROXY,
	DAF_FILE_SYSTEM
FROM
	DATA_TRANSFER,
	DATA_FILE
WHERE
	NOT HOS_NAME_PROXY IS NULL
	AND NOT DAT_DELETED
	AND DATA_TRANSFER.DAF_ID = DATA_FILE.DAF_ID
ORDER BY
	HOS_NAME_PROXY,DAF_ARRIVED_DATE,DAF_FILE_SYSTEM