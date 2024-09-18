##
## References
##
#menu "ECpdsBase"
#name "getCategoriesPerUrl"
#group "query"

##
## Variable(s)
##
#prompt "id;The url id;%"

##
## Request(s)
##
SELECT CATEGORY.*
FROM
  CATEGORY, CAT_URL
WHERE
  CAT_URL.URL_NAME = '$id'
  AND CAT_URL.CAT_ID = CATEGORY.CAT_ID
ORDER by CAT_NAME
