##
## References
##
#menu "ECpdsBase"
#name "getDestinationsForIncomingPolicy"
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
  DESTINATION D, POLICY_ASSOCIATION A, INCOMING_POLICY P
WHERE
  P.INP_ID = '$id'
  AND P.INP_ID = A.INP_ID
  AND A.DES_NAME = D.DES_NAME
