##
## References
##
#menu "ECpdsBase"
#name "getDestinationIncomingPolicy"
#group "query"

##
## Variable(s)
##
#prompt "name;Which destination to monitor;%"

##
## Request(s)
##
SELECT INCOMING_POLICY.*
FROM 
  INCOMING_POLICY, DESTINATION, POLICY_ASSOCIATION
WHERE
  DESTINATION.DES_NAME = '$name'
  AND DESTINATION.DES_NAME = POLICY_ASSOCIATION.DES_NAME
  AND INCOMING_POLICY.INP_ID = POLICY_ASSOCIATION.INP_ID
