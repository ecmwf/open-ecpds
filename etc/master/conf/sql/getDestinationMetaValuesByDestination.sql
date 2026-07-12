##
#menu "ECpdsBase"
#name "getDestinationMetaValuesByDestination"
#group "query"

##
#prompt "destinationName;Destination name;*"

SELECT DMV.*
FROM DESTINATION_META_VALUE DMV
WHERE DMV.DES_NAME='$destinationName'
ORDER BY DMV.DMF_ID, DMV.DMV_POSITION
