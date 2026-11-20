#menu "ECpdsBase"
#name "updateHostPasswordProbeReset"
#group "update"
#timeout "5"
#prompt "host;Host;;"
#bind "host;LONG"
#prompt "reference;Reference;;"
#bind "reference;STRING"
UPDATE HOST_PASSWORD_ROTATION SET HPR_STATE='STAGED',HPR_TEST_ENDPOINT=NULL,
  HPR_TESTED_AT=NULL,HPR_TEST_HOST=NULL WHERE HOS_NAME=:host AND HPR_REF=:reference
