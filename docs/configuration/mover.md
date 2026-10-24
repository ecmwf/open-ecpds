# Data Mover: ecmwf.properties

The Mover's `ecmwf.properties` configures its Master connection, public
protocol listeners, data repository, retrieval behaviour and shared runtime.
This reference includes options read by the implementation even if they are
absent from the supplied template.

See also the [Master](master.md) and [Monitor](monitor.md) references.

## File location and overrides

The source template is
[`etc/mover/conf/ecmwf.properties`](https://github.com/ecmwf/open-ecpds/blob/master/etc/mover/conf/ecmwf.properties).
Its RPM path is `/usr/local/ecpds/mover/conf/ecmwf.properties`; the launcher
selects it with the `ecmwf.properties` JVM property.

Use `/etc/ecpds/mover.cnf` or the container/service environment for launcher
settings such as addresses, ports, operational mode and shared secrets.
The template's `${...}` expressions resolve JVM properties, section references
or supported methods, not arbitrary environment variables.

The final `#include "${mover.etc}/ecmwf.properties"` loads local overrides,
normally from `/etc/ecpds/mover/ecmwf.properties`. **This is an active
directive, not a commented-out example.** Do not include the file into itself.
See the [configuration syntax and loading rules](master.md#file-location-and-configuration-layers).

For example:

```ini
[HttpPlugin]
maxThreads=200
minThreads=20
webdavEnabled=false

[SshPlugin]
maxConcurrentSessions=100
```

Restart the affected Mover after changing service configuration unless a feature
explicitly supports live updates.

## Reading this reference

**Code fallback** means the reader's value when the option is absent, not the
explicit **shipped value**. A dash means no assignment in the template.
`null` means the reader supplies no fallback; it does not guarantee that omitting
the setting is valid. Different reader fallbacks are listed separately, with
source links. Java expressions are preserved for context-dependent defaults.
Credentials are not reproduced.

Rows marked **template entry** have no direct literal reader in this process's
reviewed sources. Some entries are substitutions or retained compatibility
settings; named-socket and open-map readers are described separately.

Names are case-sensitive. Duration-reading options accept suffixes such as `s`,
`m`, `h` and `d`; bare configured durations mean **hours**. Plain numeric socket
timeouts are milliseconds. Rates in `[TestModule]` are bytes per second;
file-content settings take a path, while their fallback is content.

## Operational guidance

Public listeners, portal links and control-channel authentication are separate:

- `[PluginList]` selects services to start; plugin sections configure them.
  Advertising a protocol in `[DataPortal]` does not start its listener.
- `[HttpPlugin]` serves the Data Portal, REST control channel, optional S3
  endpoint and WebDAV. Restricting public portal access does not replace
  [control-channel authentication](../administration/mover-control-channel-security.md).
- `[Security] rccSharedSecret` signs REST control requests between relays and
  regular Movers. `cliSharedSecret` protects a different direct-socket
  connection; do not confuse the two.
- `[MoverProxy]` configures a control-plane HTTP tunnel. It is not an
  OpenTelemetry exporter or a generic outbound transfer proxy.
- `[ProxySocket] plainText=yes` disables TLS on that socket. Keep internal
  traffic on trusted networks and review TLS verification before exposing it.
- `[Mover] operational` affects work availability and test behaviour.
  The `[TestModule]` options are not normal production transfer-rate limits.

Movers follow the Master's transfer-tracing decision and forward completed
spans through the existing control connection. They need no OpenTelemetry
Collector endpoint, credentials or SDK configuration; see
[Event Logging](../event-logging/overview.md#transfer-traces-and-log-correlation).

For transfer-module settings and per-user portal policy, use the linked
references in the dynamic-families section below. Those defaults can affect
many transfers, so prefer a Host or User override when only one entity needs
a different policy.

## Option catalogue

Shared components and optional modules consume their settings only when used;
listing an option does not activate that component. Named socket sections
inherit `[SocketConfig]`, then apply their own overrides.

<!-- BEGIN PROPERTY CATALOGUE -->

### [CallBack]

Exported callback endpoint. `serverFactory` and `clientFactory` are Java socket-factory classes. This section also accepts the named socket overrides described below.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `clientFactory` | `null` (no reader fallback) | `ecmwf.common.rmi.RMIClientSocketFactory` | [CallBackObject:150](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/callback/CallBackObject.java#L150) |
| `keepAlive` | Inherits `[SocketConfig] keepAlive` | `yes` | [Template:89](https://github.com/ecmwf/open-ecpds/blob/master/etc/mover/conf/ecmwf.properties#L89) |
| `listenAddress` | Inherits `[SocketConfig] listenAddress` | `${Login[internalAddress]}` | [Template:84](https://github.com/ecmwf/open-ecpds/blob/master/etc/mover/conf/ecmwf.properties#L84) |
| `port` | `9000` | `${Ports[callBack]}` | [CallBackObject:54](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/callback/CallBackObject.java#L54) |
| `publicAddress` | Inherits `[SocketConfig] publicAddress` | `${Login[internalAddress]}` | [Template:85](https://github.com/ecmwf/open-ecpds/blob/master/etc/mover/conf/ecmwf.properties#L85) |
| `serverFactory` | `null` (no reader fallback) | `ecmwf.common.rmi.RMIServerSocketFactory` | [CallBackObject:116](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/callback/CallBackObject.java#L116) |

### [Command]

External helper commands. `ecauth` overrides the token-generation executable; `sh` selects the ECios shell command.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `sh` | `null` (no reader fallback) | - | [ForkECios:169](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ForkECios.java#L169) |

### [DataPortal]

Public endpoint hints and protocol visibility in portal pages. Empty public URLs/hosts let the portal derive values from the request/listener. These switches do not enable or disable the actual FTP, SFTP or S3 listeners. Per-user portal settings can override global hints.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `ftpEnabled` | `true` | - | [RESTServer:2615](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/service/RESTServer.java#L2615) |
| `ftpPublicHost` | Empty string | - | [RESTServer:2610](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/service/RESTServer.java#L2610) |
| `ftpPublicPort` | Empty string | - | [RESTServer:2611](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/service/RESTServer.java#L2611) |
| `httpsPublicBaseUrl` | Empty string | - | [RESTServer:2608](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/service/RESTServer.java#L2608) |
| `registrationAdminEmail` | Empty string | - | [RESTServer:1923](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/service/RESTServer.java#L1923); [RESTServer:2118](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/service/RESTServer.java#L2118) |
| `s3Enabled` | `true` | - | [RESTServer:2614](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/service/RESTServer.java#L2614) |
| `s3PublicEndpointUrl` | Empty string | - | [RESTServer:2609](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/service/RESTServer.java#L2609) |
| `sftpEnabled` | `true` | - | [RESTServer:2616](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/service/RESTServer.java#L2616) |
| `sftpPublicHost` | Empty string | - | [RESTServer:2612](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/service/RESTServer.java#L2612) |
| `sftpPublicPort` | Empty string | - | [RESTServer:2613](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/service/RESTServer.java#L2613) |

### [ECauthModule]

ECauth connection and host-list cache. Durations control connection timeout, refresh polling, refresh frequency and cached-entry lifetime. Credentials here identify the ECauth service account.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `connectionTimeout` | `10000` ms (duration fallback) | `30s` | [Tools:163](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/security/Tools.java#L163); [Tools:253](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/security/Tools.java#L253); [Tools:359](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/security/Tools.java#L359) |
| `hostListManagerDelay` | `10000` ms (duration fallback) | `10s` | [ECauthModule:1258](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/ECauthModule.java#L1258) |
| `hostListManagerFrequency` | `params.length > 2 ? Format.getDuration(params[2]) : 5 * Timer.ONE_MINUTE` ms (duration fallback) | `5m` | [ECauthModule:431](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/ECauthModule.java#L431) |
| `hostListManagerLifeTime` | `3600000` ms (duration fallback) | `1h` | [ECauthModule:430](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/ECauthModule.java#L430) |
| `listenAddress` | Inherits `[SocketConfig] listenAddress` | `${Login[internalAddress]}` | [Template:181](https://github.com/ecmwf/open-ecpds/blob/master/etc/mover/conf/ecmwf.properties#L181) |
| `pass` | `ecauth` | `Protected value/reference` | [ECauthModule:300](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/ECauthModule.java#L300) |
| `protocol` | `ssh` | `ssh` | [ECauthModule:253](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/ECauthModule.java#L253); [ECauthModule:301](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/ECauthModule.java#L301) |
| `user` | `ecauth` | `ecauth` | [ECauthModule:299](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/ECauthModule.java#L299) |

### [ECios]

ECios helper start/kill socket timeouts, in milliseconds. Also accepts named socket overrides.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `killSoTimeout` | `15000` | - | [ForkECios:258](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ForkECios.java#L258) |
| `startSoTimeout` | `15000` | - | [ForkECios:179](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ForkECios.java#L179) |

### [ECpdsClient]

ECpds socket-client read timeout in milliseconds. This is distinct from the server-side plugin timeout.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `soTimeOut` | `60000` | - | [ECpdsClient:95](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/ecpds/ECpdsClient.java#L95) |

### [ECpdsPlugin]

Master ECpds listener and request-processing behaviour. `checkPort` requires a privileged client source port when enabled. `SocketSoTimeout` applies to accepted request sockets in milliseconds. `resetDataTransferSchedulesByGroup` resets schedules on group completion. The section also accepts named socket options.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `host` | `Cnf.at("Login", "hostName", "localhost")` | - | [ECpdsClient:83](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/ecpds/ECpdsClient.java#L83) |
| `port` | `Cnf.at("Ports", "ecpds", 6640)` | - | [ECpdsClient:86](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/ecpds/ECpdsClient.java#L86) |

### [ECproxyPlugin]

Mover ECproxy data listener and repository. Explicit TCP options override the accepted socket's existing values. Buffer sizes are bytes; socket timeouts are milliseconds. Also accepts named socket settings.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `SocketSoTimeout` | `60000` | - | [ECproxyPlugin:423](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/ecproxy/ECproxyPlugin.java#L423) |
| `keepAlive` | `socket.getKeepAlive()` | - | [ECproxyPlugin:175](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/ecproxy/ECproxyPlugin.java#L175) |
| `listenAddress` | Inherits `[SocketConfig] listenAddress` | `${Login[externalAddress]}` | [Template:155](https://github.com/ecmwf/open-ecpds/blob/master/etc/mover/conf/ecmwf.properties#L155) |
| `port` | `null` (no reader fallback) | `${Ports[ecproxy]}` | [ECproxyPlugin:147](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/ecproxy/ECproxyPlugin.java#L147) |
| `publicAddress` | Inherits `[SocketConfig] publicAddress` | `${Login[externalAddress]}` | [Template:156](https://github.com/ecmwf/open-ecpds/blob/master/etc/mover/conf/ecmwf.properties#L156) |
| `receiveBufferSize` | `-1` | - | [ECproxyPlugin:171](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/ecproxy/ECproxyPlugin.java#L171) |
| `repository` | `/tmp` | `${mover.data}` | [RegularFile:102](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/RegularFile.java#L102) |
| `repository` | `null` (no reader fallback) | `${mover.data}` | [MoverServer:1476](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/MoverServer.java#L1476) |
| `sendBufferSize` | `-1` | - | [ECproxyPlugin:167](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/ecproxy/ECproxyPlugin.java#L167) |
| `soTimeOut` | Inherits `[SocketConfig] soTimeOut` | `600000` | [Template:158](https://github.com/ecmwf/open-ecpds/blob/master/etc/mover/conf/ecmwf.properties#L158) |
| `tcpNoDelay` | `socket.getTcpNoDelay()` | - | [ECproxyPlugin:176](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/ecproxy/ECproxyPlugin.java#L176) |

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
| `soTimeOut` | Inherits `[SocketConfig] soTimeOut` | `300000` | [Template:244](https://github.com/ecmwf/open-ecpds/blob/master/etc/mover/conf/ecmwf.properties#L244) |

### [FTPPassiveSocket]

FTP server passive-data sockets. `boundAddress` controls the address used for passive binding. Also accepts named socket settings, including local-port ranges.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `backLog` | `1` | - | [DataSocket:80](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ftpd/DataSocket.java#L80) |
| `boundAddress` | `boundAddress` | - | [DataSocket:99](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ftpd/DataSocket.java#L99) |
| `soTimeOut` | Inherits `[SocketConfig] soTimeOut` | `300000` | [Template:240](https://github.com/ecmwf/open-ecpds/blob/master/etc/mover/conf/ecmwf.properties#L240) |

### [Filter]

Streaming compression/decompression command definitions. The four `fly.*` keys are read by the stream manager. The template also retains `pre.out.*` definitions, but the current source has no direct reader for those entries.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `fly.in.lbzip2` | Empty command list | `lbzip2 -d` | [StreamManagerImp:234](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/StreamManagerImp.java#L234) |
| `fly.in.zstd` | Empty command list | `zstd -d --no-progress -c` | [StreamManagerImp:242](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/StreamManagerImp.java#L242) |
| `fly.out.lbzip2` | Empty command list | `lbzip2 -c` | [StreamManagerImp:136](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/StreamManagerImp.java#L136) |
| `fly.out.zstd` | Empty command list | `zstd -6 -T0 --no-progress -c` | [StreamManagerImp:144](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/StreamManagerImp.java#L144) |
| `pre.out.bzip2a` | No direct literal reader; template entry | `${mover.sh}/bzip2 $1 $2` | [Template:194](https://github.com/ecmwf/open-ecpds/blob/master/etc/mover/conf/ecmwf.properties#L194) |
| `pre.out.gzip` | No direct literal reader; template entry | `${mover.sh}/gzip $1 $2` | [Template:193](https://github.com/ecmwf/open-ecpds/blob/master/etc/mover/conf/ecmwf.properties#L193) |
| `pre.out.lbzip2` | No direct literal reader; template entry | `${mover.sh}/lbzip2 $1 $2` | [Template:195](https://github.com/ecmwf/open-ecpds/blob/master/etc/mover/conf/ecmwf.properties#L195) |

### [FtpDomainList]

Virtual FTP/SFTP domain mappings; arbitrary domain names are supported. `*` is the fallback. Domain mappings select backing filesystem/domain implementations, not network listener addresses.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `*` | `null` (no reader fallback) | `*` | [DOMAIN:47](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ftpd/DOMAIN.java#L47); [MinaFileSystemAccessor:781](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ssh/MinaFileSystemAccessor.java#L781) |
| `data` | No mapping; uses `*` fallback | `data:` | [Template:235](https://github.com/ecmwf/open-ecpds/blob/master/etc/mover/conf/ecmwf.properties#L235) |

### [FtpModuleList]

FTP extension-module lists. Keys select extensions; `*` supplies the default list. Values are comma-separated module definitions consumed by the FTP utility.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `*` | Empty list | - | [Util:217](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ftpd/Util.java#L217) |

### [FtpPlugin]

FTP control listener and behaviour. `welcome`, `browser` and `help` are file paths; `tail` is response text. `passive`, `rest` and `extended` affect FTP features. `hidden` is a list of hidden virtual domains. `alwaysCheckOnRETR` requests retrieval-time checks. The section also accepts named socket options.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `alwaysCheckOnRETR` | `false` | `yes` | [RETR:165](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ftpd/RETR.java#L165) |
| `browser` | `browser.ftp` | `${mover.conf}/ftp/browser.txt` | [PASS:165](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ftpd/PASS.java#L165) |
| `dataTimeOut` | No direct literal reader; template entry | `600000` | [Template:221](https://github.com/ecmwf/open-ecpds/blob/master/etc/mover/conf/ecmwf.properties#L221) |
| `ext` | Empty list | Empty string | [Util:317](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ftpd/Util.java#L317) |
| `extended` | `false` | `yes` | [FEAT:49](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ftpd/FEAT.java#L49) |
| `extended` | `true` | `yes` | [FtpPlugin:284](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ftpd/FtpPlugin.java#L284) |
| `help` | `help.ftp` | `${mover.conf}/ftp/help.txt` | [HELP:57](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ftpd/HELP.java#L57) |
| `hidden` | Empty list | Empty string | [DOMAIN:53](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ftpd/DOMAIN.java#L53) |
| `listenAddress` | Inherits `[SocketConfig] listenAddress` | `${Login[externalAddress]}` | [Template:218](https://github.com/ecmwf/open-ecpds/blob/master/etc/mover/conf/ecmwf.properties#L218) |
| `passive` | `true` | `yes` | [CurrentContext:150](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ftpd/CurrentContext.java#L150) |
| `port` | `21` | `${Ports[ftp]}` | [DataSocket:129](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ftpd/DataSocket.java#L129) |
| `port` | `null` (no reader fallback) | `${Ports[ftp]}` | [FtpPlugin:129](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ftpd/FtpPlugin.java#L129) |
| `port` | Empty string | `${Ports[ftp]}` | [RESTServer:2604](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/service/RESTServer.java#L2604) |
| `rest` | `true` | - | [FEAT:50](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ftpd/FEAT.java#L50); [FtpPlugin:288](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ftpd/FtpPlugin.java#L288) |
| `soTimeOut` | Inherits `[SocketConfig] soTimeOut` | `600000` | [Template:220](https://github.com/ecmwf/open-ecpds/blob/master/etc/mover/conf/ecmwf.properties#L220) |
| `tail` | `Direct comments to ecaccess@ecmwf.int` | `${mover.title}` | [ALIAS:86](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ftpd/ALIAS.java#L86); [CHMOD:90](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ftpd/CHMOD.java#L90); [DOMAIN:89](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ftpd/DOMAIN.java#L89); [UMASK:69](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ftpd/UMASK.java#L69) |
| `welcome` | `welcome.ftp` | `${mover.conf}/ftp/welcome.txt` | [PASS:166](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ftpd/PASS.java#L166) |

### [HttpPlugin]

Jetty listener, pool, protocol and portal settings. Thread limits are counts; `idleThreadsTimeout` and `idleTimeout` are milliseconds; buffer/header limits are bytes. `http`/`https=-1` leaves that connector disabled. On Movers, S3 skew is seconds and S3 request limits are bytes; CORS and DNS mappings apply to portal paths. On the Monitor, only the shared Jetty/HTTP2 readers listed here apply; its HTTPS listener is in `[MonitorPlugin]`.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `checkControlChannelIsSecure` | `false` | - | [RESTServer:3456](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/service/RESTServer.java#L3456) |
| `compression` | `true` | - | [HttpPlugin:393](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/http/HttpPlugin.java#L393) |
| `corsAllowOrigin` | Empty string | - | [HttpPlugin:274](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/http/HttpPlugin.java#L274) |
| `dnsPathList` | Empty list | - | [HttpPlugin:287](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/http/HttpPlugin.java#L287) |
| `enabledProtocols` | `TLSv1.3,TLSv1.2` | `TLSv1.3,TLSv1.2` | [HttpPlugin:446](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/http/HttpPlugin.java#L446) |
| `excludeCipherSuites` | `"SSL_RSA_WITH_DES_CBC_SHA", "SSL_DHE_RSA_WITH_DES_CBC_SHA", "SSL_DHE_DSS_WITH_DES_CBC_SHA", "SSL_RSA_EXPORT_WITH_RC4_40_MD5", "SSL_RSA_EXPORT_WITH_DES40_CBC_SHA", "SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA", "SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA"` | - | [HttpPlugin:459](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/http/HttpPlugin.java#L459) |
| `homeFile` | `null` (no reader fallback) | `${mover.conf}/http/home.html` | [RESTServer:123](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/service/RESTServer.java#L123) |
| `htdocs` | `null` (no reader fallback) | `${mover.htdocs}` | [HttpPlugin:188](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/http/HttpPlugin.java#L188) |
| `http` | `-1` | `${Ports[http]}` | [HttpPlugin:181](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/http/HttpPlugin.java#L181) |
| `http2Enabled` | `true` | - | [HttpPlugin:474](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/http/HttpPlugin.java#L474) |
| `https` | `-1` | `${Ports[https]}` | [HttpPlugin:182](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/http/HttpPlugin.java#L182) |
| `idleThreadsTimeout` | `60000` | - | [HttpPlugin:193](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/http/HttpPlugin.java#L193) |
| `idleTimeout` | `120000` | - | [HttpPlugin:488](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/http/HttpPlugin.java#L488); [HttpPlugin:497](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/http/HttpPlugin.java#L497) |
| `listenAddress` | `0.0.0.0` | `${Login[externalAddress]}` | [HttpPlugin:186](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/http/HttpPlugin.java#L186) |
| `logHeadersAndUri` | `false` | - | [RESTServer:3093](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/service/RESTServer.java#L3093); [RESTServer:3461](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/service/RESTServer.java#L3461) |
| `loginFile` | `null` (no reader fallback) | `${mover.conf}/http/login.html` | [RESTServer:132](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/service/RESTServer.java#L132) |
| `maxThreads` | `400` | - | [HttpPlugin:192](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/http/HttpPlugin.java#L192) |
| `minThreads` | `40` | - | [HttpPlugin:193](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/http/HttpPlugin.java#L193) |
| `outputBufferSize` | `32768` | - | [HttpPlugin:202](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/http/HttpPlugin.java#L202) |
| `portalFile` | `null` (no reader fallback) | `${mover.conf}/http/portal.html` | [RESTServer:126](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/service/RESTServer.java#L126) |
| `registerFile` | `null` (no reader fallback) | `${mover.conf}/http/register.html` | [RESTServer:129](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/service/RESTServer.java#L129) |
| `requestHeaderSize` | `8192` | - | [HttpPlugin:203](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/http/HttpPlugin.java#L203) |
| `responseHeaderSize` | `8192` | - | [HttpPlugin:204](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/http/HttpPlugin.java#L204) |
| `s3AuthenticationType` | `AWS_V2_OR_V4` | - | [HttpPlugin:258](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/http/HttpPlugin.java#L258) |
| `s3Enabled` | `true` | - | [HttpPlugin:251](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/http/HttpPlugin.java#L251) |
| `s3IgnoreUnknownHeaders` | `true` | - | [HttpPlugin:260](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/http/HttpPlugin.java#L260) |
| `s3MaximumTimeSkew` | `900` | - | [HttpPlugin:261](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/http/HttpPlugin.java#L261) |
| `s3ServicePath` | `/s3` | - | [HttpPlugin:253](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/http/HttpPlugin.java#L253); [HttpPlugin:394](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/http/HttpPlugin.java#L394); [RESTServer:2605](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/service/RESTServer.java#L2605) |
| `s3V4MaxNonChunkedRequestSize` | `33554432` | - | [HttpPlugin:259](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/http/HttpPlugin.java#L259) |
| `wantClientAuth` | `false` | - | [HttpPlugin:443](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/http/HttpPlugin.java#L443) |
| `webdavEnabled` | `true` | - | [HttpPlugin:350](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/http/HttpPlugin.java#L350); [RESTServer:2617](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/service/RESTServer.java#L2617) |
| `webdavPath` | `/webdav` | - | [HttpPlugin:352](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/http/HttpPlugin.java#L352); [HttpPlugin:395](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/http/HttpPlugin.java#L395); [RESTServer:2606](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/service/RESTServer.java#L2606) |

### [HttpPluginSSL]

Optional explicit HTTPS store configuration. If this section exists, `keyStorePath` and `keyStorePassword` must be supplied: the normal `[Security]` fallback branch is not used. Trust-store values default to the selected key store; store type defaults to `PKCS12`.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `keyStorePassword` | `null` (no reader fallback) | - | [HttpPlugin:421](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/http/HttpPlugin.java#L421) |
| `keyStorePath` | `null` (no reader fallback) | - | [HttpPlugin:420](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/http/HttpPlugin.java#L420) |
| `keyStoreType` | `PKCS12` | - | [HttpPlugin:422](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/http/HttpPlugin.java#L422) |
| `trustStorePassword` | `storePassword` | - | [HttpPlugin:452](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/http/HttpPlugin.java#L452) |
| `trustStorePath` | `storePath` | - | [HttpPlugin:451](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/http/HttpPlugin.java#L451) |
| `trustStoreType` | `storeType` | - | [HttpPlugin:453](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/http/HttpPlugin.java#L453) |

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

### [IncomingUsersManagementThread]

Incoming-user synchronization polling (`delay`, milliseconds) and maximum wait duration.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `delay` | `2000` | - | [MoverServer:325](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/MoverServer.java#L325) |
| `maxWaitTime` | `120000` ms (duration fallback) | - | [MoverServer:306](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/MoverServer.java#L306) |

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
| `externalAddress` | No direct literal reader; template entry | `${Login[hostName]}` | [Template:21](https://github.com/ecmwf/open-ecpds/blob/master/etc/mover/conf/ecmwf.properties#L21) |
| `hostName` | `InetAddress.getLocalHost().getHostName()` | `${ecmwf.common.ecaccess.ECaccessInit.getHostName()}` | [PluginContainer:149](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/plugin/PluginContainer.java#L149); [MoverServer:592](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/MoverServer.java#L592) |
| `hostName` | `localhost` | `${ecmwf.common.ecaccess.ECaccessInit.getHostName()}` | [ECpdsClient:83](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/ecpds/ECpdsClient.java#L83) |
| `hostName` | `null` (no reader fallback) | `${ecmwf.common.ecaccess.ECaccessInit.getHostName()}` | [MoverServer:1501](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/MoverServer.java#L1501) |
| `internalAddress` | No direct literal reader; template entry | `${mover.internalAddress}` | [Template:23](https://github.com/ecmwf/open-ecpds/blob/master/etc/mover/conf/ecmwf.properties#L23) |
| `masterServer` | No direct literal reader; template entry | `${master.address}` | [Template:19](https://github.com/ecmwf/open-ecpds/blob/master/etc/mover/conf/ecmwf.properties#L19) |
| `password` | `null` (no reader fallback) | `Protected value/reference` | [MoverServer:1526](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/MoverServer.java#L1526) |
| `root` | `Cnf.at("Login", "hostName")` | - | [MoverServer:1501](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/MoverServer.java#L1501) |
| `service` | `Cnf.at("Login", "hostName", InetAddress.getLocalHost().getHostName())` | - | [PluginContainer:149](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/plugin/PluginContainer.java#L149); [MoverServer:591](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/MoverServer.java#L591) |

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

### [MasterREST]

Continental/relay Master REST update interval, in milliseconds.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `delay` | `15000` | - | [MasterREST:319](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/MasterREST.java#L319) |

### [MasterServer]

Master host and RMI registry port used by a regular Mover.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `host` | `null` (no reader fallback) | `${Login[masterServer]}` | [MoverServer:1116](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/MoverServer.java#L1116) |
| `port` | `6601` | `${Ports[master]}` | [MoverServer:1117](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/MoverServer.java#L1117) |

### [Monitor]

External service-status integration, independent of the web monitoring UI. `activated` enables it, `debug` logs submissions and `dontSend` suppresses delivery. `delay` is a duration: the template's bare `120` means 120 hours, whereas the code fallback is five minutes.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `activated` | `false` | `no` | [MonitorManager:72](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/monitor/MonitorManager.java#L72) |
| `debug` | `false` | `no` | [MonitorManager:78](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/monitor/MonitorManager.java#L78) |
| `delay` | `300000` ms (duration fallback) | `120` | [MonitorManager:75](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/monitor/MonitorManager.java#L75) |
| `dontSend` | `false` | `no` | [MonitorManager:81](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/monitor/MonitorManager.java#L81) |

### [Mover]

Operational availability and whether successful processing removes the uncompressed working file.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `operational` | `true` | `${mover.operation}` | [MoverServer:196](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/MoverServer.java#L196) |
| `unlinkUncompressedFileOnSuccess` | `false` | - | [MoverServer:4954](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/MoverServer.java#L4954) |

### [MoverProvider]

Mover-side provider cache and portal-session caching. Timeout/TTL values are milliseconds.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `cacheTimeout` | `900000` | - | [MoverProvider:89](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/MoverProvider.java#L89) |
| `debug` | `false` | - | [MoverProvider:395](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/MoverProvider.java#L395); [MoverProvider:430](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/MoverProvider.java#L430) |
| `portalSessionTtlMs` | `3600000` | - | [MoverProvider:94](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/MoverProvider.java#L94) |

### [MoverProxy]

HTTP control-plane tunnel endpoints and connection timeout in milliseconds. `trustAllCerts=true` disables certificate verification for this REST client; do not treat that default as a production recommendation.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `connectTimeout` | `-1` | - | [MoverServer:3635](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/MoverServer.java#L3635) |
| `httpMover` | Empty string | - | [MoverServer:3635](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/MoverServer.java#L3635) |
| `httpProxy` | Empty string | - | [MoverServer:3634](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/MoverServer.java#L3634) |
| `trustAllCerts` | `true` | - | [RESTClient:102](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/service/RESTClient.java#L102) |

### [MqttPlugin]

Embedded MQTT/MQTTS broker listener ports, configuration and persistent data paths. `-1` leaves a connector unconfigured. Expiry settings are durations. `priority` and `startPriority` order the embedded broker extension and its startup; they are not Java thread priorities.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `configurationFolder` | `null` (no reader fallback) | `${mover.conf}/mqtt` | [MqttPlugin:168](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/mqtt/MqttPlugin.java#L168) |
| `dataFolder` | `null` (no reader fallback) | `${mover.mqtt}` | [MqttPlugin:169](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/mqtt/MqttPlugin.java#L169) |
| `listenAddress` | `0.0.0.0` | `${Login[externalAddress]}` | [MqttPlugin:141](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/mqtt/MqttPlugin.java#L141) |
| `messageExpiryMaxInterval` | `604800000` ms (duration fallback) | - | [MqttPlugin:145](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/mqtt/MqttPlugin.java#L145) |
| `mqtt` | `-1` | `${Ports[mqtt]}` | [MqttPlugin:136](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/mqtt/MqttPlugin.java#L136) |
| `mqtts` | `-1` | `${Ports[mqtts]}` | [MqttPlugin:137](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/mqtt/MqttPlugin.java#L137) |
| `priority` | `0` | - | [MqttPlugin:164](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/mqtt/MqttPlugin.java#L164) |
| `sessionExpiryMaxInterval` | `3600000` ms (duration fallback) | - | [MqttPlugin:143](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/mqtt/MqttPlugin.java#L143) |
| `startPriority` | `1000` | - | [MqttPlugin:165](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/mqtt/MqttPlugin.java#L165) |

### [NetworkWeather]

Network Weather admission/control limits. Values are counts except `maxConfigurationBytes` and `maxControlResponseBytes`, which are bytes. The Monitor controls activation; these settings bound accepted work rather than enabling probes.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `maxControlResponseBytes` | `65536` | - | [WeatherLimits:35](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/weather/WeatherLimits.java#L35) |

### [Other]

Shared utilities. `buffer` is an I/O buffer size in bytes; `ticketWaitDuration` is a raw millisecond timeout. `ecauthTokenCache` enables token caching. `dontCheckIp` bypasses the provider's client-address check during subscription. `tmp` is retained in templates; the launcher separately sets the JVM temporary directory.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `buffer` | `65536` | `131072` | [CurrentContext:132](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ftpd/CurrentContext.java#L132) |
| `ticketWaitDuration` | `120000` | - | [MoverServer:1713](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/MoverServer.java#L1713) |
| `tmp` | No direct literal reader; template entry | `${mover.tmp}` | [Template:273](https://github.com/ecmwf/open-ecpds/blob/master/etc/mover/conf/ecmwf.properties#L273) |

### [PluginList]

Template-defined configuration; see the reader links for its consuming component.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `ecproxy` | No plugin definition | `ecmwf.ecpds.mover.plugin.ecproxy.ECproxyPlugin,maxConnections=2000,inverseResolution=no` | [Template:47](https://github.com/ecmwf/open-ecpds/blob/master/etc/mover/conf/ecmwf.properties#L47) |
| `ftp` | No plugin definition | `ecmwf.common.ftpd.FtpPlugin,maxConnections=2000,inverseResolution=no` | [Template:45](https://github.com/ecmwf/open-ecpds/blob/master/etc/mover/conf/ecmwf.properties#L45) |
| `http` | No plugin definition | `ecmwf.ecpds.mover.plugin.http.HttpPlugin` | [Template:43](https://github.com/ecmwf/open-ecpds/blob/master/etc/mover/conf/ecmwf.properties#L43) |
| `mqtt` | No plugin definition | `ecmwf.ecpds.mover.plugin.mqtt.MqttPlugin` | [Template:44](https://github.com/ecmwf/open-ecpds/blob/master/etc/mover/conf/ecmwf.properties#L44) |
| `ssh` | No plugin definition | `ecmwf.ecpds.mover.plugin.ssh.SshPlugin` | [Template:46](https://github.com/ecmwf/open-ecpds/blob/master/etc/mover/conf/ecmwf.properties#L46) |

### [Ports]

Template port substitutions. Most keys are only referenced by listener sections and therefore have no independent code default. `ecpds` also has a direct client-reader fallback.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `callBack` | No direct literal reader; template entry | `${port.callback}` | [Template:37](https://github.com/ecmwf/open-ecpds/blob/master/etc/mover/conf/ecmwf.properties#L37) |
| `ecpds` | `6640` | - | [ECpdsClient:86](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/ecpds/ECpdsClient.java#L86) |
| `ecproxy` | No direct literal reader; template entry | `${port.ecproxy}` | [Template:33](https://github.com/ecmwf/open-ecpds/blob/master/etc/mover/conf/ecmwf.properties#L33) |
| `ftp` | No direct literal reader; template entry | `${port.ftp}` | [Template:34](https://github.com/ecmwf/open-ecpds/blob/master/etc/mover/conf/ecmwf.properties#L34) |
| `http` | No direct literal reader; template entry | `${port.http}` | [Template:29](https://github.com/ecmwf/open-ecpds/blob/master/etc/mover/conf/ecmwf.properties#L29) |
| `https` | No direct literal reader; template entry | `${port.https}` | [Template:30](https://github.com/ecmwf/open-ecpds/blob/master/etc/mover/conf/ecmwf.properties#L30) |
| `master` | No direct literal reader; template entry | `${port.master}` | [Template:36](https://github.com/ecmwf/open-ecpds/blob/master/etc/mover/conf/ecmwf.properties#L36) |
| `mqtt` | No direct literal reader; template entry | `${port.mqtt}` | [Template:31](https://github.com/ecmwf/open-ecpds/blob/master/etc/mover/conf/ecmwf.properties#L31) |
| `mqtts` | No direct literal reader; template entry | `${port.mqtts}` | [Template:32](https://github.com/ecmwf/open-ecpds/blob/master/etc/mover/conf/ecmwf.properties#L32) |
| `ssh` | No direct literal reader; template entry | `${port.ssh}` | [Template:35](https://github.com/ecmwf/open-ecpds/blob/master/etc/mover/conf/ecmwf.properties#L35) |

### [ProxySocket]

Proxy data/control socket settings. `plainText` chooses non-TLS transport and otherwise depends on the connection's addresses. `host`/`port` select the control endpoint; `dataHost`/`dataPort` can select the data endpoint separately. `cipherSuites` is the TLS suite list. Also accepts named socket options.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `cipherSuites` | Empty list | - | [ProxySocket:396](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ProxySocket.java#L396) |
| `cipherSuites` | `null` (no reader fallback) | - | [ProxySocket:400](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ProxySocket.java#L400) |
| `dataHost` | `Cnf.at("ProxySocket", "host", socketConfig.getPublicAddress())` | - | [ProxySocket:485](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ProxySocket.java#L485); [ProxySocket:567](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ProxySocket.java#L567) |
| `dataHost` | `null` (no reader fallback) | - | [ProxySocket:487](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ProxySocket.java#L487); [ProxySocket:569](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ProxySocket.java#L569) |
| `dataPort` | `Cnf.at("ProxySocket", "port", 646)` | - | [ProxySocket:486](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ProxySocket.java#L486); [ProxySocket:568](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ProxySocket.java#L568) |
| `host` | `localName` | - | [ProxySocket:156](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ProxySocket.java#L156) |
| `host` | `socketConfig.getPublicAddress()` | - | [ProxySocket:206](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ProxySocket.java#L206); [ProxySocket:485](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ProxySocket.java#L485); [ProxySocket:518](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ProxySocket.java#L518); [ProxySocket:567](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ProxySocket.java#L567); [ProxySocket:602](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ProxySocket.java#L602); [ProxySocket:679](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ProxySocket.java#L679); [ProxySocket:729](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ProxySocket.java#L729) |
| `plainText` | `localAddress.equals(definedAddress)` | `yes` | [ProxySocket:163](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ProxySocket.java#L163) |
| `port` | `646` | - | [ProxySocket:207](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ProxySocket.java#L207); [ProxySocket:486](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ProxySocket.java#L486); [ProxySocket:519](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ProxySocket.java#L519); [ProxySocket:568](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ProxySocket.java#L568); [ProxySocket:603](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ProxySocket.java#L603); [ProxySocket:680](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ProxySocket.java#L680); [ProxySocket:730](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ProxySocket.java#L730) |

### [RESTClient]

REST client diagnostics. Debug logging can expose request metadata; enable only for troubleshooting.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `debug` | `false` | - | [RESTClient:90](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/service/RESTClient.java#L90) |

### [RMIClientSocketFactory]

Template-defined configuration; see the reader links for its consuming component.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `interruptible` | Inherits `[SocketConfig] interruptible` | `yes` | [Template:102](https://github.com/ecmwf/open-ecpds/blob/master/etc/mover/conf/ecmwf.properties#L102) |
| `keepAlive` | Inherits `[SocketConfig] keepAlive` | `yes` | [Template:103](https://github.com/ecmwf/open-ecpds/blob/master/etc/mover/conf/ecmwf.properties#L103) |
| `listenAddress` | Inherits `[SocketConfig] listenAddress` | `${Login[internalAddress]}` | [Template:100](https://github.com/ecmwf/open-ecpds/blob/master/etc/mover/conf/ecmwf.properties#L100) |
| `publicAddress` | Inherits `[SocketConfig] publicAddress` | `${Login[internalAddress]}` | [Template:101](https://github.com/ecmwf/open-ecpds/blob/master/etc/mover/conf/ecmwf.properties#L101) |

### [RMIServerSocketFactory]

Template-defined configuration; see the reader links for its consuming component.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `interruptible` | Inherits `[SocketConfig] interruptible` | `yes` | [Template:95](https://github.com/ecmwf/open-ecpds/blob/master/etc/mover/conf/ecmwf.properties#L95) |
| `keepAlive` | Inherits `[SocketConfig] keepAlive` | `yes` | [Template:96](https://github.com/ecmwf/open-ecpds/blob/master/etc/mover/conf/ecmwf.properties#L96) |
| `listenAddress` | Inherits `[SocketConfig] listenAddress` | `${Login[internalAddress]}` | [Template:93](https://github.com/ecmwf/open-ecpds/blob/master/etc/mover/conf/ecmwf.properties#L93) |
| `publicAddress` | Inherits `[SocketConfig] publicAddress` | `${Login[internalAddress]}` | [Template:94](https://github.com/ecmwf/open-ecpds/blob/master/etc/mover/conf/ecmwf.properties#L94) |

### [ReplicationHost]

Template-defined configuration; see the reader links for its consuming component.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `listenAddress` | No direct literal reader; template entry | `${Login[internalAddress]}` | [Template:176](https://github.com/ecmwf/open-ecpds/blob/master/etc/mover/conf/ecmwf.properties#L176) |

### [ReportCommand]

Executable commands for Mover and remote-Host reports. Host commands expand `$host`, `$port` and `$listenAddress`.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `host` | `host-report` | `${mover.sh}/report $host $port $listenAddress` | [MoverServer:2166](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/MoverServer.java#L2166) |
| `mover` | `mover-report` | `${mover.sh}/report` | [MoverServer:2131](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/MoverServer.java#L2131) |

### [ResourceTracker]

Shared runtime-resource diagnostics. `defaultDebugFrequency` counts resource-close events between debug reports; it is not a time interval.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `defaultDebugFrequency` | `10000` | - | [ResourceTracker:45](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ResourceTracker.java#L45) |
| `enabled` | `true` | - | [ResourceTracker:42](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ResourceTracker.java#L42) |

### [RetrievalInputStream]

Retrieval stream buffering and cancellation, plus progress-monitor update duration. A negative `monitored` fallback leaves periodic monitoring unconfigured.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `buffered` | `false` | `yes` | [MoverServer:1992](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/MoverServer.java#L1992); [MoverServer:2428](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/MoverServer.java#L2428) |
| `interruptible` | `false` | `yes` | [MoverServer:1996](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/MoverServer.java#L1996); [MoverServer:2432](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/MoverServer.java#L2432) |
| `monitored` | `-1` ms (duration fallback) | `1m` | [MoverServer:2448](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/MoverServer.java#L2448); [ECproxyPlugin:332](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/ecproxy/ECproxyPlugin.java#L332) |

### [Scheduler]

Worker cadence, concurrency and watchdogs. `*JammedTimeout`, `maximumDuration*` and `timeOut*` read durations; a negative timeout leaves that execution limit unset. `*TimeRanges` accepts configured execution windows. `max*Threads` values are concurrency counts; `cacheRatio` scales prefetch. The legacy `purgeDataBase`/`updateECUsers` intervals are hours, with optional clock-time lists.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `downloadRepository` | `5000` ms (duration fallback) | `1s` | [MoverServer:6113](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/MoverServer.java#L6113) |
| `downloadRepositoryJammedTimeout` | `300000` ms (duration fallback) | `5m` | [MoverServer:6114](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/MoverServer.java#L6114) |
| `liveStatsRepository` | `2000` ms (duration fallback) | - | [MoverServer:6429](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/MoverServer.java#L6429) |
| `liveStatsRepositoryJammedTimeout` | `300000` ms (duration fallback) | - | [MoverServer:6430](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/MoverServer.java#L6430) |
| `trafficAccounting` | `10000` ms (duration fallback) | - | [MoverServer:6365](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/MoverServer.java#L6365) |
| `trafficAccountingJammedTimeout` | `300000` ms (duration fallback) | - | [MoverServer:6369](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/MoverServer.java#L6369) |
| `transferRepository` | `5000` ms (duration fallback) | `1s` | [MoverServer:5895](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/MoverServer.java#L5895) |
| `transferRepositoryJammedTimeout` | `300000` ms (duration fallback) | `5m` | [MoverServer:5896](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/MoverServer.java#L5896) |

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
| `SSLKeyStore` | `System.getProperty("javax.net.ssl.keyStore")` | `${mover.etc}/ecpds-mover.pfx` | [SSLSocketFactory:64](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/security/SSLSocketFactory.java#L64) |
| `SSLKeyStore` | `null` (no reader fallback) | `${mover.etc}/ecpds-mover.pfx` | [HttpPlugin:427](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/http/HttpPlugin.java#L427); [HttpPlugin:433](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/http/HttpPlugin.java#L433) |
| `SSLKeyStore` | `System.getProperty("javax.net.ssl.keyStore", null)` | `${mover.etc}/ecpds-mover.pfx` | [HttpPlugin:426](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/http/HttpPlugin.java#L426) |
| `SSLKeyStoreAlgorithm` | `System.getProperty("sun.SSL.keymanager.type", "SunX509")` | - | [SSLSocketFactory:76](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/security/SSLSocketFactory.java#L76) |
| `SSLKeyStorePassword` | `System.getProperty("javax.net.ssl.keyStorePassword")` | `Protected value/reference` | [SSLSocketFactory:68](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/security/SSLSocketFactory.java#L68) |
| `SSLKeyStorePassword` | `null` (no reader fallback) | `Protected value/reference` | [HttpPlugin:429](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/http/HttpPlugin.java#L429) |
| `SSLKeyStorePassword` | `System.getProperty("javax.net.ssl.keyStorePassword", null)` | `Protected value/reference` | [HttpPlugin:428](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/http/HttpPlugin.java#L428) |
| `SSLKeyStoreType` | `System.getProperty("javax.net.ssl.keyStoreType", "PKCS12")` | - | [SSLSocketFactory:72](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/security/SSLSocketFactory.java#L72); [HttpPlugin:430](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/http/HttpPlugin.java#L430) |
| `SSLTrustStore` | `System.getProperty("javax.net.ssl.trustStore", KEY_STORE_PATH)` | - | [SSLSocketFactory:80](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/security/SSLSocketFactory.java#L80) |
| `SSLTrustStoreAlgorithm` | `System.getProperty("sun.SSL.trustmanager.type", KEY_STORE_ALGORITHM)` | - | [SSLSocketFactory:92](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/security/SSLSocketFactory.java#L92) |
| `SSLTrustStorePassword` | `System.getProperty("javax.net.ssl.trustStorePassword", KEY_STORE_PASSWORD)` | - | [SSLSocketFactory:84](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/security/SSLSocketFactory.java#L84) |
| `SSLTrustStorePassword` | `System.getProperty("javax.net.ssl.trustStorePassword", storePassword)` | - | [HttpPlugin:456](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/http/HttpPlugin.java#L456) |
| `SSLTrustStorePath` | `System.getProperty("javax.net.ssl.trustStorePath", storePath)` | - | [HttpPlugin:455](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/http/HttpPlugin.java#L455) |
| `SSLTrustStoreType` | `System.getProperty("javax.net.ssl.trustStoreType", KEY_STORE_TYPE)` | - | [SSLSocketFactory:88](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/security/SSLSocketFactory.java#L88) |
| `SSLTrustStoreType` | `System.getProperty("javax.net.ssl.trustStoreType", storeType)` | - | [HttpPlugin:457](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/http/HttpPlugin.java#L457) |
| `SSLVersion` | `TLSv1.3` | - | [SSLSocketFactory:61](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/security/SSLSocketFactory.java#L61) |
| `checkCertificates` | `true` | - | [SSLSocketFactory:373](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/security/SSLSocketFactory.java#L373) |
| `cliSharedSecret` | Empty string | `Protected value/reference` | [SimplePlugin:62](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/plugin/SimplePlugin.java#L62); [ProxySocket:82](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ProxySocket.java#L82); [ECpdsClient:98](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/ecpds/ECpdsClient.java#L98) |
| `controlChannelMaxSkew` | `300000` ms (duration fallback) | - | [RESTSignature:73](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/service/RESTSignature.java#L73) |
| `rccSharedSecret` | Empty string | `Protected value/reference` | [RESTSignature:61](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/service/RESTSignature.java#L61) |

### [Server]

Per-process service switches and local paths. Master readers enable schedulers/repositories; Mover readers enable its own repositories/accounting. `dontRecord*` suppresses management-action history, not native telemetry globally. Globe cache settings belong to the Monitor. `providerMBeanTimeout` is milliseconds; topology probe timeout is a duration.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `downloadRepository` | `true` | - | [MoverServer:1141](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/MoverServer.java#L1141) |
| `liveStatsRepository` | `true` | - | [MoverServer:1156](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/MoverServer.java#L1156) |
| `maxIncomingConnections` | `10` | - | [ECtransOptions:1750](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ectrans/ECtransOptions.java#L1750) |
| `networkWeatherNegotiation` | `true` | - | [MoverServer:1150](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/MoverServer.java#L1150) |
| `ticketRepository` | `true` | - | [MoverServer:1126](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/MoverServer.java#L1126) |
| `trafficAccountingFile` | `java.nio.file.Path.of(System.getProperty("mover.tmp", System.getProperty("java.io.tmpdir")), "traffic-accounting.json").toString()` | - | [MoverServer:218](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/MoverServer.java#L218) |
| `transferRepository` | `true` | - | [MoverServer:1134](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/MoverServer.java#L1134) |

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
| `interruptible` | `false` | - | [SocketConfig:170](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/rmi/SocketConfig.java#L170) |
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
| `trace` | `true` | `yes` | [SocketConfig:131](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/rmi/SocketConfig.java#L131) |

### [SshPlugin]

SSH/SFTP listener and server identity. `keyPairsDir` stores host keys, `banner` is a file path and `softwareVersion` is the advertised server string. `maxConcurrentSessions=0` means unlimited. `hidden` controls virtual-domain visibility.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `banner` | Empty string | `${mover.conf}/ssh/welcome.txt` | [SshPlugin:179](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/ssh/SshPlugin.java#L179) |
| `hidden` | Empty list | - | [MinaFileSystemAccessor:783](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ssh/MinaFileSystemAccessor.java#L783); [MinaFileSystemAccessor:1069](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ssh/MinaFileSystemAccessor.java#L1069) |
| `keyPairsDir` | `ssh/keys` | `${mover.conf}/ssh/keys` | [SshPlugin:105](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/ssh/SshPlugin.java#L105) |
| `listenAddress` | `0.0.0.0` | `${Login[externalAddress]}` | [SshPlugin:102](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/ssh/SshPlugin.java#L102) |
| `maxConcurrentSessions` | `0` | `0` | [SshPlugin:173](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/ssh/SshPlugin.java#L173) |
| `port` | `22` | `${Ports[ssh]}` | [SshPlugin:108](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/ssh/SshPlugin.java#L108) |
| `port` | Empty string | `${Ports[ssh]}` | [RESTServer:2607](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/service/RESTServer.java#L2607) |
| `softwareVersion` | `ECPDS_SSHD` | `ECPDS_SSHD` | [SshPlugin:178](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/plugin/ssh/SshPlugin.java#L178) |

### [StorageRepository]

Asynchronous persistence. `*Size` means storage-worker thread count, **not queue capacity or batch size**. `*Delay`, idle waits and ticket timeouts are milliseconds. `*MaxAuthorisedSize` caps repository entries (`0` leaves the cap unset). Options only apply to repository types instantiated by this process.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `storageRepositoryDelay` | `3600000` | - | [StorageRepository:159](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ecaccess/StorageRepository.java#L159) |
| `storageThreadDelay` | `15000` | - | [StorageThread:126](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ecaccess/StorageThread.java#L126) |
| `ticketDelay` | `60000` | - | [TicketRepository:61](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ecaccess/TicketRepository.java#L61) |
| `ticketSize` | `0` | - | [TicketRepository:60](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ecaccess/TicketRepository.java#L60) |
| `ticketTimeOut` | `18000000` | - | [TicketRepository:62](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ecaccess/TicketRepository.java#L62) |
| `ticketTimeOutAfterCompletion` | `180000` | - | [TicketRepository:63](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ecaccess/TicketRepository.java#L63) |

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

### [TestModule]

Synthetic transfer behaviour for non-operational Mover tests. Error frequency is a count interval, initial delays are milliseconds and rates are bytes/s. Template-only upload settings have no direct readers in the current Mover source.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `downloadedBytesPerSecond` | `1048576` | `1048576` | [MoverServer:2394](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/MoverServer.java#L2394) |
| `errorsFrequency` | `1000` | `1000` | [MoverServer:281](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/MoverServer.java#L281) |
| `initialDownloadDelay` | `15` | `15` | [MoverServer:2393](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/mover/MoverServer.java#L2393) |
| `initialUploadDelay` | No direct literal reader; template entry | `500` | [Template:267](https://github.com/ecmwf/open-ecpds/blob/master/etc/mover/conf/ecmwf.properties#L267) |
| `uploadedBytesPerSecond` | No direct literal reader; template entry | `1048576` | [Template:268](https://github.com/ecmwf/open-ecpds/blob/master/etc/mover/conf/ecmwf.properties#L268) |

### [ThreadService]

Shared thread runtime. Virtual threads are opt-in; `debugThreadLocal` enables thread-local diagnostics.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `allowVirtualThread` | `false` | - | [ThreadService:66](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ThreadService.java#L66) |
| `debugThreadLocal` | `false` | - | [ThreadService:69](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ThreadService.java#L69) |

### Named socket sections

[`SocketConfig.load`](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/rmi/SocketConfig.java#L246) reads the following keys from a
component's named section. Common section names for this service are `CallBack`, `RMIServerSocketFactory`, `RMIClientSocketFactory`, `FtpPlugin`, `FTPPassiveSocket`, `FTPActiveSocket`, `ECproxyPlugin`, `ProxySocket`, `ECauthModule`, `FtpPASV`, `FtpPORT`, `FtpOPEN`, `HTTPSocketConfig`, `GCSSocketConfig`, `S3SocketConfig`, `WebDavModule`, `JSftp`, `TelnetWrapper` and `ECios`.
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

### Constructed HTTPS/TLS keys

The HTTPS helper constructs `[Security] SSL<Key>` names and otherwise consults
the corresponding `javax.net.ssl.<key>` JVM property. Its actual keys are
`SSLKeyStore`, `SSLKeyStorePassword`, `SSLKeyStoreType`, `SSLTrustStorePath`,
`SSLTrustStorePassword` and `SSLTrustStoreType`.

`SSLTrustStorePath` is the HTTPS helper's spelling; the generic socket TLS
reader separately uses `SSLTrustStore`. Do not assume these are aliases.
The explicit `[HttpPluginSSL]` map takes precedence over the HTTPS helper's
normal store-selection branch. MQTT uses `[Security] SSLKeyStore` and
`SSLKeyStorePassword` through its own helper.

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

### WebDAV initialization parameters

The Mover's WebDAV servlet forwards initialization-parameter lookups to
`[HttpPlugin]` by name. This is an extension family rather than a fixed
OpenECPDS enum; unset values defer to the bundled Jackrabbit servlet's defaults.
For example, Jackrabbit's `missing-auth-mapping` parameter is distinct from
the OpenECPDS `webdavPath` listener setting.
Only parameters actually read by the bundled servlet apply; arbitrary keys do
not become new features.

<!-- END PROPERTY CATALOGUE -->
