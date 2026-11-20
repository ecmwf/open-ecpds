#menu "ECpdsBase"
#name "getHostPasswordLegacyReceipts"
#group "select"
#timeout "5"
#prompt "host;Host;;"
#bind "host;LONG"
SELECT HLR_RECEIPT FROM HOST_PASSWORD_LEGACY_ROUTE WHERE HOS_NAME=:host ORDER BY HLR_ID FOR UPDATE
