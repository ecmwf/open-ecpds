#menu "ECpdsBase"
#name "insertHostPasswordActive"
#group "update"
#timeout "5"
#prompt "reference;Reference;;"
#bind "reference;STRING"
#prompt "host;Host;;"
#bind "host;LONG"
INSERT INTO HOST_PASSWORD_ACTIVE (HPA_ACTIVE_REF,HOS_NAME) VALUES (:reference,:host)
