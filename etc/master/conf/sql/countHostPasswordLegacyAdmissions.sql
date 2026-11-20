#menu "ECpdsBase"
#name "countHostPasswordLegacyAdmissions"
#group "select"
#timeout "5"
#prompt "host;Host;;"
#bind "host;LONG"
SELECT COUNT(*) FROM HOST_PASSWORD_LEGACY_ADMISSION WHERE HOS_NAME=:host
