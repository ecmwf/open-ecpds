##
## References
##
#menu "ECpdsBase"
#name "getLastUpdatedProductStatus"
#group "query"

##
## Request(s)
##
SELECT PRODUCT_STATUS.*
FROM
  PRODUCT_STATUS
ORDER BY
  PRS_LAST_UPDATE ASC
