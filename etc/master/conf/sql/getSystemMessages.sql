##
#menu "ECpdsBase"
#name "getSystemMessages"
#group "query"

SELECT *
FROM SYSTEM_MESSAGE
ORDER BY SYM_CREATED_AT DESC
