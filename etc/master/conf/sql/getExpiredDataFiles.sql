##
## References
##
#menu "ECpdsBase"
#name "getExpiredDataFiles"
#group "select"

##
## Variable(s)
##
#prompt "limit;Limit;%;int"

##
## Request(s)
##
SELECT * FROM DATA_FILE WHERE DAF_ID IN (SELECT DAF_ID FROM DATA_FILE WHERE DAF_DELETED <> 0 AND DAF_REMOVED = 0)
#if ('$limit' != '-1')
LIMIT $limit
#fi
