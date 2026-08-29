##
## References
##
#menu "ECpdsBase"
#name "setSysConfigValue"
#group "update"

##
## Variable(s)
##
#prompt "group;Configuration group;;"
#prompt "name;Parameter name;;"
#prompt "value;Parameter value;;"

##
## Request(s)
##
INSERT INTO SYS_CONFIG (SCF_GROUP, SCF_PARAM_NAME, SCF_PARAM_VALUE, SCF_UPDATED_AT)
 VALUES ('$group', '$name', '$value', NOW())
 ON DUPLICATE KEY UPDATE SCF_PARAM_VALUE=VALUES(SCF_PARAM_VALUE), SCF_UPDATED_AT=NOW()
