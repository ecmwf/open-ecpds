##
## References
##
#menu "ECpdsBase"
#name "resetProductStatus"
#group "update"

##
## Variable(s)
##
#prompt "metaStream;MetaStream;%"
#prompt "metaTime;MetaTime;%"
#prompt "timeStep;TimeStep;%"

##
## Request(s)
##
CHUNK 1000 DELETE FROM PRODUCT_STATUS WHERE PRS_ID IN (
  SELECT PRS_ID FROM PRODUCT_STATUS
  WHERE PRS_STREAM = '$metaStream'
  AND PRS_TIME = '$metaTime'
#if ('$timeStep' != '-1')
  AND PRS_STEP = '$timeStep'
#fi
)
