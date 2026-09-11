##
## References
##
#menu "ECpdsBase"
#name "getAllProductMetadata"
#group "select"

##
## Variable(s)
##

##
## Request(s)
##
SELECT PRM_PRODUCT, PRM_TYPE, PRM_DESCRIPTION, PRM_TIPS FROM PRODUCT_METADATA
 ORDER BY PRM_PRODUCT, PRM_TYPE
