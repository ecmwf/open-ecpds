#menu "ECpdsBase"
#name "getHostPasswordLegacyAdmissions"
#group "select"
#timeout "5"
#prompt "host;Host;;"
#bind "host;LONG"
SELECT HLA_ID,HLA_MOVER FROM HOST_PASSWORD_LEGACY_ADMISSION WHERE HOS_NAME=:host FOR UPDATE
