##
## References
##
#menu "ECpdsBase"
#name "insertStoragePolicy"
#group "update"

##
## Variable(s)
##
#prompt "name;Name;;"
#prompt "comment;Comment;;"
#prompt "active;Active;;"
#prompt "mode;Mode;;"
#prompt "destinations;Destinations;;"
#prompt "types;Destination types;;"
#prompt "groups;Transfer groups;;"
#prompt "pattern;File pattern;;"
#prompt "window;Requeue window (ms);;"
#prompt "restart;Restart window on requeue;;"
#prompt "dropReplicas;Drop replicas;;"
#prompt "dropDelay;Drop replicas after (ms);;"
#prompt "minimumRetention;Minimum replica retention (ms);;"
#prompt "extraGrace;Extra grace (ms);;"
#prompt "lazyDelay;Lazy replication delay (ms);;"
#prompt "highWatermark;High watermark (%);;"
#prompt "lowWatermark;Low watermark (%);;"
#prompt "user;User;;"
##
## Request(s)
##
INSERT INTO STORAGE_POLICY (SPO_NAME, SPO_COMMENT, SPO_ACTIVE, SPO_MODE, SPO_ORDER, SPO_DESTINATIONS,
 SPO_DESTINATION_TYPES, SPO_TRANSFER_GROUPS, SPO_FILE_PATTERN, SPO_REQUEUE_WINDOW_MS, SPO_RESTART_ON_REQUEUE,
 SPO_DROP_REPLICAS, SPO_DROP_REPLICAS_AFTER_MS, SPO_MIN_RETENTION_MS, SPO_GRACE_MS,
 SPO_LAZY_REPLICATION_MS, SPO_HIGH_WATERMARK, SPO_LOW_WATERMARK, SPO_UPDATED_AT, SPO_UPDATED_BY)
 SELECT '$name', '$comment', $active, '$mode', COALESCE(MAX(SPO_ORDER), 0) + 10, '$destinations', '$types',
 '$groups', '$pattern', $window, $restart, $dropReplicas, $dropDelay, $minimumRetention, $extraGrace,
 $lazyDelay, $highWatermark, $lowWatermark, NOW(), '$user' FROM STORAGE_POLICY
