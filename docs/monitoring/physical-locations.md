# Physical system locations

OpenECPDS can display administrator-maintained SNMP `sysLocation.0`
(`1.3.6.1.2.1.1.6.0`) on **System Topology** components and on the
**Data Mover details** page, in its Connection card. It is plain text such as
`Bonn, Hall 2, Rack 14`, not automatic physical discovery or geographic coordinates.
It does not replace Host Map/Live Earth locations or change topology host grouping.

## Architecture and persistence

Only the **Master JVM** collects SNMP and accesses `SYSTEM_PHYSICAL_LOCATION`.
Each **Monitor JVM** obtains the cached display metadata over its existing
management **RMI connection** to the Master. **Mover JVMs need no SNMP changes**.
The Master polls each explicitly configured machine once, even when it runs
several mapped components. It never scans the network, infers management addresses
from service/DNS addresses, or queries SNMP in response to a page/API request.

Database records contain the machine identifier, a SHA-256 digest of the target
address/port/context, location, collection status, last attempt and last successful
verification. Times are UTC epoch milliseconds, exposed to the UI as UTC ISO dates.
Credentials and raw management addresses are not included in the topology response
or this table.

Successful values survive polling failures and Master restarts. On startup,
background workers restore persisted values and attempt a fresh collection.
If the database is unavailable, collection for the affected machine is deferred;
any already loaded value remains visible as stale. No page waits for restoration.

## Install and enable

1. Apply `docker/ecpds/database/system-physical-location.sql` to the existing
   ECPDS database. It is additive and safe to rerun on MariaDB.
   **Do not run `ecpds.sql` against an existing installation**: that is the
   destructive initialization script.
2. Deploy matching Master and Monitor application builds, including SNMP4J
   `3.13.1` on the Master's runtime classpath. Maven copies it into the shared
   runtime libraries during packaging. ECaccess-J keeps it in `ext/`, and its Ant
   `deploy` target copies it to the Master's RPM runtime libraries; the existing
   RPM JAR wildcard includes it. No new Mover RMI method is required.
3. Configure the Master as below, create its protected credential files, allow
   UDP traffic from the Master to each selected SNMP agent, and restart the Master.

The feature is **disabled by default**. All configuration and credentials are read
at Master startup; changing them, rotating credentials or disabling collection
requires restarting the Master. Invalid configuration disables the collector
without preventing ordinary Master startup; a generic error is logged without
the configuration values or credential paths.

## Master configuration

