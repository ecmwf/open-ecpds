##
## References
##
#menu "ECpdsBase"
#name "setStoragePolicyOrder"
#group "update"

##
## Variable(s)
##
#prompt "name;Name;;"
#prompt "order;Order;;"
##
## Request(s)
##
UPDATE STORAGE_POLICY SET SPO_ORDER=$order WHERE SPO_NAME='$name'
