##
## References
##
#menu "ECpdsBase"
#name "getIncomingUsersForDestination"
#group "query"

##
## Variable(s)
##
#prompt "name;Get IncomingUsers directly associated to a given Destination;%"

##
## Request(s)
##
SELECT INCOMING_USER.*
FROM
  INCOMING_USER, INCOMING_ASSOCIATION
WHERE
  INCOMING_ASSOCIATION.DES_NAME = '$name'
  AND INCOMING_USER.INU_ID = INCOMING_ASSOCIATION.INU_ID
