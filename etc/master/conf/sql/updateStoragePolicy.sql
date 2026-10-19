##
## References
##
#menu "ECpdsBase"
#name "updateStoragePolicy"
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
UPDATE STORAGE_POLICY SET SPO_COMMENT='$comment', SPO_ACTIVE=$active, SPO_MODE='$mode',
 SPO_DESTINATIONS='$destinations', SPO_DESTINATION_TYPES='$types', SPO_TRANSFER_GROUPS='$groups',
 SPO_FILE_PATTERN='$pattern', SPO_REQUEUE_WINDOW_MS=$window, SPO_RESTART_ON_REQUEUE=$restart,
 SPO_DROP_REPLICAS=$dropReplicas, SPO_DROP_REPLICAS_AFTER_MS=$dropDelay,
 SPO_MIN_RETENTION_MS=$minimumRetention, SPO_GRACE_MS=$extraGrace, SPO_LAZY_REPLICATION_MS=$lazyDelay,
 SPO_HIGH_WATERMARK=$highWatermark, SPO_LOW_WATERMARK=$lowWatermark,
 SPO_UPDATED_AT=NOW(), SPO_UPDATED_BY='$user'
 WHERE SPO_NAME='$name'
