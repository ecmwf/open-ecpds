##
## References
##
#menu "ECpdsBase"
#name "deleteProductMetadata"
#group "update"

##
## Variable(s)
##
#prompt "product;Product name;;"
#prompt "type;Product type (empty = all types);;"

##
## Request(s)
##
DELETE FROM PRODUCT_METADATA WHERE PRM_PRODUCT='$product' AND PRM_TYPE='$type'
