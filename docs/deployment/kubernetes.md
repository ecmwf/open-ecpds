# Deploying OpenECPDS on Kubernetes

The checked-in manifests under `deploy/kubernetes` are the source of truth.
Use `kubectl` with Kustomize, GNU Make and Python 3 with PyYAML. Use a
`kubectl` version within the supported skew of your cluster. The manifests
use stable StatefulSet PVC retention policies (Kubernetes 1.32 or newer).
Build or publish the [OpenECPDS images](../getting-started/installation.md)
before deploying.

## Select and render a profile

```bash
cd deploy/kubernetes
make check PROFILE=cluster
make render PROFILE=cluster > /path/to/review.yaml
```

| Profile | Storage and service exposure |
|---|---|
| `cluster` (default) | Fresh installation with dynamically provisioned claims. Services are internal by default. |
| `local` | Single-node development using retained static local volumes. Only Monitor HTTPS has a NodePort by default. |
| `existing` | Reuses bound claims and exposes the four core services through NodePorts. Review or explicitly pin existing NodePort assignments. |

These compatibility profiles include Hawtio and OpenBao. Append `-agent` to
add the OpenBao Agent sidecar, or maintain a site overlay under `overlays/`
to select different add-ons and settings. If omitting Hawtio or OpenBao,
omit any matching storage declaration too. OpenECPDS service images default to
the release tag from `pom.xml`; set `TAG` and `ECPDS_REPOSITORY` to select
your published images. OpenBao uses a separate `OPENBAO_IMAGE` override.

## Prepare storage and configuration

Every cluster operation requires an explicit `CONTEXT` and `NAMESPACE`.
Create or select the namespace before proceeding. The examples below assume
you have set `CONTEXT` and `NAMESPACE` in your shell:

```bash
export CONTEXT=my-context NAMESPACE=ecpds
export STORAGE_CLASS=my-storage-class
make storage PROFILE=cluster
```

For dynamic storage, select an appropriate StorageClass or omit `STORAGE_CLASS`
only if you intend to use the cluster default. Keep the same class selected for
`apply`, which also configures the OpenBao claim template. Verify reclaim policies, capacity
and compatible placement for claims that the same Pod mounts. For the `local`
profile, unset `STORAGE_CLASS` and supply `LOCAL_NODE` as the node's `kubernetes.io/hostname` label. Create the
`/mnt/data/<claim-name>` directories on that node and set their appropriate
permissions before running `make storage PROFILE=local LOCAL_NODE=...`.
Local PV names are cluster-scoped; use only one installation with those names
per cluster.

The application still expects writable configuration at its original mount
paths. Provision site configuration on the relevant claims before starting:
MariaDB configuration must listen on 3309; Monitor and Mover configuration
must preserve their logical `[Login] hostName` and any `[Login] root` overrides.
For example, the development configuration uses `ecpds-mover`, not its
StatefulSet ordinal or Pod IP. The native workloads bind Monitor and Mover
listeners to their Pod IP and use stable per-Pod DNS for callback addresses.
Check site-specific bind/advertise overrides, Master identity, registration
records, TLS SANs and Mover delivery keys against those addresses. Replace
loopback `java.rmi.server.hostname` overrides in mounted Java argfiles.

If importing fresh data or configuration from local directories, run
`make bootstrap PROFILE=cluster CLAIM=<claim-name> SOURCE=<directory>` for
each required claim. The import never overwrites a nonempty volume; after
an interrupted import, inspect its partial contents before retrying.
Do not import local `run/` data over production storage.

Create these Secrets using your normal secret-management mechanism. Keys
are case-sensitive. Use the existing credential values when migrating.

