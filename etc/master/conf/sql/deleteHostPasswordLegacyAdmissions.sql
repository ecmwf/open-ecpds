#menu "ECpdsBase"
#name "deleteHostPasswordLegacyAdmissions"
#group "update"
#timeout "5"
#prompt "host;Host;;"
#bind "host;LONG"
DELETE FROM HOST_PASSWORD_LEGACY_ADMISSION WHERE HOS_NAME=:host
