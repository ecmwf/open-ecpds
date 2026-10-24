# Monitor: ecmwf.properties

The Monitor's `ecmwf.properties` configures its Master connection, HTTPS web
interface, status calculations, login presentation and display defaults.
This reference includes options omitted from the shipped file, not just the
template's assignments.

See also the [Master](master.md) and [Data Mover](mover.md) references.
The Master's schedulers and the Mover's portal are configured in their own
processes, not here.

## File location and overrides

The source template is
[`etc/monitor/conf/ecmwf.properties`](https://github.com/ecmwf/open-ecpds/blob/master/etc/monitor/conf/ecmwf.properties).
Its RPM path is `/usr/local/ecpds/monitor/conf/ecmwf.properties`; the launcher
selects it with the `ecmwf.properties` JVM property.

Use `/etc/ecpds/monitor.cnf` or the container/service environment for launcher
values such as the Master address, listener ports and keystore password.
`${...}` placeholders in the template reference JVM properties or other
sections; they are not arbitrary environment-variable substitutions.

The final `#include "${monitor.etc}/ecmwf.properties"` loads local overrides,
normally from `/etc/ecpds/monitor/ecmwf.properties`. **It is already an active
directive despite its leading `#`.** The include must not refer back to the
same file. See the [configuration syntax and loading rules](master.md#file-location-and-configuration-layers).

For example, a local override can change display preferences without copying
the complete template:

```ini
[MonitorPlugin]
productsToShowCount=15
productsToShowPeriod=72
authMode=password,otp

[Server]
globeImageryCacheDir=/var/lib/ecpds/monitor/globe-imagery
globeLabelsCacheDir=/var/lib/ecpds/monitor/globe-labels
```

Create persistent cache directories with write access for the Monitor service
account. Restart the Monitor after configuration changes unless the relevant
feature explicitly supports live updates.

## Reading this reference

**Code fallback** is the reader's value when an option is absent; **shipped
value** is the explicit template assignment. A dash means no assignment.
Multiple fallbacks are listed separately where consumers differ. `null` means
no fallback value, and is not a promise that the option is optional. Java
expressions indicate context-dependent defaults; links identify their readers.
Credentials are not reproduced.

Rows marked **template entry** have no direct literal reader in this process's
reviewed sources. A template reference or shared entry is not necessarily an
active Monitor setting; named-socket and open-map readers are described below.

Names are case-sensitive. Use explicit suffixes such as `30s` or `5m` for
duration-reading options: bare configured durations are **hours**.
Plain numeric options have their own units. In particular:

| Setting | Unit or scope |
|---------|---------------|
| `[Monitor] delay` | Duration for external service-status reporting, not web-page refresh. |
| `[MonitorPlugin] refreshDelay` | Milliseconds between background destination-status refreshes. |
| `statusCalculatorTimeout`, `destinationProductStatusResetterTimeout` | Minutes; watchdogs for stuck background tasks, not HTTP request timeouts. |
| `productsToShowPeriod` | Hours ahead in the product-status window. |
| `productStatusEventMaxAge` | Days. |
| Socket timeouts | Milliseconds. |

## Important distinctions

`[Monitor] activated` controls external monitoring integration. It does **not**
enable or disable the browser UI. `[MonitorPlugin] statusCalculator` controls
background status calculations.

`authMode` chooses which credential methods the login form displays
(`password`, `otp` or `password,otp`). It does not disable server-side
authentication or configure the OTP provider. See
[TOTP Authentication](../administration/totp.md).

`wantClientAuth=yes` asks for optional TLS client certificates; it does not
require clients to present one. The retained template flags `checkIsSecure`
and `production` have no direct readers in the current Monitor implementation;
they must not be treated as HTTPS enforcement or security-hardening switches.
See [TLS Certificates](../administration/certificates.md).

The Monitor uses both `[MonitorPlugin]` and selected `[HttpPlugin]` settings:
the latter controls shared Jetty sizing and HTTP/2 settings. TLS stores can
also be overridden by `[HttpPluginSSL]`.

`[Server] eventRepository` and `[StorageRepository] eventSize/eventDelay` are
retained shared template entries, not controls for the Master's event journal.
Configure operational-event export and journaling on the
[Master](master.md), not the Monitor.

## Option catalogue

This includes web UI readers and the shared infrastructure used by the Monitor.
Optional integrations do not become active merely because their options are
listed. File-content settings take a path when configured; their fallback is
content, not a path.

<!-- BEGIN PROPERTY CATALOGUE -->

### [AssistantService]

Optional document-grounded assistant and Ollama models. Paths select the document corpus and local RAG index; model names must exist on the configured Ollama service. Listing these settings does not deploy a model server.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `baseUrl` | `http://localhost:11434` | - | [AssistantService:180](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ai/AssistantService.java#L180) |
| `checkAnswerGroundedInDocs` | `true` | - | [AssistantService:112](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ai/AssistantService.java#L112) |
| `deepModelMaxChars` | `-1` | - | [AssistantService:105](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ai/AssistantService.java#L105) |
| `deepModelName` | `llama3.1:70b-instruct-q4_K_M` | - | [AssistantService:182](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ai/AssistantService.java#L182) |
| `docsPath` | `.` | - | [AssistantService:184](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ai/AssistantService.java#L184) |
| `embeddingBatchSize` | `64` | - | [AssistantService:104](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ai/AssistantService.java#L104) |
| `embeddingModelName` | `mxbai-embed-large` | - | [AssistantService:183](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ai/AssistantService.java#L183) |
| `fastModelName` | `qwen2.5:7b-instruct-q4_K_M` | - | [AssistantService:181](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ai/AssistantService.java#L181) |
| `filter` | `.md` | - | [AssistantService:185](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ai/AssistantService.java#L185) |
| `includeSegmentTitlesOrFilenames` | `false` | - | [AssistantService:108](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ai/AssistantService.java#L108) |
| `indexPath` | `rag-index` | - | [AssistantService:217](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ai/AssistantService.java#L217) |
| `minTokenLengthForReranking` | `3` | - | [AssistantService:107](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ai/AssistantService.java#L107) |
| `rewritePromptWithFastModel` | `true` | - | [AssistantService:110](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ai/AssistantService.java#L110) |
| `rrfK` | `60` | - | [AssistantService:218](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ai/AssistantService.java#L218) |
| `streamingTimeout` | `180` ms (duration fallback) | - | [AssistantService:416](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ai/AssistantService.java#L416) |
| `topK` | `5` | - | [AssistantService:106](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/ai/AssistantService.java#L106) |

### [CallBack]

Exported callback endpoint. `serverFactory` and `clientFactory` are Java socket-factory classes. This section also accepts the named socket overrides described below.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `clientFactory` | `null` (no reader fallback) | `ecmwf.common.rmi.RMIClientSocketFactory` | [CallBackObject:150](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/callback/CallBackObject.java#L150) |
| `keepAlive` | Inherits `[SocketConfig] keepAlive` | `yes` | [Template:82](https://github.com/ecmwf/open-ecpds/blob/master/etc/monitor/conf/ecmwf.properties#L82) |
| `listenAddress` | Inherits `[SocketConfig] listenAddress` | `${Login[internalAddress]}` | [Template:77](https://github.com/ecmwf/open-ecpds/blob/master/etc/monitor/conf/ecmwf.properties#L77) |
| `port` | `9000` | `${Ports[callBack]}` | [CallBackObject:54](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/callback/CallBackObject.java#L54) |
| `publicAddress` | Inherits `[SocketConfig] publicAddress` | `${Login[internalAddress]}` | [Template:78](https://github.com/ecmwf/open-ecpds/blob/master/etc/monitor/conf/ecmwf.properties#L78) |
| `serverFactory` | `null` (no reader fallback) | `ecmwf.common.rmi.RMIServerSocketFactory` | [CallBackObject:116](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/callback/CallBackObject.java#L116) |

### [DestinationActionForm]

Initial values for new/editable Destination forms, not retroactive changes to existing database records. Frequency and inactivity values use the form's millisecond representation; counts and size limits retain the Destination field semantics.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `dateFormat` | `yyyyMMdd` | - | [DestinationActionForm:193](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/controller/transfer/destination/DestinationActionForm.java#L193) |
| `hostForSourceName` | Empty string | `${defaultSourceHostId}` | [DestinationActionForm:208](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/controller/transfer/destination/DestinationActionForm.java#L208) |
| `masterURLs` | Empty list | `${master.urlList}` | [DestinationActionForm:1006](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/controller/transfer/destination/DestinationActionForm.java#L1006) |
| `maxConnections` | `1` | - | [DestinationActionForm:130](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/controller/transfer/destination/DestinationActionForm.java#L130) |
| `maxFileSize` | `-1` | - | [DestinationActionForm:142](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/controller/transfer/destination/DestinationActionForm.java#L142) |
| `maxInactivity` | `0` | - | [DestinationActionForm:145](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/controller/transfer/destination/DestinationActionForm.java#L145) |
| `maxPending` | `8000` | - | [DestinationActionForm:160](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/controller/transfer/destination/DestinationActionForm.java#L160) |
| `maxRequeue` | `-1` | - | [DestinationActionForm:157](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/controller/transfer/destination/DestinationActionForm.java#L157) |
| `maxStart` | `2` | - | [DestinationActionForm:154](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/controller/transfer/destination/DestinationActionForm.java#L154) |
| `resetFrequency` | `1200000` | - | [DestinationActionForm:139](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/controller/transfer/destination/DestinationActionForm.java#L139) |
| `retryCount` | `-1` | - | [DestinationActionForm:133](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/controller/transfer/destination/DestinationActionForm.java#L133) |
| `retryFrequency` | `30000` | - | [DestinationActionForm:136](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/controller/transfer/destination/DestinationActionForm.java#L136) |
| `startCount` | `0` | - | [DestinationActionForm:148](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/controller/transfer/destination/DestinationActionForm.java#L148) |
| `startFrequency` | `600000` | - | [DestinationActionForm:151](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/controller/transfer/destination/DestinationActionForm.java#L151) |

### [ECauthModule]

ECauth connection and host-list cache. Durations control connection timeout, refresh polling, refresh frequency and cached-entry lifetime. Credentials here identify the ECauth service account.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `connectionTimeout` | `10000` ms (duration fallback) | - | [Tools:163](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/security/Tools.java#L163); [Tools:253](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/security/Tools.java#L253); [Tools:359](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/security/Tools.java#L359) |

### [ECpds]

Remote Master registry host and port for the Monitor's management/data connection.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `host` | `null` (no reader fallback) | `${Login[masterServer]}` | [MasterManager:114](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterManager.java#L114) |
| `port` | `6601` | `${Ports[master]}` | [MasterManager:114](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterManager.java#L114) |

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

### [ExecutorManager]

Opt-in virtual-thread execution for the shared executor. This is not a general scheduler concurrency limit.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `allowVirtualThread` | `false` | - | [ExecutorManager:79](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ExecutorManager.java#L79) |

### [HostActionForm]

Initial form values for Host creation/editing. Boolean form values use `on`/`off`; frequency values are milliseconds. These do not rewrite existing Hosts.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `acquisitionFrequency` | `600000` | - | [HostActionForm:190](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/controller/transfer/host/HostActionForm.java#L190) |
| `active` | `on` | - | [HostActionForm:189](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/controller/transfer/host/HostActionForm.java#L189) |
| `automaticLocation` | `on` | - | [HostActionForm:183](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/controller/transfer/host/HostActionForm.java#L183) |
| `check` | `off` | - | [HostActionForm:192](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/controller/transfer/host/HostActionForm.java#L192) |
| `checkFrequency` | `600000` | - | [HostActionForm:191](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/controller/transfer/host/HostActionForm.java#L191) |
| `connections` | `0` | - | [HostActionForm:120](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/controller/transfer/host/HostActionForm.java#L120); [HostActionForm:185](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/controller/transfer/host/HostActionForm.java#L185) |
| `mailOnError` | `off` | - | [HostActionForm:194](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/controller/transfer/host/HostActionForm.java#L194) |
| `mailOnSuccess` | `off` | - | [HostActionForm:193](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/controller/transfer/host/HostActionForm.java#L193) |
| `maxConnections` | `1` | - | [HostActionForm:117](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/controller/transfer/host/HostActionForm.java#L117); [HostActionForm:184](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/controller/transfer/host/HostActionForm.java#L184) |
| `notifyOnce` | `on` | - | [HostActionForm:195](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/controller/transfer/host/HostActionForm.java#L195) |
| `retryCount` | `15` | - | [HostActionForm:123](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/controller/transfer/host/HostActionForm.java#L123); [HostActionForm:186](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/controller/transfer/host/HostActionForm.java#L186) |
| `retryFrequency` | `15000` | - | [HostActionForm:126](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/controller/transfer/host/HostActionForm.java#L126); [HostActionForm:187](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/controller/transfer/host/HostActionForm.java#L187) |

### [HostBean]

Host display/transfer-rate calculations. The defaults can be derived from the current transfer's sent-byte counters and duration; source links distinguish the calculation paths.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `duration` | `0` | - | [HostBean:994](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/dao/transfer/HostBean.java#L994) |
| `duration` | `duration` | - | [HostBean:998](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/dao/transfer/HostBean.java#L998) |
| `minSent` | `sent` | - | [HostBean:998](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/dao/transfer/HostBean.java#L998) |
| `minSent` | `curSent` | - | [HostBean:1020](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/dao/transfer/HostBean.java#L1020) |

### [HttpPlugin]

Jetty listener, pool, protocol and portal settings. Thread limits are counts; `idleThreadsTimeout` and `idleTimeout` are milliseconds; buffer/header limits are bytes. `http`/`https=-1` leaves that connector disabled. On Movers, S3 skew is seconds and S3 request limits are bytes; CORS and DNS mappings apply to portal paths. On the Monitor, only the shared Jetty/HTTP2 readers listed here apply; its HTTPS listener is in `[MonitorPlugin]`.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `http2Enabled` | `true` | - | [HttpPlugin:396](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/HttpPlugin.java#L396) |
| `idleThreadsTimeout` | `60000` | - | [HttpPlugin:209](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/HttpPlugin.java#L209) |
| `maxThreads` | `400` | - | [HttpPlugin:208](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/HttpPlugin.java#L208) |
| `minThreads` | `40` | - | [HttpPlugin:209](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/HttpPlugin.java#L209) |

### [HttpPluginSSL]

Optional explicit HTTPS store configuration. If this section exists, `keyStorePath` and `keyStorePassword` must be supplied: the normal `[Security]` fallback branch is not used. Trust-store values default to the selected key store; store type defaults to `PKCS12`.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `keyStorePassword` | `null` (no reader fallback) | - | [HttpPlugin:338](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/HttpPlugin.java#L338) |
| `keyStorePath` | `null` (no reader fallback) | - | [HttpPlugin:337](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/HttpPlugin.java#L337) |
| `keyStoreType` | `PKCS12` | - | [HttpPlugin:339](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/HttpPlugin.java#L339) |
| `trustStorePassword` | `storePassword` | - | [HttpPlugin:370](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/HttpPlugin.java#L370) |
| `trustStorePath` | `storePath` | - | [HttpPlugin:369](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/HttpPlugin.java#L369) |
| `trustStoreType` | `storeType` | - | [HttpPlugin:371](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/HttpPlugin.java#L371) |

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
| `externalAddress` | No direct literal reader; template entry | `${Login[hostName]}` | [Template:28](https://github.com/ecmwf/open-ecpds/blob/master/etc/monitor/conf/ecmwf.properties#L28) |
| `hostName` | `InetAddress.getLocalHost().getHostName()` | `${ecmwf.common.ecaccess.ECaccessInit.getHostName()}` | [PluginContainer:149](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/plugin/PluginContainer.java#L149); [GetSystemTopologyJsonAction:132](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/controller/monitoring/GetSystemTopologyJsonAction.java#L132) |
| `hostName` | `localhost` | `${ecmwf.common.ecaccess.ECaccessInit.getHostName()}` | [ECpdsClient:83](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/ecpds/ECpdsClient.java#L83) |
| `hostName` | `null` (no reader fallback) | `${ecmwf.common.ecaccess.ECaccessInit.getHostName()}` | [DestinationBean:1636](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/dao/transfer/DestinationBean.java#L1636); [MonitorServer:119](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/monitor/MonitorServer.java#L119) |
| `internalAddress` | No direct literal reader; template entry | `${monitor.internalAddress}` | [Template:31](https://github.com/ecmwf/open-ecpds/blob/master/etc/monitor/conf/ecmwf.properties#L31) |
| `masterServer` | No direct literal reader; template entry | `${master.address}` | [Template:25](https://github.com/ecmwf/open-ecpds/blob/master/etc/monitor/conf/ecmwf.properties#L25) |
| `password` | `null` (no reader fallback) | `Protected value/reference` | [MonitorServer:129](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/monitor/MonitorServer.java#L129) |
| `root` | `Cnf.at("Login", "hostName")` | - | [MonitorServer:119](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/monitor/MonitorServer.java#L119) |
| `service` | `Cnf.at("Login", "hostName", InetAddress.getLocalHost().getHostName())` | `ECpdsMonitor` | [PluginContainer:149](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/plugin/PluginContainer.java#L149) |
| `service` | `ECpdsMonitor` | `ECpdsMonitor` | [MonitorServer:151](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/monitor/MonitorServer.java#L151) |

### [MasterManager]

Monitor-side cache of Master metadata. Enabling it reduces remote lookups but depends on callback/cache synchronization.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `useCache` | `false` | `${monitor.useCache}` | [MasterManager:65](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/MasterManager.java#L65) |

### [Monitor]

External service-status integration, independent of the web monitoring UI. `activated` enables it, `debug` logs submissions and `dontSend` suppresses delivery. `delay` is a duration: the template's bare `120` means 120 hours, whereas the code fallback is five minutes.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `activated` | `false` | `no` | [MonitorManager:72](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/monitor/MonitorManager.java#L72) |
| `debug` | `false` | `no` | [MonitorManager:78](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/monitor/MonitorManager.java#L78) |
| `delay` | `300000` ms (duration fallback) | `120` | [MonitorManager:75](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/monitor/MonitorManager.java#L75) |
| `dontSend` | `false` | `no` | [MonitorManager:81](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/monitor/MonitorManager.java#L81) |

### [MonitorPlugin]

Monitor HTTPS, status calculation, display and directory-integration options. Watchdogs are minutes; refresh is milliseconds; product look-ahead is hours; product event age is days; `maxFormContentSize` is bytes. Product counts limit the normal display window, with grouped entries handled separately. LDAP settings are used by the optional directory lookup, not by the password/OTP form selector.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `authMode` | `password,otp` | `password` | [LoginAction:111](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/controller/login/LoginAction.java#L111) |
| `checkIsSecure` | No direct literal reader; template entry | `yes` | [Template:122](https://github.com/ecmwf/open-ecpds/blob/master/etc/monitor/conf/ecmwf.properties#L122) |
| `compression` | `true` | - | [HttpPlugin:412](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/HttpPlugin.java#L412) |
| `contactsFileName` | `null` (no reader fallback) | - | [MonitoringRequest:110](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/controller/monitoring/MonitoringRequest.java#L110) |
| `cookie` | `"ecpds" + httpsPort` | `ecpdsv2` | [HttpPlugin:271](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/HttpPlugin.java#L271) |
| `destinationProductStatusResetterTimeout` | `15` | `20` | [PDSContextListener:101](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/controller/PDSContextListener.java#L101) |
| `destinationsPerColumn` | `25` | `25` | [GetDestinationAction:82](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/controller/transfer/destination/GetDestinationAction.java#L82) |
| `destinationsPerPageOrColumn` | `46` | `46` | [MonitoringRequest:93](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/controller/monitoring/MonitoringRequest.java#L93) |
| `enabledProtocols` | `TLSv1.3,TLSv1.2` | `TLSv1.3,TLSv1.2` | [HttpPlugin:361](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/HttpPlugin.java#L361) |
| `excludeCipherSuites` | `"SSL_RSA_WITH_DES_CBC_SHA", "SSL_DHE_RSA_WITH_DES_CBC_SHA", "SSL_DHE_DSS_WITH_DES_CBC_SHA", "SSL_RSA_EXPORT_WITH_RC4_40_MD5", "SSL_RSA_EXPORT_WITH_DES40_CBC_SHA", "SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA", "SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA"` | - | [HttpPlugin:380](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/HttpPlugin.java#L380) |
| `extension` | Empty string | - | [ActiveDirectory:89](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/controller/monitoring/ActiveDirectory.java#L89) |
| `extraEmails` | `delay@my.domain` | - | [ActiveDirectory:86](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/controller/monitoring/ActiveDirectory.java#L86) |
| `htdocs` | `null` (no reader fallback) | `${monitor.htdocs}` | [HttpPlugin:204](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/HttpPlugin.java#L204) |
| `https` | `-1` | `${Ports[https]}` | [HttpPlugin:198](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/HttpPlugin.java#L198) |
| `initialiseEventHandler` | `true` | - | [HttpPlugin:437](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/HttpPlugin.java#L437) |
| `ldapDomain` | `my.domain` | - | [ActiveDirectory:83](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/controller/monitoring/ActiveDirectory.java#L83) |
| `ldapHost` | `localhost` | - | [ActiveDirectory:77](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/controller/monitoring/ActiveDirectory.java#L77) |
| `ldapPassword` | Empty string | - | [ActiveDirectory:74](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/controller/monitoring/ActiveDirectory.java#L74) |
| `ldapPort` | `389` | - | [ActiveDirectory:80](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/controller/monitoring/ActiveDirectory.java#L80) |
| `ldapUser` | Empty string | - | [ActiveDirectory:71](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/controller/monitoring/ActiveDirectory.java#L71) |
| `listenAddress` | Inherits `[SocketConfig] listenAddress` | `${Login[externalAddress]}` | [Template:110](https://github.com/ecmwf/open-ecpds/blob/master/etc/monitor/conf/ecmwf.properties#L110) |
| `loginAnimatedBackground` | `true` | `true` | [LoginAction:69](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/controller/login/LoginAction.java#L69); [LogoutAction:60](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/controller/login/LogoutAction.java#L60) |
| `maxFormContentSize` | `200000` | - | [HttpPlugin:233](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/HttpPlugin.java#L233) |
| `productStatusEventMaxAge` | `7` | `7` | [MonitoringEventHandler:69](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/dao/monitoring/MonitoringEventHandler.java#L69) |
| `production` | No direct literal reader; template entry | `false` | [Template:126](https://github.com/ecmwf/open-ecpds/blob/master/etc/monitor/conf/ecmwf.properties#L126) |
| `productsToShowCount` | `10` | `10` | [MonitoringRequest:97](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/controller/monitoring/MonitoringRequest.java#L97) |
| `productsToShowHeaderCount` | `80` | `50` | [MonitoringRequest:100](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/controller/monitoring/MonitoringRequest.java#L100) |
| `productsToShowMonitoredOnly` | `false` | `no` | [MonitoringRequest:106](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/controller/monitoring/MonitoringRequest.java#L106) |
| `productsToShowPeriod` | `168` | `168` | [MonitoringRequest:103](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/controller/monitoring/MonitoringRequest.java#L103) |
| `refreshDelay` | `20000` | `20000` | [DestinationProductStatusResetterTask:77](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/dao/monitoring/DestinationProductStatusResetterTask.java#L77) |
| `statusCalculator` | `false` | `true` | [PDSContextListener:93](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/controller/PDSContextListener.java#L93); [GetTopLevelDisplayAction:55](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/controller/monitoring/GetTopLevelDisplayAction.java#L55) |
| `statusCalculatorTimeout` | `15` | `15` | [PDSContextListener:97](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/controller/PDSContextListener.java#L97) |
| `wantClientAuth` | `false` | `no` | [HttpPlugin:359](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/HttpPlugin.java#L359) |

### [Other]

Shared utilities. `buffer` is an I/O buffer size in bytes; `ticketWaitDuration` is a raw millisecond timeout. `ecauthTokenCache` enables token caching. `dontCheckIp` bypasses the provider's client-address check during subscription. `tmp` is retained in templates; the launcher separately sets the JVM temporary directory.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `buffer` | No direct literal reader; template entry | `131072` | [Template:175](https://github.com/ecmwf/open-ecpds/blob/master/etc/monitor/conf/ecmwf.properties#L175) |
| `tmp` | No direct literal reader; template entry | `${monitor.tmp}` | [Template:176](https://github.com/ecmwf/open-ecpds/blob/master/etc/monitor/conf/ecmwf.properties#L176) |

### [PluginList]

Template-defined configuration; see the reader links for its consuming component.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `http` | No plugin definition | `ecmwf.ecpds.master.plugin.http.HttpPlugin@${monitor.lib}/plugin/http` | [Template:42](https://github.com/ecmwf/open-ecpds/blob/master/etc/monitor/conf/ecmwf.properties#L42) |

### [Ports]

Template port substitutions. Most keys are only referenced by listener sections and therefore have no independent code default. `ecpds` also has a direct client-reader fallback.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `callBack` | No direct literal reader; template entry | `${port.callback}` | [Template:37](https://github.com/ecmwf/open-ecpds/blob/master/etc/monitor/conf/ecmwf.properties#L37) |
| `ecpds` | `6640` | - | [ECpdsClient:86](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/ecpds/ECpdsClient.java#L86) |
| `https` | No direct literal reader; template entry | `${port.https}` | [Template:36](https://github.com/ecmwf/open-ecpds/blob/master/etc/monitor/conf/ecmwf.properties#L36) |
| `master` | No direct literal reader; template entry | `${port.master}` | [Template:38](https://github.com/ecmwf/open-ecpds/blob/master/etc/monitor/conf/ecmwf.properties#L38) |

### [RMIClientSocketFactory]

Template-defined configuration; see the reader links for its consuming component.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `interruptible` | Inherits `[SocketConfig] interruptible` | `yes` | [Template:95](https://github.com/ecmwf/open-ecpds/blob/master/etc/monitor/conf/ecmwf.properties#L95) |
| `keepAlive` | Inherits `[SocketConfig] keepAlive` | `yes` | [Template:96](https://github.com/ecmwf/open-ecpds/blob/master/etc/monitor/conf/ecmwf.properties#L96) |
| `listenAddress` | Inherits `[SocketConfig] listenAddress` | `${Login[internalAddress]}` | [Template:93](https://github.com/ecmwf/open-ecpds/blob/master/etc/monitor/conf/ecmwf.properties#L93) |
| `publicAddress` | Inherits `[SocketConfig] publicAddress` | `${Login[internalAddress]}` | [Template:94](https://github.com/ecmwf/open-ecpds/blob/master/etc/monitor/conf/ecmwf.properties#L94) |

### [RMIServerSocketFactory]

Template-defined configuration; see the reader links for its consuming component.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `interruptible` | Inherits `[SocketConfig] interruptible` | `yes` | [Template:88](https://github.com/ecmwf/open-ecpds/blob/master/etc/monitor/conf/ecmwf.properties#L88) |
| `keepAlive` | Inherits `[SocketConfig] keepAlive` | `yes` | [Template:89](https://github.com/ecmwf/open-ecpds/blob/master/etc/monitor/conf/ecmwf.properties#L89) |
| `listenAddress` | Inherits `[SocketConfig] listenAddress` | `${Login[internalAddress]}` | [Template:86](https://github.com/ecmwf/open-ecpds/blob/master/etc/monitor/conf/ecmwf.properties#L86) |
| `publicAddress` | Inherits `[SocketConfig] publicAddress` | `${Login[internalAddress]}` | [Template:87](https://github.com/ecmwf/open-ecpds/blob/master/etc/monitor/conf/ecmwf.properties#L87) |

### [ResourceTracker]

Shared runtime-resource diagnostics. `defaultDebugFrequency` counts resource-close events between debug reports; it is not a time interval.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `defaultDebugFrequency` | `10000` | - | [ResourceTracker:45](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ResourceTracker.java#L45) |
| `enabled` | `true` | - | [ResourceTracker:42](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/technical/ResourceTracker.java#L42) |

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
| `SSLKeyStore` | `System.getProperty("javax.net.ssl.keyStore")` | `${monitor.etc}/ecpds-monitor.pfx` | [SSLSocketFactory:64](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/security/SSLSocketFactory.java#L64) |
| `SSLKeyStore` | `null` (no reader fallback) | `${monitor.etc}/ecpds-monitor.pfx` | [HttpPlugin:343](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/HttpPlugin.java#L343); [HttpPlugin:349](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/HttpPlugin.java#L349) |
| `SSLKeyStore` | `System.getProperty("javax.net.ssl.keyStore", null)` | `${monitor.etc}/ecpds-monitor.pfx` | [HttpPlugin:342](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/HttpPlugin.java#L342) |
| `SSLKeyStoreAlgorithm` | `System.getProperty("sun.SSL.keymanager.type", "SunX509")` | - | [SSLSocketFactory:76](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/security/SSLSocketFactory.java#L76) |
| `SSLKeyStorePassword` | `System.getProperty("javax.net.ssl.keyStorePassword")` | `Protected value/reference` | [SSLSocketFactory:68](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/security/SSLSocketFactory.java#L68) |
| `SSLKeyStorePassword` | `null` (no reader fallback) | `Protected value/reference` | [HttpPlugin:345](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/HttpPlugin.java#L345) |
| `SSLKeyStorePassword` | `System.getProperty("javax.net.ssl.keyStorePassword", null)` | `Protected value/reference` | [HttpPlugin:344](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/HttpPlugin.java#L344) |
| `SSLKeyStoreType` | `System.getProperty("javax.net.ssl.keyStoreType", "PKCS12")` | - | [SSLSocketFactory:72](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/security/SSLSocketFactory.java#L72); [HttpPlugin:346](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/HttpPlugin.java#L346) |
| `SSLTrustStore` | `System.getProperty("javax.net.ssl.trustStore", KEY_STORE_PATH)` | - | [SSLSocketFactory:80](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/security/SSLSocketFactory.java#L80) |
| `SSLTrustStoreAlgorithm` | `System.getProperty("sun.SSL.trustmanager.type", KEY_STORE_ALGORITHM)` | - | [SSLSocketFactory:92](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/security/SSLSocketFactory.java#L92) |
| `SSLTrustStorePassword` | `System.getProperty("javax.net.ssl.trustStorePassword", KEY_STORE_PASSWORD)` | - | [SSLSocketFactory:84](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/security/SSLSocketFactory.java#L84) |
| `SSLTrustStorePassword` | `System.getProperty("javax.net.ssl.trustStorePassword", storePassword)` | - | [HttpPlugin:374](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/HttpPlugin.java#L374) |
| `SSLTrustStorePath` | `System.getProperty("javax.net.ssl.trustStorePath", storePath)` | - | [HttpPlugin:373](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/HttpPlugin.java#L373) |
| `SSLTrustStoreType` | `System.getProperty("javax.net.ssl.trustStoreType", KEY_STORE_TYPE)` | - | [SSLSocketFactory:88](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/security/SSLSocketFactory.java#L88) |
| `SSLTrustStoreType` | `System.getProperty("javax.net.ssl.trustStoreType", storeType)` | - | [HttpPlugin:375](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/HttpPlugin.java#L375) |
| `SSLVersion` | `TLSv1.3` | - | [SSLSocketFactory:61](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/security/SSLSocketFactory.java#L61) |
| `checkCertificates` | `true` | - | [SSLSocketFactory:373](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/security/SSLSocketFactory.java#L373) |
| `cliSharedSecret` | Empty string | - | [SimplePlugin:62](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/plugin/SimplePlugin.java#L62); [ECpdsClient:98](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/ecpds/ECpdsClient.java#L98) |

### [Server]

Per-process service switches and local paths. Master readers enable schedulers/repositories; Mover readers enable its own repositories/accounting. `dontRecord*` suppresses management-action history, not native telemetry globally. Globe cache settings belong to the Monitor. `providerMBeanTimeout` is milliseconds; topology probe timeout is a duration.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `anonymousPass` | `anonymous` | - | [MonitoringParametersBaseBean:149](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/dao/transfer/MonitoringParametersBaseBean.java#L149); [MonitoringParametersBaseBean:158](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/dao/transfer/MonitoringParametersBaseBean.java#L158) |
| `anonymousUser` | `anonymous` | - | [ManagementProxy:426](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/ManagementProxy.java#L426); [PDSAction:91](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/controller/PDSAction.java#L91); [MonitoringParametersBaseBean:148](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/dao/transfer/MonitoringParametersBaseBean.java#L148); [MonitoringParametersBaseBean:157](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/dao/transfer/MonitoringParametersBaseBean.java#L157) |
| `anonymousUser` | `"anonymous".equals(id)` | - | [UserDAOHandler:169](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/dao/users/UserDAOHandler.java#L169); [UserDAOHandler:205](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/dao/users/UserDAOHandler.java#L205); [UserDAOHandler:226](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/dao/users/UserDAOHandler.java#L226) |
| `eventRepository` | No direct literal reader; template entry | `no` | [Template:159](https://github.com/ecmwf/open-ecpds/blob/master/etc/monitor/conf/ecmwf.properties#L159) |
| `globeImageryCacheDir` | `System.getProperty("java.io.tmpdir") + File.separator + "ecpds-globe-imagery"` | - | [GlobeImageryProvisioner:133](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/GlobeImageryProvisioner.java#L133) |
| `globeImageryMinHeapMB` | `1024` | - | [GlobeImageryProvisioner:115](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/GlobeImageryProvisioner.java#L115) |
| `globeLabelsCacheDir` | `System.getProperty("java.io.tmpdir") + File.separator + "ecpds-globe-labels"` | - | [GlobeLabelsProvisioner:107](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/ecpds/master/plugin/http/GlobeLabelsProvisioner.java#L107) |

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
| `backLog` | `100` | `500` | [SocketConfig:158](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/rmi/SocketConfig.java#L158) |
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

### [StorageRepository]

Asynchronous persistence. `*Size` means storage-worker thread count, **not queue capacity or batch size**. `*Delay`, idle waits and ticket timeouts are milliseconds. `*MaxAuthorisedSize` caps repository entries (`0` leaves the cap unset). Options only apply to repository types instantiated by this process.

| Option | Code fallback when absent | Shipped value | Reader |
|--------|---------------------------|---------------|--------|
| `eventDelay` | No direct literal reader; template entry | `1000` | [Template:171](https://github.com/ecmwf/open-ecpds/blob/master/etc/monitor/conf/ecmwf.properties#L171) |
| `eventSize` | No direct literal reader; template entry | `20` | [Template:170](https://github.com/ecmwf/open-ecpds/blob/master/etc/monitor/conf/ecmwf.properties#L170) |

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

### Named socket sections

[`SocketConfig.load`](https://github.com/ecmwf/open-ecpds/blob/master/src/main/java/ecmwf/common/rmi/SocketConfig.java#L246) reads the following keys from a
component's named section. Common section names for this service are `CallBack`, `RMIServerSocketFactory`, `RMIClientSocketFactory` and `MonitorPlugin` (the latter uses the socket configuration for its bind address, not every raw-socket option).
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
