#menu "ECpdsBase"
#name "lockHostPasswordFleetProxies"
#group "select"
#timeout "5"
SELECT HOS_NAME FROM HOST WHERE HOS_TYPE='Proxy' FOR UPDATE
