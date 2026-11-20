#menu "ECpdsBase"
#name "lockHostPasswordUnresolvedDelivery"
#group "select"
#timeout "5"
#prompt "transfer;Transfer;;"
#bind "transfer;LONG"
SELECT HPD_ID FROM HOST_PASSWORD_DELIVERY
WHERE HPD_TRANSFER=:transfer AND HPD_STATE IN ('DISPATCHING','ACKNOWLEDGED') LIMIT 1 FOR UPDATE
