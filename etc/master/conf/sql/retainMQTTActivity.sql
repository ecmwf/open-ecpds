#menu "ECpdsBase"
#name "retainMQTTActivity"
#group "update"
#timeout "5"
#prompt "owner;Hex-encoded UTF-8 Master identity;;"
#prompt "cutoff;Oldest retained timestamp in milliseconds;;long"
#prompt "ids;Comma-separated numeric publication identifiers;;"
#prompt "empty;Empty snapshot;;int"
DELETE FROM MQTT_ACTIVITY
 WHERE MQA_OWNER=CONVERT(UNHEX('$owner') USING utf8mb4)
 AND (MQA_UPDATED_AT < $cutoff OR $empty=1 OR MQA_ID NOT IN ($ids))
