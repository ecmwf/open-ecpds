#menu "ECpdsBase"
#name "updateHostPasswordDeliveryOutcome"
#group "update"
#timeout "5"
#prompt "successful;Successful;;"
#bind "successful;BOOLEAN"
#prompt "receipt;Receipt;;"
#bind "receipt;STRING"
#prompt "state;State;;"
#bind "state;STRING"
#prompt "attemptId;Attempt id;;"
#bind "attemptId;STRING"
UPDATE HOST_PASSWORD_DELIVERY
SET HPD_SUCCEEDED_AT=IF(:successful,COALESCE(HPD_SUCCEEDED_AT,CURRENT_TIMESTAMP(6)),HPD_SUCCEEDED_AT),
  HPD_RECEIPT=:receipt,HPD_STATE=:state WHERE HPD_ID=:attemptId
