#menu "ECpdsBase"
#name "getHostPasswordRotationGrant"
#group "select"
#timeout "5"
#prompt "destination;Destination;;"
#bind "destination;STRING"
#prompt "user;User;;"
#bind "user;STRING"
SELECT G.WEU_ID FROM HOST_PASSWORD_ROTATION_GRANT G JOIN WEB_USER U ON U.WEU_ID=G.WEU_ID
WHERE G.DES_NAME=:destination AND G.WEU_ID=:user AND U.WEU_ACTIVE=1 FOR UPDATE
