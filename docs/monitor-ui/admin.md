# Administration

The Administration section provides system-wide management tools
available to users with administrator privileges.


## Administration

The admin home page provides access to: [Destination Metadata](../administration/destination-metadata.md) field configuration (defining custom fields attachable to destinations, e.g. contacts, procedures, Opsview notes), transfer requeue (bulk re-scheduling of failed transfers), file upload (injecting files directly into the data store), the audit/feedback log, [Product Descriptions](../administration/product-descriptions.md) (per-product/type Tips and Descriptions), [TLS certificate management](../administration/certificates.md), monitoring status colors, [Critical Password](../administration/critical-password.md) setup, and the destructive [Purge All Data](../administration/purge.md) reset tool.


![Administration](img/admin.png)

## Storage Lifecycle Policies

At `/do/admin/storagepolicies`, administrators define and test replica-retention policies,
inspect dry-run estimates and persistent savings counters, and set per-file operator
holds. Opt-in enforcement preserves original copies until normal expiry; advanced
settings include pressure watermarks and lazy replication. File and transfer details
show retention status and link to hold management.
See [Storage Lifecycle Policies](../administration/storage-lifecycle-policies.md).

## Network Weather

`/do/admin/networkweather` controls **bounded passive dissemination collection**
for selected local and Continental Movers. Collection starts **disabled**. An
administrator can **Activate collection** or **Deactivate collection** at runtime
without restarting the Master, Monitor or Movers. The same page separately controls
automatic historical discovery and scheduled probes. All optional features default
off. Empty measurements mean unavailable coverage, not healthy paths.

The page is organised in tabs: **Overview** (state, activation, capabilities,
storage and counters), **Manual scope**, **Automatic selection**, **Probes**,
**Sources** and **Measurements**. The last selected tab is restored after saving.
The probe results, source registration and measurement tables can be searched, sorted
and paged in the same way as the Storage Lifecycle Policies table; states and outcomes are
shown as coloured badges and paths without observations are marked **no coverage**.
A **Configuration Guide** button opens a short user guide, and the
<i class="bi bi-info-circle"></i> icon on each card expands a short explanation of its content.

