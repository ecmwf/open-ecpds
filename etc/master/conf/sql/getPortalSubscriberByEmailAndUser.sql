##
## References
##
#menu "ECpdsBase"
#name "getPortalSubscriberByEmailAndUser"
#group "query"

##
## Variable(s)
##
#prompt "email;Subscriber email;%"
#prompt "inuId;IncomingUser ID;%"

##
## Request(s)
##
SELECT PORTAL_SUBSCRIBER.*
FROM PORTAL_SUBSCRIBER
WHERE PSB_EMAIL = '$email'
AND PSB_INU_ID = '$inuId'
LIMIT 1;
