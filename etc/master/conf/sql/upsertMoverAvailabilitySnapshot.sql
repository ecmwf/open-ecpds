##
## References
##
#menu "ECpdsBase"
#name "upsertMoverAvailabilitySnapshot"
#group "update"

##
## Variable(s)
##
#prompt "mover;Data mover name;"
#prompt "minute;Minute bucket timestamp;"
#prompt "available;Available flag (1=up, 0=down);0"

##
## Request(s)
##
INSERT INTO MOVER_AVAILABILITY_SNAPSHOT
  (MAS_MOVER, MAS_MINUTE, MAS_AVAILABLE)
VALUES
  ('$mover', '$minute', '$available')
ON DUPLICATE KEY UPDATE
  MAS_AVAILABLE = '$available'
