##
## References
##
#menu "ECpdsBase"
#name "deleteDelayNotice"
#group "update"

##
## Variable(s)
##
#prompt "ref;Reference;;"
##
## Request(s)
##
DELETE FROM DELAY_NOTICE WHERE DLN_REF='$ref'
