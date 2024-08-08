##
## References
##
#menu "ECpdsBase"
#name "getChangeLogByKey"
#group "query"

##
## Variable(s)
##
#prompt "keyname;Key Name;%"
#prompt "keyvalue;Key Value;%"

##
## Request(s)
##
SELECT *
from
  CHANGE_LOG
where
  CHL_KEY_NAME = '$keyname'
  AND CHL_KEY_VALUE = '$keyvalue'
order by CHL_TIME DESC
