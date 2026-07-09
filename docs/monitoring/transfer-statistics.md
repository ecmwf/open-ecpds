# Transfer Network Statistics

OpenECPDS captures per-connection TCP socket statistics for every file transfer. These
are surfaced in the monitoring portal on the transfer detail page (`/do/transfer/data/<id>`)
under the **Network Statistics** card, and are stored persistently in the
`TRANSFER_STATISTICS` database table.

---

## How Statistics Are Collected

At the end of each TCP connection used during a transfer, OpenECPDS reads OS-level socket
diagnostics — equivalent to the Linux `tcp_info` structure — and persists one row per
connection in `TRANSFER_STATISTICS`. This happens automatically for every connection
opened by the transfer module (FTP, SFTP, S3, HTTP, …), requiring no manual
instrumentation.

The table references `DATA_TRANSFER` via a foreign key on `DAT_ID`, linking each
statistics record back to its parent transfer.

!!! note
    Statistics are only available if the Data Mover running the transfer supports
    socket-level introspection. All built-in transfer modules do. Custom or legacy
    modules may not populate these records.

---

## Connection Groups and Sending Attempts

A single file transfer can produce **multiple statistics rows** for two distinct reasons:

### Multiple Connections Within One Attempt

Many transfer protocols open more than one TCP socket during a single transfer:

- **Parallel streams** — S3 multipart uploads, FTP active/passive data channels.
- **Sequential retries within a session** — the transfer module reconnects after a
  transient error without requeueing the whole transfer.
- **Control + data channels** — FTP uses separate sockets for commands and file data;
  only the data channel is instrumented.

All connections belonging to the same sending attempt share the same *requeue count*
and appear as a single group in the portal.

### Multiple Sending Attempts (Requeues)

If a transfer fails completely and is requeued — either automatically by the scheduler
or manually by an operator — each restart is a new attempt. The portal labels these
**Attempt 1, Attempt 2, …** based on the requeue history counter. An orange badge in
the card header indicates how many attempts are present.

---

## Metrics Reference

| Metric | Description |
|--------|-------------|
| **Bytes Sent** | Cumulative TCP payload bytes transmitted on this socket, including protocol framing and overhead. Usually larger than the raw file size. |
| **Bytes Received** | Cumulative bytes received on this socket (ACKs, responses, control data). |
| **Delivery Rate** | Peak TCP goodput measured by the kernel on this socket (bytes/sec). Reflects the sustained network-layer throughput, not application-level speed. |
| **RTT** | Smoothed round-trip time as reported by the kernel. High RTT indicates distant or congested network paths. |
| **Retransmits** | TCP segments retransmitted due to loss or timeout. Any non-zero value suggests congestion or packet loss on the path to the destination. |
| **Start Time** | Wall-clock time when this TCP connection was opened. |
| **End Time** | Wall-clock time when the connection was closed and statistics were captured. |

!!! tip "Interpreting retransmits"
    A small number of retransmits (1–5) on a long transfer is normal. A high retransmit
    count relative to total bytes sent (>1%) indicates significant packet loss and is
    worth investigating at the network level.

---

## How This Differs From the Global Transfer Rate

The **global transfer rate** shown in the main transfer card is computed end-to-end by
OpenECPDS:

```
global rate = acknowledged file size ÷ (completion time − queue start time)
```

This covers the full wall-clock span of the transfer, including:

- Queue wait time
- Connection establishment and protocol handshakes
- Any gaps between sequential connections
- Time spent in error/retry loops

The **per-socket delivery rate** in Network Statistics is narrower:

- Measured only during the active lifetime of that one TCP connection
- Reported by the kernel, not computed by OpenECPDS
- Does not include queue time or inter-connection gaps

As a result, the **global rate will typically be lower** than the peak per-socket
delivery rate, and may span a much longer time window.

| Metric | Scope | Includes overhead | Source |
|--------|-------|-------------------|--------|
| Global transfer rate | Full transfer lifecycle | Yes (queue wait, handshakes) | OpenECPDS (computed) |
| Socket delivery rate | Per TCP connection | No | OS kernel (`tcp_info`) |

## Monitoring Portal

In the transfer detail page, the **Network Statistics** card provides:

- A **count badge** showing the total number of TCP connections recorded
- An **attempts badge** (orange) when more than one sending attempt is present
- An **ⓘ info button** that expands an explanation panel inline
- A **collapsible table** grouped by attempt, showing one row per connection

The card is only shown when at least one statistics record exists for the transfer.
If the card is absent, the transfer module either did not support socket introspection,
or the statistics were already purged by the nightly maintenance job.
