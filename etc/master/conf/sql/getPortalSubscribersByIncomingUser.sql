##
## References
##
#menu "ECpdsBase"
#name "getPortalSubscribersByIncomingUser"
#group "query"

##
## Variable(s)
##
#prompt "inuId;Get PortalSubscribers for a given IncomingUser;%"

##
## Request(s)
##
SELECT PORTAL_SUBSCRIBER.*
FROM PORTAL_SUBSCRIBER
WHERE PSB_INU_ID = '$inuId'
ORDER BY PSB_CREATED_TIME DESC;
