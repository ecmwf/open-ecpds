#menu "ECpdsBase"
#name "insertHostPasswordProbeAttempt"
#group "update"
#timeout "5"
#prompt "host;Host;;"
#bind "host;LONG"
INSERT INTO HOST_PASSWORD_PROBE_ATTEMPT (HOS_NAME) VALUES (:host)
