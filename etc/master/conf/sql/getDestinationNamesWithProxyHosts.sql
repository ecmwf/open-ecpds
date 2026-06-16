##
## References
##
#menu "ECpdsBase"
#name "getDestinationNamesWithProxyHosts"
#group "select"

##
## Request(s)
##
SELECT DISTINCT a.DES_NAME
FROM ASSOCIATION a
JOIN HOST h ON a.HOS_NAME = h.HOS_NAME
WHERE h.HOS_TYPE = 'Proxy'
AND h.HOS_ACTIVE <> 0
