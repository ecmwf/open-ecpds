##
## References
##
#menu "ECpdsBase"
#name "removeTransferGroup"
#group "update"

##
## Variable(s)
##
#prompt "groupName;Transfer group name;%"

##
## Request(s)
##
## Null out TRG_NAME on HOST rows that reference this group.
UPDATE HOST SET TRG_NAME = NULL
  WHERE TRG_NAME = '$groupName';

## Null out TRG_NAME on DESTINATION rows that reference this group.
UPDATE DESTINATION SET TRG_NAME = NULL
  WHERE TRG_NAME = '$groupName';

## Note: DATA_FILE.TRG_NAME is intentionally NOT cleared here.
## The FK constraint (DATA_FILE_ibfk_1) correctly blocks deletion of a
## TransferGroup that still has data files stored on its DataMovers.
## Nulling that field would cause NPEs in the scheduler's replication
## and filtering paths (TransferScheduler lines 1422 and 1546).
