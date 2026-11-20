#menu "ECpdsBase"
#name "getHostPasswordOperationsGrant"
#group "select"
#timeout "5"
#prompt "user;User;;"
#bind "user;STRING"
#prompt "url;Url;;"
#bind "url;STRING"
SELECT W.CAT_ID FROM WEU_CAT W JOIN CAT_URL C ON C.CAT_ID=W.CAT_ID
WHERE W.WEU_ID=:user AND C.URL_NAME=:url LIMIT 1 FOR UPDATE
