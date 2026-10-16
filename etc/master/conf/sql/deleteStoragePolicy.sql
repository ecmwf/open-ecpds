##
## References
##
#menu "ECpdsBase"
#name "deleteStoragePolicy"
#group "update"

##
## Variable(s)
##
#prompt "name;Name;;"
##
## Request(s)
##
DELETE FROM STORAGE_POLICY WHERE SPO_NAME='$name'
