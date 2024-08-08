##
## References
##
#menu "ECpdsBase"
#name "getDestinationsByUserPolicies"
#group "query"

##
## Variable(s)
##
#prompt "id;Get Destinations for a given user;%"

##
## Request(s)
##
SELECT DESTINATION.*
FROM
  DESTINATION,POLICY_ASSOCIATION,POLICY_USER
WHERE
  DESTINATION.DES_NAME = POLICY_ASSOCIATION.DES_NAME
  AND POLICY_ASSOCIATION.INP_ID = POLICY_USER.INP_ID
  AND POLICY_USER.INU_ID = '$id'
