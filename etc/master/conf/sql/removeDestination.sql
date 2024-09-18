##
## References
##
#menu "ECpdsBase"
#name "removeDestination"
#group "update"

##
## Variable(s)
##
#prompt "destinationName;Destination name;%"
#prompt "remove;Should we remove the Destination?;%;java.lang.String"

##
## Request(s)
##
CHUNK 1000 DELETE FROM UPLOAD_HISTORY
  WHERE UPH_ID IN (
    SELECT UPH_ID FROM UPLOAD_HISTORY UPH, DATA_TRANSFER DAT
    WHERE UPH.DAT_ID = DAT.DAT_ID
    AND DES_NAME = '$destinationName'
#if ('$remove' == 'false')
    AND DAT_DELETED = '1'
#fi
);

CHUNK 1000 DELETE FROM INCOMING_HISTORY
  WHERE INH_ID IN (
    SELECT INH_ID FROM INCOMING_HISTORY INH, DATA_TRANSFER DAT
    WHERE INH.DAT_ID = DAT.DAT_ID
    AND DES_NAME = '$destinationName'
#if ('$remove' == 'false')
    AND DAT_DELETED = '1'
#fi
);

CHUNK 1000 DELETE FROM TRANSFER_HISTORY
  WHERE TRH_ID IN (
    SELECT TRH_ID FROM TRANSFER_HISTORY TRH, DATA_TRANSFER DAT
    WHERE TRH.DAT_ID = DAT.DAT_ID
    AND DAT.DES_NAME = '$destinationName'
#if ('$remove' == 'false')
    AND DAT_DELETED = '1'
#fi
);

CHUNK 1000 DELETE FROM PUBLICATION
  WHERE PUB_ID IN (
    SELECT PUB_ID FROM PUBLICATION PUB, DATA_TRANSFER DAT
    WHERE PUB.DAT_ID = DAT.DAT_ID
    AND DES_NAME = '$destinationName'
#if ('$remove' == 'false')
    AND DAT_DELETED = '1'
#fi
);

CHUNK 1000 DELETE FROM DATA_TRANSFER
  WHERE DAT_ID IN (
    SELECT DAT_ID FROM DATA_TRANSFER
    WHERE DES_NAME = '$destinationName'
#if ('$remove' == 'false')
    AND DAT_DELETED = '1'
#fi
);

#if ('$remove' == 'true')
DELETE FROM ALIAS
  WHERE DES_NAME = '$destinationName'
  OR ALI_DES_NAME = '$destinationName';

DELETE FROM INCOMING_ASSOCIATION
  WHERE DES_NAME = '$destinationName';

DELETE FROM POLICY_ASSOCIATION
  WHERE DES_NAME = '$destinationName';

DELETE FROM ASSOCIATION
  WHERE DES_NAME = '$destinationName';

DELETE FROM DES_ECU
  WHERE DES_NAME = '$destinationName';

DELETE FROM CHANGE_LOG
  WHERE CHL_KEY_NAME = 'DES_NAME'
  AND CHL_KEY_VALUE = '$destinationName';

DELETE FROM BANDWIDTH
  WHERE DES_NAME = '$destinationName';

DELETE FROM DESTINATION
  WHERE DES_NAME = '$destinationName';
#fi
