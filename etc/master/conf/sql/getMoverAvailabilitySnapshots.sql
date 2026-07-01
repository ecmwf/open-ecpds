##
## References
##
#menu "ECpdsBase"
#name "getMoverAvailabilitySnapshots"
#group "select"

##
## Variable(s)
##
#prompt "mover;Data mover name;"
#prompt "retentionHours;Hours to look back;168"

##
## Request(s)
##
SELECT
  MAS_MINUTE    AS MAS_MINUTE,
  MAS_AVAILABLE AS MAS_AVAILABLE
FROM MOVER_AVAILABILITY_SNAPSHOT
WHERE MAS_MOVER  = '$mover'
  AND MAS_MINUTE >= DATE_SUB(NOW(), INTERVAL '$retentionHours' HOUR)
ORDER BY MAS_MINUTE ASC
