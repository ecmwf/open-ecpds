#menu "ECpdsBase"
#name "lockHostPasswordRetirement"
#group "select"
#timeout "5"
#prompt "host;Host;;"
#bind "host;LONG"
#prompt "reference;Reference;;"
#bind "reference;STRING"
#prompt "successor;Successor;;"
#bind "successor;STRING"
SELECT HPT_STATE FROM HOST_PASSWORD_RETIREMENT
WHERE HOS_NAME=:host AND HPT_REFERENCE=:reference AND HPT_SUCCESSOR=:successor FOR UPDATE
