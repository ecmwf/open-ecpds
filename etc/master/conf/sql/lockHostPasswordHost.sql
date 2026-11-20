#menu "ECpdsBase"
#name "lockHostPasswordHost"
#group "select"
#timeout "5"
#prompt "host;Host;;"
#bind "host;LONG"
SELECT HOS_NAME FROM HOST WHERE HOS_NAME=:host FOR UPDATE
