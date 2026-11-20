#menu "ECpdsBase"
#name "lockHostPasswordDeliveryTransfer"
#group "select"
#timeout "5"
#prompt "transfer;Transfer;;"
#bind "transfer;LONG"
SELECT DAT_ID FROM DATA_TRANSFER WHERE DAT_ID=:transfer FOR UPDATE
