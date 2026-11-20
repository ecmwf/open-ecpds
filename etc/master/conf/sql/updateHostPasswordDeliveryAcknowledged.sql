#menu "ECpdsBase"
#name "updateHostPasswordDeliveryAcknowledged"
#group "update"
#timeout "5"
#prompt "attemptId;Attempt id;;"
#bind "attemptId;STRING"
UPDATE HOST_PASSWORD_DELIVERY SET HPD_STATE='ACKNOWLEDGED'
WHERE HPD_ID=:attemptId AND HPD_STATE='DISPATCHING'
