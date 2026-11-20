#menu "ECpdsBase"
#name "lockHostPasswordPointers"
#group "select"
#timeout "5"
#prompt "host;Host;;"
#bind "host;LONG"
SELECT HPA_ACTIVE_REF,HPA_PREVIOUS_REF FROM HOST_PASSWORD_ACTIVE WHERE HOS_NAME=:host FOR UPDATE
