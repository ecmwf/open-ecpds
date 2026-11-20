#menu "ECpdsBase"
#name "getHostPasswordDelivery"
#group "select"
#timeout "5"
#prompt "attemptId;Attempt id;;"
#bind "attemptId;STRING"
SELECT HOS_NAME,HPD_REFERENCE,HPD_TRANSFER,HPD_MOVER,HPD_KEY,HPD_ROUTE,HPD_CONTEXT
FROM HOST_PASSWORD_DELIVERY WHERE HPD_ID=:attemptId
