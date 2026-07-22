##
## References
##
#menu "ECpdsBase"
#name "getDestinationsByUser"
#group "query"

##
## Variable(s)
##
#prompt "uid;Get Destinations for a given user;%"
#prompt "name;Destination;%"
#prompt "comment;Destination comment;%"
#prompt "country;Country name or iso;%"
#prompt "options;Properties and JavaScript;%"
#prompt "enabled;Enabled;%"
#prompt "monitor;Monitor;%"
#prompt "backup;Backup;%"
#prompt "forceProxy;Force Proxy;%"
#prompt "fromToAliases;From To Aliases;%"
#prompt "orderColumn;Order column index;1"
#prompt "ascending;Sort direction ascending;true"
#prompt "start;Start offset;0"
#prompt "length;Page length;-1"
#prompt "status;Select by status;%"
#prompt "type;Select by type;%"
#prompt "filter;Select by filter;%"

##
## Request(s)
##
SELECT DESTINATION.*
FROM
  DESTINATION,COUNTRY
WHERE
  DESTINATION.COU_ISO = COUNTRY.COU_ISO
  AND CONCAT('/do/transfer/destination/',DES_NAME)
    IN (SELECT URL_NAME FROM WEU_CAT,CAT_URL WHERE WEU_ID = '$uid' and WEU_CAT.CAT_ID=CAT_URL.CAT_ID)
  $name
  $comment
  $country
  $options
  $enabled
  $monitor
  $backup
  $forceProxy
#fi
#if ('$status' != 'All Status')
  AND STA_CODE = '$status'
#fi
#if ('$type' != '')
  AND  DES_TYPE IN ($type)
#fi
#if ('$filter' != 'All')
  AND DES_FILTER_NAME = '$filter'
#fi
#if ('$fromToAliases' == 'to')
  AND DESTINATION.DES_NAME IN (SELECT ALIAS.DES_NAME FROM ALIAS)
#fi
#if ('$fromToAliases' == 'from')
  AND DESTINATION.DES_NAME IN (SELECT ALIAS.ALI_DES_NAME FROM ALIAS)
#fi
#if ('$orderColumn' == '0')
  ORDER BY COU_ISO
#fi
#if ('$orderColumn' == '1')
  ORDER BY DES_NAME
#fi
#if ('$orderColumn' == '2')
  ORDER BY DES_NAME
#fi
#if ('$orderColumn' == '3')
  ORDER BY STA_CODE
#fi
#if ('$orderColumn' == '4')
  ORDER BY (SELECT COUNT(*) FROM ALIAS WHERE ALIAS.ALI_DES_NAME = DESTINATION.DES_NAME)
#fi
#if ('$orderColumn' == '5')
  ORDER BY DES_TYPE
#fi
#if ('$orderColumn' == '6')
  ORDER BY DES_FILTER_NAME
#fi
#if ('$orderColumn' == '7')
  ORDER BY DES_ACTIVE
#fi
#if ('$orderColumn' == '8')
  ORDER BY DES_ACQUISITION
#fi
#if ('$orderColumn' == '9')
  ORDER BY DES_MONITOR
#fi
#if ('$ascending' == 'true')
  ASC
#fi
#if ('$ascending' == 'false')
  DESC
#fi
#if ('$length' != '-1')
LIMIT $start,$length
#fi
