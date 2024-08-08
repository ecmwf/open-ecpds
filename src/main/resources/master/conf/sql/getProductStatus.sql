##
## References
##
#menu "ECpdsBase"
#name "getProductStatus"
#group "select"

##
## Variable(s)
##
#prompt "stream;Stream name;%"
#prompt "time;Time value;%"
#prompt "type;Time value;%"
#prompt "step;Step value;%;long"
#prompt "limit;Limit;%;int"

##
## Request(s)
##
SELECT *
FROM
  PRODUCT_STATUS PRS
WHERE
  PRS.PRS_STREAM = '$stream'
  AND PRS.PRS_TIME = '$time'
  AND PRS.PRS_STEP = '$step'
  AND (PRS.PRS_TYPE = '$type' OR PRS.PRS_TYPE IS NULL)
ORDER BY
  PRS_LAST_UPDATE DESC
#if ('$limit' != '-1')
LIMIT $limit
#fi
