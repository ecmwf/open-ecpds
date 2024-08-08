##
## References
##
#menu "ECpdsBase"
#name "getIncomingPoliciesForIncomingUser"
#group "query"

##
## Variable(s)
##
#prompt "name;Get IncomingPolicies for a given IncomingUser;%"

##
## Request(s)
##
SELECT INCOMING_POLICY.*
FROM
  INCOMING_POLICY,POLICY_USER
WHERE
  POLICY_USER.INU_ID = '$name'
  AND INCOMING_POLICY.INP_ID = POLICY_USER.INP_ID
