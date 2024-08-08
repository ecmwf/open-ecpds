##
## References
##
#menu "ECpdsBase"
#name "getMetaDataByDataFile"
#group "query"

##
## Variable(s)
##
#prompt "datafile; Data File;%;int"

##
## Request(s)
##
SELECT MV.*
FROM 
  METADATA_VALUE MV
WHERE 
  MV.DAF_ID= '$datafile'
