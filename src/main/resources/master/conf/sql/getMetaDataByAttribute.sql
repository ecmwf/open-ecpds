##
## References
##
#menu "ECpdsBase"
#name "getMetaDataByAttribute"
#group "select"

##
## Variable(s)
##
#prompt "attribute; Attribute name;%"

##
## Request(s)
##
SELECT DISTINCT(MV.MEV_VALUE),MV.MEA_NAME
FROM 
  METADATA_VALUE MV
WHERE 
  MV.MEA_NAME= '$attribute'
  

