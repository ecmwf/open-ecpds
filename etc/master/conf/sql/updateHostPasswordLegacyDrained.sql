#menu "ECpdsBase"
#name "updateHostPasswordLegacyDrained"
#group "update"
#timeout "5"
#prompt "host;Host;;"
#bind "host;LONG"
UPDATE HOST_PASSWORD_LEGACY_FENCE SET HPL_DRAINED_AT=CURRENT_TIMESTAMP(6) WHERE HOS_NAME=:host
