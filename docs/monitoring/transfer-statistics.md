# Transfer Network Statistics

OpenECPDS captures available per-connection TCP socket statistics for file transfers.
The transfer detail page (`/do/transfer/data/<id>`) separates recorded measurements into
**Retrieval socket statistics**, **Dissemination socket statistics** and
**Socket statistics - unidentified leg**. Only nonempty sections appear.
The leg comes from recorded execution provenance, never from addresses or byte direction.
Legacy rows and rows whose provenance cannot be read remain unidentified.

---

## How Statistics Are Collected

At the end of each TCP connection used during a transfer, OpenECPDS reads OS-level socket
diagnostics — equivalent to the Linux `tcp_info` structure — and persists one record per
connection when supported and enabled. Acquisition payload GETs request the same
socket diagnostics as dissemination; listing and size queries do not enable them.
The Host's `ectrans.socketStatistics` setting is respected (default `true`).
Network Weather consumes immutable snapshots of these measurements and accepts late
socket-close supplements. Retrieval measurements are persisted through active
Network Weather collection for selected acquisition paths; enabling the socket setting
alone does not activate collection or select a path.

!!! note
    Statistics are only available if the Data Mover running the transfer supports
    socket-level introspection and the module instruments its sockets. Linux `ss`
    must be available. Module-specific limitations remain; for example FTP socket
    statistics require keep-alive to be disabled. Missing measurements indicate no
    socket coverage, not zero RTT or a healthy connection.

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

Connections are grouped by the recorded attempt index within their leg's section.
Each section has independent navigation, timelines and totals.

### Multiple Sending Attempts (Requeues)

If a transfer fails completely and is requeued — either automatically by the scheduler
or manually by an operator — each restart is a new attempt. The portal labels these
**Attempt N** based on the stored attempt index, not a count of operator requeues.
Indices can have gaps because they are shared across recorded transfer executions.
An orange badge in
the card header indicates how many attempts are present.

---

## Metrics Reference

| Metric | Description |
|--------|-------------|
| **Bytes Sent** | Cumulative TCP payload bytes transmitted on this socket, including protocol framing and overhead. Usually larger than the raw file size. |
| **Bytes Received** | Cumulative TCP payload bytes received on this socket, including retrieved data and protocol responses. |
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

In the transfer detail page, each socket-statistics section provides:

- A **count badge** showing the total number of TCP connections recorded
- An **attempts badge** (orange) when more than one recorded attempt is present
- An **ⓘ info button** that expands an explanation panel inline
- A **collapsible table** grouped by attempt, showing one row per connection

Average throughput uses received bytes for retrieval and sent bytes for dissemination,
converted to bits per second over the attempt's socket time span. It is omitted when
the leg is unidentified; sent/received totals remain visible separately.

A section is only shown when at least one statistics record exists for that leg.
Absent sections can mean disabled capture, unsupported introspection, no selected
Network Weather retrieval path, no measurements yet, or already-purged records.
