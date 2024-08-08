##
## References
##
#menu "ECpdsBase"
#name "getPolicyUserList"
#group "query"

##
## Variable(s)
##
#prompt "id;The user id;%"

##
## Request(s)
##
## DESC to have the alphabetical order reversed to allow giving priority in the DATA
## field to the first policies.
##
SELECT POLICY_USER.*
FROM
  INCOMING_POLICY, POLICY_USER
WHERE
  POLICY_USER.INU_ID='$id' AND
  POLICY_USER.INP_ID=INCOMING_POLICY.INP_ID
ORDER BY INCOMING_POLICY.INP_ID DESC
