#menu "ECpdsBase"
#name "getHostPasswordLegacyRoutes"
#group "select"
#timeout "5"
#prompt "host;Host;;"
#bind "host;LONG"
SELECT HLR_ID,HLR_RELAY,HLR_PROXY,HLR_MOVER,HLR_KEY,HLR_INCARNATION
FROM HOST_PASSWORD_LEGACY_ROUTE WHERE HOS_NAME=:host ORDER BY HLR_ID FOR UPDATE
