##
## References
##
#menu "ECpdsBase"
#name "getExistingStorageDirectoriesPerProxyHost"
#group "select"

##
## Request(s)
##
SELECT DISTINCT
    DATE(FROM_UNIXTIME(daf.DAF_ARRIVED_TIME / 1000)) AS DAF_ARRIVED_DATE,
    dat.HOS_NAME_PROXY,
    daf.DAF_FILE_SYSTEM
FROM DATA_TRANSFER dat
JOIN DATA_FILE daf
    ON dat.DAF_ID = daf.DAF_ID
WHERE
    dat.HOS_NAME_PROXY IS NOT NULL
    AND dat.DAT_DELETED = 0
    AND daf.DAF_FILE_SYSTEM IS NOT NULL
    AND dat.STA_CODE <> 'DONE'
ORDER BY
    dat.HOS_NAME_PROXY,
    DAF_ARRIVED_DATE,
    daf.DAF_FILE_SYSTEM
