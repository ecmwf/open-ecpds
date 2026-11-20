#menu "ECpdsBase"
#name "getHostPasswordActiveMarker"
#group "select"
#timeout "5"
#prompt "host;Host;;"
#bind "host;LONG"
SELECT 1 FROM HOST_PASSWORD_ACTIVE WHERE HOS_NAME=:host
