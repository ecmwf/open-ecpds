# ECaccess user directory and import

The optional `ecpds-ecaccess` extension implements ECCMD user-directory lookup
for the OpenECPDS Master. It runs against an **external**
ECCMD daemon; it does not deploy a gateway, ECCMD server, JBoss or an EJB server.

It supplies `UserDirectoryProvider`, not credential authentication. The core
remains responsible for local account activation, WebUser permissions and
database persistence. The same optional jar also provides
[ECaccess gateway transfers](../transfer-modules/ecaccess.md), enabled separately
through the generic remote-service SPI.

## Build the client contracts and extension

The adapter is excluded from ordinary OpenECPDS builds. It depends on a small
client-contract jar containing ECCMD's RMI interfaces and data types. The wire
package names are necessary to deserialize ECCMD stubs.

Obtain `ecaccess-eccmd-client-1.0.0.jar` from your ECCMD service administrator
or your organization's artifact repository. This external dependency is not
built by OpenECPDS. If it is not available through your Maven repositories,
install the supplied JAR locally, then build the extension:

```sh
cd /path/to/open-ecpds

mvn install:install-file \
  -Dfile=/path/to/ecaccess-eccmd-client-1.0.0.jar \
  -DgroupId=ecmwf -DartifactId=ecaccess-eccmd-client \
  -Dversion=1.0.0 -Dpackaging=jar -DgeneratePom=true

mvn -Pecaccess-extension -pl ecpds-ecaccess -am package -DskipTests
```

The client-contract JAR must contain only the required client interfaces and data
types, not server applications or overlapping core implementations. `1.0.0`
identifies the contract artifact, not the ECCMD daemon version. Use an artifact
repository for reproducible builds beyond a local checkout.

Install the built `ecpds-ecaccess/target/ecpds-ecaccess-<version>.jar` and
`ecaccess-eccmd-client-1.0.0.jar` on the **Master application classpath**, alongside
the matching OpenECPDS core and runtime libraries. The adapter jar does not bundle
core classes. Do not install the legacy `eccmd.jar`, `gateway.jar` or `monitor.jar`
alongside upstream: they contain overlapping implementations.

The adapter uses the existing `RMIClientSocketFactory` transport and ECCMD's
`EccmdEngine` registry binding. The extracted contracts retain other signature
types required to load the full remote proxy, but no server implementation or
job scheduler is packaged.

## Master configuration

Add these sections to the Master's local `ecmwf.properties` override:

```ini
[UserDirectoryExtension]
class=ecmwf.ecpds.extension.ecaccess.EcaccessIdentityProvider
host=eccmd.example.org
port=640

[RMIClientSocketFactory]
connectTimeOut=10000
soTimeOut=10000
```

No global ECCMD singleton is installed.
The two effective socket timeouts must be between 1 and 60,000 milliseconds
before the extension can start. Values in `[RMIClientSocketFactory]` override
the inherited `[SocketConfig]` defaults; they need not be repeated when those
defaults are already suitable. These settings are shared with other RMI clients
using that factory on the Master; choose values suitable for that deployment.
Keep the existing trusted RMI network/TLS setup: the extension does not turn
plain RMI into a new encrypted transport.

`host` is required and accepts up to eight comma-, semicolon- or pipe-separated
hosts. `port` defaults to 640 and accepts 1 through 65535. Hosts are tried in
configured order when discovering a connection. Missing providers preserve the
core's default behavior. Explicit registration activates the extension.

Restart the Master after changes. A configured provider with invalid options
fails startup. Daemon availability is checked on demand, so an unavailable
daemon rejects the current request rather than preventing Master startup.

## Authentication policy

This extension never sends passwords or passcodes to ECCMD and does not implement
`AuthenticationProvider`. Register `EcaccessIdentityProvider` only under
`[UserDirectoryExtension]`; incompatible provider registrations fail startup.

The existing `[TOTP]` token endpoint and password/passcode client settings remain in
the core. Static/certificate paths remain unchanged. OpenECPDS's generic authentication
and interactive-login SPIs remain available for other providers; they are independent
of this directory extension.

## User import and refresh

The directory role calls ECCMD `getECUser(user)` for a locally missing ECUser.
A null result means absent; errors remain lookup failures. The core imports the
ECUser through its existing persistence path. Import does not automatically grant
WebUser permissions.

Configured scheduled ECUser refreshes also use this directory hook and the
core's existing update handling. No authentication or per-thread identity cache
is involved.

The adapter uses on-demand discovery and the configured socket waits. Operators should retry
lookup after restoring the ECCMD service.
