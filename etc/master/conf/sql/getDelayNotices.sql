##
## References
##
#menu "ECpdsBase"
#name "getDelayNotices"
#group "select"

##
## Variable(s)
##
#prompt "ref;Reference pattern;;"
#prompt "limit;Maximum number of notices;;"
##
## Request(s)
##
SELECT DLN_REF, DLN_STATUS, DLN_CREATED_AT, DLN_CREATED_BY, DLN_UPDATED_AT, DLN_VERSION, DLN_CONTENT
 FROM DELAY_NOTICE WHERE DLN_REF LIKE '$ref' ORDER BY DLN_REF DESC LIMIT $limit
