#menu "ECpdsBase"
#name "saveMQTTActivity"
#group "update"
#timeout "5"
#prompt "owner;Hex-encoded UTF-8 Master identity;;"
#prompt "id;Publication identifier;;long"
#prompt "updatedAt;Last change in milliseconds;;long"
#prompt "expiresAt;Expiry in milliseconds;;long"
#prompt "payload;Hex-encoded UTF-8 metadata JSON;;"
INSERT INTO MQTT_ACTIVITY (MQA_OWNER, MQA_ID, MQA_UPDATED_AT, MQA_EXPIRES_AT, MQA_PAYLOAD)
 VALUES (CONVERT(UNHEX('$owner') USING utf8mb4), $id, $updatedAt, $expiresAt, CONVERT(UNHEX('$payload') USING utf8mb4))
 ON DUPLICATE KEY UPDATE MQA_UPDATED_AT=VALUES(MQA_UPDATED_AT), MQA_EXPIRES_AT=VALUES(MQA_EXPIRES_AT), MQA_PAYLOAD=VALUES(MQA_PAYLOAD)
