# Plugin Infrastructure

OpenECPDS supports two complementary extension frameworks that together cover both sides
of data exchange:

- **Transfer Modules** — extend *outgoing* connections (acquisition and dissemination)
- **Server Plugins** — extend *incoming* connections (Data Portal)

Both frameworks follow the same principle: implement a well-defined interface, register
the class in configuration, and the platform handles lifecycle, threading, connection
management, and monitoring automatically.

## Transfer Modules (outgoing)

Transfer modules plug into the ECtrans transfer engine used by **Data Acquisition** and
**Data Dissemination**. Each module implements a protocol adapter that the engine calls
to connect, authenticate, transfer data, list directories, and clean up.

Built-in modules cover:

| Module | Protocol | Page |
|--------|----------|------|
| `FTP` | FTP | [ftp](../transfer-modules/ftp.md) |
| `FTPS` | FTP over TLS | [ftps](../transfer-modules/ftps.md) |
| `SFTP` | SSH File Transfer | [sftp](../transfer-modules/sftp.md) |
| `HTTP` | HTTP / HTTPS | [http](../transfer-modules/http.md) |
| `S3` | Amazon S3 | [s3](../transfer-modules/s3.md) |
| `Azure` | Azure Blob Storage | [azure](../transfer-modules/azure.md) |
| `GCS` | Google Cloud Storage | [gcs](../transfer-modules/gcs.md) |
| `ECauth` | SSH/Telnet via ECaccess | [ecauth](../transfer-modules/ecauth.md) |
| `Portal` | Local Data Portal staging | [portal](../transfer-modules/portal.md) |

### Adding a new transfer module

A new outgoing protocol is added by implementing the ECtrans module interface and
referencing the class from a host configuration. No changes to the core platform are
required. The module receives connection parameters, handles the protocol lifecycle, and
returns results to the transfer engine.

## Server Plugins (incoming)

Server plugins extend the Data Portal with new *incoming* protocols. Each plugin runs
as an independent server, listening on its own port, authenticating users against the
OpenECPDS user database, and delegating data operations to the mover's virtual
filesystem.

The base classes in `ecmwf.common.plugin` provide:

| Class | Purpose |
|-------|---------|
| `PluginThread` | Base thread lifecycle and caller-injection mechanism |
| `ServerPlugin` | Adds TCP listen loop, per-connection threading, connection limiting, JMX monitoring |
| `SimplePlugin` | Adds command-dispatch loop, HMAC authentication helpers, and stream utilities |

Built-in server plugins on the Data Mover:

| Plugin class | Protocol | Incoming port |
|---|---|---|
| `ecmwf.common.ftpd.FtpPlugin` | FTP | configurable (`PORT_FTP`) |
| `ecmwf.ecpds.mover.plugin.ssh.SshPlugin` | SFTP / SCP | configurable (`PORT_SSH`) |
| `ecmwf.ecpds.mover.plugin.http.HttpPlugin` | HTTPS / S3 / WebDAV | configurable (`PORT_HTTPS`) |
| `ecmwf.ecpds.mover.plugin.mqtt.MqttPlugin` | MQTTS | configurable (`PORT_MQTTS`) |
| `ecmwf.ecpds.mover.plugin.ecproxy.ECproxyPlugin` | ECproxy (internal) | configurable |

Plugins are registered in `ecmwf.properties` under `[PluginList]`:

```properties
[PluginList]
http=ecmwf.ecpds.mover.plugin.http.HttpPlugin
mqtt=ecmwf.ecpds.mover.plugin.mqtt.MqttPlugin
ftp=ecmwf.common.ftpd.FtpPlugin,maxConnections=2000,inverseResolution=no
ssh=ecmwf.ecpds.mover.plugin.ssh.SshPlugin
ecproxy=ecmwf.ecpds.mover.plugin.ecproxy.ECproxyPlugin,maxConnections=2000,inverseResolution=no
```

### Adding a new server plugin

To add a new incoming protocol:

1. Extend `ServerPlugin` (or `SimplePlugin` for text-command protocols) from
   `ecmwf.common.plugin`.
2. Implement the required abstract methods:
   - `getPort()` — the TCP port to listen on
   - `newInstance()` — factory method called per accepted connection
   - `startConnection(Socket)` — handle one client session
   - `refuseConnection(Socket, int)` — called when connection limits are exceeded
3. Register the class in `[PluginList]` in `ecmwf.properties`.

The framework handles TCP accept loop, per-connection thread allocation, maximum
connection enforcement, JMX MBean registration, and graceful shutdown automatically.

## Extending OpenECPDS

Both frameworks are designed to be extended. If you need a protocol that is not yet
supported — whether outgoing or incoming — ECMWF is happy to help guide or collaborate
on integration and development.

!!! tip "Suggest a new protocol"
    Open a discussion or issue in the
    [GitHub repository](https://github.com/ecmwf/open-ecpds) to suggest a new transfer
    module or server plugin. See [Contributing](../contributing.md) for more details.

## Related

- [Transfer Modules overview](../transfer-modules/index.md)
- [Protocols & Connections](protocols.md)
- [Data Portal](../use-cases/data-portal.md)
- [Architecture Overview](../architecture/overview.md)
- [Contributing](../contributing.md)
