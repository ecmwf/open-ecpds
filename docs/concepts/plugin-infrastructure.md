# Plugin Infrastructure

OpenECPDS supports complementary extension points for identity and data exchange:

- **Identity Extensions** — alternative Master WebUser authentication and external user lookup
- **Transfer Modules** — extend *outgoing* connections (acquisition and dissemination)
- **Server Plugins** — extend *incoming* connections (Data Portal)

Implement the relevant interface and explicitly register its implementation.
Transfer modules and server plugins retain their existing loaders; identity
extensions use the startup-only SPI described below.

## Identity extensions

The `ecmwf.ecpds.extension` package provides the initial OpenECPDS Extension SPI:

| Interface | Purpose |
|---|---|
| `AuthenticationProvider` | Validate credentials for the requested Master WebUser |
| `UserDirectoryProvider` | Supply a missing ECUser from an external directory |
| `InteractiveLoginProvider` | Browser login returning a verified issuer and subject |
| `Extension` | API-version check, startup and shutdown |

These are **Master-side hooks**, not a replacement for Data Portal authentication.
Authentication does not grant permissions, create a WebUser or bypass the active
WebUser check. The core retains authorization and database persistence.

### Installation and configuration

Compile providers against the matching `ecmwf:ecpds-core` artifact. Do not bundle
copies of OpenECPDS classes in the extension jar. Install trusted provider jars
and their dependencies on the Master application classpath. A `[PluginList]`
entry or transfer-module archive does not make an identity provider available.

Enable providers explicitly in the Master's local `ecmwf.properties` override:

```ini
[AuthenticationExtension]
class=org.example.ecpds.CompanyAuthentication
endpoint=https://identity.example.invalid

[UserDirectoryExtension]
class=org.example.ecpds.CompanyDirectory
endpoint=https://directory.example.invalid
```

The class names above illustrate administrator-supplied implementations; they
are not bundled providers. Each class must be public, implement the appropriate
SPI interface, and have a public no-argument constructor. Missing or blank
`class` disables that role. Installing a jar alone never activates it.

Providers receive an immutable configuration snapshot through
`ExtensionContext.option(name)` and the local service identity through `root()`.
Additional keys belong to the provider. Protect configuration containing secrets;
never log credentials or option values.

One instance is created per configured role, even if both roles name the same
class. Authentication starts before the directory; shutdown is in reverse
order. A failed startup closes instantiated providers and fails Master startup,
rather than silently reverting to built-in authentication. An incompatible
`apiVersion()` is rejected before `start` is called.

Providers must be thread-safe, bound their network waits and release resources
in `close`, including resources allocated during failed initialization.
Configuration and implementation changes require a Master restart. Extensions
are trusted in-process Java code, not sandboxed applications.

### Authentication policy

The existing checks and authentication ordering remain in place:

1. Empty credentials are rejected; the ECUser must exist or be imported, and a
   corresponding active WebUser must already exist locally.
2. Existing configured static/anonymous and certificate authentication paths
   retain precedence.
3. If TOTP is active, its existing authentication path retains precedence.
4. Otherwise a configured `AuthenticationProvider` replaces local WebUser
   password validation. Without a configured provider, local validation is unchanged.

An authentication provider returns `ACCEPTED`, `REJECTED` or `UNSUPPORTED`.
**Only `ACCEPTED` succeeds.** Neither rejection, an unsupported request, a null
result nor a provider outage falls back to a local password. Throw `IOException`
for an outage; return `REJECTED` for invalid credentials. Provider failures are
reported to clients as a generic authentication failure.

During login, directory lookup runs only when the ECUser is missing locally. Return an ECUser
with exactly the requested name, or `null` when absent; throw `IOException` for
an outage. Imported users do not automatically receive a WebUser or permissions.
Providers can override `authenticateIdentity` to return an accepted result with
fresh ECUser data. The core verifies the exact requested name and persists the
update before accepting login; failed persistence fails authentication. Providers
that implement only `authenticate` retain their original behavior. Existing
scheduled ECUser refreshes, when configured, also use the directory lookup hook.

### Minimal authentication implementation

```java
public final class CompanyAuthentication implements AuthenticationProvider {
    private IdentityClient client;

    @Override
    public int apiVersion() {
        return API_VERSION;
    }

    @Override
    public void start(ExtensionContext context) throws IOException {
        client = new IdentityClient(context.option("endpoint"));
    }

    @Override
    public Result authenticate(String user, String credentials, String origin)
            throws IOException {
        return client.validate(user, credentials) ? Result.ACCEPTED : Result.REJECTED;
    }

    @Override
    public void close() throws IOException {
        if (client != null) {
            client.close();
        }
    }
}
```

This sketch uses an integration-specific `IdentityClient`; it is not an
OpenECPDS class. Its implementation must set connection/read timeouts and keep
credentials out of logs and exception messages.

### ECaccess integration boundary

The optional `ecpds-ecaccess` module implements ECCMD authentication and directory
lookup without copying `MasterServer`. It is built only with the
`ecaccess-extension` Maven profile and a separately extracted client-contract jar.
See [ECaccess identity](../administration/ecaccess-identity.md) for build,
configuration and passcode semantics.
Gateway transfers still require an additional cross-service integration boundary:
the existing ECaccess-specific Mover/Master remote-access plumbing is not supplied
by the identity SPI. This SPI does not enable arbitrary remote extension calls.

## Interactive login extensions

Interactive login is independent of credential validation. An implementation of
`InteractiveLoginProvider` supplies a display name, an HTTPS authorization URL
and protocol validation returning `ExternalIdentity(issuer, subject)`.

The Master owns random state, nonce, S256 PKCE secrets, expiry, single-use
transactions and explicit local-account mapping. The Monitor keeps a random
binding in its HTTPS browser session and uses the existing management connection
to start and complete login. Neither the Monitor nor a browser may submit a
trusted username or identity in place of protocol verification.

Enable one provider with `[InteractiveLoginExtension] class` on the Master.
Map verified identities through `[InteractiveLoginMapping]`, with the local
WebUser ID as key and a JSON object containing `issuer` and `subject` as value.
Duplicate identities fail startup. Unmapped identities and missing, disabled or
anonymous local accounts cannot log in. No account provisioning or permission
assignment occurs.

The provider uses the same API-version and startup/shutdown lifecycle as other
identity extensions. It starts after authentication and directory providers.
An active interactive provider adds a separate button; existing credential,
certificate and TOTP login behavior is unchanged. Interactive login deliberately
does not run through the credential-validation SPI or require a local password.

The initial bundled example is
`ecmwf.ecpds.extension.oidc.OidcLoginProvider`, implementing OIDC authorization-code
login. See [OpenID Connect login](../administration/openid-connect.md) for setup,
supported provider features and operational requirements.

Extension implementations receive protocol secrets and are trusted application
code. They must verify the external protocol before returning an identity,
bound network waits and avoid logging codes, tokens or credentials. Local logout
clears the pending browser transaction and the application user; federated
provider logout is not part of this initial interface.

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

These extension points are designed to be extended. If you need a protocol that is not yet
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
