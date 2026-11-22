#menu "ECpdsBase"
#name "migrateIncomingUserPassword"
#group "update"
#timeout "5"
#prompt "id;User;;"
#bind "id;STRING"
#prompt "legacy;Legacy password;;"
#bind "legacy;STRING"
#prompt "hash;Password hash;;"
#bind "hash;STRING"
UPDATE INCOMING_USER SET INU_PASSWORD_HASH=:hash,
  INU_PASSWORD=CASE WHEN INU_S3_SECRET IS NULL THEN INU_PASSWORD ELSE NULL END
WHERE INU_ID=:id AND INU_PASSWORD_HASH IS NULL
  AND BINARY INU_PASSWORD=BINARY CONVERT(:legacy USING latin1)
