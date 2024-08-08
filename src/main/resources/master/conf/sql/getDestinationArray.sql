##
## References
##
#menu "ECpdsBase"
#name "getDestinationArray"
#group "query"

##
## Variable(s)
##
#prompt "monitored;Select monitored Destinations;%"

##
## Request(s)
##
SELECT DESTINATION.*
FROM
  DESTINATION
WHERE
#if ('$monitored' == 'true')
  DES_MONITOR <> 0
#fi
#if ('$monitored' == 'false')
  DES_MONITOR = 0
#fi
ORDER BY
  DES_NAME
