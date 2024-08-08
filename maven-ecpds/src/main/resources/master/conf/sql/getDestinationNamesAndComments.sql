##
## References
##
#menu "ECpdsBase"
#name "getDestinationNamesAndComments"
#group "select"

##
## Request(s)
##
SELECT DES_NAME, DES_COMMENT
FROM
  DESTINATION
ORDER BY
  DES_NAME
