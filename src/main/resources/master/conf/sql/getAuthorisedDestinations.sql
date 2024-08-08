##
## References
##
#menu "ECpdsBase"
#name "getAuthorisedDestinations"
#group "select"

##
## Variable(s)
##
#prompt "user;Which user;%"

##
## Request(s)
##
SELECT
	DES_NAME
FROM
	CAT_URL,
	CATEGORY,
	WEU_CAT,
	DESTINATION
WHERE
	CAT_URL.CAT_ID = CATEGORY.CAT_ID
	AND WEU_CAT.CAT_ID = CATEGORY.CAT_ID
	AND URL_NAME like  concat('/do/transfer/destination/operations/',DES_NAME,'/')
	AND WEU_ID = '$user'
