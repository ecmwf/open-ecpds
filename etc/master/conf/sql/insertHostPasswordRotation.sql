#menu "ECpdsBase"
#name "insertHostPasswordRotation"
#group "update"
#timeout "5"
#prompt "host;Host;;"
#bind "host;LONG"
#prompt "reference;Reference;;"
#bind "reference;STRING"
INSERT INTO HOST_PASSWORD_ROTATION (HOS_NAME,HPR_REF,HPR_STATE) VALUES (:host,:reference,'STAGING')