Paths, source/transfer-group scopes, pins, exclusions and probe policies are
edited with builders rather than free text: choose a source (or *Other source*
for a Continental Mover root), filter and select an active Dissemination Host,
then press **Add**. The Host target and method are filled in from the Host
configuration; duplicates, limits, invalid characters and probe mode/interval
combinations are refused immediately, and saved entries that no longer match an
active Dissemination Host are flagged. Pins, exclusions and probes can also be
picked from the approved candidates. For probes, a port is suggested from the
transfer method but remains an explicit operator choice. The builders only assist
entry; the Master still validates every save. Selector choices (Movers, transfer
groups and active Dissemination Hosts, up to the deployment's `maxHostChoices`) are shared by administrators
and reloaded at most every five minutes, or immediately with **Refresh choices**.
Without JavaScript, the underlying one-entry-per-line fields remain usable.
Path builders hide entries already added for the selected source, including in
the approved-candidate shortcut. Removing an entry makes it selectable again;
the same Host remains available for other sources. Probe builders still allow
the same path with different probe settings, but hide exact policies already added
from both Host and approved-candidate selectors. Changing the port, mode or interval
refreshes those choices; removing a policy makes it available again.
The source/group builder allows one transfer group per source: its group selector
and Add button are disabled for a source already listed, until that entry is removed.
The same transfer group can still be assigned to other sources.
Save errors identify policy sections and offending paths, and line numbers for
probe and source/group input. Pins, exclusions and probe paths must first appear
in Approved candidates; a changed Host target or method requires updating its approval.
If a save fails, submitted entries and switches remain in the form so they can
be corrected and submitted again. These displayed edits are not saved until the
Master accepts the request; reloading the page retrieves the saved configuration.
**Refresh choices** uses the Monitor's shared confirmation modal if it would discard edited entries,
unfinished builder input or switch changes. Cancel keeps the current form.

### Deployment capacity

Capacity is configured in the Master's properties file under `[NetworkWeather]`.
The Master reports its effective limits to the Monitor; builder counters, help text,
manual saves, discovery, selection and probe validation use those same limits.
Matching updated components are required, but these capacity values do not need to
be duplicated in the Monitor or Mover configuration. Restart the Master after changing
them; lowering limits below an existing saved policy requires reducing that policy first.
Collection and probe activation remain separate runtime switches in the administration UI.

| Setting | Default | Scope |
|---|---:|---|
| `maxSources` | 20 | Distinct source Movers; source/group entries still allow one group per source. |
| `maxSelectedPaths` | 50 | Simultaneously monitored paths and maximum pinned paths. |
| `maxCandidates` | 500 | Explicit approved pool and combined group-discovered pool; also bounds exclusions. Must be at least `maxSelectedPaths`. |
| `maxProbePolicies` | 50 | Configured scheduled probe policies. |
| `maxHostChoices` | 5000 | Hosts available to the selectors; the filter shows at most 300 matches at once. |
| `maxConfigurationBytes` | 49152 | UTF-8 size of each saved JSON configuration, including its metadata; up to the `MEDIUMTEXT` capacity (16,777,215 bytes). |

For example, a larger deployment can use:

```ini
[NetworkWeather]
maxSources=80
maxSelectedPaths=1000
maxCandidates=10000
maxProbePolicies=200
maxHostChoices=20000
maxConfigurationBytes=4194304
```

These are sizing examples, not throughput guarantees. Candidate queries remain
deadline-bounded and approved Host lookups are batched. Source verification polling
scales with the configured source capacity, but slow or unavailable callbacks still
consume its single worker. Probe execution retains its independent rate limit.
Increasing the policy count does not automatically increase probe execution frequency
or the event queues' capacity.

The installation schema uses `MEDIUMTEXT` for `SYS_CONFIG.SCF_PARAM_VALUE`,
providing storage for larger policies. The Master checks actual storage
capacity before saving a larger document and fails explicitly rather than allowing truncation.
Do not load the full installation dump into a populated database. Coordinate schema operations with the DBA.

Large browser submissions also need adequate Monitor form capacity. Set, for example,
`maxFormContentSize=16777216` under `[MonitorPlugin]` in the Monitor's properties file
and restart it. The default is 200,000 bytes; budget for all submitted fields together
and URL encoding, not just one policy field. Check any reverse proxy's request limit too.

For many selected paths on an individual Continental Mover, its REST capability response
may exceed the default 65,536-byte bound. Set `[NetworkWeather] maxControlResponseBytes`
to an appropriate positive byte count on Movers and relays receiving those replies,
and restart those services. This setting is local to the REST client, not supplied by the Master.

### Automatic selection and historical discovery

In the **Automatic selection** tab, approve explicit candidates
using `source | host ID | configured target | transfer method`, or approve bounded
`source | transfer group` scopes. Group scopes discover active Dissemination Hosts
without maintaining an individual list. A source/group approval is administrative
scope, **not evidence that this source performed a historical transfer**.
The candidate, selected-path and emitting-source limits are deployment-configurable
(defaults: 500 candidates, 50 selected paths and 20 sources).
Pins, exclusions and probe policies reference explicit approved candidates; exclusions
override pins. Saving the automatic policy does not activate passive collection.

Enable **Automatic selection** to reassess daily, or request an earlier reassessment.
Enable **Allow bounded historical SQL discovery** separately to rank candidates using
seven days of upload activity and destination-scoped trouble samples. Historical
addresses are only matched to currently approved Host targets; historical rows never
establish actual emitting-source identity. Ranking mixes activity, trouble and
source/method diversity. Existing valid coverage is retained on partial input or
failure; complete runs can replace at most 10% of the configured selected-path capacity
(rounded down, minimum one; five with the default capacity) of unpinned, non-troubled paths per day,
and only with materially stronger evidence. Known invalid or excluded paths are
removed before optional history work, even if that later work fails.
An empty automatic selection suspends collection without erasing activation intent.
If recording a pruned scope fails, removed paths remain stopped in that Master;
the page warns to retry before restarting, since the last saved scope still exists.

Use **Find candidates from successful transfers** in Automatic selection to search
a user-chosen period of 1–30 days, optionally filtered by protocol (ECtrans module,
such as FTP or SFTP). The protocol filter uses the Host's current transfer method
and is applied in the database search.
Choose the source to assign explicitly: transfer history identifies the successful
Host but not its historical source. Only currently
active dissemination Hosts with a `DONE` history entry in that period are suggested.
Already-approved paths for the chosen source are omitted from the preview.
Review and select matches, then add individual Approved candidates or source/group
scopes. Group scopes also permit Hosts without a recent success and support only
one group per source. Existing entries are preserved and duplicates are skipped;
conflicting groups and capacity errors leave the form unchanged. The preview is
searchable and paginated (50 Hosts per page), with selections retained across pages.
Search results use `maxHostChoices`; adding paths uses `maxCandidates`.
Click **Save** to persist the additions. Searches use bounded, read-only database access and report
timeouts or result-limit errors rather than silently returning incomplete results.

**Historical discovery** is separate: it ranks already permitted candidates using
up to seven days of activity, volume and error signals. It neither populates approval
lists nor limits selection to Hosts with successful transfers.

Policy, selection and bounded pagination progress survive restart. Assessment
counters and reasons are transient. Query errors, pressure skips and partial samples
are visible in the administration status. Disable automatic selection before saving
a fixed manual scope. Automatic policy changes are rejected during an in-flight
reassessment rather than racing its persisted selection; retry after it finishes.

Historical reads use the existing Hikari pool, require two idle connections and no
pool waiters, and have a separate 100-ms acquisition limit. One low-priority,
non-overlapping reassessment has a 30-second SQL work budget and two-second statement
timeouts, plus a per-connection network timeout capped at three seconds and restored
before pool return. Upload pages contain at most 500 rows, with at most ten pages per run.
Destination discovery returns at most 101 names and visits at most 100 indexed
destination/time ranges, sampling at most 50 history rows each. The history run
returns at most 10,000 rows, including destination discovery. Upload timestamp/ID
and destination continuations are saved; incomplete samples are never presented
as an exhaustive history scan. No DDL, pool-wide timeout changes or historical
backfill is performed.

**Returned-row caps do not bound rows examined by the database.** Validate representative
`EXPLAIN` plans, driver/server cancellation, shared-pool pressure and collection-off/on
load on the test system before enabling historical discovery in production. Unsupported
pools and missing indexes fail explicitly; discovery does not fall back to an
unbounded read. The administration and globe refresh paths do not query history.

### Scheduled diagnostics

Approve probes separately with:
`source | host ID | configured hostname | method | approved service port | mode | interval seconds`.
Review the explicit port against the real transfer-module service configuration;
the scheduler never guesses a port, performs protocol login or sends credentials.
`TCP_CONNECT` requires an interval of at least 60 seconds; `MTR_ICMP` and `MTR_TCP`
require at least 300 seconds. New automatically selected paths do **not** acquire
probe approval. MTR needs a JSON-capable `mtr` executable and suitable privileges
on the actual emitting Mover.

Jobs run asynchronously on the exact local or Continental source, through the
existing signed RMI/REST control and result transport, without local-source fallback.
There is one pending job per source, at most twenty globally, and at most twelve
starts per minute, with jitter and failure backoff. Jobs expire after 60 seconds.
Source execution has one worker and no waiting-job queue, a bounded DNS wait with
one resolver thread, a three-second TCP connect limit and a twenty-second MTR limit,
within a 25-second execution budget. An uninterruptible DNS implementation cannot
create replacement resolver threads. MTR output is limited to 32 KiB and 32 hops;
its process tree is terminated on timeout or control loss. A missing tool,
insufficient privileges, malformed output, no target response and missing source
results are distinct from measured connectivity success. Intermediate-hop loss
does not establish an end-to-end fault.

Completed diagnostics and source-unavailable notices are capped and kept for one
hour in Master memory. They are not a durable probe archive. Result replays after
a lost acknowledgement are idempotent. Collection deactivation or probe-policy
removal stops further admission/scheduling; a source cancels optional work when
it learns of control loss. Existing transfers and interactive diagnostics are unchanged.

Deploy matching Master, Monitor and Mover code: signed collection capability
**version 2** covers the extended job/result transport. Older sources cannot
silently qualify for this capability.

### Live Earth weather overlay

The Network Weather icon (<i class="bi bi-cloud-lightning-rain"></i>) in the Live Earth
toolbar is shown while collection is active or its state cannot yet be confirmed.
Click it to switch the overlay on
(highlighted) or off (normal); it starts **off**. Its preference is saved
in browser local storage, independently of transfer animation and other globe
preferences. Enabling it displays a searchable table of permitted source/Host/method
evidence, socket coverage, observation age and available diagnostic summaries, plus
independent Host-location halos. Gray/absent evidence means unknown coverage, not
healthy connectivity; orange delivery trouble is not a network-fault diagnosis.
Unresolved coordinates (including internal addresses) do not create invented locations,
but never hide the icon or permitted table rows. The Dissemination filter
also applies to the overlay.

The server filters by the viewer's active monitoring authorization, Host and
destination permissions before aggregation. Paths lacking permitted retained
delivery evidence are not disclosed to restricted viewers. Raw endpoint addresses,
MTR hops, transfer IDs and administrative errors are not sent to the globe.
Idle paths with previously permitted retained evidence show no recent observations,
not zero-latency success. Viewing the overlay never starts collection or a probe.

When collection is disabled or suspended, the server returns no weather paths and
the icon is hidden and the overlay cleared on the next refresh. Errors and stalled
requests also clear stale evidence, but keep the icon available: its tooltip and opened
panel show the failure rather than silently disappearing. Refresh is every 15 seconds with an eight-second
request deadline; a disabled collection therefore overrides even a saved `on`
preference. The preference is retained, so it can resume when collection becomes
available again. Off viewers request only control state, not path/geolocation data.
Geographic lookups are shared and cached for five minutes, rather than repeated
for every viewer refresh.

### Scope and runtime controls

The **Manual scope** builder stores paths as
`source | host ID | configured target | transfer method`.
Use the actual emitting Mover root, not its relay. The target and transfer method
must exactly match an active Dissemination Host; these settings are checked again
on activation. The limits come from the Master's deployment configuration. A blank scope clears the
selection. **Saving scope disables collection**, requiring explicit reactivation.

Activation requires restored control state, the transactional schema/writer and
fresh verified identities for every selected source. The page displays blockers,
registration state, five-minute observed counts and persistence health. Source
loss after activation gates that source independently; it does not stop unrelated
sources. Controls require administration permission at both Monitor and Master,
use CSRF-protected POSTs and revision checks, and are audited.

Deactivation stops Master admission before saving the disabled intent. Source
capture stops on its next negotiation or control-lease expiry, within 60 seconds.
Already admitted socket writes can finish; retained measurements are not erased.
If recording deactivation fails, the Master stays stopped and reports the error:
retry before restarting, because the previously saved activation intent can
otherwise be restored. Ordinary transfer processing, KPI byte accounting and
viewer-driven animation are not stopped.

The scope and activation intent survive Master restarts. Restored activation
starts suspended until the background schema check succeeds. Missing schema or
repeated transaction failures suspend collection; readiness recovery can resume
saved activation intent. Pending evidence and the assessment itself are
memory-only and do not survive a restart.

### Source registration and transport

Updated Movers negotiate by default (`[Server] networkWeatherNegotiation=yes`).
An explicit `no` remains a per-Mover opt-out. Negotiation alone captures nothing.
A low-priority scheduler negotiates and delivers every 30 seconds, using direct
RMI or the existing Continental REST relay. REST Weather operations require
`[Security] rccSharedSecret` signing at every hop, without changing legacy
endpoints. Cached control expires monotonically after at most 60 seconds.
Failed, mismatched or slow replies invalidate it. Older unsupported services
back off for five minutes; other failures use bounded backoff and explicit logs.
Scheduler MBeans expose lease, capture and outbox status.

The Master verifies a fresh Ed25519 challenge using the callback already
registered through DataMover subscription authentication. It never installs a
public key from an observation, incoming IP or arbitrary URL. The process-private
key is not returned or exposed in the UI. The signed identity must advertise
collection protocol version 1 (dispatch timing and per-execution writer handover).
Deploy matching Master,
Monitor, local relay and Continental Mover versions.

One background worker checks at most one selected source per step,
refreshing after 30 seconds with 120-second monotonic proof expiry. An existing
valid proof retains its original expiry during refresh; callback replacement,
failed verification or lost subscription invalidates it. The callback deadline
is five seconds with no queued work or extra workers for a stuck transport.
Neither administration refreshes nor ingress perform identity callbacks or
topology queries. Registration inherits, and does not strengthen, existing
subscription authentication.

For a selected Continental source without a direct subscription, an operator
can explicitly approve its existing Proxy Host on the Master:

```ini
[NetworkWeatherRelay]
continental-source.example=101
```

The key is the actual Continental Mover root; the value is the existing active
Proxy Host ID, not an arbitrary URL or relay hostname. A subscribed local Mover
from that Host's configured transfer group forwards the challenge over the Host's
existing REST route. The returned identity must match the configured source and
challenge; route changes, relay subscription replacement or loss invalidate the
registration. Host route snapshots are refreshed in the background at most once
per 30 seconds, for the configured selected sources, and expire after 60 seconds.
Only these explicitly mapped hosts are looked up; page refresh and observation
ingress never perform topology SQL. A failed source mapping is logged and does
not stop verification of other sources.

The local relay must use `[MoverProxy] trustAllCerts=no`, valid HTTPS target
names and an appropriate JVM truststore. Registration rejects HTTP, URL user
credentials and trust-all certificate mode; shared request signing alone does
not authenticate the public-key response. All target alternatives must satisfy
these checks. The REST operation shares the existing five-second/three-attempt,
64-KiB-response Weather budget and requires control-channel signing at the
Continental service as well. No identity is synthesized from transfer history,
an incoming observation, a heartbeat or an IP address.

Each outgoing batch carries a domain-separated signature covering the typed
envelope, dispatch timestamp, identities, fields and reported queue ages. Relays
preserve the original proof; JSON property order does not affect verification.
The Master checks the current callback/route and unexpired process key before
and after cryptography, with at most 100 signature-check starts per second.
Source/signature/timing rejections and temporary deferrals are visible separately
from verified batches; verified does not mean admitted or committed.

Challenge timing estimates clock offset and uncertainty. The Master independently
bounds signed dispatch/event age; underreporting queue age cannot bypass expiry.
Uncertain minute boundaries or excessive clock offset remain untimed evidence,
not falsely precise buckets. A signature authenticates the source's claim, not
its diagnosis; no network fault is inferred from arbitrary error text.

### Persistence, budgets and coverage

Execution identity and source/path scope are frozen before preparation/submission,
independently of Live Earth viewers. Success, failure, explicit cancellation,
post-callback preparation/submission errors and cleanup failures produce bounded
terminal evidence. Late socket closes supplement the same sending. Routine
cleanup is not cancellation; success despite stop intent remains success.
Failures before callback/module creation can remain uncovered. Acquisition and
other transfer legs are not included.

Existing module socket-statistics settings still determine available measurements;
capture launches no additional socket-statistics subprocess. At most 16 connections
are copied per sending. Missing/invalid/oversized snapshots, contention, drops and
expiry are coverage loss, not packet loss. Socket-less failures remain sending
outcomes, not invented zero-valued connections. New structured rows carry
addresses, timing, RTT, sent/received bytes, pacing/delivery rates, congestion
window and segment counts when available. Missing values remain unavailable,
including peak rate in the summary; raw socket text is not transmitted or invented.

Each source outbox and the Master writer queue is bounded to 1,000 events,
8 MiB reserved memory and five-minute expiry. Batches hold at most 100 events,
64 KiB per event and 256 KiB overall, with JSON and Java-serialization bounds.
Weather HTTP replies default to 64 KiB (`maxControlResponseBytes`) and relay attempts to three within
five seconds. Partial receipts retire only acknowledged events; temporary failure
retains stable identities. Scope/epoch changes or disabling discard optional
source work with counters.

**An accepted receipt means bounded pipeline admission, not a committed database
transaction.** Ingress and delivery callbacks perform no new SQL. One low-priority
Master worker starts at most ten events/100 connection rows per second and stops
starting work after a 200-ms step budget; individual SQL statements have a
two-second timeout, so this is not a hard 200-ms transaction deadline. Transactions
have two retries with backoff. Deleted transfers, missing terminal headers,
transaction failures, queue pressure and expiry are visible coverage gaps.
Schema metadata is checked in the background every 30 seconds, without historical
scans or automatic DDL. These caps are safeguards, not a production latency
guarantee; validate shared database/pool pressure on the test system.

One transaction records the immutable sending header, deduplicated connection
identity and `TRANSFER_STATISTICS` rows. Replay after a lost acknowledgement
reuses the identities; conflicting immutable evidence is rejected. Tagged
executions use this writer exclusively, including failures, and untagged or
unselected executions use the successful-only writer. Successful-only index
allocation also checks the indexed Weather headers, so socket-less failures do
not share an index with later successful statistics. Successful upload
history is unchanged. Transfer details show sending outcome when available and
identify missing provenance; the sending index is not a requeue count.

### Deployment validation

1. Keep collection disabled and deploy matching components. Verify the required
   Weather identity tables, covering statistics index, InnoDB parent tables and
   matching key types. The supplied installation schema includes these objects.
   **Do not load the destructive full installation dump into a populated database.**
   Schema operations require the site's database change procedure, backups and DBA
   review of metadata locks and resource pressure.
2. Configure approved Continental routes and verified HTTPS/signing, then save a
   small local and Continental scope. Wait for writer readiness and source
   registration. Activate from the administrator page; no service restart is
   needed for subsequent controls.
3. Exercise successful and failed deliveries, plain retries/requeues, cancellation,
   late socket cleanup, relay outages and service restarts. Check source capture,
   admission counts, committed socket rows and queue/loss counters. Test with
   nobody viewing Live Earth, and compare transfer latency, database/pool load
   and ordinary byte totals against the disabled baseline.
4. Deactivate and verify immediate Master refusal and source lease expiry while
   transfers continue. Only enable in production after the test-system outcome
   and the normal deployment review are satisfactory.

## Monitoring Status Colors

Administrators can configure the shared status palette at
`/do/admin/monitoringcolors`. The settings cover product-generation,
OpenView, and arrival/transfer statuses used by monitoring legends, indicators,
and charts. Values are stored in the shared system configuration and must use
`#RRGGBB` format. Saving a value equal to its built-in
default removes the override; **Reset All to Defaults** removes every override.
Changes apply immediately on the current Monitor and are picked up by other
Monitor instances within 15 seconds.

## Scheduler Traffic

**Scheduler Traffic** (`/do/admin/schedulertraffic`), linked from Administration
Tasks, the start page and the administration menu, monitors the Master's current
**Proxy**, **Backup** and **Replication** workers. Switch between the three types
using the buttons above the table. The read-only page uses the same card/table
style as Outstanding Transfers, with local filtering, sorting, pagination,
manual refresh and a selectable auto-refresh interval (5 seconds by default).

Each row represents one current worker per data file, identified by the transfer
that started it; aliases can share a file. It shows destination, transfer/data-file
links where permitted, requested Target, worker phase, transfer status, source
Mover when known, target Host, file size, start time and elapsed time.
**Starting**, **Processing** and **Finalizing** describe the scheduler worker;
the separate **Transfer status** describes dissemination, not whether this
background copy has succeeded.

Completed workers disappear. This is neither a waiting queue nor an outcome
history; consult the transfer history for completed or failed operations. Disabled
schedulers are explicitly identified, and connection errors are shown rather
than reported as an empty successful snapshot. Replication can involve several
members of a Mover group; Proxy/Backup select the source internally and Proxy
Host selection may change during an attempt. File size is not bytes moved:
these blocking operations do not expose reliable live percentage or throughput.

Refresh retrieves detached in-memory worker snapshots through the management
interface. It performs no database scans, Mover probes or transfer-path I/O and
does not change scheduler behaviour. Access follows the normal administration
URL permissions (`/do/admin/schedulertraffic`); deploy the updated Master and
Monitor together.
