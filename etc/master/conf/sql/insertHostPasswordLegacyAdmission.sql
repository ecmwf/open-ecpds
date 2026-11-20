#menu "ECpdsBase"
#name "insertHostPasswordLegacyAdmission"
#group "update"
#timeout "5"
#prompt "host;Host;;"
#bind "host;LONG"
#prompt "admissionId;Admission id;;"
#bind "admissionId;STRING"
#prompt "mover;Mover;;"
#bind "mover;STRING"
#prompt "operation;Operation;;"
#bind "operation;STRING"
INSERT INTO HOST_PASSWORD_LEGACY_ADMISSION (HOS_NAME,HLA_ID,HLA_MOVER,HLA_OPERATION)
VALUES (:host,:admissionId,:mover,:operation)
