##
## References
##
#menu "ECpdsBase"
#name "getNextDelayNoticeNumber"
#group "select"

##
## Variable(s)
##
#prompt "year;Year;;"
##
## Request(s)
##
SELECT COALESCE(MAX(CAST(SUBSTRING(DLN_REF, 10) AS UNSIGNED)), 0) + 1 AS NEXT_NUMBER FROM DELAY_NOTICE
 WHERE DLN_REF LIKE 'DLY-$year-%'
