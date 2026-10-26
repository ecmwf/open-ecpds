# ECaccess Master authentication and user import

The optional `ecpds-ecaccess` extension implements the ECCMD identity behavior
previously embedded in ECaccess-J's ECPDS Master. It runs against an **external**
ECCMD daemon; it does not deploy a gateway, ECCMD server, JBoss or an EJB server.

It supplies both `AuthenticationProvider` and `UserDirectoryProvider`. The core
remains responsible for local account activation, WebUser permissions and
database persistence. This extension does not implement `ECaccessModule` gateway
transfers; that integration is separate.

## Build the client contracts and extension

The adapter is excluded from ordinary OpenECPDS builds. It depends on a small
client-contract jar built from ECaccess-J's original RMI interfaces and data
types. Those original package names are necessary to deserialize ECCMD stubs;
they are not duplicates of the equivalent types under OpenECPDS's new packages.

Build OpenECPDS's core first and use its matching runtime dependencies:

```sh
cd /path/to/open-ecpds
mvn -pl ecpds-core -am package -DskipTests

ant -f /path/to/ECaccess-J/ant/ecpds-client.xml \
  -Dopenecpds.core=/path/to/open-ecpds/ecpds-core/target/ecmwf-8.2.1-20102026.jar \
  -Dopenecpds.lib=/path/to/open-ecpds/lib

mvn install:install-file \
  -Dfile=/path/to/ECaccess-J/build/ecpds-client/ecaccess-eccmd-client-1.0.0.jar \
  -DgroupId=ecmwf -DartifactId=ecaccess-eccmd-client \
  -Dversion=1.0.0 -Dpackaging=jar -DgeneratePom=true

mvn -Pecaccess-extension -pl ecpds-ecaccess -am package -DskipTests
```

Adjust the core jar filename to the version/build being deployed. The Ant target
can also compile against `ecpds-core/target/classes` during development. It
compiles a selected client-contract source set against upstream classes, without
building the ECaccess applications. `1.0.0` identifies this initial extracted
contract artifact, not the ECCMD daemon version. Publish that artifact to your
internal Maven repository for reproducible builds beyond a local checkout.

Install `ecpds-ecaccess/target/ecpds-ecaccess-8.2.1.jar` and
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
[AuthenticationExtension]
class=ecmwf.ecpds.extension.ecaccess.EcaccessIdentityProvider
host=eccmd.example.org
port=640

[UserDirectoryExtension]
class=ecmwf.ecpds.extension.ecaccess.EcaccessIdentityProvider
host=eccmd.example.org
port=640

[RMIClientSocketFactory]
connectTimeOut=10000
soTimeOut=10000
```

Use the same directory for both roles unless you deliberately need separate
services. Instances are independent; no global ECCMD singleton is installed.
The two socket timeouts must be explicitly set between 1 and 60,000 milliseconds
before the extension can start. These settings are shared with other RMI clients
using that factory on the Master; choose values suitable for that deployment.
Keep the existing trusted RMI network/TLS setup: the extension does not turn
plain RMI into a new encrypted transport.

`host` is required and accepts up to eight comma-, semicolon- or pipe-separated
hosts. `port` defaults to 640 and accepts 1 through 65535. Hosts are tried in
configured order when discovering a connection. Missing providers preserve the
core's default behavior. Existing `[Jeccmdd]` options do not automatically
activate this extension.

Restart the Master after changes. A configured provider with invalid options
fails startup. Daemon availability is checked on demand, so an unavailable
daemon rejects the current request rather than preventing Master startup.

## Authentication behavior

After the core's existing ECUser/active-WebUser checks and static/certificate
paths, and when built-in TOTP is not active:

1. Call ECCMD `getECUser(user, credential)` to validate a password.
2. If the daemon rejects it or returns no user, and the credential is a six- or
   eight-digit passcode, call
   `computePreAuth(user, credential, masterRoot, "monitoring")`.
3. Only after successful preauthentication, fetch the ECUser by name.
4. Return the authenticated ECUser to the core, which checks the exact user name
   and updates the existing local ECUser before accepting authentication.

The preauthentication step intentionally preserves ECaccess-J's behavior:
dissemination users may authenticate with a passcode even when disabled for
ECaccess service access itself. An active local WebUser is still required.
The preauthentication result is not used as a login credential or logged.

A transport failure is an outage, not an invalid-password response: it does not
trigger passcode fallback. A failed credential RPC is **never automatically
replayed**, because a passcode may already have been consumed. The stale stub is
discarded and the next request rediscovers the daemon. There is no fallback to
the local WebUser password after external rejection or failure.

The core retains TOTP precedence exactly as in ECaccess-J. Enabling this provider
does not disable static/certificate login or override active TOTP. Configure
your intended authentication policy explicitly.

## User import and refresh

The directory role calls ECCMD `getECUser(user)` for a locally missing ECUser.
A null result means absent; errors remain lookup failures. The core imports the
ECUser through its existing persistence path. Import does not automatically grant
WebUser permissions.

Configured scheduled ECUser refreshes also use this directory hook and the
core's existing update handling. Successful external credential authentication
can independently supply fresh ECUser data through
`AuthenticationProvider.authenticateIdentity`, avoiding a second authentication
or a shared per-thread identity cache.

Unlike the old Master implementation's extended startup polling, this adapter
uses on-demand discovery and the configured socket waits. Operators should retry
login after restoring the ECCMD service.
