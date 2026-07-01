##
## References
##
#menu "ECpdsBase"
#name "deleteOldMoverAvailabilitySnapshots"
#group "update"

##
## Variable(s)
##
#prompt "retentionHours;Retention in hours;168"

##
## Request(s)
##
DELETE FROM MOVER_AVAILABILITY_SNAPSHOT
WHERE MAS_MINUTE < DATE_SUB(NOW(), INTERVAL '$retentionHours' HOUR)
