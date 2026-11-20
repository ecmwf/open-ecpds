#menu "ECpdsBase"
#name "lockHostPasswordProbeCandidate"
#group "select"
#timeout "5"
#prompt "host;Host;;"
#bind "host;LONG"
#prompt "reference;Reference;;"
#bind "reference;STRING"
SELECT HPR_REF FROM HOST_PASSWORD_ROTATION
WHERE HOS_NAME=:host AND HPR_REF=:reference AND HPR_STATE IN ('STAGED','TESTED') FOR UPDATE
