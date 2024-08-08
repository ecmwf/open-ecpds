##
## References
##
#menu "ECpdsBase"
#name "getIncomingUsersForIncomingPolicy"
#group "query"

##
## Variable(s)
##
#prompt "name;Get IncomingUsers for a given IncomingPolicy;%"

##
## Request(s)
##
SELECT INCOMING_USER.*
FROM
  INCOMING_USER,POLICY_USER
WHERE
  POLICY_USER.INP_ID = '$name'
  AND INCOMING_USER.INU_ID = POLICY_USER.INU_ID
