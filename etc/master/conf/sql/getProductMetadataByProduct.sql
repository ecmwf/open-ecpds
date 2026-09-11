##
## References
##
#menu "ECpdsBase"
#name "getProductMetadataByProduct"
#group "select"

##
## Variable(s)
##
#prompt "product;Product name;;"

##
## Request(s)
##
SELECT PRM_PRODUCT, PRM_TYPE, PRM_DESCRIPTION, PRM_TIPS, PRM_GROUP_TIMES FROM PRODUCT_METADATA
 WHERE PRM_PRODUCT='$product'
 ORDER BY PRM_TYPE
