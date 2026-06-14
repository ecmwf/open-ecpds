##
## References
##
#menu "ECpdsBase"
#name "upsertPortalTraffic"
#group "update"

##
## Variable(s)
##
#prompt "user;User name;"
#prompt "time;Minute bucket timestamp;"
#prompt "connections;Connection count;0"
#prompt "bytesIn;Bytes received;0"
#prompt "bytesOut;Bytes sent;0"
#prompt "durationIn;Duration in ms (in);0"
#prompt "durationOut;Duration in ms (out);0"

##
## Request(s)
##
INSERT INTO PORTAL_TRAFFIC
  (PTR_USER, PTR_TIME, PTR_CONNECTIONS, PTR_BYTES_IN, PTR_BYTES_OUT, PTR_DURATION_IN, PTR_DURATION_OUT)
VALUES
  ('$user', '$time', '$connections', '$bytesIn', '$bytesOut', '$durationIn', '$durationOut')
ON DUPLICATE KEY UPDATE
  PTR_CONNECTIONS  = PTR_CONNECTIONS  + '$connections',
  PTR_BYTES_IN     = PTR_BYTES_IN     + '$bytesIn',
  PTR_BYTES_OUT    = PTR_BYTES_OUT    + '$bytesOut',
  PTR_DURATION_IN  = PTR_DURATION_IN  + '$durationIn',
  PTR_DURATION_OUT = PTR_DURATION_OUT + '$durationOut'
