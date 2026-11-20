#menu "ECpdsBase"
#name "insertHostPasswordRetirement"
#group "update"
#timeout "5"
#prompt "host;Host;;"
#bind "host;LONG"
#prompt "reference;Reference;;"
#bind "reference;STRING"
#prompt "successor;Successor;;"
#bind "successor;STRING"
INSERT INTO HOST_PASSWORD_RETIREMENT (HOS_NAME,HPT_REFERENCE,HPT_SUCCESSOR,HPT_STATE)
VALUES (:host,:reference,:successor,'DESTROYING') ON DUPLICATE KEY UPDATE HPT_REFERENCE=VALUES(HPT_REFERENCE)
