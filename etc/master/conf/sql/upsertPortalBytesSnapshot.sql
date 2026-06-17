##
## References
##
#menu "ECpdsBase"
#name "upsertPortalBytesSnapshot"
#group "update"

##
## Variable(s)
##
#prompt "user;User name;"
#prompt "minute;Minute bucket timestamp;"
#prompt "uploadBytes;Upload bytes;0"
#prompt "downloadBytes;Download bytes;0"

##
## Request(s)
##
INSERT INTO PORTAL_BYTES_SNAPSHOT
  (PBS_USER, PBS_MINUTE, PBS_UPLOAD_BYTES, PBS_DOWNLOAD_BYTES)
VALUES
  ('$user', '$minute', '$uploadBytes', '$downloadBytes')
ON DUPLICATE KEY UPDATE
  PBS_UPLOAD_BYTES   = PBS_UPLOAD_BYTES   + '$uploadBytes',
  PBS_DOWNLOAD_BYTES = PBS_DOWNLOAD_BYTES + '$downloadBytes'
