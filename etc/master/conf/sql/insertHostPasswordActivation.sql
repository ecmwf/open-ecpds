#menu "ECpdsBase"
#name "insertHostPasswordActivation"
#group "update"
#timeout "5"
#prompt "destination;Destination;;"
#bind "destination;STRING"
#prompt "user;User;;"
#bind "user;STRING"
#prompt "host;Host;;"
#bind "host;LONG"
#prompt "reference;Reference;;"
#bind "reference;STRING"
#prompt "ageOffsetSeconds;Age offset seconds;;"
#bind "ageOffsetSeconds;LONG"
INSERT INTO HOST_PASSWORD_ACTIVATION
  (HPA_REF,HOS_NAME,DES_NAME,WEU_ID,HPA_ENDPOINT,HPA_HOST_CONFIG,HPA_RETIRE_GRACE_SECONDS)
SELECT HPR_REF,HOS_NAME,:destination,:user,HPR_TEST_ENDPOINT,HPR_TEST_HOST,HPR_RETIRE_GRACE_SECONDS
FROM HOST_PASSWORD_ROTATION
WHERE HOS_NAME=:host AND HPR_REF=:reference AND HPR_STATE='TESTED'
  AND HPR_TESTED_AT > TIMESTAMPADD(SECOND, :ageOffsetSeconds, CURRENT_TIMESTAMP(6))
  AND HPR_TESTED_AT <= CURRENT_TIMESTAMP(6)
