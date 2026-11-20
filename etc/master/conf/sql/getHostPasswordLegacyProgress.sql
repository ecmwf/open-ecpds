#menu "ECpdsBase"
#name "getHostPasswordLegacyProgress"
#group "select"
#timeout "5"
#prompt "host;Host;;"
#bind "host;LONG"
SELECT COUNT(*),COALESCE(SUM(HLR_SEEN_AT > CURRENT_TIMESTAMP(6)-INTERVAL 30 SECOND),0),
  COALESCE(SUM(HLR_SESSIONS),0)
FROM HOST_PASSWORD_LEGACY_ROUTE WHERE HOS_NAME=:host
