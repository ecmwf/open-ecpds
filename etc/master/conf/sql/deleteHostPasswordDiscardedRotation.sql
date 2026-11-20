#menu "ECpdsBase"
#name "deleteHostPasswordDiscardedRotation"
#group "update"
#timeout "5"
#prompt "host;Host;;"
#bind "host;LONG"
#prompt "reference;Reference;;"
#bind "reference;STRING"
DELETE FROM HOST_PASSWORD_ROTATION WHERE HOS_NAME=:host AND HPR_REF=:reference AND HPR_STATE='DISCARDING'
