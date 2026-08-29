##
## References
##
#menu "ECpdsBase"
#name "getSysConfigValue"
#group "select"

##
## Variable(s)
##
#prompt "group;Configuration group;;"
#prompt "name;Parameter name;;"

##
## Request(s)
##
SELECT SCF_PARAM_VALUE FROM SYS_CONFIG
 WHERE SCF_GROUP='$group' AND SCF_PARAM_NAME='$name'
