#menu "ECpdsBase"
#name "getHostPasswordProbeAttempts"
#group "select"
#timeout "5"
#prompt "host;Host;;"
#bind "host;LONG"
SELECT HPP_ID FROM HOST_PASSWORD_PROBE_ATTEMPT WHERE HOS_NAME=:host LIMIT 3 FOR UPDATE
