#menu "ECpdsBase"
#name "getHostPasswordLegacyDispatchBlocks"
#group "select"
#timeout "5"
#prompt "hosts;Hosts;;"
#bind "hosts;STRING"
SELECT requested.host_id
FROM JSON_TABLE(:hosts, '$[*]' COLUMNS (host_id BIGINT PATH '$')) AS requested
LEFT JOIN HOST_PASSWORD_ACTIVE active ON active.HOS_NAME = requested.host_id
LEFT JOIN HOST_PASSWORD_LEGACY_FENCE fence ON fence.HOS_NAME = requested.host_id
WHERE active.HOS_NAME IS NOT NULL OR fence.HOS_NAME IS NOT NULL
