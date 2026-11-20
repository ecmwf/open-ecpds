#menu "ECpdsBase"
#name "insertHostPasswordLegacyFence"
#group "update"
#timeout "5"
#prompt "host;Host;;"
#bind "host;LONG"
INSERT INTO HOST_PASSWORD_LEGACY_FENCE (HOS_NAME) VALUES (:host)
ON DUPLICATE KEY UPDATE HOS_NAME=VALUES(HOS_NAME)
