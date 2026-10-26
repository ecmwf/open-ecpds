# ECaccess gateway transfers

The optional `ecpds-ecaccess` extension provides
`ecmwf.ecpds.mover.ECaccessModule`, retaining the legacy module class name.
It supports uploads, downloads with resume offsets, listing and filename
filtering, size queries, deletion, directory creation/removal and moving files.

This is **not** the built-in [ECauth module](ecauth.md). ECauth is an SSH/Telnet
authentication transport; ECaccessModule transfers through an external ECaccess
gateway discovered through ECCMD.

## Deployment

Build the adapter and extracted client-contract jar using the
[ECaccess extension build instructions](../administration/ecaccess-identity.md#build-the-client-contracts-and-extension).
The ordinary OpenECPDS build does not include them.

Install the matching `ecpds-ecaccess-8.2.1.jar` on the application classpaths of
the Master and every Mover/Proxy executing this transfer method. Install
`ecaccess-eccmd-client-1.0.0.jar` on the Master classpath as well. The legacy
ECCMD wire contracts are confined to the Master-side adapter; Movers use the
extension's own `EcaccessGatewayAccess` remote interface. Do not add old
`master.jar`, `mover.jar`, `eccmd.jar` or `gateway.jar` alongside upstream:
classpath precedence is not a supported way to replace core classes.

Master and participating Movers must use matching core and extension versions.
Restart affected services after installation or configuration changes.

The Transfer Module definition must use class
`ecmwf.ecpds.mover.ECaccessModule`. Leave its **Archive** empty to use the
application classpath; do not point it at an old ECaccess-J archive. Existing
Transfer Methods can keep referring to that definition. This extension does
not create or modify database definitions automatically.

## Master configuration

Register the extension explicitly in the Master's local `ecmwf.properties`
override:

```ini
[RemoteServiceExtensions]
ecaccess=EcaccessTransfers

[EcaccessTransfers]
class=ecmwf.ecpds.extension.ecaccess.EcaccessGatewayProvider
host=eccmd.example.org
port=640

[RMIClientSocketFactory]
connectTimeOut=10000
soTimeOut=10000
```

`ecaccess` is the service ID used by this module; `EcaccessTransfers` names its
configuration section. Registration is independent of the authentication and
user-directory providers. Installing a jar or setting `[Jeccmdd] useEccmd`
does not activate the new provider. No EJB/JNDI backend is loaded.

`host` is required and accepts up to eight comma-, semicolon- or pipe-separated
ECCMD hosts. `port` defaults to 640. Explicit connection and read timeouts between
1 and 60,000 milliseconds are required; these factory settings also affect other
Master RMI clients. Preserve the site's existing RMI transport/network policy.

The Master resolves ECCMD's `EccmdEngine` registry binding on demand. Invalid
provider configuration fails startup, while an unavailable ECCMD daemon fails
the current connection attempt without preventing Master startup.

## Control and data paths

```text
Mover ECaccessModule
  -> Master RMI: getExtensionService("ecaccess", gateway)
  -> optional Master provider: ECCMD getDataAccess(gateway)
  -> exported Master relay: EcaccessGatewayAccess
  -> existing external gateway
```

Subsequent control calls use that Master relay, retaining the existing
Master-mediated gateway-access topology. The core contains only generic
remote-service registration, lifecycle and lookup hooks. All gateway operations,
ECCMD types and relay implementation live in the optional jar.

File bytes do **not** pass through the Master RMI channel. The inherited
`ProxyModule` creates a ticket and advertises the Mover's `ECproxyPlugin` callback
address through `ProxySocket`. Keep the existing public address, listener port,
TLS and shared-secret configuration compatible with the external gateway. A
working RMI connection alone does not establish that this callback is reachable.

The module requires **Master RMI connectivity**. REST-only connections fail
explicitly; there is no REST endpoint or EJB/JNDI fallback.

## Host properties

For a Host whose Transfer Method selects this module:

```properties
ecaccess.gateway = "gateway-one,gateway-two"
ecaccess.loadbalancing = "true"
ecaccess.destination = "genericFtp"
```

| Property | Behavior |
|---|---|
| `ecaccess.gateway` | Ordered gateway names known to ECCMD; default `mover`. These are not ECCMD server hostnames. |
| `ecaccess.loadbalancing` | Default `false`; when enabled, start after the gateway recorded in `ecaccess.lastused`, then wrap around. |
| `ecaccess.lastused` | Updated in the transfer setup after successful selection; existing setup persistence remains responsible for retaining it. |
| `ecaccess.destination` | Remote gateway destination definition; default `genericFtp`, also supports `genericSftp`, `genericExec` and `genericFile`. |

Without load balancing, selection starts with the first gateway. Missing,
unavailable or unreachable gateways are tried in order before transfer begins.
Once an operation starts, a failed upload, delete, move or other mutation is
**not replayed or switched to another gateway within that attempt**. Normal
ECPDS scheduling/retry policy remains unchanged.

The gateway receives the existing ECUser, a detached MSUser association, the
inactive destination override and a transfer cookie suffixed with `@gateway`.
Source filter, initial size and MD5 metadata are forwarded for gateway-side
processing. Checksum delegation retains the existing remote-checksum behavior;
it does not add a new local checksum check.

Closing removes a nonnull remote transfer cookie and releases the Master relay,
including after failed operations. Unselected relays are released immediately;
RMI distributed garbage collection releases abandoned relays, and provider
shutdown unexports remaining relays.
