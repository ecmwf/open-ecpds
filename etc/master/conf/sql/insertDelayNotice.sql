##
## References
##
#menu "ECpdsBase"
#name "insertDelayNotice"
#group "update"

##
## Variable(s)
##
#prompt "ref;Reference;;"
#prompt "status;Status;;"
#prompt "createdBy;User;;"
#prompt "content;JSON content;;"
##
## Request(s)
##
INSERT INTO DELAY_NOTICE (DLN_REF, DLN_STATUS, DLN_CREATED_AT, DLN_CREATED_BY, DLN_UPDATED_AT, DLN_VERSION, DLN_CONTENT)
 VALUES ('$ref', '$status', NOW(), '$createdBy', NOW(), 1, '$content')
