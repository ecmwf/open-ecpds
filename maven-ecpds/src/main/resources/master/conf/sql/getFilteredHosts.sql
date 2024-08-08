##
## References
##
#menu "ECpdsBase"
#name "getFilteredHosts"
#group "select"

##
## Variable(s)
##
#prompt "label;Label;%"
#prompt "filter;Filter;%"
#prompt "network;Network Code;%"
#prompt "type;Type;%"
#prompt "id;Id;%"
#prompt "login;Login;%"
#prompt "nickname;Nickname;%"
#prompt "comment;Comment;%"
#prompt "options;Options;%"
#prompt "dir;Dir;%"
#prompt "hostname;Hostname;%"
#prompt "enabled;Enabled;%"
#prompt "method;TransferMethod;%"
#prompt "email;UserEmail;%"
#prompt "password;Password;%"
#prompt "sort;Column;%"
#prompt "order;Ordering (Descending=2,Ascending=1);%"
#prompt "start;Start column;%"
#prompt "length;Column length;%"

##
## Request(s)
##
SELECT
  SQL_CALC_FOUND_ROWS HOS_HOST,HOS_NAME,HOS_ACTIVE,TME_NAME,TRG_NAME,HOS_NETWORK_NAME,HOS_NICKNAME,HOS_TYPE
FROM
  HOST
WHERE
	1 = 1
#if ('$label' != 'All')
	AND HOS_NETWORK_CODE = '$label'
#fi
#if ('$filter' != 'All')
	AND HOS_FILTER_NAME = '$filter'
#fi
#if ('$network' != 'All')
	AND TRG_NAME = '$network'
#fi
#if ('$type' != 'All')
	AND HOS_TYPE = '$type'
#fi
	$id
	$login
	$nickname
	$comment
	$options
	$dir
	$hostname
	$enabled
	$method
	$email
	$password
#if ('$sort' == '0')
ORDER BY (HOS_NICKNAME IS NULL), HOS_NICKNAME
#fi
#if ('$sort' == '1')
ORDER BY (HOS_HOST IS NULL), HOS_HOST
#fi
#if ('$sort' == '3')
ORDER BY (HOS_TYPE IS NULL), HOS_TYPE
#fi
#if ('$sort' == '4')
ORDER BY (TME_NAME IS NULL), TME_NAME
#fi
#if ('$sort' == '5')
ORDER BY (TRG_NAME IS NULL), TRG_NAME
#fi
#if ('$sort' == '6')
ORDER BY (HOS_NETWORK_NAME IS NULL), HOS_NETWORK_NAME
#fi
#if ('$sort' == '7')
ORDER BY (HOS_ACTIVE IS NULL), HOS_ACTIVE
#fi
#if ('$order' == '1')
ASC
#fi
#if ('$order' == '2')
DESC
#fi
#if ('$start' != '-1')
LIMIT $start,$length
#fi
