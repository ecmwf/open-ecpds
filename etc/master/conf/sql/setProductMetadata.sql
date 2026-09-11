##
## References
##
#menu "ECpdsBase"
#name "setProductMetadata"
#group "update"

##
## Variable(s)
##
#prompt "product;Product name;;"
#prompt "type;Product type (empty = all types);;"
#prompt "description;Description text;;"
#prompt "tips;Tips text;;"

##
## Request(s)
##
INSERT INTO PRODUCT_METADATA (PRM_PRODUCT, PRM_TYPE, PRM_DESCRIPTION, PRM_TIPS, PRM_UPDATED_AT)
 VALUES ('$product', '$type', '$description', '$tips', NOW())
 ON DUPLICATE KEY UPDATE PRM_DESCRIPTION=VALUES(PRM_DESCRIPTION), PRM_TIPS=VALUES(PRM_TIPS), PRM_UPDATED_AT=NOW()
