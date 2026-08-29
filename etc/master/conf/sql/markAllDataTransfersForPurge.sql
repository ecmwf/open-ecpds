##
## References
##
#menu "ECpdsBase"
#name "markAllDataTransfersForPurge"
#group "update"

##
## Request(s)
##

UPDATE DATA_TRANSFER
SET DAT_DELETED = 1,
    DAT_EXPIRY_TIME = DAT_SCHEDULED_TIME + 1,
    DAT_QUEUE_TIME = DAT_QUEUE_TIME - (8 * 24 * 60 * 60 * 1000)
WHERE DAT_DELETED = 0
