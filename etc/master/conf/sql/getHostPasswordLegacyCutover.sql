#menu "ECpdsBase"
#name "getHostPasswordLegacyCutover"
#group "select"
#timeout "5"
#prompt "host;Host;;"
#bind "host;LONG"
SELECT HLC_ID,HLC_REFERENCE,HLC_ENDPOINT,HLC_CONFIGURATION
FROM HOST_PASSWORD_LEGACY_CUTOVER WHERE HOS_NAME=:host FOR UPDATE
