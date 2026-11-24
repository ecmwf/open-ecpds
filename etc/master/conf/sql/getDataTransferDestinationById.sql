##
## References
##
#menu "ECpdsBase"
#name "getDataTransferDestinationById"
#group "select"

##
## Variable(s)
##
#prompt "id;Data transfer id;;"
#bind "id;LONG"

##
## Request(s)
##
SELECT DES_NAME FROM DATA_TRANSFER WHERE DAT_ID = :id
