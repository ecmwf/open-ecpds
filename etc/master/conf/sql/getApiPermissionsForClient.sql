##
#menu "ECpdsBase"
#name "getApiPermissionsForClient"
#group "query"
#prompt "clientId;API client identifier;;"

SELECT APU_ID, APP_PATTERN
FROM API_PERMISSION
WHERE APU_ID='$clientId'
