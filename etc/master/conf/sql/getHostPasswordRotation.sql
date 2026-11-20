#menu "ECpdsBase"
#name "getHostPasswordRotation"
#group "select"
#timeout "5"
#prompt "host;Host;;"
#bind "host;LONG"
SELECT HPR_REF,HPR_STATE,HPR_CREATED_AT,HPR_TEST_ENDPOINT,HPR_TESTED_AT,HPR_TEST_HOST,
  HPR_RETIRE_GRACE_SECONDS
FROM HOST_PASSWORD_ROTATION WHERE HOS_NAME=:host
