##
## References
##
#menu "ECpdsBase"
#name "getUsersPerCategory"
#group "query"

##
## Variable(s)
##
#prompt "id;The user id;%"

##
## Request(s)
##
SELECT WEB_USER.*
FROM 
  WEB_USER, WEU_CAT
WHERE 
  WEU_CAT.CAT_ID='$id' AND
  WEU_CAT.WEU_ID=WEB_USER.WEU_ID
