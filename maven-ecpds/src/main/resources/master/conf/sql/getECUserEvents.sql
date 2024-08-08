##
## References
##
#menu "ECpdsBase"
#name "getECUserEvents"
#group "select"

##
## Variable(s)
##
#prompt "user;Which Web user to monitor;%"
#prompt "date;Date;%;java.sql.Date"
#prompt "search;Search string;%"
#prompt "sort; Column;%"
#prompt "order; Ordering (Descending=2,Ascending=1);%"
#prompt "start; Start column;%"
#prompt "length; Column length;%"

##
## Request(s)
##
SELECT SQL_CALC_FOUND_ROWS ECUSER.ECU_NAME, ACT_PLUGIN, ACT_HOST, ACT_AGENT, EVE_TIME, EVE_ACTION, EVE_COMMENT
	FROM ECUSER, ACTIVITY, EVENT
WHERE
    ECUSER.ECU_NAME = ACTIVITY.ECU_NAME
	AND ACTIVITY.ACT_ID = EVENT.ACT_ID
	AND EVE_DATE = '$date'
#if ('$user' != '%')
	AND ECUSER.ECU_NAME LIKE '$user'
#fi
	AND NOT (EVE_ACTION = 'login')
	AND NOT (EVE_ACTION = 'logout')
#if ('$search' != '%')
  AND (ECUSER.ECU_NAME COLLATE latin1_general_cs like '%$search%'
  	OR EVE_ACTION COLLATE latin1_general_cs like '%$search%'
  	OR EVE_COMMENT COLLATE latin1_general_cs like '%$search%')
#fi
#if ('$sort' == '0')
	ORDER BY (EVE_TIME IS NULL), EVE_TIME
#fi
#if ('$sort' == '1')
	ORDER BY (ECUSER.ECU_NAME IS NULL), ECUSER.ECU_NAME
#fi
#if ('$sort' == '2')
	ORDER BY (EVE_ACTION IS NULL), EVE_ACTION
#fi
#if ('$sort' == '3')
	ORDER BY (EVE_COMMENT IS NULL), EVE_COMMENT
#fi
#if ('$order' == '1')
	ASC
#fi
#if ('$order' == '2')
	DESC
#fi
LIMIT $start,$length
