# Master: ecmwf.properties

The Master's `ecmwf.properties` controls scheduling, database access, public
services, operational events and shared infrastructure. This reference includes
options read by the implementation even when they are absent from the shipped
file.

Other services have separate references: [Monitor](monitor.md) and
[Data Mover](mover.md).

## Identity extensions

`[AuthenticationExtension] class` and `[UserDirectoryExtension] class` select
optional, trusted Master identity providers. Both default to blank (disabled).
Provider-specific options belong in the same section. Configuration is read at
startup and requires a restart; a configured provider that cannot start fails
Master startup. See [Identity extensions](../concepts/plugin-infrastructure.md#identity-extensions)
for installation, lifecycle and authentication precedence.

`[InteractiveLoginExtension] class` independently enables browser-based Monitor
login. It defaults to blank. `[InteractiveLoginMapping]` maps local WebUser IDs
to JSON objects containing the verified external `issuer` and `subject`.
See [OpenID Connect login](../administration/openid-connect.md) for the bundled
provider, full configuration and account requirements.

## File location and configuration layers

The source template is
[`etc/master/conf/ecmwf.properties`](https://github.com/ecmwf/open-ecpds/blob/master/etc/master/conf/ecmwf.properties).
RPM installations place it in `/usr/local/ecpds/master/conf/ecmwf.properties`.
The launcher selects the file with the `ecmwf.properties` JVM property; container
mounts can change where its contents are stored.

Use `/etc/ecpds/master.cnf` or the container/service environment for launcher
settings such as addresses, ports, database connection details and secrets.
The launcher translates supported variables into the JVM properties referenced
by the template. Arbitrary environment variables do **not** automatically become
`ecmwf.properties` options.

The final directive:

```ini
#include "${master.etc}/ecmwf.properties"
```

loads a local override file from the launcher's `master.etc` directory if it
exists and is readable. Despite the leading `#`, **`#include` is an active
configuration directive**, not an ordinary comment. It must start at the
beginning of the line. The supplied service layout uses a separate directory for
this file, normally `/etc/ecpds/master`; do not point an include back to the
file containing it. An unreadable or missing include produces a warning.

An override contains only the sections and values to change, for example:

```ini
[TransferScheduler]
queueSize=80

[StoragePolicy]
enabled=no
interval=1m
```

Restart the affected service after changing configuration unless a feature's
documentation explicitly describes runtime reload support. Many values are
cached when classes or services initialize. `#reload "Group"` permits the
configuration loader to reload that group, but does not make every consumer
apply a changed value dynamically.

## Syntax and defaults

| Syntax | Meaning |
|--------|---------|
| `[Group]` followed by `key=value` | A case-sensitive section and option name. |
| `# text` | A comment, except recognized directives such as `#include`, `#reload` and `#property`. |
| `${master.data}` | A JVM system property supplied by the launcher. |
| `${Login[hostName]}` | Another option in this configuration. |
| `${fully.qualified.Class.method()}` | A supported static-method substitution used by the templates. |
| `yes` / `no`, `true` / `false` | Boolean values. |
| `30s`, `5m`, `24h`, `7d` | Explicit durations for duration-reading options; ISO-8601 durations are also accepted. |

!!! warning "Duration units depend on the reader"
    Options read as durations treat a bare configured number as **hours**, not
    milliseconds. Plain integer options may instead use milliseconds, seconds,
    minutes, bytes or counts. For example, `[Monitor] delay=120` means 120 hours,
    while `[SocketConfig] connectTimeOut=30000` means 30 seconds.
    Duration defaults passed by the Java implementation are milliseconds;
    the tables below label these separately from configured values.

The catalogue distinguishes **code fallback** (what a particular reader uses
when the option is absent) from **shipped value** (what the template explicitly
sets). They are not necessarily equal. Removing a setting can therefore change
behaviour. A dash in the shipped column means it is not assigned in the template.
`null` means that reader supplies no value, not that the option is safe to omit.
An empty list and an empty string are shown explicitly.

Context-dependent Java expressions are retained rather than replaced by a
guessed constant. Where readers use different fallbacks, they appear on separate
rows with links to their source. File-content options interpret the configured
value as a **path**, but their fallback is **content**. Shipped credentials are
not reproduced here.

Entries marked **template entry** have no direct literal reader in the reviewed
sources. They may be consumed through substitutions or retained for
compatibility; their presence alone is not evidence of an active runtime
switch. Named-socket and open-map readers are documented separately.

## Operational guidance

- Enable native log export, journaling and tracing independently; see
  [Event Logging](../event-logging/overview.md). Only the Master needs Collector
  connectivity and OpenTelemetry connection settings.
- `[StoragePolicy] enabled=yes` can enforce existing policies immediately.
  Read [Storage Lifecycle Policies](../administration/storage-lifecycle-policies.md)
  before enabling it.
- Scheduler switches in `[Server]` enable workers; `[Scheduler]` configures
  their cadence, concurrency and watchdogs. Increasing concurrency also increases
  database, memory and Mover load.
- Repository `*Size` settings select **storage-worker thread counts**, not
  queue capacities or records per batch. Do not use them as retention limits.
- Keep RMI and callback interfaces on trusted networks. Public service bind
  addresses and advertised addresses serve different purposes.
- Protect credentials and keystores with restrictive file permissions.
  `trustAllCerts`, `checkCertificates=no` and plaintext proxy connections
  weaken transport protection; shipped values are not security recommendations.

## Option catalogue

The following sections cover literal property readers, shared infrastructure,
named socket overrides and dynamic option families in the OpenECPDS sources.
Shared components only use their options when instantiated; listing a component
does not enable it. Host, destination, user and transfer-module property families
are linked below rather than conflated with service switches.

<!-- BEGIN PROPERTY CATALOGUE -->

### [API]

Template-defined configuration; see the reader links for its consuming component.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `api` | No direct literal reader; template entry | `Protected value/reference` | [Template:129](https://github.com/ecmwf/open-ecpds/blob/master/etc/master/conf/ecmwf.properties#L129) |

### [CallBack]

Exported callback endpoint. `serverFactory` and `clientFactory` are Java socket-factory classes. This section also accepts the named socket overrides described below.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `clientFactory` | `null` (no reader fallback) | `ecmwf.common.rmi.RMIClientSocketFactory` | [CallBackObject:150](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/callback/CallBackObject.java#L150) |
| `keepAlive` | Inherits `[SocketConfig] keepAlive` | `yes` | [Template:261](https://github.com/ecmwf/open-ecpds/blob/master/etc/master/conf/ecmwf.properties#L261) |
| `listenAddress` | Inherits `[SocketConfig] listenAddress` | `${Login[internalAddress]}` | [Template:256](https://github.com/ecmwf/open-ecpds/blob/master/etc/master/conf/ecmwf.properties#L256) |
| `port` | `9000` | `${Ports[callBack]}` | [CallBackObject:54](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/callback/CallBackObject.java#L54) |
| `publicAddress` | Inherits `[SocketConfig] publicAddress` | `${Login[internalAddress]}` | [Template:257](https://github.com/ecmwf/open-ecpds/blob/master/etc/master/conf/ecmwf.properties#L257) |
| `serverFactory` | `null` (no reader fallback) | `ecmwf.common.rmi.RMIServerSocketFactory` | [CallBackObject:116](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/callback/CallBackObject.java#L116) |

### [Command]

External helper commands. `ecauth` overrides the token-generation executable; `sh` selects the ECios shell command.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `ecauth` | `Cnf.at("Server", "ecauth", Cnf.at("ECauthSession", "cmd"))` | - | [ECauthTokenGenerator:46](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ecaccess/ECauthTokenGenerator.java#L46) |
| `sh` | `null` (no reader fallback) | - | [ForkECios:169](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ForkECios.java#L169) |

### [DataBase]

Database broker and JDBC configuration. `initSleep` is seconds; `purge` is hours and `maxTransferLife` is days. `fetchSize` is rows; retry delays are milliseconds. Broker-specific flags only apply to that broker. The shipped Hibernate broker deliberately differs from the generic broker fallback.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `alias` | `null` (no reader fallback) | `${database.alias}` | [ECaccessServer:165](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ecaccess/ECaccessServer.java#L165) |
| `alias` | `System.getProperty("database.alias", "")` | `${database.alias}` | [ManagementImpl:3326](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/ManagementImpl.java#L3326) |
| `backup` | No direct literal reader; template entry | `${master.conf}/backup.sql` | [Template:228](https://github.com/ecmwf/open-ecpds/blob/master/etc/master/conf/ecmwf.properties#L228) |
| `brokerFactory` | `ecmwf.common.database.BrokerFactoryOJB` | `ecmwf.common.database.BrokerFactoryHibernate` | [ECaccessServer:163](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ecaccess/ECaccessServer.java#L163) |
| `debugCache` | `false` | - | [BrokerHibernate:110](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/database/BrokerHibernate.java#L110) |
| `debugFrequency` | `10000` | - | [BrokerHibernate:116](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/database/BrokerHibernate.java#L116) |
| `debugPool` | `false` | - | [BrokerHibernate:113](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/database/BrokerHibernate.java#L113) |
| `debugSql` | `false` | `no` | [ECaccessServer:167](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ecaccess/ECaccessServer.java#L167) |
| `driver` | `null` (no reader fallback) | `org.mariadb.jdbc.Driver` | [ECaccessServer:164](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ecaccess/ECaccessServer.java#L164) |
| `emulateCalcFoundRows` | `"hsqldb".equalsIgnoreCase(Cnf.at("DataBase", "subProtocol", ""))` | - | [DBResultSet:55](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/database/DBResultSet.java#L55) |
| `enableStreamingFetch` | `true` | `no` | [BrokerHibernate:95](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/database/BrokerHibernate.java#L95) |
| `fetchSize` | `500` | - | [BrokerHibernate:92](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/database/BrokerHibernate.java#L92) |
| `initCount` | `1` | - | [ECaccessServer:146](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ecaccess/ECaccessServer.java#L146) |
| `initSleep` | `30` | - | [ECaccessServer:147](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ecaccess/ECaccessServer.java#L147) |
| `logEvents` | `true` | `yes` | [ECaccessServer:167](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ecaccess/ECaccessServer.java#L167) |
| `maxIdentityCount` | `7` | `7` | [MasterServer:2049](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L2049) |
| `maxTransferLife` | `210` | `210` | [MasterServer:2050](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L2050) |
| `password` | `null` (no reader fallback) | `Protected value/reference` | [ECaccessServer:165](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ecaccess/ECaccessServer.java#L165) |
| `protocol` | `null` (no reader fallback) | `jdbc` | [ECaccessServer:164](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ecaccess/ECaccessServer.java#L164) |
| `purge` | `360` | `360` | [ECaccessServer:215](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ecaccess/ECaccessServer.java#L215) |
| `repository` | `null` (no reader fallback) | `${master.conf}/sql` | [ECaccessServer:166](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ecaccess/ECaccessServer.java#L166) |
| `retryTransactionCount` | `2` | - | [BrokerHibernate:104](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/database/BrokerHibernate.java#L104) |
| `retryTransactionDelay` | `1000` | - | [BrokerHibernate:107](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/database/BrokerHibernate.java#L107) |
| `server` | `null` (no reader fallback) | - | [ECaccessServer:166](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ecaccess/ECaccessServer.java#L166) |
| `setAutoCommit` | `false` | - | [BrokerHibernate:98](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/database/BrokerHibernate.java#L98) |
| `setReadOnly` | `false` | - | [BrokerHibernate:101](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/database/BrokerHibernate.java#L101) |
| `subProtocol` | Empty string | `mariadb` | [DBResultSet:56](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/database/DBResultSet.java#L56) |
| `subProtocol` | `null` (no reader fallback) | `mariadb` | [ECaccessServer:164](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ecaccess/ECaccessServer.java#L164) |
| `user` | `null` (no reader fallback) | `master` | [ECaccessServer:165](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ecaccess/ECaccessServer.java#L165) |

### [ECaccessScheduler]

Shared scheduler's minimum wait duration.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `minimumWait` | `1000` ms (duration fallback) | - | [ECaccessScheduler:59](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ecaccess/ECaccessScheduler.java#L59) |

### [ECauthModule]

ECauth connection and host-list cache. Durations control connection timeout, refresh polling, refresh frequency and cached-entry lifetime. Credentials here identify the ECauth service account.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `connectionTimeout` | `10000` ms (duration fallback) | - | [Tools:163](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/security/Tools.java#L163); [Tools:253](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/security/Tools.java#L253); [Tools:359](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/security/Tools.java#L359) |

### [ECauthSession]

Legacy fallback command for ECauth token generation; `[Command] ecauth` and `[Server] ecauth` take precedence.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `cmd` | `null` (no reader fallback) | - | [ECauthTokenGenerator:47](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ecaccess/ECauthTokenGenerator.java#L47) |

### [ECios]

ECios helper start/kill socket timeouts, in milliseconds. Also accepts named socket overrides.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `killSoTimeout` | `15000` | - | [ForkECios:258](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ForkECios.java#L258) |
| `startSoTimeout` | `15000` | - | [ForkECios:179](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ForkECios.java#L179) |

### [ECpdsBase]

Database-facing cache refresh pause and expiry durations.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `cachePause` | `5000` ms (duration fallback) | - | [ECpdsBase:71](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/database/ECpdsBase.java#L71) |
| `cacheTimeout` | `60000` ms (duration fallback) | - | [ECpdsBase:68](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/database/ECpdsBase.java#L68) |

### [ECpdsClient]

ECpds socket-client read timeout in milliseconds. This is distinct from the server-side plugin timeout.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `soTimeOut` | `60000` | - | [ECpdsClient:95](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/ecpds/ECpdsClient.java#L95) |

### [ECpdsPlugin]

Master ECpds listener and request-processing behaviour. `checkPort` requires a privileged client source port when enabled. `SocketSoTimeout` applies to accepted request sockets in milliseconds. `resetDataTransferSchedulesByGroup` resets schedules on group completion. The section also accepts named socket options.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `SocketSoTimeout` | `60000` | - | [ECpdsPlugin:608](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/ecpds/ECpdsPlugin.java#L608) |
| `checkPort` | `true` | `no` | [ECpdsPlugin:764](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/ecpds/ECpdsPlugin.java#L764) |
| `defaultApplication` | `pgen` | - | [ECpdsPlugin:154](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/ecpds/ECpdsPlugin.java#L154) |
| `failOnDestinationNotFound` | `true` | - | [MasterServer:14111](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L14111); [ECpdsPlugin:1966](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/ecpds/ECpdsPlugin.java#L1966); [ECpdsPlugin:2088](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/ecpds/ECpdsPlugin.java#L2088) |
| `forceStandby` | `false` | - | [ECpdsPlugin:136](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/ecpds/ECpdsPlugin.java#L136) |
| `host` | `Cnf.at("Login", "hostName", "localhost")` | - | [ECpdsClient:83](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/ecpds/ECpdsClient.java#L83) |
| `ignoreMissingScheduleTime` | `true` | - | [ECpdsPlugin:1654](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/ecpds/ECpdsPlugin.java#L1654) |
| `listenAddress` | Inherits `[SocketConfig] listenAddress` | `${Login[externalAddress]}` | [Template:318](https://github.com/ecmwf/open-ecpds/blob/master/etc/master/conf/ecmwf.properties#L318) |
| `maxSourceNameLength` | `255` | - | [ECpdsPlugin:151](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/ecpds/ECpdsPlugin.java#L151) |
| `port` | `Cnf.at("Ports", "ecpds", 6640)` | `${Ports[ecpds]}` | [ECpdsClient:86](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/ecpds/ECpdsClient.java#L86) |
| `port` | `null` (no reader fallback) | `${Ports[ecpds]}` | [ECpdsPlugin:466](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/ecpds/ECpdsPlugin.java#L466) |
| `resetDataTransferSchedulesByGroup` | `false` | `yes` | [ECpdsPlugin:157](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/ecpds/ECpdsPlugin.java#L157) |
| `soTimeOut` | Inherits `[SocketConfig] soTimeOut` | `600000` | [Template:320](https://github.com/ecmwf/open-ecpds/blob/master/etc/master/conf/ecmwf.properties#L320) |
| `transferGroup` | `null` (no reader fallback) | - | [ECpdsPlugin:293](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/ecpds/ECpdsPlugin.java#L293) |
| `waitForInitialWait` | `25000` ms (duration fallback) | - | [ECpdsPlugin:147](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/ecpds/ECpdsPlugin.java#L147) |
| `waitForRefreshInterval` | `15000` ms (duration fallback) | - | [ECpdsPlugin:143](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/ecpds/ECpdsPlugin.java#L143) |
| `waitForReloadInterval` | `5000` ms (duration fallback) | - | [ECpdsPlugin:139](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/ecpds/ECpdsPlugin.java#L139) |

### [ECproxyPlugin]

Mover ECproxy data listener and repository. Explicit TCP options override the accepted socket's existing values. Buffer sizes are bytes; socket timeouts are milliseconds. Also accepts named socket settings.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `repository` | `/tmp` | - | [RegularFile:102](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/RegularFile.java#L102) |

### [ExecModule]

Defaults for the executable transfer module. `script` runs after file reception; `dir` falls back to the transfer directory. `synchronous` waits for command completion, `returnCode` is its accepted exit status, and `usePipe` supplies arguments on standard input. `mkdirs` creates parent directories and `usetmp` stages reception in a temporary file. Empty prefix and suffix select an effective `.tmp` suffix.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `args` | Empty string | - | [ExecModule:121](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ectrans/module/ExecModule.java#L121) |
| `dir` | `dir` | - | [ExecModule:117](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ectrans/module/ExecModule.java#L117) |
| `mkdirs` | `true` | - | [ExecModule:122](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ectrans/module/ExecModule.java#L122) |
| `prefix` | Empty string | - | [ExecModule:124](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ectrans/module/ExecModule.java#L124) |
| `returnCode` | `0` | - | [ExecModule:120](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ectrans/module/ExecModule.java#L120) |
| `script` | `System.getProperty("ecmwf.dir", "../..") + "/gateway/bin/ectrans"` | - | [ExecModule:112](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ectrans/module/ExecModule.java#L112) |
| `suffix` | Empty string | - | [ExecModule:125](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ectrans/module/ExecModule.java#L125) |
| `synchronous` | `true` | - | [ExecModule:118](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ectrans/module/ExecModule.java#L118) |
| `usePipe` | `false` | - | [ExecModule:119](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ectrans/module/ExecModule.java#L119) |
| `usetmp` | `true` | - | [ExecModule:123](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ectrans/module/ExecModule.java#L123) |

### [ExecutorManager]

Opt-in virtual-thread execution for the shared executor. This is not a general scheduler concurrency limit.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `allowVirtualThread` | `false` | - | [ExecutorManager:79](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ExecutorManager.java#L79) |

### [FTPActiveSocket]

FTP server active-data sockets. Accepts every named socket option below, in addition to these direct readers.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `reuseAddress` | `true` | `yes` | [DataSocket:165](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ftpd/DataSocket.java#L165) |
| `soTimeOut` | Inherits `[SocketConfig] soTimeOut` | `7200000` | [Template:364](https://github.com/ecmwf/open-ecpds/blob/master/etc/master/conf/ecmwf.properties#L364) |

### [FTPPassiveSocket]

FTP server passive-data sockets. `boundAddress` controls the address used for passive binding. Also accepts named socket settings, including local-port ranges.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `backLog` | `1` | - | [DataSocket:80](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ftpd/DataSocket.java#L80) |
| `boundAddress` | `boundAddress` | - | [DataSocket:99](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ftpd/DataSocket.java#L99) |
| `maxPrivilegedPort` | Inherits `[SocketConfig] maxPrivilegedPort` | `60000` | [Template:359](https://github.com/ecmwf/open-ecpds/blob/master/etc/master/conf/ecmwf.properties#L359) |
| `minPrivilegedPort` | Inherits `[SocketConfig] minPrivilegedPort` | `50000` | [Template:358](https://github.com/ecmwf/open-ecpds/blob/master/etc/master/conf/ecmwf.properties#L358) |
| `privilegedLocalPort` | Inherits `[SocketConfig] privilegedLocalPort` | `yes` | [Template:357](https://github.com/ecmwf/open-ecpds/blob/master/etc/master/conf/ecmwf.properties#L357) |
| `soTimeOut` | Inherits `[SocketConfig] soTimeOut` | `300000` | [Template:360](https://github.com/ecmwf/open-ecpds/blob/master/etc/master/conf/ecmwf.properties#L360) |

### [Filter]

Streaming compression/decompression command definitions. The four `fly.*` keys are read by the stream manager. The template also retains `pre.out.*` definitions, but the current source has no direct reader for those entries.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `fly.in.lbzip2` | Empty command list | - | [StreamManagerImp:234](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/StreamManagerImp.java#L234) |
| `fly.in.zstd` | Empty command list | - | [StreamManagerImp:242](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/StreamManagerImp.java#L242) |
| `fly.out.lbzip2` | Empty command list | - | [StreamManagerImp:136](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/StreamManagerImp.java#L136) |
| `fly.out.zstd` | Empty command list | - | [StreamManagerImp:144](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/StreamManagerImp.java#L144) |

### [FtpDomainList]

Virtual FTP/SFTP domain mappings; arbitrary domain names are supported. `*` is the fallback. Domain mappings select backing filesystem/domain implementations, not network listener addresses.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `*` | `null` (no reader fallback) | `*` | [DOMAIN:47](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ftpd/DOMAIN.java#L47); [MinaFileSystemAccessor:781](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ssh/MinaFileSystemAccessor.java#L781) |
| `attachments` | No mapping; uses `*` fallback | `attachments:` | [Template:347](https://github.com/ecmwf/open-ecpds/blob/master/etc/master/conf/ecmwf.properties#L347) |
| `countries` | No mapping; uses `*` fallback | `countries:` | [Template:349](https://github.com/ecmwf/open-ecpds/blob/master/etc/master/conf/ecmwf.properties#L349) |
| `data` | No mapping; uses `*` fallback | `data:` | [Template:351](https://github.com/ecmwf/open-ecpds/blob/master/etc/master/conf/ecmwf.properties#L351) |
| `destinations` | No mapping; uses `*` fallback | `destinations:` | [Template:348](https://github.com/ecmwf/open-ecpds/blob/master/etc/master/conf/ecmwf.properties#L348) |
| `hosts` | No mapping; uses `*` fallback | `hosts:` | [Template:350](https://github.com/ecmwf/open-ecpds/blob/master/etc/master/conf/ecmwf.properties#L350) |
| `types` | No mapping; uses `*` fallback | `types:` | [Template:352](https://github.com/ecmwf/open-ecpds/blob/master/etc/master/conf/ecmwf.properties#L352) |

### [FtpModuleList]

FTP extension-module lists. Keys select extensions; `*` supplies the default list. Values are comma-separated module definitions consumed by the FTP utility.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `*` | Empty list | - | [Util:217](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ftpd/Util.java#L217) |

### [FtpPlugin]

FTP control listener and behaviour. `welcome`, `browser` and `help` are file paths; `tail` is response text. `passive`, `rest` and `extended` affect FTP features. `hidden` is a list of hidden virtual domains. `alwaysCheckOnRETR` requests retrieval-time checks. The section also accepts named socket options.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `alwaysCheckOnRETR` | `false` | - | [RETR:165](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ftpd/RETR.java#L165) |
| `browser` | `browser.ftp` | `${master.conf}/ftp/browser.txt` | [PASS:165](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ftpd/PASS.java#L165) |
| `dataTimeOut` | No direct literal reader; template entry | `600000` | [Template:334](https://github.com/ecmwf/open-ecpds/blob/master/etc/master/conf/ecmwf.properties#L334) |
| `ext` | Empty list | Empty string | [Util:317](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ftpd/Util.java#L317) |
| `extended` | `false` | `no` | [FEAT:49](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ftpd/FEAT.java#L49) |
| `extended` | `true` | `no` | [FtpPlugin:284](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ftpd/FtpPlugin.java#L284) |
| `help` | `help.ftp` | `${master.conf}/ftp/help.txt` | [HELP:57](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ftpd/HELP.java#L57) |
| `hidden` | Empty list | Empty string | [DOMAIN:53](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ftpd/DOMAIN.java#L53) |
| `listenAddress` | Inherits `[SocketConfig] listenAddress` | `${Login[externalAddress]}` | [Template:331](https://github.com/ecmwf/open-ecpds/blob/master/etc/master/conf/ecmwf.properties#L331) |
| `passive` | `true` | `yes` | [CurrentContext:150](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ftpd/CurrentContext.java#L150) |
| `port` | `21` | `${Ports[ftp]}` | [DataSocket:129](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ftpd/DataSocket.java#L129) |
| `port` | `null` (no reader fallback) | `${Ports[ftp]}` | [FtpPlugin:129](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ftpd/FtpPlugin.java#L129) |
| `rest` | `true` | - | [FEAT:50](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ftpd/FEAT.java#L50); [FtpPlugin:288](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ftpd/FtpPlugin.java#L288) |
| `soTimeOut` | Inherits `[SocketConfig] soTimeOut` | `600000` | [Template:333](https://github.com/ecmwf/open-ecpds/blob/master/etc/master/conf/ecmwf.properties#L333) |
| `tail` | `Direct comments to ecaccess@ecmwf.int` | `FTP Admin Interface` | [ALIAS:86](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ftpd/ALIAS.java#L86); [CHMOD:90](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ftpd/CHMOD.java#L90); [DOMAIN:89](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ftpd/DOMAIN.java#L89); [UMASK:69](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ftpd/UMASK.java#L69) |
| `welcome` | `welcome.ftp` | `${master.conf}/ftp/welcome.txt` | [PASS:166](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ftpd/PASS.java#L166) |

### [GeoIP]

Template-defined configuration; see the reader links for its consuming component.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `forced.136.156` | No direct literal reader; template entry | `44.520320,11.360250,Europe,IT,Bologna` | [Template:369](https://github.com/ecmwf/open-ecpds/blob/master/etc/master/conf/ecmwf.properties#L369) |

### [IO]

Local repository I/O implementation. `byteBufferSizeInMb` sets the buffer size in MiB; pool idle age is seconds. Direct buffers and pooling only apply when the byte-buffer path is used. `channelForce`, `channelForceMetadata` and `fdSync` govern persistence/sync behaviour; changing them trades durability and performance.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `byteBufferDirect` | `true` | - | [RegularFile:76](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/RegularFile.java#L76) |
| `byteBufferPool` | `true` | - | [RegularFile:70](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/RegularFile.java#L70) |
| `byteBufferPoolMaxIdleSec` | `60` | - | [RegularFile:86](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/RegularFile.java#L86) |
| `byteBufferSizeInMb` | `1` | - | [RegularFile:79](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/RegularFile.java#L79) |
| `byteBufferUse` | `true` | - | [RegularFile:73](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/RegularFile.java#L73) |
| `channelForce` | `true` | - | [RegularFile:67](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/RegularFile.java#L67) |
| `channelForceMetadata` | `true` | - | [RegularFile:82](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/RegularFile.java#L82) |
| `fdSync` | `true` | - | [RegularFile:64](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/RegularFile.java#L64) |

### [InterruptibleRMIServerSideSocket]

Cleanup interval for server-side interruptible RMI socket state, in minutes.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `cleanupIntervalInMinutes` | `5` | - | [InterruptibleRMIServerSocket:355](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/rmi/interruptible/InterruptibleRMIServerSocket.java#L355) |

### [InterruptibleRMISocket]

Interruptible RMI stream-buffer pooling. Buffer size is bytes and buffer TTL is minutes.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `bufferSize` | `65536` | - | [InterruptibleRMISocket:160](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/rmi/interruptible/InterruptibleRMISocket.java#L160) |
| `bufferTTLInMinutes` | `5` | - | [InterruptibleRMISocket:164](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/rmi/interruptible/InterruptibleRMISocket.java#L164) |
| `usePooledBufferedStreams` | `true` | - | [InterruptibleRMISocket:65](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/rmi/interruptible/InterruptibleRMISocket.java#L65) |

### [Login]

Component identity, registration credential and addresses. `root` defaults to the hostname in normal service readers. Several utility readers use their own hostname/service fallback, so removing identity settings is not recommended. External/internal addresses are primarily consumed through template references and named socket sections.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `externalAddress` | No direct literal reader; template entry | `${Login[hostName]}` | [Template:19](https://github.com/ecmwf/open-ecpds/blob/master/etc/master/conf/ecmwf.properties#L19) |
| `hostName` | `InetAddress.getLocalHost().getHostName()` | `${ecmwf.common.ecaccess.ECaccessInit.getHostName()}` | [ECaccessScheduler:336](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ecaccess/ECaccessScheduler.java#L336); [PluginContainer:149](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/plugin/PluginContainer.java#L149) |
| `hostName` | `role` | `${ecmwf.common.ecaccess.ECaccessInit.getHostName()}` | [TransferTracing:105](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/telemetry/TransferTracing.java#L105) |
| `hostName` | `null` (no reader fallback) | `${ecmwf.common.ecaccess.ECaccessInit.getHostName()}` | [MasterServer:7603](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L7603) |
| `hostName` | `master` | `${ecmwf.common.ecaccess.ECaccessInit.getHostName()}` | [OperationalEvents:94](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/OperationalEvents.java#L94); [OperationalEvents:121](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/OperationalEvents.java#L121) |
| `hostName` | `localhost` | `${ecmwf.common.ecaccess.ECaccessInit.getHostName()}` | [ECpdsClient:83](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/ecpds/ECpdsClient.java#L83) |
| `internalAddress` | No direct literal reader; template entry | `${master.internalAddress}` | [Template:22](https://github.com/ecmwf/open-ecpds/blob/master/etc/master/conf/ecmwf.properties#L22) |
| `password` | `null` (no reader fallback) | `Protected value/reference` | [MasterServer:7623](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L7623) |
| `root` | `Cnf.at("Login", "hostName", role)` | - | [TransferTracing:105](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/telemetry/TransferTracing.java#L105) |
| `root` | `Cnf.at("Login", "hostName")` | - | [MasterServer:7603](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L7603) |
| `root` | `Cnf.at("Login", "hostName", "master")` | - | [OperationalEvents:94](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/OperationalEvents.java#L94); [OperationalEvents:121](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/OperationalEvents.java#L121) |
| `service` | `Cnf.at("Login", "hostName", InetAddress.getLocalHost().getHostName())` | - | [ECaccessScheduler:336](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ecaccess/ECaccessScheduler.java#L336); [PluginContainer:149](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/plugin/PluginContainer.java#L149) |

### [LoginManagement]

Failed-login throttling and ban tracking. `active` enables enforcement; `max` is consecutive failures. `timer`, `wait` and the login-path `delay` are seconds. The cleanup thread also reads `delay` as a raw millisecond polling interval with a different fallback; both readers are shown. `whitelist` bypasses these checks for matching entries.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `active` | `false` | - | [LoginManagement:52](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/security/LoginManagement.java#L52) |
| `delay` | `4` | - | [LoginManagement:75](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/security/LoginManagement.java#L75) |
| `delay` | `600000` | - | [LoginManagement:288](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/security/LoginManagement.java#L288) |
| `max` | `20` | - | [LoginManagement:70](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/security/LoginManagement.java#L70) |
| `timer` | `3600` | - | [LoginManagement:58](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/security/LoginManagement.java#L58) |
| `wait` | `300` | - | [LoginManagement:78](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/security/LoginManagement.java#L78) |
| `whitelist` | Empty list | - | [LoginManagement:64](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/security/LoginManagement.java#L64) |

### [Mail]

SMTP and optional mail-store configuration. `host`/`user` are the legacy shared defaults; `smtpHost` overrides the outgoing host. `starttls`, `tls` and `auth` affect transport/authentication. Store settings apply only to mail retrieval. Keep passwords in protected local configuration.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `auth` | `true` | - | [MailMBean:286](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ecaccess/MailMBean.java#L286) |
| `debug` | `false` | - | [MailMBean:280](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ecaccess/MailMBean.java#L280) |
| `defaultFrom` | `mailUser` | - | [MailMBean:445](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ecaccess/MailMBean.java#L445) |
| `folderName` | `INBOX` | - | [MailMBean:277](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ecaccess/MailMBean.java#L277) |
| `host` | Empty string | `${mail.host}` | [MailMBean:272](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ecaccess/MailMBean.java#L272) |
| `password` | Empty string | - | [MailMBean:276](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ecaccess/MailMBean.java#L276) |
| `port` | `25` | - | [MailMBean:284](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ecaccess/MailMBean.java#L284) |
| `smtpHost` | `defaultHost` | - | [MailMBean:273](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ecaccess/MailMBean.java#L273) |
| `starttls` | `true` | - | [MailMBean:285](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ecaccess/MailMBean.java#L285) |
| `storeHost` | Empty string | - | [MailMBean:274](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ecaccess/MailMBean.java#L274) |
| `storePort` | `-1` | - | [MailMBean:302](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ecaccess/MailMBean.java#L302) |
| `storeType` | Empty string | - | [MailMBean:279](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ecaccess/MailMBean.java#L279) |
| `tls` | `true` | - | [MailMBean:314](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ecaccess/MailMBean.java#L314) |
| `user` | Empty string | `${mail.user}` | [MailMBean:275](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ecaccess/MailMBean.java#L275) |

### [Master]

Whether Master registration updates a Mover's ECproxy address and port.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `updateECproxyAddressAndPort` | `false` | - | [MasterServer:4621](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L4621) |

### [MetaData]

Metadata reload duration and notification recipient when contact information is missing.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `missingContactsMail` | Empty string | `${metadata.missingContactsMail}` | [ManagementImpl:186](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/ManagementImpl.java#L186) |
| `reload` | `86400000` ms (duration fallback) | `24h` | [ManagementImpl:130](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/ManagementImpl.java#L130) |

### [Monitor]

External service-status integration, independent of the web monitoring UI. `activated` enables it, `debug` logs submissions and `dontSend` suppresses delivery. `delay` is a duration: the template's bare `120` means 120 hours, whereas the code fallback is five minutes.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `activated` | `false` | `no` | [MonitorManager:72](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/monitor/MonitorManager.java#L72) |
| `debug` | `false` | `no` | [MonitorManager:78](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/monitor/MonitorManager.java#L78) |
| `delay` | `300000` ms (duration fallback) | `120` | [MonitorManager:75](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/monitor/MonitorManager.java#L75) |
| `dontSend` | `false` | `no` | [MonitorManager:81](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/monitor/MonitorManager.java#L81) |

### [NetworkWeather]

Network Weather admission/control limits. Values are counts except `maxConfigurationBytes` and `maxControlResponseBytes`, which are bytes. The Monitor controls activation; these settings bound accepted work rather than enabling probes.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `maxCandidates` | `500` | `500` | [WeatherLimits:22](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/weather/WeatherLimits.java#L22) |
| `maxConfigurationBytes` | `49152` | `49152` | [WeatherLimits:23](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/weather/WeatherLimits.java#L23) |
| `maxControlResponseBytes` | `65536` | - | [WeatherLimits:35](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/weather/WeatherLimits.java#L35) |
| `maxHostChoices` | `5000` | `5000` | [WeatherLimits:22](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/weather/WeatherLimits.java#L22) |
| `maxProbePolicies` | `50` | `50` | [WeatherLimits:22](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/weather/WeatherLimits.java#L22) |
| `maxSelectedPaths` | `50` | `50` | [WeatherLimits:21](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/weather/WeatherLimits.java#L21) |
| `maxSources` | `20` | `20` | [WeatherLimits:21](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/weather/WeatherLimits.java#L21) |

### [OpenTelemetry]

Master-only log, metric and trace SDK defaults. Metric intervals/timeouts and log/trace schedule delays are milliseconds; queue/batch limits count records or spans, and sample ratio is 0-1. Standard JVM/environment SDK settings override these defaults. With log journaling enabled, log queue/batch values govern only in-memory fail-open delivery, not the database backlog.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `logsMaxExportBatchSize` | `512` | `512` | [OperationalEvents:104](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/OperationalEvents.java#L104) |
| `logsMaxQueueSize` | `2048` | `2048` | [OperationalEvents:103](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/OperationalEvents.java#L103) |
| `logsScheduleDelayMillis` | `1000` | `1000` | [OperationalEvents:105](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/OperationalEvents.java#L105) |
| `metricsEnabled` | `false` | `no` | [TransferMetrics:67](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/TransferMetrics.java#L67) |
| `metricsExportIntervalMillis` | `60000` | `60000` | [TransferMetrics:74](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/TransferMetrics.java#L74) |
| `metricsExportTimeoutMillis` | `30000` | `30000` | [TransferMetrics:75](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/TransferMetrics.java#L75) |
| `tracesEnabled` | `false` | `no` | [TransferTracing:84](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/telemetry/TransferTracing.java#L84) |
| `tracesMaxExportBatchSize` | `512` | `512` | [TransferTracing:95](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/telemetry/TransferTracing.java#L95) |
| `tracesMaxQueueSize` | `2048` | `2048` | [TransferTracing:94](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/telemetry/TransferTracing.java#L94) |
| `tracesSampleRatio` | `0.01` | `0.01` | [TransferTracing:93](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/telemetry/TransferTracing.java#L93) |
| `tracesScheduleDelayMillis` | `1000` | `1000` | [TransferTracing:96](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/telemetry/TransferTracing.java#L96) |

### [OperationalEvents]

Independent Splunk-file and native OpenTelemetry event outputs. Journaling persists events before OTLP delivery; install the journal schema before enabling it. `journalOwner` must remain stable and distinguish independent Masters.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `journalCriticalOldestAgeMillis` | `1800000` | `1800000` | [OperationalEvents](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/OperationalEvents.java) |
| `journalCriticalPendingCount` | `10000` | `10000` | [OperationalEvents](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/OperationalEvents.java) |
| `journalEnabled` | `false` | `no` | [OperationalEvents:88](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/OperationalEvents.java#L88) |
| `journalOwner` | `Cnf.at("Login", "root", Cnf.at("Login", "hostName", "master"))` | - | [OperationalEvents:93](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/OperationalEvents.java#L93) |
| `journalWarningOldestAgeMillis` | `300000` | `300000` | [OperationalEvents](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/OperationalEvents.java) |
| `journalWarningPendingCount` | `1000` | `1000` | [OperationalEvents](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/OperationalEvents.java) |
| `openTelemetryEnabled` | `false` | `no` | [OperationalEvents:85](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/OperationalEvents.java#L85) |
| `splunkEnabled` | `true` | `yes` | [OperationalEvents:84](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/OperationalEvents.java#L84) |

The warning and critical thresholds are positive values, and each warning value
must be lower than its corresponding critical value. They are loaded at Master
startup. Reaching either the pending-count or oldest-admission-age threshold
raises the corresponding alert on the journal page.

### [Operator]

Transfer-operation message template. `$destination[...]`, `$dataFile[...]` and related placeholders are expanded from transfer context.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `template` | `null` (no reader fallback) | `$destination[name]: $dataFile[metaTime]-$dataFile[metaStream]($dataFile[timeStep]) -> $dataTransfer[target]($dataFile[formatSize]) $system[message] on $transferMethod[name]://$host[login]@$host[host]($host[nickname];$host[networkCode]) $dataTransfer[comment]` | [TransferScheduler:3222](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/transfer/TransferScheduler.java#L3222) |

### [OpsViewManager]

OpsView publication endpoints, credentials, filters and TLS verification. URLs default relative to `url`; filter names differ by acquisition/dissemination/other roles. `trustAllCerts=true` skips certificate validation.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `acquisitionFilterName` | `ECPDS_Acquisition` | - | [OpsViewManager:120](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/opsview/OpsViewManager.java#L120) |
| `activated` | `true` | `${opsview.activated}` | [OpsViewManager:81](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/opsview/OpsViewManager.java#L81) |
| `disseminationFilterName` | `ECPDS_Dissemination` | - | [OpsViewManager:116](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/opsview/OpsViewManager.java#L116) |
| `filterName` | No direct literal reader; template entry | `ECPDS_Destinations` | [Template:303](https://github.com/ecmwf/open-ecpds/blob/master/etc/master/conf/ecmwf.properties#L303) |
| `options` | Empty string | - | [OpsViewManager:84](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/opsview/OpsViewManager.java#L84) |
| `otherFilterName` | `ECPDS_Other` | - | [OpsViewManager:124](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/opsview/OpsViewManager.java#L124) |
| `password` | Empty string | `Protected value/reference` | [OpsViewManager:113](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/opsview/OpsViewManager.java#L113) |
| `trustAllCerts` | `true` | `yes` | [OpsViewManager:146](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/opsview/OpsViewManager.java#L146); [OpsViewManager:224](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/opsview/OpsViewManager.java#L224) |
| `url` | `https://localhost/passive:1443` | - | [OpsViewManager:87](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/opsview/OpsViewManager.java#L87) |
| `urlDetail` | `URL + "/rest/detail"` | `https://${opsview.host}/rest/detail` | [OpsViewManager:107](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/opsview/OpsViewManager.java#L107) |
| `urlHost` | `URL + "/rest/config/host"` | `https://${opsview.host}/rest/config/host` | [OpsViewManager:93](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/opsview/OpsViewManager.java#L93) |
| `urlLogin` | `URL + "/rest/login"` | `https://${opsview.host}/rest/login` | [OpsViewManager:90](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/opsview/OpsViewManager.java#L90) |
| `urlNotes` | `URL + "/rest/notes/service"` | - | [OpsViewManager:101](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/opsview/OpsViewManager.java#L101) |
| `urlReload` | `URL + "/rest/reload"` | `https://${opsview.host}/rest/reload` | [OpsViewManager:104](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/opsview/OpsViewManager.java#L104) |
| `user` | Empty string | `grid-apiconfig` | [OpsViewManager:110](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/opsview/OpsViewManager.java#L110) |

### [Other]

Shared utilities. `buffer` is an I/O buffer size in bytes; `ticketWaitDuration` is a raw millisecond timeout. `ecauthTokenCache` enables token caching. `dontCheckIp` bypasses the provider's client-address check during subscription. `tmp` is retained in templates; the launcher separately sets the JVM temporary directory.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `buffer` | `65536` | `131072` | [CurrentContext:132](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ftpd/CurrentContext.java#L132); [ECpdsPlugin:3141](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/ecpds/ECpdsPlugin.java#L3141) |
| `dontCheckIp` | `false` | `yes` | [ECaccessProvider:292](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ecaccess/ECaccessProvider.java#L292) |
| `ecauthTokenCache` | `true` | - | [ECauthTokenManager:52](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ecaccess/ECauthTokenManager.java#L52) |
| `ticketWaitDuration` | `120000` | - | [AttachmentAccessImpl:348](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/AttachmentAccessImpl.java#L348) |
| `tmp` | No direct literal reader; template entry | `${master.tmp}` | [Template:375](https://github.com/ecmwf/open-ecpds/blob/master/etc/master/conf/ecmwf.properties#L375) |
| `trace` | `false` | - | [MonitorCall:50](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MonitorCall.java#L50) |

### [PluginList]

Template-defined configuration; see the reader links for its consuming component.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `ecpds` | No plugin definition | `ecmwf.ecpds.master.plugin.ecpds.ECpdsPlugin@${master.lib}/plugin/ecpds.jar,maxConnections=1000,inverseResolution=no` | [Template:37](https://github.com/ecmwf/open-ecpds/blob/master/etc/master/conf/ecmwf.properties#L37) |
| `ftp` | No plugin definition | `ecmwf.common.ftpd.FtpPlugin@${master.lib}/plugin/ftp.jar,maxConnections=50,inverseResolution=no` | [Template:38](https://github.com/ecmwf/open-ecpds/blob/master/etc/master/conf/ecmwf.properties#L38) |

### [Ports]

Template port substitutions. Most keys are only referenced by listener sections and therefore have no independent code default. `ecpds` also has a direct client-reader fallback.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `callBack` | No direct literal reader; template entry | `${port.callback}` | [Template:28](https://github.com/ecmwf/open-ecpds/blob/master/etc/master/conf/ecmwf.properties#L28) |
| `ecpds` | `6640` | `${port.ecpds}` | [ECpdsClient:86](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/ecpds/ECpdsClient.java#L86) |
| `ftp` | No direct literal reader; template entry | `${port.ftp}` | [Template:27](https://github.com/ecmwf/open-ecpds/blob/master/etc/master/conf/ecmwf.properties#L27) |

### [ProxySocket]

Proxy data/control socket settings. `plainText` chooses non-TLS transport and otherwise depends on the connection's addresses. `host`/`port` select the control endpoint; `dataHost`/`dataPort` can select the data endpoint separately. `cipherSuites` is the TLS suite list. Also accepts named socket options.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `cipherSuites` | Empty list | `SSL_RSA_WITH_NULL_MD5` | [ProxySocket:396](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ProxySocket.java#L396) |
| `cipherSuites` | `null` (no reader fallback) | `SSL_RSA_WITH_NULL_MD5` | [ProxySocket:400](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ProxySocket.java#L400) |
| `dataHost` | `Cnf.at("ProxySocket", "host", socketConfig.getPublicAddress())` | - | [ProxySocket:485](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ProxySocket.java#L485); [ProxySocket:567](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ProxySocket.java#L567) |
| `dataHost` | `null` (no reader fallback) | - | [ProxySocket:487](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ProxySocket.java#L487); [ProxySocket:569](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ProxySocket.java#L569) |
| `dataPort` | `Cnf.at("ProxySocket", "port", 646)` | - | [ProxySocket:486](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ProxySocket.java#L486); [ProxySocket:568](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ProxySocket.java#L568) |
| `host` | `localName` | `${Login[hostName]}` | [ProxySocket:156](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ProxySocket.java#L156) |
| `host` | `socketConfig.getPublicAddress()` | `${Login[hostName]}` | [ProxySocket:206](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ProxySocket.java#L206); [ProxySocket:485](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ProxySocket.java#L485); [ProxySocket:518](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ProxySocket.java#L518); [ProxySocket:567](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ProxySocket.java#L567); [ProxySocket:602](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ProxySocket.java#L602); [ProxySocket:679](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ProxySocket.java#L679); [ProxySocket:729](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ProxySocket.java#L729) |
| `plainText` | `localAddress.equals(definedAddress)` | - | [ProxySocket:163](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ProxySocket.java#L163) |
| `port` | `646` | - | [ProxySocket:207](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ProxySocket.java#L207); [ProxySocket:486](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ProxySocket.java#L486); [ProxySocket:519](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ProxySocket.java#L519); [ProxySocket:568](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ProxySocket.java#L568); [ProxySocket:603](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ProxySocket.java#L603); [ProxySocket:680](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ProxySocket.java#L680); [ProxySocket:730](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ProxySocket.java#L730) |

### [RMIClientSocketFactory]

Template-defined configuration; see the reader links for its consuming component.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `interruptible` | Inherits `[SocketConfig] interruptible` | `yes` | [Template:274](https://github.com/ecmwf/open-ecpds/blob/master/etc/master/conf/ecmwf.properties#L274) |
| `keepAlive` | Inherits `[SocketConfig] keepAlive` | `yes` | [Template:275](https://github.com/ecmwf/open-ecpds/blob/master/etc/master/conf/ecmwf.properties#L275) |
| `listenAddress` | Inherits `[SocketConfig] listenAddress` | `${Login[internalAddress]}` | [Template:272](https://github.com/ecmwf/open-ecpds/blob/master/etc/master/conf/ecmwf.properties#L272) |
| `publicAddress` | Inherits `[SocketConfig] publicAddress` | `${Login[internalAddress]}` | [Template:273](https://github.com/ecmwf/open-ecpds/blob/master/etc/master/conf/ecmwf.properties#L273) |

### [RMIServerSocketFactory]

Template-defined configuration; see the reader links for its consuming component.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `interruptible` | Inherits `[SocketConfig] interruptible` | `yes` | [Template:267](https://github.com/ecmwf/open-ecpds/blob/master/etc/master/conf/ecmwf.properties#L267) |
| `keepAlive` | Inherits `[SocketConfig] keepAlive` | `yes` | [Template:268](https://github.com/ecmwf/open-ecpds/blob/master/etc/master/conf/ecmwf.properties#L268) |
| `listenAddress` | Inherits `[SocketConfig] listenAddress` | `${Login[internalAddress]}` | [Template:265](https://github.com/ecmwf/open-ecpds/blob/master/etc/master/conf/ecmwf.properties#L265) |
| `publicAddress` | Inherits `[SocketConfig] publicAddress` | `${Login[internalAddress]}` | [Template:266](https://github.com/ecmwf/open-ecpds/blob/master/etc/master/conf/ecmwf.properties#L266) |

### [RequestParser]

Optional parser-backed ECpds ingestion. `parserDir` holds parser files; `requestSize` is parser worker count, `requestDelay` is milliseconds and `maxAuthorisedSize` bounds pending requests. `logRequests` controls request logging, not operational-event platform selection.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `clean` | `true` | - | [ECpdsPlugin:382](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/ecpds/ECpdsPlugin.java#L382) |
| `lastTimeStamp` | `-1` | - | [ECpdsPlugin:381](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/ecpds/ECpdsPlugin.java#L381) |
| `logRequests` | `false` | `yes` | [ECpdsPlugin:133](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/ecpds/ECpdsPlugin.java#L133) |
| `maxAuthorisedSize` | `1000` | - | [ECpdsParser:338](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/ecpds/ECpdsParser.java#L338) |
| `parserDir` | `null` (no reader fallback) | - | [ECpdsPlugin:379](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/ecpds/ECpdsPlugin.java#L379) |
| `ratio` | `1` | - | [ECpdsPlugin:381](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/ecpds/ECpdsPlugin.java#L381) |
| `requestDelay` | `1000` | - | [ECpdsParser:336](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/ecpds/ECpdsParser.java#L336) |
| `requestSize` | `25` | - | [ECpdsParser:335](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/ecpds/ECpdsParser.java#L335) |
| `useParser` | `false` | `no` | [ECpdsPlugin:380](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/ecpds/ECpdsPlugin.java#L380) |

### [ResourceTracker]

Shared runtime-resource diagnostics. `defaultDebugFrequency` counts resource-close events between debug reports; it is not a time interval.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `defaultDebugFrequency` | `10000` | - | [ResourceTracker:45](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ResourceTracker.java#L45) |
| `enabled` | `true` | - | [ResourceTracker:42](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ResourceTracker.java#L42) |

### [Scheduler]

Worker cadence, concurrency and watchdogs. `*JammedTimeout`, `maximumDuration*` and `timeOut*` read durations; a negative timeout leaves that execution limit unset. `*TimeRanges` accepts configured execution windows. `max*Threads` values are concurrency counts; `cacheRatio` scales prefetch. The legacy `purgeDataBase`/`updateECUsers` intervals are hours, with optional clock-time lists.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `acquisitionScheduler` | `1000` ms (duration fallback) | `1s` | [MasterServer:11417](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L11417) |
| `acquisitionSchedulerJammedTimeout` | `600000` ms (duration fallback) | `10m` | [MasterServer:11418](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L11418) |
| `acquisitionSchedulerTimeRanges` | Empty list | - | [MasterServer:11419](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L11419) |
| `backupScheduler` | `5000` ms (duration fallback) | `1m` | [MasterServer:10548](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L10548) |
| `backupSchedulerJammedTimeout` | `600000` ms (duration fallback) | `10m` | [MasterServer:10549](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L10549) |
| `backupSchedulerTimeRanges` | Empty list | - | [MasterServer:10550](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L10550) |
| `cacheRatio` | `40` | `80` | [MasterServer:13024](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L13024) |
| `dataTransferCheck` | `300000` ms (duration fallback) | `5m` | [MasterServer:9390](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L9390) |
| `dataTransferCheckJammedTimeout` | `600000` ms (duration fallback) | `30m` | [MasterServer:9391](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L9391) |
| `dataTransferCheckTimeRanges` | Empty list | - | [MasterServer:9392](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L9392) |
| `debug` | `false` | - | [MasterServer:10912](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L10912); [MasterServer:13230](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L13230) |
| `destinationThread` | `1000` ms (duration fallback) | `1s` | [TransferScheduler:3042](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/transfer/TransferScheduler.java#L3042) |
| `destinationThreadJammedTimeout` | `300000` ms (duration fallback) | `5m` | [TransferScheduler:3043](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/transfer/TransferScheduler.java#L3043) |
| `downloadScheduler` | `1000` ms (duration fallback) | `1s` | [MasterServer:13261](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L13261) |
| `downloadSchedulerJammedTimeout` | `600000` ms (duration fallback) | `10m` | [MasterServer:13262](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L13262) |
| `downloadSchedulerTimeRanges` | Empty list | - | [MasterServer:13263](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L13263) |
| `eventScheduler` | `5000` ms (duration fallback) | `1s` | [MasterServer:8205](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L8205) |
| `eventSchedulerJammedTimeout` | `600000` ms (duration fallback) | `10m` | [MasterServer:8206](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L8206) |
| `eventSchedulerTimeRanges` | Empty list | - | [MasterServer:8207](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L8207) |
| `eventScriptFile` | Empty string | `${master.etc}/event.js` | [MasterServer:796](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L796); [MasterServer:8375](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L8375) |
| `exportDataBase` | No direct literal reader; template entry | `0` | [Template:221](https://github.com/ecmwf/open-ecpds/blob/master/etc/master/conf/ecmwf.properties#L221) |
| `failOnMissingSourceFile` | `false` | - | [MasterServer:14222](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L14222) |
| `filterScheduler` | `5000` ms (duration fallback) | `1s` | [MasterServer:9450](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L9450) |
| `filterSchedulerJammedTimeout` | `300000` ms (duration fallback) | `5m` | [MasterServer:9451](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L9451) |
| `filterSchedulerTimeRanges` | Empty list | - | [MasterServer:9452](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L9452) |
| `hostCheckScheduler` | `300000` ms (duration fallback) | `5m` | [MasterServer:9132](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L9132) |
| `hostCheckSchedulerJammedTimeout` | `1800000` ms (duration fallback) | `30m` | [MasterServer:9133](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L9133) |
| `hostCheckSchedulerTimeRanges` | Empty list | - | [MasterServer:9134](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L9134) |
| `interruptSlowAcquisitionThread` | `true` | `yes` | [MasterServer:11395](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L11395) |
| `interruptSlowDownloadThread` | No direct literal reader; template entry | `yes` | [Template:191](https://github.com/ecmwf/open-ecpds/blob/master/etc/master/conf/ecmwf.properties#L191) |
| `maxAcquisitionThreads` | `100` | `140` | [MasterServer:11388](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L11388) |
| `maxBackupThreads` | `15` | `15` | [MasterServer:10529](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L10529) |
| `maxDownloadThreads` | `60` | `${retrieval.maxPoolSize}` | [MasterServer:13224](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L13224) |
| `maxEventThreads` | `200` | `50` | [MasterServer:8186](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L8186) |
| `maxFilterThreads` | `15` | `100` | [MasterServer:9431](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L9431) |
| `maxPresetPerDestination` | `60` | - | [MasterServer:13125](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L13125) |
| `maxProxyThreads` | `15` | `150` | [MasterServer:10909](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L10909) |
| `maxPurgeThreads` | `50` | `250` | [MasterServer:10245](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L10245) |
| `maxReplicateThreads` | `200` | `200` | [MasterServer:9818](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L9818) |
| `maxReplicateThreadsPerMover` | `10` | `15` | [MasterServer:9821](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L9821) |
| `maximumDurationAcquisitionThread` | `600000` ms (duration fallback) | `10m` | [MasterServer:11391](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L11391) |
| `maximumDurationDownloadThread` | No direct literal reader; template entry | `25m` | [Template:188](https://github.com/ecmwf/open-ecpds/blob/master/etc/master/conf/ecmwf.properties#L188) |
| `minimumDurationDownloadThread` | No direct literal reader; template entry | `5m` | [Template:189](https://github.com/ecmwf/open-ecpds/blob/master/etc/master/conf/ecmwf.properties#L189) |
| `minimumRateDownloadThread` | No direct literal reader; template entry | `2359296` | [Template:190](https://github.com/ecmwf/open-ecpds/blob/master/etc/master/conf/ecmwf.properties#L190) |
| `moverAvailabilityScheduler` | `60000` ms (duration fallback) | - | [MasterServer:9042](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L9042) |
| `moverAvailabilitySchedulerJammedTimeout` | `300000` ms (duration fallback) | - | [MasterServer:9044](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L9044) |
| `portalBytesScheduler` | `30000` ms (duration fallback) | - | [MasterServer:8950](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L8950) |
| `portalBytesSchedulerJammedTimeout` | `300000` ms (duration fallback) | - | [MasterServer:8951](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L8951) |
| `portalTrafficScheduler` | `60000` ms (duration fallback) | - | [MasterServer:8915](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L8915) |
| `portalTrafficSchedulerJammedTimeout` | `300000` ms (duration fallback) | - | [MasterServer:8916](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L8916) |
| `processChecksum` | `true` | - | [MasterServer:13233](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L13233) |
| `proxyScheduler` | `5000` ms (duration fallback) | - | [MasterServer:10931](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L10931) |
| `proxySchedulerJammedTimeout` | `600000` ms (duration fallback) | - | [MasterServer:10932](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L10932) |
| `proxySchedulerTimeRanges` | Empty list | - | [MasterServer:10933](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L10933) |
| `purgeDataBase` | `24` | `24` | [ECaccessServer:207](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ecaccess/ECaccessServer.java#L207) |
| `purgeDataBaseTime` | Empty list | `00:15:00` | [ECaccessServer:205](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ecaccess/ECaccessServer.java#L205) |
| `purgeScheduler` | `5000` ms (duration fallback) | `1s` | [MasterServer:10264](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L10264) |
| `purgeSchedulerJammedTimeout` | `600000` ms (duration fallback) | - | [MasterServer:10265](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L10265) |
| `purgeSchedulerTimeRanges` | Empty list | - | [MasterServer:10266](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L10266) |
| `removeExpiredDataTransfers` | No direct literal reader; template entry | `1` | [Template:215](https://github.com/ecmwf/open-ecpds/blob/master/etc/master/conf/ecmwf.properties#L215) |
| `removeExpiredRoots` | No direct literal reader; template entry | `1` | [Template:216](https://github.com/ecmwf/open-ecpds/blob/master/etc/master/conf/ecmwf.properties#L216) |
| `replicateScheduler` | `5000` ms (duration fallback) | `1s` | [MasterServer:9840](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L9840) |
| `replicateSchedulerJammedTimeout` | `600000` ms (duration fallback) | `10m` | [MasterServer:9841](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L9841) |
| `replicateSchedulerTimeRanges` | Empty list | - | [MasterServer:9842](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L9842) |
| `timeOutBackupThread` | `-1` ms (duration fallback) | `20m` | [MasterServer:10526](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L10526) |
| `timeOutEventThread` | `-1` ms (duration fallback) | `5m` | [MasterServer:8183](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L8183) |
| `timeOutFilterThread` | `-1` ms (duration fallback) | - | [MasterServer:9428](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L9428) |
| `timeOutProxyThread` | `-1` ms (duration fallback) | - | [MasterServer:10906](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L10906) |
| `timeOutPurgeThread` | `-1` ms (duration fallback) | - | [MasterServer:10242](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L10242) |
| `timeOutReplicateThread` | `-1` ms (duration fallback) | `20m` | [MasterServer:9815](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L9815) |
| `transferScheduler` | `2000` ms (duration fallback) | `1s` | [TransferScheduler:174](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/transfer/TransferScheduler.java#L174) |
| `transferSchedulerJammedTimeout` | `300000` ms (duration fallback) | `5m` | [TransferScheduler:175](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/transfer/TransferScheduler.java#L175) |
| `transferSchedulerTimeRanges` | Empty list | - | [TransferScheduler:176](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/transfer/TransferScheduler.java#L176) |
| `updateECUsers` | `24` | `24` | [ECaccessServer:203](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ecaccess/ECaccessServer.java#L203) |
| `updateECUsersTime` | Empty list | `00:15:00` | [ECaccessServer:201](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ecaccess/ECaccessServer.java#L201) |

### [ScriptManager]

JavaScript/Graal engine settings. `runningTimeout` and `longRunningTime` are milliseconds. `resourceLimits=0` leaves the statement limit disabled. Shared engines, compilation and virtual-thread execution are independent switches.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `allowExperimentalOptions` | `false` | - | [ScriptManager:96](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ScriptManager.java#L96) |
| `allowVirtualThread` | `false` | - | [ScriptManager:101](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ScriptManager.java#L101) |
| `engineCompilation` | `true` | - | [ScriptManager:98](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ScriptManager.java#L98) |
| `longRunningTime` | `1000` | - | [ScriptManager:79](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ScriptManager.java#L79) |
| `resourceLimits` | `0` | - | [ScriptManager:95](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ScriptManager.java#L95) |
| `runningTimeout` | `10000` | - | [ScriptManager:76](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ScriptManager.java#L76) |
| `useSharedEngine` | `true` | - | [ScriptManager:95](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ScriptManager.java#L95) |
| `warnVirtualThreadSupport` | `false` | - | [ScriptManager:97](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ScriptManager.java#L97) |

### [Security]

Authentication, secret protection, socket TLS and optional HTTPS store defaults. SSL store readers can fall back to JVM `javax.net.ssl.*` properties; HTTPS has its own fallback path. JCE settings protect stored secrets and are not HTTPS cipher settings. `checkCertificates=no` disables certificate checks. See the TLS and control-channel guides before changing these values.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `JCEAlias` | `SecretWriting.class.getName()` | - | [SecretWriting:96](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/security/SecretWriting.java#L96) |
| `JCECipherAlgorithm` | `DES/ECB/PKCS5Padding` | - | [SecretWriting:151](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/security/SecretWriting.java#L151) |
| `JCEKeyStore` | `null` (no reader fallback) | - | [SecretWriting:95](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/security/SecretWriting.java#L95) |
| `JCEKeyStoreAlgorithm` | `DES` | - | [SecretWriting:166](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/security/SecretWriting.java#L166) |
| `JCEKeyStorePassword` | `null` (no reader fallback) | - | [SecretWriting:94](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/security/SecretWriting.java#L94) |
| `JCEKeyStoreSize` | `-1` | - | [SecretWriting:170](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/security/SecretWriting.java#L170) |
| `JCEKeyStoreType` | `JCEKS` | - | [SecretWriting:97](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/security/SecretWriting.java#L97) |
| `SSLKeyStore` | `System.getProperty("javax.net.ssl.keyStore")` | `${master.etc}/ecpds-master.pfx` | [SSLSocketFactory:64](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/security/SSLSocketFactory.java#L64) |
| `SSLKeyStoreAlgorithm` | `System.getProperty("sun.SSL.keymanager.type", "SunX509")` | - | [SSLSocketFactory:76](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/security/SSLSocketFactory.java#L76) |
| `SSLKeyStorePassword` | `System.getProperty("javax.net.ssl.keyStorePassword")` | `Protected value/reference` | [SSLSocketFactory:68](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/security/SSLSocketFactory.java#L68) |
| `SSLKeyStoreType` | `System.getProperty("javax.net.ssl.keyStoreType", "PKCS12")` | - | [SSLSocketFactory:72](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/security/SSLSocketFactory.java#L72) |
| `SSLTrustStore` | `System.getProperty("javax.net.ssl.trustStore", KEY_STORE_PATH)` | - | [SSLSocketFactory:80](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/security/SSLSocketFactory.java#L80) |
| `SSLTrustStoreAlgorithm` | `System.getProperty("sun.SSL.trustmanager.type", KEY_STORE_ALGORITHM)` | - | [SSLSocketFactory:92](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/security/SSLSocketFactory.java#L92) |
| `SSLTrustStorePassword` | `System.getProperty("javax.net.ssl.trustStorePassword", KEY_STORE_PASSWORD)` | - | [SSLSocketFactory:84](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/security/SSLSocketFactory.java#L84) |
| `SSLTrustStoreType` | `System.getProperty("javax.net.ssl.trustStoreType", KEY_STORE_TYPE)` | - | [SSLSocketFactory:88](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/security/SSLSocketFactory.java#L88) |
| `SSLVersion` | `TLSv1.3` | - | [SSLSocketFactory:61](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/security/SSLSocketFactory.java#L61) |
| `check` | `false` | - | [ECaccessServer:188](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ecaccess/ECaccessServer.java#L188) |
| `checkCertificates` | `true` | - | [SSLSocketFactory:373](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/security/SSLSocketFactory.java#L373) |
| `cliSharedSecret` | Empty string | `Protected value/reference` | [SimplePlugin:62](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/plugin/SimplePlugin.java#L62); [ProxySocket:82](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ProxySocket.java#L82); [ECpdsClient:98](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/ecpds/ECpdsClient.java#L98) |
| `ejbPassword` | No direct literal reader; template entry | `Protected value/reference` | [Template:249](https://github.com/ecmwf/open-ecpds/blob/master/etc/master/conf/ecmwf.properties#L249) |
| `ejbUser` | No direct literal reader; template entry | `${Login[hostName]}` | [Template:248](https://github.com/ecmwf/open-ecpds/blob/master/etc/master/conf/ecmwf.properties#L248) |
| `jaasModule` | No direct literal reader; template entry | `ecmwf` | [Template:247](https://github.com/ecmwf/open-ecpds/blob/master/etc/master/conf/ecmwf.properties#L247) |

### [Server]

Per-process service switches and local paths. Master readers enable schedulers/repositories; Mover readers enable its own repositories/accounting. `dontRecord*` suppresses management-action history, not native telemetry globally. Globe cache settings belong to the Monitor. `providerMBeanTimeout` is milliseconds; topology probe timeout is a duration.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `acqDownloadScheduler` | `true` | - | [MasterServer:1010](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L1010) |
| `acquisitionScheduler` | `true` | `yes` | [MasterServer:996](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L996) |
| `allowPasswordOn` | `"(" + getRoot() + "\|" + Format.getHostAddress(getRoot()) + ")"` | - | [MasterServer:808](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L808) |
| `anonymousPass` | `anonymous` | - | [MasterServer:7239](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L7239) |
| `anonymousUser` | `anonymous` | - | [MasterServer:6287](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L6287); [MasterServer:7040](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L7040); [MasterServer:7217](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L7217); [MasterServer:7238](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L7238) |
| `anonymousUser` | `"anonymous".equals(user)` | - | [MasterServer:7308](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L7308) |
| `attachments` | `/tmp/ecpds-attachments` | `${master.data}/attachments` | [AttachmentAccessImpl:57](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/AttachmentAccessImpl.java#L57); [ManagementImpl:126](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/ManagementImpl.java#L126) |
| `backupScheduler` | `true` | `yes` | [MasterServer:982](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L982) |
| `checkFileName` | `ecpds.tst` | - | [MasterServer:9135](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L9135) |
| `containersToNotify` | `"ECpdsMonitor/" + getRoot()` | `ECpdsMonitor/(.*)` | [MasterServer:804](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L804) |
| `dataTransferCheck` | `true` | - | [MasterServer:948](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L948) |
| `defaultTransferGroup` | `null` (no reader fallback) | `internet` | [DataBaseImpl:2288](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/DataBaseImpl.java#L2288); [TransferServerProvider:1249](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/transfer/TransferServerProvider.java#L1249) |
| `dissDownloadScheduler` | `true` | - | [MasterServer:1017](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L1017) |
| `dontRecordActions` | `"output", "report"` | `output,report` | [MasterServer:7113](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L7113) |
| `dontRecordTransferActions` | `true` | `yes` | [MasterServer:7112](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L7112) |
| `downloadScheduler` | No direct literal reader; template entry | `yes` | [Template:102](https://github.com/ecmwf/open-ecpds/blob/master/etc/master/conf/ecmwf.properties#L102) |
| `ecauth` | `Cnf.at("ECauthSession", "cmd")` | `ecauth` | [ECauthTokenGenerator:47](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ecaccess/ECauthTokenGenerator.java#L47) |
| `email` | Empty string | Empty string | [MasterServer:7063](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L7063) |
| `eventRepository` | `true` | `yes` | [MasterServer:910](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L910) |
| `eventScheduler` | `true` | `yes` | [MasterServer:914](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L914) |
| `filterScheduler` | `true` | `yes` | [MasterServer:1024](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L1024) |
| `geoip2DataFile` | `null` (no reader fallback) | `${master.conf}/GeoLite2-City.mmdb` | [GeoIP2Helper:81](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/GeoIP2Helper.java#L81) |
| `groupNameTranslationList` | Empty list | - | [MasterServer:6849](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L6849) |
| `historyRepository` | `true` | `yes` | [MasterServer:903](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L903) |
| `hostCheckScheduler` | `true` | `no` | [MasterServer:940](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L940) |
| `hostoutputs` | `null` (no reader fallback) | `${master.data}/hostoutputs` | [MasterServer:299](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L299) |
| `liveTransferMonitoringForceEnabled` | `false` | - | [MasterServer:874](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L874) |
| `mailRepository` | `true` | `${mail.activated}` | [MasterServer:925](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L925) |
| `mailSubjectTag` | `getRoot()` | - | [MasterServer:826](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L826); [ECpdsPlugin:176](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/ecpds/ECpdsPlugin.java#L176) |
| `maxIncomingConnections` | `10` | - | [ECtransOptions:1750](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ectrans/ECtransOptions.java#L1750) |
| `monitoring` | Empty string | `https://${Login[hostName]}` | [MasterServer:7001](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L7001) |
| `monitoringTaskScheduler` | No direct literal reader; template entry | `no` | [Template:115](https://github.com/ecmwf/open-ecpds/blob/master/etc/master/conf/ecmwf.properties#L115) |
| `moverAvailabilityScheduler` | `true` | - | [MasterServer:1049](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L1049) |
| `notificationRepository` | `true` | `yes` | [MasterServer:933](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L933) |
| `notifyLocalContainer` | `true` | `no` | [MasterServer:799](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L799) |
| `portalBytesScheduler` | `true` | - | [MasterServer:1038](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L1038) |
| `portalTrafficScheduler` | `true` | - | [MasterServer:1031](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L1031) |
| `providerMBeanTimeout` | `120000` | `180000` | [ECaccessProvider:550](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ecaccess/ECaccessProvider.java#L550) |
| `proxyHostRepository` | `true` | - | [MasterServer:896](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L896) |
| `proxyScheduler` | `true` | - | [MasterServer:989](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L989) |
| `purgeScheduler` | `true` | - | [MasterServer:1003](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L1003) |
| `replicateScheduler` | `true` | `yes` | [MasterServer:975](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L975) |
| `sendMailNotificationsForTransfers` | `true` | - | [MasterServer:822](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L822) |
| `systemTopologyProbeTimeout` | `3000` ms (duration fallback) | - | [ManagementImpl:147](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/ManagementImpl.java#L147) |
| `testPhrase` | `Test transfer file` | - | [MasterServer:9136](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L9136) |
| `ticketRepository` | `true` | `yes` | [MasterServer:882](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L882) |
| `transferRepository` | `true` | `yes` | [MasterServer:889](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L889) |
| `transferScheduler` | `true` | `yes` | [MasterServer:875](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L875) |
| `useDataProxy` | No direct literal reader; template entry | `no` | [Template:122](https://github.com/ecmwf/open-ecpds/blob/master/etc/master/conf/ecmwf.properties#L122) |

### [SessionCache]

Shared session cache logging and minimum pause (milliseconds). Debug frequency is a diagnostic event-count interval.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `activityLogger` | `true` | - | [SessionCache:84](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/SessionCache.java#L84) |
| `debug` | `false` | - | [SessionCache:90](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/SessionCache.java#L90) |
| `debugFrequency` | `1000000` | - | [SessionCache:81](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/SessionCache.java#L81) |
| `minimumPause` | `15000` | - | [SessionCache:87](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/SessionCache.java#L87) |

### [SocketConfig]

Defaults for sockets. Buffers are bytes (`-1` leaves OS defaults), timeouts are milliseconds, backlog is pending connections, and `keepAlive` is TCP keepalive rather than application connection pooling. Named socket sections override these values; see the complete named-option list below.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `backLog` | `100` | `10000` | [SocketConfig:158](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/rmi/SocketConfig.java#L158) |
| `connectTimeOut` | `15000` | `30000` | [SocketConfig:155](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/rmi/SocketConfig.java#L155) |
| `host` | `null` (no reader fallback) | - | [SocketConfig:173](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/rmi/SocketConfig.java#L173) |
| `interruptible` | `false` | `no` | [SocketConfig:170](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/rmi/SocketConfig.java#L170) |
| `keepAlive` | `null` (no reader fallback) | `yes` | [SocketConfig:161](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/rmi/SocketConfig.java#L161) |
| `listenAddress` | `null` (no reader fallback) | `${Login[externalAddress]}` | [SocketConfig:140](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/rmi/SocketConfig.java#L140) |
| `maxPrivilegedPort` | `1023` | - | [SocketConfig:89](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/rmi/SocketConfig.java#L89) |
| `minPrivilegedPort` | `500` | - | [SocketConfig:86](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/rmi/SocketConfig.java#L86) |
| `port` | `-1` | - | [SocketConfig:176](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/rmi/SocketConfig.java#L176) |
| `privilegedLocalPort` | `false` | - | [SocketConfig:137](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/rmi/SocketConfig.java#L137) |
| `publicAddress` | `null` (no reader fallback) | `${Login[externalAddress]}` | [SocketConfig:143](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/rmi/SocketConfig.java#L143) |
| `receiveBufferSize` | `-1` | `-1` | [SocketConfig:146](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/rmi/SocketConfig.java#L146) |
| `reuseAddress` | `null` (no reader fallback) | - | [SocketConfig:167](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/rmi/SocketConfig.java#L167) |
| `sendBufferSize` | `-1` | `-1` | [SocketConfig:149](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/rmi/SocketConfig.java#L149) |
| `serverPort` | `0` | - | [SocketConfig:179](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/rmi/SocketConfig.java#L179) |
| `soTimeOut` | `86400000` | `86400000` | [SocketConfig:152](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/rmi/SocketConfig.java#L152) |
| `tcpNoDelay` | `null` (no reader fallback) | - | [SocketConfig:164](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/rmi/SocketConfig.java#L164) |
| `trace` | `true` | `no` | [SocketConfig:131](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/rmi/SocketConfig.java#L131) |

### [SshPlugin]

SSH/SFTP listener and server identity. `keyPairsDir` stores host keys, `banner` is a file path and `softwareVersion` is the advertised server string. `maxConcurrentSessions=0` means unlimited. `hidden` controls virtual-domain visibility.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `hidden` | Empty list | - | [MinaFileSystemAccessor:783](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ssh/MinaFileSystemAccessor.java#L783); [MinaFileSystemAccessor:1069](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ssh/MinaFileSystemAccessor.java#L1069) |

### [StoragePolicy]

Storage lifecycle worker. `batchSize` counts files, `maxRelatedTransfers` bounds related records and other timing settings are durations. `enabled` is opt-in; existing ENFORCE policies take effect when enabled. See the lifecycle policy guide before activation.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `batchSize` | `100` | `100` | [StorageLifecycleScheduler:121](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/storage/StorageLifecycleScheduler.java#L121) |
| `enabled` | `false` | `no` | [StorageLifecycleScheduler:86](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/storage/StorageLifecycleScheduler.java#L86) |
| `interval` | `30000` ms (duration fallback) | `30s` | [StorageLifecycleScheduler:80](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/storage/StorageLifecycleScheduler.java#L80) |
| `jammedTimeout` | `600000` ms (duration fallback) | - | [StorageLifecycleScheduler:81](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/storage/StorageLifecycleScheduler.java#L81) |
| `maxRelatedTransfers` | `10000` | `10000` | [StorageLifecycleScheduler:170](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/storage/StorageLifecycleScheduler.java#L170) |
| `maxUsageAge` | `120000` ms (duration fallback) | - | [MasterServer:3552](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L3552) |
| `stepBudget` | `10000` ms (duration fallback) | `10s` | [StorageLifecycleScheduler:128](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/storage/StorageLifecycleScheduler.java#L128) |
| `timeRanges` | Empty list | - | [StorageLifecycleScheduler:82](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/storage/StorageLifecycleScheduler.java#L82) |

### [StorageRepository]

Asynchronous persistence. `*Size` means storage-worker thread count, **not queue capacity or batch size**. `*Delay`, idle waits and ticket timeouts are milliseconds. `*MaxAuthorisedSize` caps repository entries (`0` leaves the cap unset). Options only apply to repository types instantiated by this process.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `eventDelay` | `30000` | `1000` | [MasterServer:8095](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L8095) |
| `eventMaxAuthorisedSize` | `0` | - | [MasterServer:8096](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L8096) |
| `eventSize` | `5` | `15` | [MasterServer:8094](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L8094) |
| `historyDelay` | `30000` | `1000` | [MasterServer:7909](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L7909) |
| `historyMaxAuthorisedSize` | `0` | - | [MasterServer:7910](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L7910) |
| `historySize` | `5` | `15` | [MasterServer:7908](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L7908) |
| `notificationDelay` | `1000` | `45000` | [MasterServer:8806](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L8806) |
| `notificationSize` | `5` | `25` | [MasterServer:8805](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L8805) |
| `providerDelay` | `30000` | `1000` | [ECaccessProvider:509](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ecaccess/ECaccessProvider.java#L509) |
| `providerSize` | `0` | `5` | [ECaccessProvider:508](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ecaccess/ECaccessProvider.java#L508) |
| `proxyHostDelay` | `15000` | - | [MasterServer:8657](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L8657) |
| `proxyHostSize` | `2` | - | [MasterServer:8656](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L8656) |
| `storageRepositoryDelay` | `3600000` | `45000` | [StorageRepository:159](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ecaccess/StorageRepository.java#L159) |
| `storageThreadDelay` | `15000` | `45000` | [StorageThread:126](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ecaccess/StorageThread.java#L126) |
| `ticketDelay` | `60000` | `45000` | [TicketRepository:61](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ecaccess/TicketRepository.java#L61) |
| `ticketSize` | `0` | `1` | [TicketRepository:60](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ecaccess/TicketRepository.java#L60) |
| `ticketTimeOut` | `18000000` | - | [TicketRepository:62](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ecaccess/TicketRepository.java#L62) |
| `ticketTimeOutAfterCompletion` | `180000` | - | [TicketRepository:63](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ecaccess/TicketRepository.java#L63) |
| `transferDelay` | `250` | `1000` | [MasterServer:7761](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L7761) |
| `transferSize` | `5` | `25` | [MasterServer:7760](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterServer.java#L7760) |

### [StreamPlug]

Shared stream-copy buffer size in bytes.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `buffSize` | `65536` | - | [StreamPlugThread:60](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/StreamPlugThread.java#L60) |

### [TOTP]

OTP authentication provider configuration, including token endpoint, separate password/passcode client credentials, grant type and expected HTTP status. `active=false` disables this provider path; the login-form selector does not enable it.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `active` | `false` | - | [TOTP:51](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/security/TOTP.java#L51) |
| `clientIdForPasscode` | `null` (no reader fallback) | - | [TOTP:63](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/security/TOTP.java#L63) |
| `clientIdForPassword` | `null` (no reader fallback) | - | [TOTP:57](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/security/TOTP.java#L57) |
| `clientSecretForPasscode` | `null` (no reader fallback) | - | [TOTP:66](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/security/TOTP.java#L66) |
| `clientSecretForPassword` | `null` (no reader fallback) | - | [TOTP:60](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/security/TOTP.java#L60) |
| `expectedStatus` | `200` | - | [TOTP:72](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/security/TOTP.java#L72) |
| `grantType` | `password` | - | [TOTP:69](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/security/TOTP.java#L69) |
| `url` | `null` (no reader fallback) | - | [TOTP:54](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/security/TOTP.java#L54) |

### [ThreadService]

Shared thread runtime. Virtual threads are opt-in; `debugThreadLocal` enables thread-local diagnostics.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `allowVirtualThread` | `false` | - | [ThreadService:66](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ThreadService.java#L66) |
| `debugThreadLocal` | `false` | - | [ThreadService:69](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ThreadService.java#L69) |

### [TransferScheduler]

Destination-dispatch concurrency and monitoring cache. `poolSize=0` means unlimited destinations; `queueSize` is the pending-transfer fetch limit. `delay` is milliseconds, `monitoringTimeout` minutes and subscription frequency a duration.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `delay` | `1000` | - | [TransferScheduler:2143](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/transfer/TransferScheduler.java#L2143) |
| `monitoringCache` | `true` | `yes` | [TransferScheduler:2781](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/transfer/TransferScheduler.java#L2781) |
| `monitoringSubscribeFrequency` | `600000` ms (duration fallback) | - | [TransferScheduler:2958](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/transfer/TransferScheduler.java#L2958) |
| `monitoringTimeout` | `10` | `10` | [TransferScheduler:2811](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/transfer/TransferScheduler.java#L2811) |
| `poolSize` | `400` | `0` | [TransferScheduler:149](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/transfer/TransferScheduler.java#L149) |
| `queueSize` | `20` | `55` | [TransferScheduler:152](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/transfer/TransferScheduler.java#L152) |

### [TransferServerProvider]

Mover-selection heuristics and usage updates. Update frequency is seconds; CV threshold and file-size penalty are dimensionless factors. Debug output helps inspect allocation decisions.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `cvThreshold` | `0.03` | - | [TransferServerProvider:1433](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/transfer/TransferServerProvider.java#L1433) |
| `debug` | `false` | - | [TransferServerProvider:2108](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/transfer/TransferServerProvider.java#L2108) |
| `fileSizePenaltyFactor` | `1.0` | - | [TransferServerProvider:1439](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/transfer/TransferServerProvider.java#L1439) |
| `startUsageUpdater` | `true` | - | [TransferServerProvider:1465](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/transfer/TransferServerProvider.java#L1465) |
| `usageUpdaterFreqInSec` | `2` | - | [TransferServerProvider:1693](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/transfer/TransferServerProvider.java#L1693) |

### Named socket sections

[`SocketConfig.load`](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/rmi/SocketConfig.java#L246) reads the following keys from a
component's named section. Common section names for this service are `CallBack`, `RMIServerSocketFactory`, `RMIClientSocketFactory`, `ECpdsPlugin`, `FtpPlugin`, `FTPPassiveSocket`, `FTPActiveSocket`, `ProxySocket` and `ManagementEXEC`; transfer/helper code can also use `ECauthModule`, `FtpPASV`, `FtpPORT`, `FtpOPEN`, `HTTPSocketConfig`, `GCSSocketConfig`, `S3SocketConfig`, `WebDavModule`, `JSftp`, `TelnetWrapper` and `ECios`.
A transfer module or plugin must actually use that socket implementation for
its settings to apply; Jetty HTTP listeners and third-party transfer clients
do not automatically honour every low-level socket option.

Basic options inherit the values in `[SocketConfig]` shown above; the advanced
TCP options from `tcpCongestion` onward default to **unset** and are read from
the named section, not from the global `[SocketConfig]` defaults. Advanced
options depend on OS/socket support. Caller-supplied addresses, ports and
module/Host settings may override socket defaults.

| Option(s) | Meaning |
|-----------|---------|
| `listenAddress` | Local bind/source address. |
| `publicAddress` | Advertised address, distinct from the local bind address. |
| `receiveBufferSize`, `sendBufferSize` | Bytes; -1 leaves the OS default. |
| `soTimeOut` | Read timeout in milliseconds; aliases `soTimeout` and `timeOut` are tried in that order when the preferred spelling is absent. |
| `connectTimeOut` | Connection timeout in milliseconds. |
| `backLog` | Pending incoming-connection queue length. |
| `keepAlive` | TCP keepalive switch. |
| `tcpNoDelay` | TCP_NODELAY switch. |
| `reuseAddress` | SO_REUSEADDR switch. |
| `interruptible` | Use the interruptible socket implementation. |
| `privilegedLocalPort` | Allocate a local source/listening port from the configured local range. |
| `minPrivilegedPort`, `maxPrivilegedPort` | Inclusive local-port range; inherited base defaults are 500 and 1023. |
| `trace` | Socket diagnostics. |
| `host`, `port` | Default remote endpoint when the caller does not supply one. |
| `serverPort` | Local listening port; 0 requests an automatically assigned port. |
| `tcpCongestion` | Congestion-control algorithm name. |
| `soMaxPacingRate` | Maximum socket pacing rate, bytes/s. |
| `tcpMaxSegment` | Maximum TCP segment size, bytes. |
| `tcpTimeStamp` | TCP timestamps switch. |
| `tcpWindowClamp` | TCP receive-window clamp, bytes. |
| `tcpKeepAliveTime` | Idle time before keepalive probes, seconds. |
| `tcpKeepAliveInterval` | Time between keepalive probes, seconds. |
| `tcpKeepAliveProbes` | Keepalive probe count. |
| `tcpLingerEnable` | Enable linger-on-close. |
| `tcpLingerTime` | Linger timeout, seconds. |
| `tcpUserTimeout` | Maximum unacknowledged-data time, milliseconds. |
| `tcpQuickAck` | TCP quick-acknowledgement switch. |

### Master-managed maps

These sections accept names determined by users, services or network addresses,
so there is no finite list of key names:

| Section / key pattern | Default when absent | Meaning |
|-----------------------|---------------------|---------|
| `[API] <clientId>` | Empty definition | Startup API client provisioning: `secret:service-name-regex`. Treat values as credentials, not public examples. |
| `[Services] <serviceName>` | Service unavailable | External execution endpoint in `host:port` form. Uses `[ManagementEXEC]` socket settings. |
| `[Restricted] <user>` | No configured root restriction | Comma-separated regular expressions matched against the connecting root's resolved hostname. |
| `[Roots] <user>` | Empty value | Static authentication credential for that user; **not a filesystem-root path**. Keep this secret. |
| `[NetworkWeatherRelay] <sourceId>` | No approved relay mapping | Active Proxy Host ID approved to relay that Network Weather source. |
| `[GeoIP] forced.<address-or-prefix>` | Normal GeoIP resolution | Forced `latitude,longitude,region,country,city` for matching addresses. |
| `[DestinationTypes] <position>.<id>.<category>` | Built-in Destination types | Additional Destination type labels, in `position.id.category=label` form. |

Forced GeoIP entries are checked before database lookup. Local/private addresses
otherwise skip GeoIP lookup; this does not prevent name resolution.
See [Live Earth monitoring](../monitor-ui/monitoring.md).

### FTP mapping and per-user families

| Section / key pattern | Default when absent | Meaning |
|-----------------------|---------------------|---------|
| `[FtpPlugin] restrict.<user>` | No prefix restriction | Require the client's textual IP address to start with the configured prefix. This is not CIDR syntax. |
| `[FtpAliases] <domain>` | Empty list | Alias list for the selected virtual domain. |
| `[FtpDomainList] <domain>` | No mapping; `*` is the fallback | Virtual domain to backing-domain mapping. |
| `[FtpModuleList] <extension>` | Default `*` module list | Extension-specific FTP module definitions. |
| `[Filter] pre.out.<name>` | No direct reader in current sources | Retained template definitions for file-to-file filter commands using `$1` and `$2` paths. |
| `[Filter] fly.out.lbzip2`, `fly.in.lbzip2`, `fly.out.zstd`, `fly.in.zstd` | No configured command | Streaming compression/decompression commands. |

Streaming filter executables must be installed on the machine executing them.
Adding a definition does not apply it to every transfer; the current stream
manager selects the named `lbzip2` or `zstd` command when using that format.

### Plugin and module families

`[PluginList]` is an open map of plugin names to definitions:

```ini
[PluginList]
name=implementation.Class@/path/to/plugin,option=value
```

The library path and per-plugin options depend on the plugin. Server plugins
support `maxConnections` and `inverseResolution`; deleting or adding a plugin
definition changes which services start. These are options **inside the value**,
not new top-level `ecmwf.properties` groups. See
[Plugin Infrastructure](../concepts/plugin-infrastructure.md).

`[ECtrans]` is an open family of global transfer-option defaults, with keys
`module.option` (for example, `ftp.passive`). Its option set is defined by
`ECtransOptions` and the selected module, not a fixed list of literal `Cnf`
keys. Entity/transfer setup values can override these defaults. The complete
option/default references are:

- [ECtrans common options](../transfer-modules/ectrans.md) and the protocol pages
  under [Transfer Modules](../transfer-modules/index.md).
- [Host options](../concepts/host-options.md),
  [Destination options](../concepts/destination-options.md),
  [Web User options](../concepts/web-user-options.md) and
  [Data User options](../use-cases/data-portal-user-options.md).

These entity options are not interchangeable with service sections. Some
options are only meaningful for a particular module or execution role.

<!-- END PROPERTY CATALOGUE -->
