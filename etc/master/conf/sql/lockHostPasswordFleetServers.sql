#menu "ECpdsBase"
#name "lockHostPasswordFleetServers"
#group "select"
#timeout "5"
SELECT TRS_NAME FROM TRANSFER_SERVER FOR UPDATE
