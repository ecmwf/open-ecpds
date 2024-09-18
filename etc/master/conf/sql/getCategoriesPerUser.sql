##
## References
##
#menu "ECpdsBase"
#name "getCategoriesPerUser"
#group "query"

##
## Variable(s)
##
#prompt "id;The user id;%"

##
## Request(s)
##
SELECT CATEGORY.*
FROM 
  CATEGORY, WEU_CAT
WHERE
  WEU_CAT.WEU_ID = '$id'
  AND WEU_CAT.CAT_ID = CATEGORY.CAT_ID
