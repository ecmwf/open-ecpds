#menu "ECpdsBase"
#name "deleteHostPasswordLegacyAdmission"
#group "update"
#timeout "5"
#prompt "host;Host;;"
#bind "host;LONG"
#prompt "admissionId;Admission id;;"
#bind "admissionId;STRING"
DELETE FROM HOST_PASSWORD_LEGACY_ADMISSION WHERE HOS_NAME=:host AND HLA_ID=:admissionId
