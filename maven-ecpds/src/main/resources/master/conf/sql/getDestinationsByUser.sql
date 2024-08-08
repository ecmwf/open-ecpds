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
#prompt "fromToAliases;From To Aliases;%"
#prompt "asc;Sort direction;%"
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
#if ('$asc' == 'true')
  ORDER BY DES_NAME ASC
#fi
#if ('$asc' == 'false')
  ORDER BY DES_NAME DESC
#fi
