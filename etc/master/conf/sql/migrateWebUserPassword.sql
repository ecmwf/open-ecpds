#menu "ECpdsBase"
#name "migrateWebUserPassword"
#group "update"
#timeout "5"
#prompt "id;User;;"
#bind "id;STRING"
#prompt "legacy;Legacy password;;"
#bind "legacy;STRING"
#prompt "hash;Password hash;;"
#bind "hash;STRING"
UPDATE WEB_USER SET WEU_PASSWORD_HASH=:hash, WEU_PASSWORD=NULL
WHERE WEU_ID=:id AND WEU_PASSWORD_HASH IS NULL
  AND BINARY WEU_PASSWORD=BINARY CONVERT(:legacy USING latin1)
