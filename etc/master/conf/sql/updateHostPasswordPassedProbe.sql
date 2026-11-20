#menu "ECpdsBase"
#name "updateHostPasswordPassedProbe"
#group "update"
#timeout "5"
#prompt "endpoint;Endpoint;;"
#bind "endpoint;STRING"
#prompt "configuration;Configuration;;"
#bind "configuration;STRING"
#prompt "host;Host;;"
#bind "host;LONG"
#prompt "reference;Reference;;"
#bind "reference;STRING"
UPDATE HOST_PASSWORD_ROTATION SET HPR_STATE='TESTED',HPR_TEST_ENDPOINT=:endpoint,
  HPR_TESTED_AT=CURRENT_TIMESTAMP(6),HPR_TEST_HOST=:configuration
WHERE HOS_NAME=:host AND HPR_REF=:reference AND HPR_STATE='STAGED'
