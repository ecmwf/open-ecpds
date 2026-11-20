#menu "ECpdsBase"
#name "updateHostPasswordFirstSuccess"
#group "update"
#timeout "5"
#prompt "reference;Reference;;"
#bind "reference;STRING"
#prompt "host;Host;;"
#bind "host;LONG"
UPDATE HOST_PASSWORD_ACTIVATION
SET HPA_FIRST_SUCCESS_AT=COALESCE(HPA_FIRST_SUCCESS_AT,CURRENT_TIMESTAMP(6))
WHERE HPA_REF=:reference AND HOS_NAME=:host
