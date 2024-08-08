##
## References
##
#menu "ECpdsBase"
#name "getStatistics"
#group "select"

##
## Variable(s)
##
#prompt "fromDate;From Time in Milliseconds;;long"
#prompt "toDate;To Time in Milliseconds;;long"
#prompt "group;Which Transfer Group;%"
#prompt "code;Which Status Code;%"
#prompt "type;Which Type of Destination;%"

##
## Request(s)
##
SELECT
	DATE,SUM(SIZE) AS SIZE, SUM(DESTINATION) AS DESTINATION
FROM STATISTICS
WHERE
  	DATE >= '$fromDate'
  	AND DATE < '$toDate'
	AND TRG_NAME LIKE '$group'
	AND STA_CODE LIKE '$code'
	AND DES_TYPE LIKE '$type'
GROUP BY
	DATE
