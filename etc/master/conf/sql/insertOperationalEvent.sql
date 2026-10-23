#menu "ECpdsBase"
#name "insertOperationalEvent"
#group "update"
#timeout "5"
#prompt "owner;Hex-encoded UTF-8 Master journal owner;;"
#prompt "id;Hex-encoded event identifier;;"
#prompt "payload;Hex-encoded UTF-8 event JSON;;"
INSERT INTO OPERATIONAL_EVENT (OEV_OWNER, OEV_ID, OEV_PAYLOAD)
 VALUES (CONVERT(UNHEX('$owner') USING utf8mb4), UNHEX('$id'), CONVERT(UNHEX('$payload') USING utf8mb4))