| Secret | Required keys |
|---|---|
| `ecpds-database-credentials` | `MYSQL_ROOT_PASSWORD` |
| `ecpds-master-credentials` | `CLI_SHARED_SECRET`, `JOLOKIA_USER`, `JOLOKIA_PASSWORD` |
| `ecpds-monitor-credentials` | `KEYSTORE_PASSWORD`, `JOLOKIA_USER`, `JOLOKIA_PASSWORD` |
| `ecpds-mover-credentials` | `KEYSTORE_PASSWORD`, `RCC_SHARED_SECRET`, `JOLOKIA_USER`, `JOLOKIA_PASSWORD` |
| `hawtio-credentials` | `HAWTIO_USER`, `HAWTIO_PASSWORD` |
| `openbao-tls` | `tls.crt`, `tls.key` |

Use the same Jolokia authentication across the configured Hawtio connections.
Jolokia is exposed via separate internal Services, not via a NodePort.

## Deploy and verify

After provisioning storage, configuration and Secrets:

```bash
make diff PROFILE=cluster
make validate PROFILE=cluster
make apply PROFILE=cluster
make status PROFILE=cluster
make wait PROFILE=cluster
```

`diff` returns a nonzero status when differences exist. `apply` does not
create or modify the declared application PVs/PVCs; on a fresh cluster
profile OpenBao's StatefulSet creates its own claim. OpenBao intentionally
remains unready until initialized and unsealed. The controller checks detect
listener and database initialization failures, but do not validate application
readiness end to end. Confirm login, registration and a representative transfer
before accepting the deployment. Set workload resources and expose required TCP
protocols explicitly in a site overlay.

## OpenBao credential vault

OpenBao is a native single-replica StatefulSet with a headless Service.
The vault runs in the Master's namespace
with a persistent claim (`data-openbao-0`). It prefers the Master's
node but can use another node if resources or volume placement require it. It
has no NodePort or ingress. Use a cluster network plugin that enforces
NetworkPolicy: the policy permits application traffic to port 8200 only from
Master pods in the same namespace. The Monitor and Movers do not need access.
Master credential rotation is opt-in. Hosts that have not begun cutover can
continue using their configured credentials without OpenBao. Disabling OpenBao
does not revert a fenced Host or a Host with an active vault credential.

Build the image from the repository root with `make -C docker build-openbao`.
Make it available to the cluster, or publish it to your registry and set
`OPENBAO_IMAGE` to that reference. The vault needs a TLS Secret named
`openbao-tls` in the deployment namespace. Use a certificate
with `openbao` in its DNS SAN and provide the full certificate chain in
`tls.crt`:

```bash
kubectl --context="$CONTEXT" --namespace="$NAMESPACE" create secret tls openbao-tls \
  --cert=/path/to/tls.crt --key=/path/to/tls.key
```

The vault starts sealed and will not be ready until it is initialized and
unsealed. An authorized operator can use
`kubectl --context="$CONTEXT" --namespace="$NAMESPACE" port-forward pod/openbao-0 8200:8200`
for bootstrap and recovery; configure the CLI to trust the issuing CA and
verify the TLS server name `openbao` (set `BAO_TLS_SERVER_NAME=openbao`
when using `BAO_ADDR=https://127.0.0.1:8200`). From `deploy/kubernetes`, enable
KV v2 and install the Master policy:

```bash
bao secrets enable -path=ecpds -version=2 kv
bao policy write ecpds-host-passwords openbao/ecpds-host-passwords.hcl
```

