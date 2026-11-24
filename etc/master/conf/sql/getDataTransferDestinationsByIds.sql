#menu "ECpdsBase"
#name "getDataTransferDestinationsByIds"
#group "select"
#prompt "ids;Data transfer ids;;"
#bind "ids;STRING"
SELECT transfer.DAT_ID, transfer.DES_NAME
FROM JSON_TABLE(:ids, '$[*]' COLUMNS (transfer_id BIGINT PATH '$')) AS requested
JOIN DATA_TRANSFER transfer ON transfer.DAT_ID = requested.transfer_id
