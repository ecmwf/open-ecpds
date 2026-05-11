##
## References
##
#menu "ECpdsBase"
#name "getDestinationNamesByHost"
#group "select"

##
## Request(s)
##
SELECT
  HOS_NAME,
  DES_NAME
FROM
  ASSOCIATION
ORDER BY
  HOS_NAME,
  DES_NAME