To run OpenBao Agent alongside the Master, first [configure Kubernetes
authentication in OpenBao](https://openbao.org/docs/auth/kubernetes/), including
access to Kubernetes TokenReview. Configure the `ecpds-master` auth role at
`auth/kubernetes` for service account `ecpds-master-openbao` in the deployment
namespace only, audience `openbao`, and policy `ecpds-host-passwords`. Use a
short renewable service token and do not grant the role the default policy;
`ecpds-host-passwords` includes self-renewal for the Agent.
Then provide the OpenBao CA and apply the matching `-agent` profile:

```bash
kubectl --context="$CONTEXT" --namespace="$NAMESPACE" create configmap openbao-ca \
  --from-file=ca.crt=/path/to/issuing-ca.pem
make apply PROFILE=cluster-agent
```

Adding or removing the sidecar rolls the Master StatefulSet; schedule this
during a maintenance window. The projected service account
token is mounted only in the Agent. The Agent writes its token to a Pod-local,
memory-backed volume that the Master mounts read-only.

Configure the Master JVM to trust the vault certificate's issuing CA and set
the following in the Master's `ecmwf.properties`:

```ini
[OpenBao]
enabled=true
address=https://openbao:8200
mount=ecpds
tokenFile=/run/ecpds/openbao/token
```

The Master reads the file on every vault request so it sees token replacements
without a restart. Credential rotation requires the rotation tables included
in the supplied `docker/ecpds/database/ecpds.sql` initialization schema.
The journal stores references and staging state, not passwords. The Master
retains a pending discard until OpenBao confirms that the password version
was permanently destroyed. The scoped policy allows reading version metadata
for this confirmation but not listing keys or deleting their metadata.
Staging cannot change the credential used by a transfer. The self-service
workflow is opt-in. The deployment does not configure the vault's
Kubernetes auth backend or role automatically.

When a Data Mover registers, the Master inspects the RMI callback stub and
logs a warning if it has no TLS socket factory or uses an unrecognized one.
Registration still proceeds. The warning checks the factory on the stub,
not the negotiated TLS session or the peer certificate.

For mutual TLS on the existing callback listener and registry, configure
participating processes with:

```ini
[CallBack]
requireMutualTls=true
clientFactory=ecmwf.common.rmi.StrictRmiTlsSocketFactory
serverFactory=ecmwf.common.rmi.StrictRmiTlsSocketFactory
```

Provide the process identity and trusted internal issuers through the JSSE
key and trust stores (`javax.net.ssl.keyStore` and `javax.net.ssl.trustStore`).
The RMI-advertised address must match the server certificate SAN. The flag
also enables mutual TLS for registry lookups and prevents a plaintext
registry fallback. Enable it on both ends; existing plaintext peers will
not interoperate on that listener. This does not enable password rotation.

For credential rotation, Host credentials must not be assigned to a
fixed Mover. The Master selects an eligible Mover for each transfer attempt.
If it assigns a retry to a different Mover, the replacement must be able to
use the active Host credential through its approved delivery route. Fleet membership
alone does not authorize password retrieval: the Master must authorize the
specific attempt and its current assignment. OpenBao access remains on the
Master.

Credential rotation requires a distinct RSA delivery key for each final Mover,
independent of its TLS identity, and a separate Master signing key. The Master needs only the
Mover public keys, and each Mover needs only its own delivery private key and
the Master signing public key. An encrypted envelope is signed by the Master
and bound to the Mover, Host, purpose, transport route and short-lived attempt.
A retry assigned to another Mover needs a new envelope.

[Continental Movers](../architecture/continental-data-movers.md) are in scope.
Their HTTPS control relay requires verified HTTPS and
[control-channel signing](../administration/mover-control-channel-security.md).
The relay forwards only ciphertext encrypted to the final Continental Mover;
it cannot decrypt it and does not have OpenBao access. A retry on a direct
Mover needs its own envelope for the RMI route.

The probe endpoints are opt-in. Enable them only for explicitly approved
Hosts after configuring the prerequisites below. A Mover loads
its RSA delivery private key as PKCS#8 DER and the
Master signing public key as X.509 SubjectPublicKeyInfo DER from absolute paths.
Only explicitly listed Hosts can be probed. Each entry starts with the module
implementation class, followed by its approved endpoint configuration.
For example, a pinned-SSH password endpoint is configured as:

```ini
[PasswordProbe]
enabled=true
masterSigningPublicKey=/run/ecpds/probe/master-signing.der
deliveryPrivateKey=/run/ecpds/probe/mover-delivery.der

[PasswordProbeHosts]
42=ecmwf.common.ectrans.module.JSftpModule sftp.example.org 22 feed SHA256:<base64-fingerprint> /ecpds-probe
```

Mount the private key read-only and make it readable only by the Mover process.
On a direct Mover acting as a relay, `[PasswordProbe] relayEnabled=true`
requires strict mutual TLS on RMI. In `[PasswordProbeRelayOrigins]`, map each
approved Proxy Host ID to one HTTPS origin. The relay also requires
`[MoverProxy] trustAllCerts=false` and control-channel signing.

On the Master, `[PasswordProbe] dispatchEnabled=true` permits internal probe
dispatch with `masterSigningPrivateKey` set to an absolute PKCS#8 DER path.
`[PasswordProbeMoverKeys]` maps each final Mover's `[Login] root` to its
approved RSA public key file (X.509 SubjectPublicKeyInfo DER). The probe
requires matching `[PasswordProbeHosts]` entries on the Master and final
Mover. Differences in the approved module, archive, address, port, account,
server identity or resource configuration cause rejection before the Mover
connects. The probe response includes an authentication code bound to the exact encrypted
request, so the relay cannot change the result. A verified pass records the
tested endpoint fingerprint, live Host configuration fingerprint, and timestamp
against the exact staged password reference. Before dispatch and again before
recording a pass, the Master requires an active dissemination Host linked to
one active destination, with a matching address/account and method, port and
Host options accepted by its rotation capability. Automatic location is
excluded. See [supported modules and credential formats](../transfer-modules/index.md#password-rotation-capability).
A pass does not activate the credential. Activation requires
Monitor **Destination Operations** access for the exact destination and a
separate, revocable per-user rotation grant for that destination. Both
[reauthentication policies](../administration/openid-connect.md#credential-rotation-and-fresh-mfa)
bind the challenge to the exact pending reference.
An administrator grants or revokes the separate permission in
`HOST_PASSWORD_ROTATION_GRANT (DES_NAME, WEU_ID)` using the normal
restricted database administration channel. Adding a grant does not add
Operations access, and removing either permission blocks subsequent requests
and promotion. The pending-password dialog cannot grant itself access.
The database promotion transaction checks the current probe proof, Operations
access and explicit grant under lock. It consumes the pending reference,
records a password-free audit entry and retains the previous vault reference
until retirement. Monitor invokes it only through the gated, reauthenticated
activation RPC.
Switching a Host from configured credentials to OpenBao requires a persistent
fence in `HOST_PASSWORD_LEGACY_FENCE`, a confirmed drain, and no open
admissions on the configured-credential path. The Master rejects operations
that would use the configured credential for a fenced Host or one with an active
vault reference. The fence does not cancel requests admitted before it was set
or close existing Mover sessions. Use **Prepare online cutover** rather than
setting fences manually.
`trackSynchronousLegacyAdmissions=true` journals known
Host size, list, delete, mkdir, rmdir, and move commands before dispatch and
clears the admission only after a successful response. Errors can leave
unresolved admissions. `drainEnabled=true` additionally journals scheduled
uploads and streaming dispatch. A confirmed dispatch acknowledgement clears
the Master admission; actual session closure must still be certified on the
final Mover. Neither journal expiry nor transport failure certifies a drain.

Configure session tracking on every participating final Mover:

```ini
[PasswordDelivery]
enabled=true
trackLegacySessions=true
legacySessionHosts=42,43
legacySessionStateDirectory=/var/lib/ecpds/legacy-password-sessions
```

Enable tracking from process startup for the explicit
Host set. It requires the `[PasswordProbe]` identity keys and
approved Hosts. Use an absolute, persistent, process-owned private directory.
Tracked sessions using configured credentials bypass connection pooling. Their
admission remains open until actual disconnection is confirmed, including
asynchronous close.
Execution timeouts, failed close, and modules that cannot confirm
disconnection retain uncertain admissions rather than reporting a false zero.
Other Hosts retain their ordinary pooling behavior.

A signed Master command can persist a per-Host fence that rejects subsequent
connections using the configured credential on that Mover. The direct mutual-TLS
RMI and signed, verified Continental HTTPS paths return Mover-signed receipts bound to the
Host, route, cutover, one-use challenge, process incarnation, and session
count. Retrying the same cutover requires a fresh challenge; a different
cutover is rejected. There is deliberately no unfence API. Do not remove
fenced Hosts from the tracking configuration or delete their marker files.
Restart preserves fences but changes the incarnation: a new process reporting
zero is not proof that its predecessor drained.

The opt-in Master coordinator persists route coverage, approved keys and
process incarnations before sending fences. Configure `[PasswordDrainFleet]`
entries as `relay-root proxy-host-id final-mover-root`; direct routes use
proxy `0` and the same root twice. Coverage conservatively includes **every**
registered `TRANSFER_SERVER` and every Proxy Host, including inactive records.
Historical records can therefore block preparation: inventory them rather
than assuming an unreachable route has zero sessions. Continental final
roots must match `[PasswordDeliveryRelayMovers]`.

The coordinator limits each request to 20 seconds, four concurrent Hosts,
and one outstanding collection per Host. An interrupted or timed-out
collection never promotes a credential; any persistent fences already
written remain in force. Activation atomically rechecks the full fleet,
current Host configuration, zero Master admissions and fresh, signed zero
session receipts received within 30 seconds. The coordinator can reconcile
outstanding Master dispatch acknowledgements only after every enrolled process
is persistently fenced and confirms that no configured-credential sessions remain. It
atomically retains the exact admission IDs and signed receipts in
`HOST_PASSWORD_LEGACY_RECONCILIATION` before removing those admissions.
Timeout alone never reconciles them. Changed keys, changed process
incarnations and unreachable routes fail closed; an unknown session or a
predecessor lost on restart still requires operator investigation.

An interrupted first cutover with an uncertain session or a changed Mover
incarnation has no supported force-clear operation. Restore any unavailable
route and collect fresh evidence from the originally enrolled processes where
possible. If predecessor evidence has been lost, keep the Host fenced and
escalate for operator investigation; do not delete marker files or edit journal
rows to force activation. The `RECONCILE` action described below does not
override this protection.

The database limits probes to three attempts
per Host per rolling hour, even if a pending password is replaced.

### Credential rotation enablement

Credential rotation is an opt-in feature. Deploy matching Master, Monitor and
Mover versions, then complete these prerequisites:

1. Verify the rotation tables are present, configure Master-only OpenBao access, and establish
   vault backup, unseal and application-token replacement procedures.
2. Require mutual TLS between Monitor, Master and direct Movers. For Continental
   routes, additionally configure verified HTTPS and control-channel signing.
3. Install distinct delivery keys for final Movers, the Master signing key, and
   identical module/endpoint approvals on the Master and final Movers.
4. Inventory every registered Mover and Proxy Host, configure complete
   `[PasswordDrainFleet]` coverage and probe/relay routing, and enable persistent
   session tracking from Mover startup for every enrolled Host.
5. Grant the exact Destination Operations permission and destination-specific
   rotation grant. Configure local reauthentication or fresh IdP MFA as required.
6. Enable the Master settings shown below, the Monitor's `monitorEnabled`, and
   `[PasswordProbe] enabled` / `[PasswordDelivery] enabled` on final Movers.
   Keep the default disabled settings for deployments that do not use rotation.

```ini
[HostPasswordRotation]
monitorEnabled=true
trackSynchronousLegacyAdmissions=true
deliveryEnabled=true
drainEnabled=true
activationEnabled=true
retirementEnabled=true
reauthentication=LOCAL_PASSWORD
```

Changing configuration or module approvals invalidates existing probe evidence.
Use **Stage**, **Test**, **Prepare online cutover** and **Activate** for an enrolled Host.
Verify the scheduled transfer reaches its intended endpoint and that its final
Mover reports confirmed closure; only that delivery evidence starts the grace
period. Exercise the configured direct and Continental routes before enrolling
additional Hosts. Enabling the vault alone does not enable these actions.

`[HostPasswordRotation] monitorEnabled=true` enables
the HTTPS pending-password API at
`/do/transfer/destination/passwordRotation/<destination>/<Host-ID>`. This
requires strict mutual-TLS Monitor-to-Master RMI, OpenBao on the Master, and a
new Monitor login after enabling the API. Each request requires both the
exact Destination Operations ACL and an active rotation grant. Set the same
flag in the Monitor process to show **Pending password** beside active
dissemination Hosts in the HTTPS Destination view. The dialog supports staging,
testing, discard, reconciliation, online cutover preparation and guarded
activation. Activation additionally requires `drainEnabled`,
`deliveryEnabled`, `activationEnabled`, and `retirementEnabled` on the Master;
all default to false. The Master
selects the probe route from `[PasswordProbeDispatchMovers]` (Host ID to
connected Mover root) and optionally `[PasswordProbeDispatchProxyHosts]`
(Host ID to Continental Proxy Host ID). For Continental probes, use the
relay root in the first mapping and configure the final Mover separately
in `[PasswordDeliveryRelayMovers]`.

An authenticated `GET` returns the pending reference and state, along
with an `X-ECPDS-Rotation-CSRF` response header. For `POST`, send that
header and `Content-Type: application/json`. Use `operation: "STAGE"`
with a `password` field and optional integral `retirementGraceHours`
(1–720, default 24), or `operation: "PROBE"`, `"DISCARD"`,
`"RECONCILE"`, or `"DRAIN"` with the current `reference`. Keep passwords out of URLs,
command lines and request-body logs. Reconcile only confirms an existing
staged vault version or retries destruction already in progress. A missing
or unconfirmed vault version leaves the journal intact for operator
investigation.

`BEGIN_ACTIVATION` creates a one-use challenge bound to the current Monitor
session, origin, account, destination, Host, immutable reference and endpoint/
Host configuration. `ACTIVATE` completes the challenge with fresh local
password re-entry by default. Optional `FRESH_MFA` uses the HTTPS
`/do/transfer/destination/passwordRotationCallback` callback and retains only
the short-lived authorization code in the browser's server session, never a
local password. IdP-only accounts do not require a fabricated local password;
accounts that have one must still re-enter it. Register the exact callback URI
with the configured identity provider. Standalone uses `LOCAL_PASSWORD`,
without an IdP or MFA requirement.

The internal scheduled-delivery path is gated by
`[HostPasswordRotation] deliveryEnabled` on the Master and
`[PasswordDelivery] enabled` on the final Mover. Both default to false.
The Master maps an assigned Continental Proxy Host ID to the final Mover
root in `[PasswordDeliveryRelayMovers]`; the relay still uses
`[PasswordProbeRelayOrigins]` for the verified HTTPS origin. The final
Mover uses a fresh, module-owned encrypted connection for each size check and upload.
Every upload is durably bound to its final Mover, key, credential reference and
complete delivery context before dispatch. An unconfirmed upload stops;
the journal also blocks subsequent attempts for that transfer, including
reassignment after a Master restart, until a verified final-Mover terminal
receipt resolves it. Ordinary transfer status or dispatch acknowledgement is
insufficient. A confirmed, closed failure permits a new attempt but does not
start retirement. Timeouts and uncertain disconnection produce no terminal
receipt. Non-empty resume targets are rejected instead of overwritten.

The staging user chooses the previous vault version's destruction grace in
whole hours, from 1 to 720, default 24. The grace starts at the Master's first
verified successful-delivery receipt, after confirmed transfer-session closure,
not at staging, probing, activation or dispatch. A retrying worker durably
records destruction intent, asks OpenBao to permanently destroy the previous
immutable version, and clears its pointer only after confirmed destruction.
Unavailable or unverifiable evidence never starts grace; ambiguous
destruction retains both the previous pointer and intent for recovery.
A further rotation is blocked until the preceding vault credential has been
retired. The first cutover from a configured credential has no vault version to
retire; activation does not overwrite the Host password field.

Credential rotation is an opt-in `TransferModule.passwordRotation()` capability.
The module defines its approved endpoint, credential format, probe, isolated
connection and confirmed-close behavior. Modules without the complete
capability are not eligible. Supported encrypted adapters include SFTP, FTPS,
HTTPS, WebDAV, S3, Azure and GCS; see the
[module reference](../transfer-modules/index.md#password-rotation-capability)
for supported modes and restrictions.

Keep the bootstrap token and unseal material out of the cluster deployment. Back
up the vault separately and test restores; destroying a record does not erase
historical snapshots. This single-node deployment disables memory locking, so
protect the node against swapping vault process memory.

To stop the vault without deleting the claim, use
`kubectl --context="$CONTEXT" --namespace="$NAMESPACE" scale statefulset/openbao --replicas=0`. Unseal it again after
restarting it.

## Find the application ports

Use `make ports PROFILE=<profile>` to see the selected Services and ports. In
`local`, Monitor HTTPS is on the NodePort of `ecpds-monitor-public`. For
internal-only Services, use a site-approved exposure mechanism or, for
administrative access to Monitor, forward the port from a trusted workstation:

```bash
kubectl --context="$CONTEXT" --namespace="$NAMESPACE" port-forward service/ecpds-monitor 3443:3443
```

Master and Mover retain their FTP passive Service ports (`60000–60009` and
`60010–60019`). Set each application's `[FTPPassiveSocket]` configuration
(`privilegedLocalPort=yes`, `minPrivilegedPort` and `maxPrivilegedPort`) to
match; local `run/` files may specify a different range. External FTP requires a TCP exposure mechanism that maps
the advertised passive address and ports correctly; HTTP ingress or random
NodePort translation by itself is not sufficient. Confirm callback routing
and TLS identities separately for any off-cluster RMI clients.

## Reuse persistent storage

Use `existing` or `existing-agent` to attach workloads to bound claims. Do not
run `make storage` for this procedure. The profile expects all of its claims,
including `etc-ecpds-hawtio` and `data-openbao-0`; provision any missing claim
explicitly without replacing bound claims. Pin site-specific ports and settings
in an overlay where necessary.

Before replacing workloads, record the current manifests, images, Service
ports, claim bindings and retention policies. Back up the database, vault,
application state and keys, and verify recovery. During a maintenance window,
stop new work, drain the fleet, and stop any workloads using those claims before
starting replacements. Do not replace a Mover with an unresolved credential
cutover or uncertain delivery; a restart does not recover session evidence.

Workload selectors are immutable. If a controller must be replaced, verify
storage retention, scale the controller to zero, wait for its Pods to
terminate, and delete only the controller. A standalone Pod with the same
name as a managed workload must also be stopped and removed by exact name.
Keep the Services, Secrets and claims. Do not remove OpenBao's network policy
while its Pod is running.

Run `make validate` and `make apply` with the selected profile. Confirm that
the claims and Service ports are unchanged, unseal OpenBao if deployed,
verify component identities and connectivity, and exercise a representative
transfer before resuming work.

If replacement fails, stop the replacement controllers and confirm their Pods
are gone before restoring the saved workload and Service definitions. Keep
the same data claims and restore the vault's network policy before restarting
it. Do not run two writers against the same state.

## Remove workloads or data

`make delete-workloads PROFILE=<profile>` removes the selected controllers
but retains Services, policies, Secrets and storage. `make delete` is an
alias for this non-purging operation; `build` and `start` only alias `apply`.

To delete one unused claim deliberately after backing it up, use
`make purge-data PROFILE=<profile> CLAIM=<claim> CONFIRM_DATA=<context>/<namespace>/<claim>`.
A dynamically provisioned volume with a `Delete` reclaim policy may be
destroyed by this operation. It never directly deletes PVs or Namespaces.

## Related

- [Illustrative Physical Infrastructure](infrastructure.md)
- [Releasing to a Container Registry](release.md)
