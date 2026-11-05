#menu "ECpdsBase"
#name "discardOperationalEvents"
#group "update"
#timeout "5"
#prompt "owner;Hex-encoded UTF-8 Master journal owner;;"
DELETE FROM OPERATIONAL_EVENT
 WHERE OEV_OWNER=CONVERT(UNHEX('$owner') USING utf8mb4)
