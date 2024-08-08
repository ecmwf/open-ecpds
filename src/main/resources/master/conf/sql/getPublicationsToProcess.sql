##
## References
##
#menu "ECpdsBase"
#name "getPublicationsToProcess"
#group "select"

##
## Variable(s)
##
#prompt "currentTimeMillis;Current Time in Milliseconds;;long"
#prompt "limit;Limit;%;int"

##
## Request(s)
##
SELECT
  PUB_ID, DAT_ID, PUB_SCHEDULED_TIME, PUB_PROCESSED_TIME, PUB_DONE, PUB_OPTIONS
FROM
  PUBLICATION
WHERE
  NOT PUB_DONE<>0
  AND (PUB_PROCESSED_TIME IS NULL OR ($currentTimeMillis - PUB_PROCESSED_TIME) > 180000)
ORDER BY
  PUB_SCHEDULED_TIME ASC, DAT_ID ASC
LIMIT $limit
