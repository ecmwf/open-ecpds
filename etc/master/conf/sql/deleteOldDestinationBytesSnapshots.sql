##
## References
##
#menu "ECpdsBase"
#name "deleteOldDestinationBytesSnapshots"
#group "update"

##
## Variable(s)
##
#prompt "retentionHours;Number of hours to retain;168"

##
## Request(s)
##
DELETE FROM DESTINATION_BYTES_SNAPSHOT
WHERE DBS_MINUTE < DATE_SUB(NOW(), INTERVAL '$retentionHours' HOUR)
