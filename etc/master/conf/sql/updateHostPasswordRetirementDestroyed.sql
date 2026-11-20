#menu "ECpdsBase"
#name "updateHostPasswordRetirementDestroyed"
#group "update"
#timeout "5"
#prompt "reference;Reference;;"
#bind "reference;STRING"
UPDATE HOST_PASSWORD_RETIREMENT SET HPT_STATE='DESTROYED',HPT_DESTROYED_AT=CURRENT_TIMESTAMP(6)
WHERE HPT_REFERENCE=:reference
