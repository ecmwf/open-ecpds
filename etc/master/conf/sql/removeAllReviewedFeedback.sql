##
## References
##
#menu "ECpdsBase"
#name "removeAllReviewedFeedback"
#group "update"

##
## Request(s)
##
CHUNK 1000 DELETE FROM FEEDBACK
  WHERE FBK_ID IN (
    SELECT FBK_ID FROM FEEDBACK
    WHERE FBK_REVIEWED = 1
);
