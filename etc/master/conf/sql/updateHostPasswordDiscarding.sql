#menu "ECpdsBase"
#name "updateHostPasswordDiscarding"
#group "update"
#timeout "5"
#prompt "host;Host;;"
#bind "host;LONG"
#prompt "reference;Reference;;"
#bind "reference;STRING"
UPDATE HOST_PASSWORD_ROTATION SET HPR_STATE='DISCARDING'
WHERE HOS_NAME=:host AND HPR_REF=:reference AND HPR_STATE IN ('STAGED','TESTED')
