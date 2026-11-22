# Standalone Container

The standalone image packages the complete OpenECPDS stack — MariaDB, Master Server,
Data Mover, and Monitor — in a single Docker container that starts pre-configured and
ready to use. No build step, no manual database setup, no separate compose file required.

!!! tip "Purpose"
    The standalone image is designed for **evaluation, demos, and local exploration**.
    It is not intended for production use. For a full deployment, see
    [Installation](installation.md) and [First Run](first-run.md).

## Architecture at a glance

![Data flows from providers to the Data Mover for acquisition and from the Mover to customers for dissemination. The Mover includes the Data Portal. Users connect to one container holding the Master, Mover, Monitor, MariaDB and OpenBao, with /data mounted on persistent host storage.](../img/Figure28.png)

The Master schedules transfers, the Data Mover handles data protocols, and the
Monitor provides the UI and REST API. MariaDB stores the database; OpenBao is
the credential vault and is enabled by default. The arrows show data flow,
not which side initiates a network connection.

The services run together in one container; `/data` is mounted from the host.
Preserve and back up the host data directory to retain the environment across
container recreation. Externally supplied keys and secrets need their own
backups; see [backup and recovery](#backup-and-recovery).

## Requirements

| Resource | Minimum | Recommended |
|---|---|---|
| RAM | 2 GB | 4 GB |
| Disk (image) | 4.5 GB | — |
| Disk (data volume) | 1 GB | 10 GB+ |
| Docker | 20.10+ | latest |

## Start

```bash
docker run -d \
  --name standalone \
  --stop-timeout 180 \
  -v "$(pwd)/ecpds-data:/data" \
  -p 7443:7443 \
  -p 7022:7022 \
  -p 8443:8443 \
  -p 8883:8883 \
  -p 127.0.0.1:9640:9640 \
  -p 127.0.0.1:7640:7640 \
  -p 127.0.0.1:8080:8080 \
  -p 127.0.0.1:9404:9404 \
  -p 127.0.0.1:9405:9405 \
  -p 127.0.0.1:9406:9406 \
  ghcr.io/ecmwf/open-ecpds/standalone:latest
```

Start with an empty `/data` volume to initialise the database. The embedded
vault initializes and unseals automatically. A build-specific
marker is recorded there; the same image build can reuse the volume across
restarts or recreation. A different build, or a nonempty directory without a
marker, is rejected without deleting anything. Restore the matching image or
map a different empty directory. Keep the image as well as a backup of the data.
Wait about 30 seconds for all
services to become available.

Standalone holds an advisory lock on `/data/.standalone.lock` while it runs.
A second standalone container using the same volume fails to start. Use storage
that supports shared file locks, and do not remove or replace the lock file
while a container is running. The lock does not protect against non-cooperating
programs or make a running database safe to copy.

!!! note "Self-signed certificate"
    The container initially uses self-signed TLS certificates, so your browser
    will show a warning. For connections beyond a trusted test environment,
    configure clients to verify a trusted certificate and hostname instead of
    bypassing certificate checks. See
    [standalone TLS guidance](standalone-security.md).

## Access

!!! info "Running on a remote machine?"
    All URLs below use `localhost`, which assumes the container is running on the same
    machine as your browser or terminal. If you started the container on a remote server,
    replace `localhost` with that server's hostname or IP address.

| Service | URL | Credentials |
|---|---|---|
| Monitoring UI | `https://localhost:8443` | `admin`, `monitor`, or `operator` / generated password |
| REST API | `https://localhost:8443/ecpds/v1/` | `api` / generated secret |
| Data Portal (HTTPS) | `https://localhost:7443` | `test` / generated password |
| Data Portal (S3) | `https://localhost:7443/s3` | `test` / separate generated S3 secret |
| Data Portal (WebDAV) | `https://localhost:7443/webdav` | `test` / generated password |
| Data Portal (SFTP) | `sftp://localhost:7022` | `test` / generated password |
| MQTTS broker | `mqtts://localhost:8883` | `test` / generated password |
| Hawtio | `https://localhost:8080` | `hawtio` / generated password |

Retrieve the generated passwords and S3 secret as the container administrator with
`docker exec standalone cat /data/lib/standalone/initial-accounts`. The file is
root-only; store its contents securely and remove it after recording them. Password
changes in the UI do not update this initial file. The API secret is in the separate
root-only `/data/lib/standalone/api-client-secret` file.

Hawtio includes preset connections for the Master, Monitor, and Mover Jolokia agents.
When prompted by Hawtio, use `jolokia` and the generated Jolokia password.

<a id="trusting-or-replacing-the-https-certificate"></a>
<a id="internal-service-connections"></a>
For certificate replacement, Hawtio TLS configuration and internal service
certificate maintenance, see the optional
[standalone TLS guide](standalone-security.md). No certificate setup is needed
to start exploring locally.

## Service ports

These are container ports. The example publishes the CLI's Master and Mover
ports on localhost. To use the CLI from another machine, see
[using the CLI with standalone](ecpds-cli.md#using-the-cli-against-the-standalone-container).

| Port | Service |
|---|---|
| `7443` | Data Mover — HTTPS Data Portal |
| `7022` | Data Mover — SFTP |
| `8883` | Data Mover — MQTTS (MQTT over TLS) |
| `8443` | Monitor — HTTPS UI |
| `9640` | Master — ECpds CLI |
| `7640` | Data Mover — ECpds CLI file uploads |
| `8080` | Hawtio — authenticated JMX console (HTTPS, Monitor certificate) |
| `9404` | Prometheus JMX exporter — Master |
| `9405` | Prometheus JMX exporter — Mover |
| `9406` | Prometheus JMX exporter — Monitor |

!!! note "FTP not available in standalone"
    OpenECPDS fully supports FTP in production deployments. FTP passive mode (PASV) is
    not compatible with Docker port mapping — the server advertises its internal address
    for data connections, which external clients cannot reach. Use SFTP (port 7022) as a
    drop-in alternative.

!!! warning "Evaluation credentials and management ports"
    The standalone image exposes unauthenticated Prometheus exporter endpoints.
    The command above binds Hawtio, metrics and CLI ports to localhost only.
    The CLI ports use an unencrypted protocol; use an encrypted tunnel for
    remote access. Do not publish these ports on an untrusted network.

### Connect Grafana to Prometheus metrics

The JMX exporter endpoints are available at `http://localhost:9404/metrics` (Master),
`http://localhost:9405/metrics` (Mover), and `http://localhost:9406/metrics` (Monitor)
when using the localhost port mappings above. Add the endpoint(s) as Prometheus scrape
targets, then configure Grafana to use that Prometheus instance as a data source. If
Prometheus runs in another container, use a host address reachable from that container
instead of `localhost` (which would refer to the Prometheus container itself). These
endpoints have no authentication; keep them restricted to trusted clients.

## Populate some data

The standalone image ships with a few example destinations (e.g. `hourly_aq`), but they
start out empty and switched off. Before trying the protocols below, log into the
Monitoring UI and start one or two of them so there is some data to experiment with. This
requires the container to have outbound Internet access, as these destinations acquire
data from external providers:

1. Open `https://localhost:8443` and log in as `admin` with its generated password.
2. On the main page, in the **Transmission** section, select **Destinations**.
3. Click a destination's name (e.g. `hourly_aq`) in the table to open its **Data** page.
4. Click the green **Start Destination** (▶) button above the transfers table.

Once started, the destination retrieves files from its source host and makes them
available through the Data Portal, so the commands below have something to list and download.

## Try the protocols

The examples use the `test` account. Enter its generated password when prompted.
For tools that require a command-line password, use a protected mechanism supported
by the tool; command-line arguments can be visible to other users on the host.
For the examples using shell variables, enter the generated `test` and `test_s3`
values without echo:

```bash
read -rs -p 'Test password: ' TEST_PASSWORD; echo
read -rs -p 'Test S3 secret: ' S3_SECRET; echo
```

### HTTPS — browser or curl

Open `https://localhost:7443` in a browser to access the Data Portal UI, or use curl:

```bash
# List available destinations as JSON
curl -k -u test -H "Accept: application/json" \
  https://localhost:7443/ecpds/data/list/
```

Example output:

```json
[{"name":"efas_iconeu_opendata","size":null,"time":"2025-09-29T13:56:23.598Z","directory":true},{"name":"hourly_aq","size":null,"time":"2025-09-29T14:02:53.465Z","directory":true},{"name":"wis2_sbo","size":null,"time":"2025-09-29T13:58:05.020Z","directory":true}]
```

```bash
# List files in a destination
curl -k -u test -H "Accept: application/json" \
  https://localhost:7443/ecpds/data/list/hourly_aq
```

Example output (trimmed):

```json
[{"name":"HourlyData_2026091600.dat","size":758823,"time":"2026-09-16T14:50:54.098Z","directory":false},{"name":"HourlyData_2026091601.dat","size":757791,"time":"2026-09-16T14:50:55.191Z","directory":false}, ... ,{"name":"HourlyData_2026091705.dat","size":746933,"time":"2026-09-17T08:15:20.546Z","directory":false}]
```

### SFTP

First, [verify and trust the server's SSH host key](sftp-host-keys.md).

```bash
sftp -P 7022 test@localhost
# Enter the generated test password when prompted.
```

### S3

Using [AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html):

```bash
aws configure set aws_access_key_id test
aws configure
# Enter test as the access key ID and the generated test_s3 value as the secret.
aws s3 ls s3:// --endpoint-url https://localhost:7443/s3 --no-verify-ssl
```

Using [rclone](https://rclone.org/):

```bash
rclone lsd :s3: \
  --s3-provider=Other \
  --s3-endpoint=https://localhost:7443/s3 \
  --s3-access-key-id=test \
  --s3-secret-access-key="$S3_SECRET" \
  --s3-v2-auth \
  --no-check-certificate
```

> **Note:** `--s3-v2-auth` selects AWS Signature V2 authentication. Use this setting with the rclone example to avoid `SignatureDoesNotMatch` errors from incompatible V4 request signing.

### WebDAV

Using [curl](https://curl.se/):

```bash
# List root directory (PROPFIND)
curl -k -u test -X PROPFIND https://localhost:7443/webdav/
```

Using [rclone](https://rclone.org/):

```bash
rclone lsd :webdav: \
  --webdav-url=https://localhost:7443/webdav \
  --webdav-vendor=other \
  --webdav-user=test \
  --webdav-pass="$(rclone obscure "$TEST_PASSWORD")" \
  --no-check-certificate
```

Using [cadaver](https://notroj.github.io/cadaver/) (interactive WebDAV client):

```bash
# cadaver does not support self-signed certs natively — set SSL_CERT_FILE or
# configure ~/.cadaverrc: use-ssl-noverify=yes
cadaver https://localhost:7443/webdav
# Username: test; enter the generated test password.
dav:/webdav/> ls
```

Or mount directly from your OS file manager:

- **macOS Finder:** Go → **Connect to Server…** (⌘K), enter `https://localhost:7443/webdav`, click **Connect**, then log in as `test`.
- **Windows Explorer:** Right-click **This PC → Map network drive**, choose a drive letter, enter `https://localhost:7443/webdav` as the folder, check **Connect using different credentials** and log in as `test`.
- **Linux (GNOME Files / Nautilus):** Go → **Connect to Server…**, enter `davs://localhost:7443/webdav`, log in as `test`.
- **Linux (command-line):** mount with `davfs2`: `sudo mount -t davfs https://localhost:7443/webdav /mnt/webdav`

!!! note
    All OS file managers will warn about the self-signed certificate — accept it to proceed.
    The WebDAV share appears as a network drive alongside your local disks.

### MQTTS

Using [mqttx CLI](https://mqttx.app/cli) (recommended):

```bash
mqttx sub \
  -h localhost -p 8883 \
  -l mqtts \
  -u test -P "$TEST_PASSWORD" \
  --insecure \
  -t '#'
```

### REST API

The REST API is served by the Monitor on port `8443`. The initial `api` client has full
permissions and a randomly generated secret. Retrieve it as the container administrator
with `docker exec standalone cat /data/lib/standalone/api-client-secret`, and protect
it like a password. Each authenticated curl example prompts for the secret. Manage
or reset the client in the Monitor UI; resetting it does not update the initial secret file.
See the [REST API Reference](../rest-api.md) for the complete endpoint list.

```bash
# Check server version (no auth required)
curl -k https://localhost:8443/ecpds/v1/version
```

```json
{ "status": "ok", "version": "8.3.0-16112026" }
```

```bash
# List all destinations
curl -k -u api https://localhost:8443/ecpds/v1/destination/list
```

```bash
# Get details for a specific destination
curl -k -u api https://localhost:8443/ecpds/v1/destination/hourly_aq
```

```bash
# List all incoming (data) users
curl -k -u api https://localhost:8443/ecpds/v1/incoming/user/list
```

```bash
# Create a disposable example user with a generated password.
# The response contains the password; save it in a private file.
response_file=$(mktemp)
curl -k -u api -X POST -o "$response_file" \
  "https://localhost:8443/ecpds/v1/incoming/user/add2?id=newuser&email=newuser@example.com&iso=gb&portalService=standard-login&active=true"
```

The generated password is returned in the response's `pass` field, not in the
URL. Protect the response like a password, remove the file when no longer
needed, and do not reuse this example account or its password elsewhere.
Deactivate the example user after trying it. Unlike `add2`, the `add` endpoint
accepts `pass` only as a query parameter: URLs can be recorded in shell history,
access logs, proxies and diagnostics. Do not put a real or reusable password
in a URL.

```bash
# Associate a user with a destination
curl -k -u api -X POST \
  "https://localhost:8443/ecpds/v1/incoming/association/add?id=newuser&destination=hourly_aq"
```

```bash
# List a user's destination associations
curl -k -u api \
  "https://localhost:8443/ecpds/v1/incoming/association/list?id=newuser"
```

```bash
# Remove a user–destination association
curl -k -u api -X DELETE \
  "https://localhost:8443/ecpds/v1/incoming/association/del?id=newuser&destination=hourly_aq"
```

```bash
# Deactivate a user
curl -k -u api -X DELETE \
  https://localhost:8443/ecpds/v1/incoming/user/del/newuser
```

!!! tip "Pretty-print JSON responses"
    Pipe any `curl` output through `| python3 -m json.tool` or `| jq .` for
    readable formatting.

!!! note "Permission configuration"
    The `api` user is pre-configured in the standalone container with the `.*` permission
    pattern, granting access to all API operations. In production, restrict permissions
    per the [REST API Reference — Permission Configuration](../rest-api.md#permission-configuration).

## Try MQTT upload notifications

The pre-configured **`mqtt_demo`** destination lets you test notifications without
an external data provider. See
[MQTT destination configuration](../notifications/mqtt-dissemination.md#demo-upload-notifications-and-public-download-links)
for its settings.

### 1. Subscribe before uploading

In one terminal, use [mqttx CLI](https://mqttx.app/cli) to connect to the standalone
MQTTS broker and leave the subscription running. Enter the generated `test`
password without echoing it first; the expanded argument may still be visible
in process listings:

```bash
read -rs -p 'Test password: ' TEST_PASSWORD; echo
mqttx sub \
  -h localhost -p 8883 \
  -l mqtts \
  -u test -P "$TEST_PASSWORD" \
  --insecure \
  -t 'openecpds/demo/#'
```

Wait until MQTTX reports a successful connection/subscription before proceeding.
The demo uses non-retained messages: subscribing after publication will not replay
the notification. `--insecure` is for the evaluation container's self-signed certificate.

### 2. Upload a small file

In a second terminal, create a file:

```bash
printf 'Hello from the standalone MQTT demo!\n' > mqtt-demo.txt
sftp -P 7022 test@localhost
```

Enter the generated test password. If necessary, first
[verify the SSH host key](sftp-host-keys.md), then run these commands at the SFTP prompt:

```text
put mqtt-demo.txt /mqtt_demo/mqtt-demo.txt
bye
```

**Leave `mqtt_demo` stopped in the Monitor.** Its Properties include
`incoming.event = "yes"`, `mqtt.publish = "yes"` and
`scheduler.standby = "yes"`. The uploaded transfer enters **HOLD**, meaning the file
is available for download without scheduling outbound delivery. HOLD is expected
for this demo and triggers the notification.

### 3. Wait for the notification

Keep the first terminal open. Publication is asynchronous: allow roughly a minute
for the event repository and scheduler to process the upload; this is not a delivery
deadline. The topic is:

```text
openecpds/demo/mqtt-demo.txt
```

The JSON body includes file identity, destination, size, publication/expiry times,
and authenticated download links:

```text
https://localhost:7443/ecpds/data/file/mqtt_demo/mqtt-demo.txt
sftp://localhost:7022/mqtt_demo/mqtt-demo.txt
https://localhost:7443/s3/test/mqtt_demo/mqtt-demo.txt
```

The S3 link requires a signed request using the `test` bucket/account. Files up to
**16 KiB** also include `content` with `encoding: "base64"` and a `data` string.
Larger files keep the links but omit inline content. Base64 is not encryption:
any subscriber authorized for the topic can read embedded file content.

Verify the uploaded file through HTTPS:

```bash
curl --fail -k -u test \
  https://localhost:7443/ecpds/data/file/mqtt_demo/mqtt-demo.txt
```

### 4. Check the result in the Monitor

Log in at `https://localhost:8443` and open **Operations → MQTT Activity**
(`/do/monitoring/mqtt`). Filter on destination `mqtt_demo` to see publication
processing and per-Mover broker outcomes. The transfer's history also records the
publication ID and result counts. **ACCEPTED means broker acceptance, not proof
that a subscriber received the message.**

When enabled, Splunk and native OpenTelemetry also receive an **NTF** operational
event with the result, not the message body. The OpenTelemetry Journal is a
separate export queue: a stopped Collector does not prevent MQTT publication.

If nothing arrives, check that the subscription was active before upload, port
`8883` is published, and the file appears under `mqtt_demo`. A transfer stuck in
**WAIT** with no publication usually means `scheduler.standby` is missing from the
destination configuration. Inspect MQTT Activity for failures or unconfirmed
results; use a different filename for another test upload.

!!! note "Remote hosts and custom port mappings"
    Replace `localhost` in the commands with your container host. Also edit the
    destination's JavaScript public endpoint settings so notification links use
    the subscriber-reachable hostname and externally mapped HTTPS/SFTP ports.
    Docker cannot infer those public URLs. Standalone defaults are **7443** for
    HTTPS/S3, **7022** for SFTP and **8883** for MQTTS; Eclipse's ports differ.

## Try Aviso watch and replay

The pre-configured **`aviso_demo`** destination demonstrates the read-only
[Aviso API](../notifications/history.md#aviso-client-api) independently of MQTT.
The `test` user has access through the `test_destinations` policy. Its Properties are:

```properties
incoming.event = "yes"
notification.aviso = "yes"
notification.captureMode = "strict"
scheduler.standby = "yes"
```

No MQTT subscription, MQTT options or JavaScript are needed. Strict capture stores
the transfer update and notification together. **Leave `aviso_demo` stopped in the
Monitor** so uploaded files are available in HOLD without outbound dissemination.

### 1. Start a watch

In one terminal, connect before uploading:

```bash
curl --fail-with-body -k -N -i --user test \
  -H 'Content-Type: application/json' \
  -d '{"event_type":"file_available","identifier":{"destination":"aviso_demo"}}' \
  https://localhost:7443/ecpds/aviso/api/v1/watch
```

Enter the generated Data Portal password. The `-i` flag shows the
response headers. Once you see **HTTP 200** with `Content-Type: text/event-stream`,
leave the watch running and continue to the upload. The stream normally starts
with a `live-notification` frame whose JSON `type` is `connection_established`,
but you don't need to wait for that housekeeping message. If file notifications
are already arriving, the watch is working even if you don't see it.

If you see neither response headers nor events, don't assume the watch is
connected. Try the standalone endpoint directly if you're going through a
reverse proxy, or continue with the upload and use `from_date` in step 3 to
verify it by replay instead. A watch without a cursor
receives new events only. `-k` is for the evaluation container's self-signed
certificate; use a trusted certificate in production.

### 2. Upload a file

In another terminal, note the UTC timestamp printed before uploading. You can
use it for replay if you don't receive a live event.

```bash
date -u +%Y-%m-%dT%H:%M:%SZ
printf 'Hello from the standalone Aviso demo!\n' > aviso-demo.txt
sftp -P 7022 test@localhost
```

Enter the generated test password. If necessary, first
[verify the SSH host key](sftp-host-keys.md), then upload:

```text
put aviso-demo.txt /aviso_demo/aviso-demo.txt
bye
```

The watch emits an SSE `live-notification` frame containing a CloudEvent with
type `int.ecmwf.aviso.file_available`, an ID such as `file_available@123`,
`data.identifier.destination` set to `aviso_demo`, and
`data.identifier.target` set to `aviso-demo.txt`. Transfer details, including
size and status, are in `data.payload`. Allow time for notification processing;
the watch closes after five minutes by default and can be reconnected.

### 3. Replay the notification

Copy the event ID received above into `from_id`:

```bash
curl --fail-with-body -k -N --user test \
  -H 'Content-Type: application/json' \
  -d '{
    "event_type": "file_available",
    "identifier": {"destination": "aviso_demo"},
    "from_id": "file_available@123"
  }' \
  https://localhost:7443/ecpds/aviso/api/v1/replay
```

Replay includes the starting event, emits `replay` frames, and closes when
complete. If you didn't receive a live event ID, replace the `from_id` field
with `from_date` and use the UTC timestamp you noted before uploading, in
`YYYY-MM-DDTHH:MM:SSZ` format. Access uses the Data Portal user's destination
and file permissions, not MQTT topic permissions.

Replace `localhost` with the standalone host reachable from your terminal; from
the development container this may be `ecpds-mover`. New database initializations
include this demo; existing databases are not changed by rebuilding the image.

## Logs

!!! note "DEBUG logging"
    The standalone image runs with `DEBUG` logging enabled by default. This produces
    verbose output useful for exploring how OpenECPDS processes transfers, connects to
    data movers, and handles incoming requests. In production the log level is controlled
    via the `LOG_LEVEL` environment variable (e.g. `LOG_LEVEL=warn`).

```bash
# All services (supervisord output)
docker logs -f standalone

# Individual service logs
docker exec standalone tail -f /data/log/master/master.log
docker exec standalone tail -f /data/log/mover/mover.log
docker exec standalone tail -f /data/log/monitor/monitor.log
```

## Stop and remove

```bash
# Stop (data is preserved in ./ecpds-data)
docker stop standalone

# Restart later
docker start standalone

# Remove completely (keeps ./ecpds-data on disk)
docker rm standalone
```

## Known limitations

!!! warning "MTR / network path diagnostics on Docker Desktop (macOS/Windows)"
    The Host **Network** page in the Monitoring UI runs `mtr`/`traceroute` inside the
    container to chart the network path to a remote host. On **Docker Desktop for macOS
    or Windows**, this will typically only show the first hop (the `docker0` gateway)
    followed by all `* * *` timeouts, even though the same command works fine from a
    terminal on the host machine.

    This is a limitation of Docker Desktop's virtualized networking, not an OpenECPDS
    issue. On macOS/Windows, containers reach the outside world through a userspace VM
    networking layer (gVisor-tap-vsock) that performs NAT for outbound
    traffic but does not relay the ICMP "TTL Exceeded" replies sent back by intermediate
    routers for each traceroute probe. Only the local `docker0` bridge answers, because
    that hop never actually leaves the VM.

    On a native Linux Docker host (e.g. production data movers), containers sit on a
    real bridge with normal kernel routing, so `mtr` reports the full path correctly.
    There is no workaround for Docker Desktop itself — to validate MTR/traceroute
    behaviour, run the standalone container on a Linux host/VM instead.

<a id="optional-openbao-credential-rotation"></a>
## Embedded OpenBao

OpenBao starts and unseals automatically, without extra setup.

!!! warning "Evaluation only"
    The default stores vault unseal material on `/data` and is not suitable for
    production credentials. For an independently protected key or manual
    unsealing, see [Standalone vault modes](../deployment/standalone-vault.md).

Host password rotation remains opt-in; see the
[credential rotation guide](../deployment/kubernetes.md#credential-rotation-enablement).

## Database encryption key

By default the database-encryption key and its password are generated under
`/data/lib/master/database-encryption`. A backup of `/data` therefore contains
both the database and its key. Protect the entire backup as sensitive material.

To keep that key off the data volume, provision a PKCS#12 AES-256 key and its
configuration separately before the first start. For example, on a Linux host,
as root:

```bash
install -d -m 0750 -o root -g 12001 /secure/ecpds-database-key
umask 077
export ECPDS_DATABASE_KEY_PASSWORD="$(openssl rand -hex 24)"
keytool -genseckey -alias database-encryption -keyalg AES -keysize 256 \
  -storetype PKCS12 -keystore /secure/ecpds-database-key/key.p12 \
  -storepass:env ECPDS_DATABASE_KEY_PASSWORD -keypass:env ECPDS_DATABASE_KEY_PASSWORD
printf '[DatabaseEncryption]\nkeyStore=/run/ecpds/database-key/key.p12\nkeyStorePassword=%s\nkeyAlias=database-encryption\n' \
  "$ECPDS_DATABASE_KEY_PASSWORD" > /secure/ecpds-database-key/ecmwf.properties
unset ECPDS_DATABASE_KEY_PASSWORD
chown root:12001 /secure/ecpds-database-key/key.p12 /secure/ecpds-database-key/ecmwf.properties
chmod 0640 /secure/ecpds-database-key/key.p12 /secure/ecpds-database-key/ecmwf.properties
```

Add these arguments to the `docker run` command:

```bash
--mount type=bind,source=/secure/ecpds-database-key,target=/run/ecpds/database-key,readonly \
-e ECPDS_DATABASE_ENCRYPTION_DIR=/run/ecpds/database-key \
```

UID/GID `12001` is the container's Master identity; adapt host ownership or ACLs
if user namespaces remap it. The Master must be able to read `key.p12`, while
the other service identities must not be able to read either file. Both files
must be root-owned in the container, must not be symbolic links, group-writable
or world-accessible. The root-owned configuration contains
the keystore password. Protect and back up this directory separately from
`/data`, and mount the same key at the same path after every restart or restore.
Changing or losing it makes existing encrypted database values unreadable.
Standalone records a fingerprint of the PKCS#12 file and refuses to start if
it changes. Keep the original file byte-for-byte; key rotation is not yet
supported.

## Backup and recovery

Stop the container gracefully before copying `/data`, so MariaDB files and
OpenBao Raft data are consistent. Allow enough time for all services to stop;
if Docker forcibly kills the container, do not treat that stop as a clean backup.
Preserve numeric ownership and permissions, and protect
the backup: it includes service credentials, private keys and, unless configured
separately as above, the database-encryption key. Save the exact image used by
the container. For non-default vault modes, protect and back up the required
seal and recovery material separately; see [vault modes](../deployment/standalone-vault.md).

For the bind mount shown in the start command, an example is:

```bash
docker stop -t 180 standalone
sudo tar --numeric-owner --xattrs -cpf /secure/standalone-data.tar -C "$(pwd)/ecpds-data" .
docker image save -o /secure/standalone-image.tar "$(docker inspect -f '{{.Image}}' standalone)"
docker start standalone
```

The archives above are **not encrypted**. Store them in encrypted, access-controlled
backup storage. The default vault unseals automatically when restarted.
To restore, load the saved image, extract the trusted data archive into a new,
empty directory with numeric ownership preserved, and mount it as `/data` with
the same external secrets, if used. Follow the relevant
[vault recovery steps](../deployment/standalone-vault.md) and verify application
access. Rehearse this procedure on an isolated host.

The data marker rejects any different image build; do not remove or edit the
marker to bypass this check.

Docker health reports supervisor status, MariaDB connectivity and the local
service ports. In manual OpenBao mode the container remains unhealthy until the
vault is unsealed and the Master token is ready. Check its state with:

```bash
docker inspect -f 'running={{.State.Running}} health={{.State.Health.Status}}' standalone
```

Configure an independent monitoring system to alert when the container stops,
stays unhealthy or cannot be inspected, or when host disk capacity is low.
Also monitor transfer failures and certificate expiry: Docker health does not
verify transfers, remote endpoints or certificate validity. Allow for manual
unsealing during planned restarts if using manual vault mode.

## Next steps

- Explore the [Monitoring UI](https://localhost:8443) to see destinations, hosts, and
  transfer history. Three accounts give different perspectives:
    - **`admin`** — full administrator view with access to all destinations,
      hosts, transfer queues, users, and system configuration.
    - **`monitor`** — restricted user view showing only a limited set of
      destinations and features, as a regular monitoring user would experience it.
    - **`operator`** — operator view: all destinations and the Operations
      section, without the administration pages.

  See [Monitor UI overview](../monitor-ui/overview.md) for how the three profiles differ.
- Read [Data Acquisition](../use-cases/acquisition.md) and [Data Dissemination](../use-cases/dissemination.md) to understand the two active services.
- Explore the [Data Portal](../use-cases/data-portal.md) for how remote users push and pull data.
- Ready for a real deployment? See [Installation](installation.md) → [First Run](first-run.md).
