##
## References
##
#menu "ECpdsBase"
#name "markAllDataFilesForPurge"
#group "update"

##
## Request(s)
##

UPDATE DATA_FILE SET DAF_DELETED = 1 WHERE DAF_DELETED = 0
