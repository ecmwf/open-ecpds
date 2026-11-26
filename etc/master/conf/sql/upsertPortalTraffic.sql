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
#bind "user;STRING"
#prompt "time;Minute bucket timestamp;"
#bind "time;TIMESTAMP"
#prompt "connections;Connection count;0"
#bind "connections;INTEGER"
#prompt "denials;Denied access count;0"
#bind "denials;LONG"
#prompt "bytesIn;Bytes received;0"
#bind "bytesIn;LONG"
#prompt "bytesOut;Bytes sent;0"
#bind "bytesOut;LONG"
#prompt "durationIn;Duration in ms (in);0"
#bind "durationIn;LONG"
#prompt "durationOut;Duration in ms (out);0"
#bind "durationOut;LONG"

##
## Request(s)
##
INSERT INTO PORTAL_TRAFFIC
  (PTR_USER, PTR_TIME, PTR_CONNECTIONS, PTR_DENIALS, PTR_BYTES_IN, PTR_BYTES_OUT, PTR_DURATION_IN, PTR_DURATION_OUT)
VALUES
  (:user, :time, :connections, :denials, :bytesIn, :bytesOut, :durationIn, :durationOut)
ON DUPLICATE KEY UPDATE
  PTR_CONNECTIONS  = PTR_CONNECTIONS  + :connections,
  PTR_DENIALS      = PTR_DENIALS      + :denials,
  PTR_BYTES_IN     = PTR_BYTES_IN     + :bytesIn,
  PTR_BYTES_OUT    = PTR_BYTES_OUT    + :bytesOut,
  PTR_DURATION_IN  = PTR_DURATION_IN  + :durationIn,
  PTR_DURATION_OUT = PTR_DURATION_OUT + :durationOut
