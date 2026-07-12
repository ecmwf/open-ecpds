##
#menu "ECpdsBase"
#name "getAllDestinationMetaFields"
#group "query"

SELECT DMF.*
FROM DESTINATION_META_FIELD DMF
ORDER BY DMF.DMF_POSITION, DMF.DMF_ID
