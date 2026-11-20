#menu "ECpdsBase"
#name "lockHostPasswordDeliveryState"
#group "select"
#timeout "5"
#prompt "attemptId;Attempt id;;"
#bind "attemptId;STRING"
SELECT HPD_STATE FROM HOST_PASSWORD_DELIVERY WHERE HPD_ID=:attemptId FOR UPDATE
