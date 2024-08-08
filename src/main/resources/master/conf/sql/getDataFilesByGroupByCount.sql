##
## References
##
#menu "ECpdsBase"
#name "getDataFilesByGroupByCount"
#group "count"

##
## Variable(s)
##
#prompt "groupBy;Group by;%"

##
## Request(s)
##
SELECT COUNT(*)
FROM
  DATA_FILE
WHERE
  DAF_GROUP_BY = '$groupBy'
  AND (NOT (DAF_DELETED<>0))
  AND (DAF_DOWNLOADED<>0)
