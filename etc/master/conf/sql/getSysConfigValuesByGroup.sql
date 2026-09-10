##
## References
##
#menu "ECpdsBase"
#name "getSysConfigValuesByGroup"
#group "select"

##
## Variable(s)
##
#prompt "group;Configuration group;;"

##
## Request(s)
##
SELECT SCF_PARAM_NAME, SCF_PARAM_VALUE FROM SYS_CONFIG
 WHERE SCF_GROUP='$group'
 ORDER BY SCF_PARAM_NAME
