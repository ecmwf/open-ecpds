##
## References
##
#menu "ECpdsBase"
#name "removeAllFeedback"
#group "update"

##
## Request(s)
##
CHUNK 1000 DELETE FROM FEEDBACK
  WHERE FBK_ID IN (
    SELECT FBK_ID FROM FEEDBACK
);
