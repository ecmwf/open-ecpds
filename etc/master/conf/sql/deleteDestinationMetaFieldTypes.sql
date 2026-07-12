##
#menu "ECpdsBase"
#name "deleteDestinationMetaFieldTypes"
#group "update"

##
#prompt "fieldId;Field ID;0;int"

DELETE FROM DESTINATION_META_FIELD_TYPE WHERE DMF_ID='$fieldId'
