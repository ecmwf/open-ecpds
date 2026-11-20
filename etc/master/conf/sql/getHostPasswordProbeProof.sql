#menu "ECpdsBase"
#name "getHostPasswordProbeProof"
#group "select"
#timeout "5"
#prompt "host;Host;;"
#bind "host;LONG"
#prompt "reference;Reference;;"
#bind "reference;STRING"
#prompt "configuration;Configuration;;"
#bind "configuration;STRING"
#prompt "endpoint;Endpoint;;"
#bind "endpoint;STRING"
#prompt "ageOffsetSeconds;Age offset seconds;;"
#bind "ageOffsetSeconds;LONG"
SELECT HPR_REF FROM HOST_PASSWORD_ROTATION
WHERE HOS_NAME=:host AND HPR_REF=:reference AND HPR_STATE='TESTED'
  AND HPR_TEST_HOST=:configuration AND HPR_TEST_ENDPOINT=:endpoint
  AND HPR_TESTED_AT > TIMESTAMPADD(SECOND, :ageOffsetSeconds, CURRENT_TIMESTAMP(6))
  AND HPR_TESTED_AT <= CURRENT_TIMESTAMP(6)
FOR UPDATE
