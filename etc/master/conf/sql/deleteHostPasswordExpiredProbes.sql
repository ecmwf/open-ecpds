#menu "ECpdsBase"
#name "deleteHostPasswordExpiredProbes"
#group "update"
#timeout "5"
#prompt "host;Host;;"
#bind "host;LONG"
DELETE FROM HOST_PASSWORD_PROBE_ATTEMPT
WHERE HOS_NAME=:host AND HPP_AT <= CURRENT_TIMESTAMP(6) - INTERVAL 1 HOUR
