##
## References
##
#menu "ECpdsBase"
#name "getDestinationsForIncomingUser"
#group "query"

##
## Variable(s)
##
#prompt "id;Which id;%"

##
## Request(s)
##
SELECT D.*
FROM
  DESTINATION D, INCOMING_ASSOCIATION A, INCOMING_USER U
WHERE
  U.INU_ID = '$id'
  AND U.INU_ID = A.INU_ID
  AND A.DES_NAME = D.DES_NAME
