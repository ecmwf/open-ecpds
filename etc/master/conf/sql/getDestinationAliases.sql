##
## References
##
#menu "ECpdsBase"
#name "getDestinationAliases"
#group "query"

##
## Variable(s)
##
#prompt "destination;Which host;%"
#prompt "mode;Which host;%"

##
## Request(s)
##
SELECT D.*
FROM 
  DESTINATION D, ALIAS A
WHERE 
#if ('$mode' == 'aliases')	
	A.ALI_DES_NAME = '$destination'	
	AND A.DES_NAME = D.DES_NAME
#fi
#if ('$mode' == 'aliasedFrom')	
	A.DES_NAME = '$destination'	
	AND A.ALI_DES_NAME = D.DES_NAME
#fi
