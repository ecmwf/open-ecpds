##
## References
##
#menu "ECpdsBase"
#name "getDestinationCountsByHost"
#group "select"

##
## Request(s)
##
SELECT
  HOS_NAME,
  COUNT(DES_NAME) AS CNT
FROM
  ASSOCIATION
GROUP BY
  HOS_NAME
