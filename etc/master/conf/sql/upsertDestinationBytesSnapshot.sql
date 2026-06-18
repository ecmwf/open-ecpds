##
## References
##
#menu "ECpdsBase"
#name "upsertDestinationBytesSnapshot"
#group "update"

##
## Variable(s)
##
#prompt "destination;Destination name;"
#prompt "minute;Minute bucket timestamp;"
#prompt "uploadBytes;Upload bytes;0"
#prompt "downloadBytes;Download bytes;0"

##
## Request(s)
##
INSERT INTO DESTINATION_BYTES_SNAPSHOT
  (DBS_DESTINATION, DBS_MINUTE, DBS_UPLOAD_BYTES, DBS_DOWNLOAD_BYTES)
VALUES
  ('$destination', '$minute', '$uploadBytes', '$downloadBytes')
ON DUPLICATE KEY UPDATE
  DBS_UPLOAD_BYTES   = DBS_UPLOAD_BYTES   + '$uploadBytes',
  DBS_DOWNLOAD_BYTES = DBS_DOWNLOAD_BYTES + '$downloadBytes'
