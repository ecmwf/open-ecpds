##
#menu "ECpdsBase"
#name "getDestinationMetaValuesByFieldId"
#group "query"

##
#prompt "fieldId;Field ID;0;int"

SELECT DMV.*
FROM DESTINATION_META_VALUE DMV
WHERE DMV.DMF_ID='$fieldId'
ORDER BY DMV.DES_NAME, DMV.DMV_POSITION
