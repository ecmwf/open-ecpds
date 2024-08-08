##
## References
##
#menu "ECpdsBase"
#name "removeCategory"
#group "update"

##
## Variable(s)
##
#prompt "categoryId;Category Id;%"

##
## Request(s)
##
DELETE FROM CAT_URL
  WHERE CAT_ID = '$categoryId';

DELETE FROM WEU_CAT
  WHERE CAT_ID = '$categoryId';

DELETE FROM CATEGORY
  WHERE CAT_ID = '$categoryId';
