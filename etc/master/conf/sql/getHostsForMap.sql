##
## References
##
#menu "ECpdsBase"
#name "getHostsForMap"
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

##
## Request(s)
##
SELECT
  HOS_NAME,HOS_NICKNAME,HOS_HOST,HOS_TYPE,HOS_ACTIVE,HOS_NETWORK_NAME,TME_NAME,HOS_COMMENT,HOS_AUTOMATIC_LOCATION,HLO_LATITUDE,HLO_LONGITUDE
FROM
  HOST
  LEFT JOIN HOST_LOCATION ON HOST.HLO_ID = HOST_LOCATION.HLO_ID
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
ORDER BY HOS_NAME ASC
