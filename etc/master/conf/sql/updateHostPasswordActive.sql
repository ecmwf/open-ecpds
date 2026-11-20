#menu "ECpdsBase"
#name "updateHostPasswordActive"
#group "update"
#timeout "5"
#prompt "reference;Reference;;"
#bind "reference;STRING"
#prompt "host;Host;;"
#bind "host;LONG"
#prompt "activeReference;Active reference;;"
#bind "activeReference;STRING"
UPDATE HOST_PASSWORD_ACTIVE SET HPA_PREVIOUS_REF=HPA_ACTIVE_REF,HPA_ACTIVE_REF=:reference
WHERE HOS_NAME=:host AND HPA_ACTIVE_REF=:activeReference AND HPA_PREVIOUS_REF IS NULL
