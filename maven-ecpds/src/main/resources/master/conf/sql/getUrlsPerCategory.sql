##
## References
##
#menu "ECpdsBase"
#name "getUrlsPerCategory"
#group "select"

##
## Variable(s)
##
#prompt "id;The user id;%"

##
## Request(s)
##
SELECT URL.*
FROM
  URL, CAT_URL
WHERE
  CAT_URL.CAT_ID = '$id' AND
  URL.URL_NAME = CAT_URL.URL_NAME
ORDER by URL_NAME
