##
## References
##
#menu "ECpdsBase"
#name "getIncomingPermissionsForIncomingUser"
#group "query"

##
## Variable(s)
##
#prompt "id;Which id;%"

##
## Request(s)
##
SELECT P.*
FROM
  INCOMING_PERMISSION P, INCOMING_USER U
WHERE
  U.INU_ID = '$id'
  AND U.INU_ID = P.INU_ID
