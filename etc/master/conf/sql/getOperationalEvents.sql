#menu "ECpdsBase"
#name "getOperationalEvents"
#group "select"
#timeout "5"
#prompt "owner;Hex-encoded UTF-8 Master journal owner;;"
#prompt "limit;Maximum number of events;;"
SELECT OEV_ID, OEV_PAYLOAD FROM OPERATIONAL_EVENT
 WHERE OEV_OWNER=CONVERT(UNHEX('$owner') USING utf8mb4) ORDER BY OEV_SEQUENCE LIMIT $limit
