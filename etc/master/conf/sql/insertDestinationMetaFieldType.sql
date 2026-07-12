##
#menu "ECpdsBase"
#name "insertDestinationMetaFieldType"
#group "update"

##
#prompt "fieldId;Field ID;0;int"
#prompt "desType;Destination Type;0;int"

INSERT IGNORE INTO DESTINATION_META_FIELD_TYPE (DMF_ID, DES_TYPE) VALUES ('$fieldId','$desType')
