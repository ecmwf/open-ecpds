##
## References
##
#menu "ECpdsBase"
#name "deleteOldPortalBytesSnapshots"
#group "update"

##
## Variable(s)
##
#prompt "retentionHours;Number of hours to retain;168"

##
## Request(s)
##
DELETE FROM PORTAL_BYTES_SNAPSHOT
WHERE PBS_MINUTE < DATE_SUB(NOW(), INTERVAL '$retentionHours' HOUR)
