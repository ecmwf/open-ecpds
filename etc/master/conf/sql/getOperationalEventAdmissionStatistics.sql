#menu "ECpdsBase"
#name "getOperationalEventAdmissionStatistics"
#group "select"
#timeout "5"
#prompt "owner;Hex-encoded UTF-8 Master journal owner;;"
#prompt "limit;Maximum rows to inspect;;"
SELECT COUNT(*) AS PENDING_COUNT, COALESCE(SUM(PAYLOAD_BYTES),0) AS PAYLOAD_BYTES,
 MIN(OEV_CREATED_AT) AS OLDEST_ADMISSION FROM (
 SELECT OCTET_LENGTH(OEV_PAYLOAD) AS PAYLOAD_BYTES, OEV_CREATED_AT
 FROM OPERATIONAL_EVENT WHERE OEV_OWNER=CONVERT(UNHEX('$owner') USING utf8mb4)
 ORDER BY OEV_SEQUENCE LIMIT $limit
 ) AS JOURNAL_SAMPLE
