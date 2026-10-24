#menu "ECpdsBase"
#name "getOperationalEventStatistics"
#group "select"
#timeout "5"
#prompt "owner;Hex-encoded UTF-8 Master journal owner;;"
SELECT COUNT(*) AS PENDING_COUNT, MIN(OEV_CREATED_AT) AS OLDEST_ADMISSION
 FROM OPERATIONAL_EVENT WHERE OEV_OWNER=CONVERT(UNHEX('$owner') USING utf8mb4)
