##
## References
##
#menu "ECpdsBase"
#name "getTrafficByDestinationName"
#group "select"

##
## Variable(s)
##
#prompt "destination;Destination name;%"

##
## Request(s)
##
SELECT
  BAN_DATE AS DATE,
  BAN_BYTES AS BYTES,
  BAN_DURATION AS DURATION,
  BAN_FILES AS FILES
from
  BANDWIDTH
where
  DES_NAME = '$destination'
group by DATE
order by DATE desc
