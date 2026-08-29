##
## References
##
#menu "ECpdsBase"
#name "deleteSysConfigValue"
#group "update"

##
## Variable(s)
##
#prompt "group;Configuration group;;"
#prompt "name;Parameter name;;"

##
## Request(s)
##
DELETE FROM SYS_CONFIG WHERE SCF_GROUP='$group' AND SCF_PARAM_NAME='$name'
