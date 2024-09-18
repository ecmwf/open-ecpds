##
## References
##
#menu "ECpdsBase"
#name "removeUrl"
#group "update"

##
## Variable(s)
##
#prompt "urlName;Url Name;%"

##
## Request(s)
##
DELETE FROM CAT_URL
  WHERE URL_NAME = '$urlName';

DELETE FROM URL
  WHERE URL_NAME = '$urlName';
