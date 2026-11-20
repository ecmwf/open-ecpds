#menu "ECpdsBase"
#name "getHostPasswordLegacyDispatchBlock"
#group "select"
#timeout "5"
#prompt "host;Host;;"
#bind "host;LONG"
SELECT 1
WHERE EXISTS (SELECT 1 FROM HOST_PASSWORD_ACTIVE WHERE HOS_NAME=:host)
   OR EXISTS (SELECT 1 FROM HOST_PASSWORD_LEGACY_FENCE WHERE HOS_NAME=:host)
