##
## References
##
#menu "ECpdsBase"
#name "removeIncomingPolicy"
#group "update"

##
## Variable(s)
##
#prompt "id;IncomingPolicy id;%"

##
## Request(s)
##
DELETE FROM POLICY_ASSOCIATION
  WHERE INP_ID = '$id';

DELETE FROM POLICY_USER
  WHERE INP_ID = '$id';

DELETE FROM INCOMING_POLICY
  WHERE INP_ID = '$id';
