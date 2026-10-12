##
## References
##
#menu "ECpdsBase"
#name "updateDelayNotice"
#group "update"

##
## Variable(s)
##
#prompt "ref;Reference;;"
#prompt "version;Expected version;;"
#prompt "status;Status;;"
#prompt "content;JSON content;;"
##
## Request(s)
##
UPDATE DELAY_NOTICE SET DLN_STATUS='$status', DLN_CONTENT='$content', DLN_UPDATED_AT=NOW(), DLN_VERSION=DLN_VERSION+1
 WHERE DLN_REF='$ref' AND DLN_VERSION=$version
