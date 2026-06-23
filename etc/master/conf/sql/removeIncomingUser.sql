##
## References
##
#menu "ECpdsBase"
#name "removeIncomingUser"
#group "update"

##
## Variable(s)
##
#prompt "id;IncomingUser id;%"

##
## Request(s)
##
DELETE FROM POLICY_USER
  WHERE INU_ID = '$id';

DELETE FROM INCOMING_PERMISSION
  WHERE INU_ID = '$id';

DELETE FROM INCOMING_ASSOCIATION
  WHERE INU_ID = '$id';
  
DELETE FROM PORTAL_TRAFFIC
  WHERE  PTR_USER = '$id';

DELETE FROM PORTAL_TRAFFIC_DAILY
  WHERE  PTD_USER = '$id';

DELETE FROM PORTAL_BYTES_SNAPSHOT
  WHERE  PBS_USER = '$id';

DELETE FROM INCOMING_USER
  WHERE INU_ID = '$id';
