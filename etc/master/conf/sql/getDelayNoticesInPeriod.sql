##
## References
##
#menu "ECpdsBase"
#name "getDelayNoticesInPeriod"
#group "select"

##
## Variable(s)
##
#prompt "fromDate;Start date (inclusive);;"
#prompt "toDate;End date (inclusive);;"
##
## Request(s)
##
SELECT DLN_REF, DLN_STATUS, DLN_CREATED_AT, DLN_CREATED_BY, DLN_UPDATED_AT, DLN_VERSION, DLN_CONTENT
 FROM DELAY_NOTICE
 WHERE DLN_CREATED_AT >= '$fromDate' AND DLN_CREATED_AT < DATE_ADD('$toDate', INTERVAL 1 DAY)
 ORDER BY DLN_CREATED_AT DESC, DLN_REF DESC
