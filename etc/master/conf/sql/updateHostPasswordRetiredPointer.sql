#menu "ECpdsBase"
#name "updateHostPasswordRetiredPointer"
#group "update"
#timeout "5"
#prompt "host;Host;;"
#bind "host;LONG"
#prompt "successor;Successor;;"
#bind "successor;STRING"
#prompt "reference;Reference;;"
#bind "reference;STRING"
UPDATE HOST_PASSWORD_ACTIVE SET HPA_PREVIOUS_REF=NULL
WHERE HOS_NAME=:host AND HPA_ACTIVE_REF=:successor AND HPA_PREVIOUS_REF=:reference
