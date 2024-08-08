##
## References
##
#menu "ECpdsBase"
#name "removeWebUser"
#group "update"

##
## Variable(s)
##
#prompt "webUserId;Web User Id;%"

##
## Request(s)
##
DELETE FROM WEU_CAT
  WHERE WEU_ID = '$webUserId';
  
DELETE FROM EVENT
  WHERE ACT_ID IN (SELECT ACT_ID FROM ACTIVITY WHERE ECU_NAME = '$webUserId');

DELETE FROM ACTIVITY
  WHERE ECU_NAME = '$webUserId';

DELETE FROM PERMISSION
  WHERE ECU_NAME = '$webUserId';

DELETE FROM HOS_ECU
  WHERE ECU_NAME = '$webUserId';

DELETE FROM DES_ECU
  WHERE ECU_NAME = '$webUserId';

UPDATE DESTINATION
  SET ECU_NAME = 'anonymous'
  WHERE ECU_NAME = '$webUserId';

UPDATE HOST
  SET ECU_NAME = 'anonymous'
  WHERE ECU_NAME = '$webUserId';

DELETE FROM WEB_USER
  WHERE WEU_ID = '$webUserId';

DELETE FROM ECUSER
  WHERE ECU_NAME = '$webUserId';
