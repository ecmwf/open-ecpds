#menu "ECpdsBase"
#name "lockHostPasswordLegacyAdmission"
#group "select"
#timeout "5"
#prompt "host;Host;;"
#bind "host;LONG"
SELECT 1 FROM HOST_PASSWORD_LEGACY_ADMISSION WHERE HOS_NAME=:host LIMIT 1 FOR UPDATE
