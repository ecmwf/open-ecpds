##
## References
##
#menu "ECpdsBase"
#name "insertTransferStatistics"
#group "insert"

##
## Request(s)
##
INSERT INTO TRANSFER_STATISTICS (DAT_ID, TST_START_TIME, TST_END_TIME, TST_LOCAL_ADDRESS, TST_REMOTE_ADDRESS,
       TST_RTT_MS, TST_BYTES_SENT, TST_BYTES_RECEIVED, TST_PACING_RATE_BPS, TST_DELIVERY_RATE_BPS,
       TST_CWND, TST_SEGS_OUT, TST_SEGS_IN, TST_RAW)
VALUES ($datId, $startTime, $endTime, '$localAddress', '$remoteAddress',
       $rttMs, $bytesSent, $bytesReceived, $pacingRateBps, $deliveryRateBps,
       $cwnd, $segsOut, $segsIn, '$raw')
