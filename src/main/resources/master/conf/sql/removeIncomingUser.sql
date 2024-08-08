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

DELETE FROM INCOMING_USER
  WHERE INU_ID = '$id';