Use the protected local `ecmwf.properties` override described in
[Master configuration](../configuration/master.md#file-location-and-configuration-layers).
These are example identifiers and documentation-only management addresses:

```ini
[PhysicalLocation]
enabled=yes
interval=30m
staleAfter=24h
timeout=2s
retries=1
concurrency=4
maxTargets=256

[PhysicalLocationSystems]
master=machine-a
mover.MOVER_1=machine-a
monitor.MONITOR_1=machine-b
database.db.internal:3306=machine-c

[PhysicalLocationMachine.machine-a]
address=192.0.2.10
port=161
profile=site

[PhysicalLocationMachine.machine-b]
address=192.0.2.11
profile=site

[PhysicalLocationMachine.machine-c]
address=192.0.2.12
profile=site

[PhysicalLocationProfile.site]
version=v3
username=location-reader
authProtocol=SHA256
authPasswordFile=/etc/ecpds/master/snmp-auth.secret
privacyPasswordFile=/etc/ecpds/master/snmp-privacy.secret
```

Mappings are **exact and case-sensitive**:

| Key | Identity |
|---|---|
| `master` | This Master deployment |
| `mover.NAME` | Data Mover's configured `TRANSFER_SERVER` name |
| `monitor.ROOT` | Monitor's registered `[Login] root` identity, not its nickname |
| `database.HOST:PORT` | Individual host as reported by topology and configured JDBC port (`3306` when omitted) |

Use a distinct `machine-*` identifier for each actual managed machine. Several
components may reference the same identifier; each machine has one persisted row.
Identifiers/profile names must match `[A-Za-z0-9][A-Za-z0-9._-]{0,127}`.
Only machines referenced by a system mapping are eligible for collection.
Do not use TransferGroups as a proxy for physical machines.

Management addresses must be **literal IPv4 or IPv6 unicast addresses**. Hostnames,
unspecified, multicast and link-local/scoped addresses are rejected. This avoids
unbounded DNS resolution and accidental service-alias/VIP/container targeting.
An SNMP agent in a container describes that agent's configured location; it does
not reveal the container host automatically. Likewise, do not map a database VIP
or managed cloud database to a physical server unless that association is meaningful
and maintained by your infrastructure administrators.

SNMPv3 uses **authPriv**, **SHA256** (default) or **SHA512** authentication and
**AES128** privacy, without automatic downgrade. Optional `context` selects a
specific SNMPv3 context (up to 32 UTF-8 bytes). Each poll has an isolated session,
so different agents may use the same username with different credentials.
Passwords must contain at least eight UTF-8 bytes.

Where operationally necessary, explicitly select SNMPv2c:

```ini
[PhysicalLocationProfile.legacy]
version=v2c
communityPasswordFile=/etc/ecpds/master/snmp-community.secret
```

SNMPv2c community strings travel in clear text. Restrict their use to approved
management networks and read-only access. SNMPv1 is not supported.

Credential files contain only the credential, optionally ending in one LF/CRLF.
Other control characters and blank files are rejected; the maximum file size is
4096 bytes. Restrict permissions, for example `0600`, and ownership to the Master
service account; do not commit these files or put credentials in command lines.
Mount them read-only where appropriate. Ordinary configuration contains only
their paths, not their contents. Avoid enabling third-party SNMP debug/packet logs
in production.

| Option | Default | Supported range |
|---|---|---|
| `interval` | `30m` | `1m`–`7d` |
| `staleAfter` | `24h` | At least `interval`, at most `30d` |
| `timeout` | `2s` | `100ms`–`10s`, per transmission |
| `retries` | `1` | `0`–`2` retransmissions |
| `concurrency` | `4` | `1`–`16` outstanding machine polls |
| `maxTargets` | `256` | `1`–`1024` unique machines; at most eight times this many component mappings |
| Machine `port` | `161` | `1`–`65535` |

Workers have no unbounded task queue. A machine cannot have overlapping polls.
Failures back off exponentially to 16 times the normal interval, capped at
24 hours (or the configured interval when that is already longer).
Other machines continue independently. SNMPv3 engine discovery may add protocol
exchanges; no topology request waits for them.

## Host-side setup and network access

Ask the system administrator to configure Net-SNMP's `sysLocation`, for example:

```text
sysLocation Bonn, Hall 2, Rack 14
```

Provision an SNMPv3 user with authentication/privacy matching the configured
profile and grant **read-only access**, preferably limited to `sysLocation.0`.
Follow your distribution's Net-SNMP instructions for secure user provisioning;
setting `sysLocation` alone does not grant access or create a user.
Reload/restart `snmpd` as required by that distribution.

Allow **UDP from the Master to the agent's configured port** (normally 161), plus
the response traffic back to the Master's ephemeral UDP source port. This does
not use the Monitor/Mover RMI port, SNMP traps, or UDP/162. Containers and firewalls
must expose the correct management endpoint. Agents reachable only from a Mover
are not supported by this centralized collector; no RMI-to-SNMP relay is created.

## Display and failure semantics

Available locations appear directly on topology component boxes, shortened to
keep the graph readable. Click/tap for full text, status and verification/attempt
times. Database-cluster details show each mapped endpoint separately; the single
database box uses the first endpoint's location, not a claim that the entire cluster
is co-located. An open details panel follows topology refreshes without resetting
saved graph positions. **Refresh now** refreshes cached topology, not SNMP polling.
The Data Mover details page uses the same Master cache when loaded.

The display is plain text, never HTML or Markdown. UTF-8 is decoded strictly;
CR/LF/tab are replaced by spaces, leading/trailing whitespace is stripped, other
control characters and values exceeding 1024 Unicode characters are rejected.
Long values wrap in the details panel, including on phones. Disabled/unmapped
components have no added field or unavailable-state clutter.

| Status | Meaning |
|---|---|
| Verified | Non-empty value successfully retrieved |
| Not configured | Agent successfully returned an empty value; any previous location is cleared |
| Object unavailable | Agent returned `noSuchObject`/`noSuchInstance` |
| Timed out | No usable response before the request timeout/retry budget |
| Collection failed | Protocol/access/report error, malformed value or local transport failure |
| Awaiting collection | Configured, but background restoration/collection has not completed |
| Database unavailable | Latest persistence/restoration failed; an already cached value is retained |

Failed collection retains last-known information only for the same target identity.
A failed poll or an expired `staleAfter` age marks the value **Stale**.
Last verified includes successful empty responses; last attempt does not imply
success. Some authentication/access failures are silently discarded by agents,
so a timeout is not proof of network failure.

Changing the configured management address, port or context invalidates the old
target's value immediately on restart, even before a new poll succeeds. Credential
rotation does not. Older/duplicate poll timestamps cannot overwrite newer records.
Disabling collection hides the metadata but retains database rows. Removed machine
rows are not automatically purged; obsolete rows can be removed through normal
database administration when no longer needed. Use deployment-unique machine
identifiers if independent Masters share a database.

## Operations and checks

The Master's `PhysicalLocationScheduler` MBean exposes `Activity` and numeric
`TargetCount`, `ActivePollCount`, `PollCount`, `SuccessCount`, `FailureCount`,
`TimeoutCount`, `PersistenceFailureCount`, cumulative `PollDurationMillis` and
`StaleCount` attributes through the existing JMX/Hawtio tools. Counters reset on
restart; persisted locations do not. Successful empty responses count as successes.
Logs identify the configured machine and result, not returned location text,
credentials or raw agent errors. Missing schema/database warnings are separate
from SNMP timeout warnings.

After enabling, verify that mapped nodes show the expected rack text, that the
Data Mover Connection card agrees, and that stopping an agent produces stale
last-known information without affecting other systems. Test this in a development
deployment rather than disrupting a production SNMP agent.
