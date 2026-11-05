#menu "ECpdsBase"
#name "getMQTTActivity"
#group "select"
#timeout "5"
#prompt "owner;Hex-encoded UTF-8 Master identity;;"
#prompt "limit;Maximum retained publications;;"
SELECT MQA_PAYLOAD FROM MQTT_ACTIVITY
 WHERE MQA_OWNER=CONVERT(UNHEX('$owner') USING utf8mb4)
 ORDER BY MQA_UPDATED_AT DESC, MQA_ID DESC LIMIT $limit
