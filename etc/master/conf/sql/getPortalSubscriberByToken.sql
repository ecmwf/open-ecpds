##
## References
##
#menu "ECpdsBase"
#name "getPortalSubscriberByToken"
#group "query"

##
## Variable(s)
##
#prompt "token;Find PortalSubscriber by verify token;%"

##
## Request(s)
##
SELECT PORTAL_SUBSCRIBER.*
FROM PORTAL_SUBSCRIBER
WHERE PSB_VERIFY_TOKEN = '$token'
LIMIT 1;
