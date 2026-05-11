##
## References
##
#menu "ECpdsBase"
#name "getAuthorisedHostsAndDestinations"
#group "select"

##
## Variable(s)
##
#prompt "user;Which user;%"

##
## Request(s)
##
SELECT
  ASSOCIATION.HOS_NAME,
  DESTINATION.DES_NAME
FROM
  CAT_URL,
  CATEGORY,
  WEU_CAT,
  DESTINATION,
  ASSOCIATION
WHERE
  CAT_URL.CAT_ID = CATEGORY.CAT_ID
  AND WEU_CAT.CAT_ID = CATEGORY.CAT_ID
  AND URL_NAME like concat('/do/transfer/destination/operations/',DESTINATION.DES_NAME,'/')
  AND WEU_ID = '$user'
  AND ASSOCIATION.DES_NAME = DESTINATION.DES_NAME
ORDER BY
  ASSOCIATION.HOS_NAME,
  DESTINATION.DES_NAME
