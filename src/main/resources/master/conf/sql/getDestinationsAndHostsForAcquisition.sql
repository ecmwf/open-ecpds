##
## References
##
#menu "ECpdsBase"
#name "getDestinationsAndHostsForAcquisition"
#group "select"

##
## Variable(s)
##
#prompt "currentTimeMillis;Current Time in Milliseconds;;long"
#prompt "type;Host type;%"
#prompt "limit;Limit;%;int"

##
## Request(s)
##
SELECT A.DES_NAME, HOS_TYPE,HOS_DATA, HOS_DIR,HOS_HOST,HOS_LOGIN,H.HOS_NAME,
	HOS_NETWORK_CODE,HOS_NETWORK_NAME,HOS_NICKNAME,HOS_PASSWD,H.TRG_NAME
FROM
  HOST H, ASSOCIATION A, DESTINATION D, HOST_OUTPUT O
WHERE
  A.HOS_NAME = H.HOS_NAME
  AND A.DES_NAME = D.DES_NAME
  AND H.HOU_ID = O.HOU_ID
  AND HOS_ACTIVE <> 0
  AND DES_ACTIVE <> 0
  AND DES_ACQUISITION <> 0
  AND NOT (D.STA_CODE = 'STOP')
  AND NOT (D.STA_CODE = 'INIT')
  AND HOS_TYPE = '$type'
  AND (HOU_ACQUISITION_TIME IS NULL OR ($currentTimeMillis - HOU_ACQUISITION_TIME) > HOS_ACQUISITION_FREQUENCY)
ORDER BY A.DES_NAME, A.ASO_PRIORITY ASC
LIMIT $limit
