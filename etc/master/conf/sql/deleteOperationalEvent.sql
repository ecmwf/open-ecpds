#menu "ECpdsBase"
#name "deleteOperationalEvent"
#group "update"
#timeout "5"
#prompt "owner;Hex-encoded UTF-8 Master journal owner;;"
#prompt "id;Hex-encoded event identifier;;"
DELETE FROM OPERATIONAL_EVENT
 WHERE OEV_OWNER=CONVERT(UNHEX('$owner') USING utf8mb4) AND OEV_ID=UNHEX('$id')
