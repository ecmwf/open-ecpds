# Standalone Container

The standalone image packages the complete OpenECPDS stack — MariaDB, Master Server,
Data Mover, and Monitor — in a single Docker container that starts pre-configured and
ready to use. No build step, no manual database setup, no separate compose file required.

!!! tip "Purpose"
    The standalone image is designed for **evaluation, demos, and local exploration**.
    It is not intended for production use. For a full deployment, see
    [Installation](installation.md) and [First Run](first-run.md).

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
  -v $(pwd)/ecpds-data:/data \
  -p 7443:7443 \
  -p 7022:7022 \
  -p 8443:8443 \
  -p 8883:8883 \
  -p 127.0.0.1:8080:8080 \
  -p 127.0.0.1:9404:9404 \
  -p 127.0.0.1:9405:9405 \
  -p 127.0.0.1:9406:9406 \
  ghcr.io/ecmwf/open-ecpds/standalone:latest
```

Start with an empty `/data` volume to initialise the database. Standalone does not
migrate existing databases. The volume persists everything across ordinary
restarts. Wait about 30 seconds for all services to become available.

## Optional OpenBao credential rotation

Standalone uses configured Host credentials by default and does not require OpenBao.
Enable OpenBao if you want to use self-service credential rotation.
It uses a separate OpenBao container with persistent Raft storage and
TLS; OpenBao has no published host port and is reachable only over the Compose
network. The standalone Master runs under its own Linux identity. Mover and
Monitor use separate identities; Hawtio shares Monitor's identity to access its
TLS keystore. None can read the Master token or inspect
the Master process environment/file descriptors. The root-owned supervisor and
container administrator remain trusted.

The standalone authentication flow uses local-password reauthentication only;
standalone does not provide the identity-provider MFA option. Enabling this
backend does **not** enable credential rotation by itself. Configure the
[rotation prerequisites and feature settings](../deployment/kubernetes.md#credential-rotation-enablement)
on the Master, Monitor and participating Movers before enabling the action.

1. Build the optional OpenBao image and standalone image. From the repository's
   `docker` directory, stage the normal standalone inputs and build
   `open-ecpds/openbao:2.7.2` and the desired standalone image tag:

   ```bash
   make get-rpms
   make build-openbao ARCH=amd64
   make build-sa ARCH=amd64
   ```

   Use the local standalone tag produced by `build-sa` in `STANDALONE_IMAGE`,
   or select the matching GHCR tag. Use a Java 25 base image when building
   the standalone image from the OpenECPDS RPMs.
2. From `docker/ecpds/standalone`, start the vault with the local launcher:

   ```bash
   bash openbao/standalone-openbao-start.sh
   ```

   The launcher uses OpenSSL to generate the development CA and server certificate
   on first use and may prompt for `sudo` to set the server key's ownership.
   It reuses existing `tls.crt`, `tls.key`, and `ca.crt` in `openbao/tls/` without
   replacing them. Keep `openbao/operator/ca.key` offline and out of the
   containers. For a shared environment, supply a certificate issued by the site's
   trusted CA and grant the vault's UID 100 read access to the private key.
3. Initialize the vault once. Protect the initialization output
   as recovery material; split and store the five key shares separately offline.
   Never put these keys or the initial root token in Compose environment variables,
   command-line arguments, image layers, or the Raft volume.

   ```bash
   umask 077
   ./openbao/standalone-openbao-control.sh init > openbao/operator/init-response.json
   ```

   Enter three distinct shares when prompted by `unseal` (run it three times):

   ```bash
   ./openbao/standalone-openbao-control.sh unseal
   ```

4. Configure the KV v2 mount and Master policy, then issue a 30-day,
   non-renewable Master token. Both commands prompt for the root token without
   echoing it. Store the generated application token with the protected helper:

   ```bash
   ./openbao/standalone-openbao-control.sh bootstrap
   ./openbao/standalone-openbao-control.sh issue-token
   ./openbao/standalone-openbao-save-token.sh
   ./openbao/standalone-openbao-control.sh revoke-root
   ```

   The token file is created mode `0600`; keep it outside version control and
   replace it before expiry. Revoking the initial root token leaves only the
   least-privilege application token for runtime access. After separately storing
   the unseal shares, remove the local initialization response. To replace the
   application token, first use the recovery shares to generate a temporary root
   token, issue the replacement, and revoke that root token. Remove the old token
   file, use the helper to save its replacement, then recreate the container to
   remount the new file and copy it into the Master-only `/run` path:

   ```bash
   docker compose -f compose.openbao.yml up -d --force-recreate standalone
   ```
5. Start the standalone service using the opt-in Compose file:

   ```bash
   STANDALONE_IMAGE=ghcr.io/ecmwf/open-ecpds/standalone:latest \
     docker compose -f compose.openbao.yml up -d standalone
   ```

The entrypoint installs only the public CA certificate into the Java trust store
and copies the protected token to a `0600` runtime file owned by the Master.
Ordinary `docker run` startup does not require OpenBao. OpenBao data survives restarts
and `docker compose down`; do not use `down -v` unless you intend to erase it.
After a host/container restart, OpenBao remains sealed and requires the operator
to enter the threshold of offline shares again. There is deliberately no
auto-unseal key stored beside the encrypted Raft data.

If the initial root token is lost while the recovery shares remain available,
use OpenBao's `operator generate-root` workflow inside the vault container with
the required offline shares. If the shares are lost, restore both an encrypted
Raft backup and its separately protected recovery material; the data volume
alone is not a recovery backup. Revoke expired/replaced application tokens
through OpenBao after confirming the Master is using the replacement.

For automated regression commands, see
[Credential rotation checks](../contributing.md#credential-rotation-checks).

!!! note "Self-signed certificate"
    The container uses a self-signed TLS certificate. Accept the browser security
    warning on first visit, or pass `--insecure` / `--no-verify-ssl` to CLI tools.

## Access

!!! info "Running on a remote machine?"
    All URLs below use `localhost`, which assumes the container is running on the same
    machine as your browser or terminal. If you started the container on a remote server,
    replace `localhost` with that server's hostname or IP address.

| Service | URL | Credentials |
|---|---|---|
| Monitoring UI | `https://localhost:8443` | `admin` / `admin2021` · `monitor` / `monitor2021` · `operator` / `operator2021` |
| REST API | `https://localhost:8443/ecpds/v1/` | `api` / `api2021` |
| Data Portal (HTTPS) | `https://localhost:7443` | `test` / `test2021` |
| Data Portal (S3) | `https://localhost:7443/s3` | `test` / `test2021` |
| Data Portal (WebDAV) | `https://localhost:7443/webdav` | `test` / `test2021` |
| Data Portal (SFTP) | `sftp://localhost:7022` | `test` / `test2021` |
| MQTTS broker | `mqtts://localhost:8883` | `test` / `test2021` |
| Hawtio | `https://localhost:8080` | `hawtio` / `hawtio2021` |

Hawtio includes preset connections for the Master, Monitor, and Mover Jolokia agents.
When prompted by Hawtio, use `jolokia` / `jolokia2021` for the Jolokia login. These
credentials are for evaluation only; do not expose the standalone image to an untrusted
network.

Hawtio is served over HTTPS using the same keystore as the Monitor UI
(`/etc/ecpds/monitor/ecpds-monitor.pfx`), so the browser shows the same self-signed
certificate warning. Hawtio reads the certificate at startup: after replacing the Monitor
certificate, restart Hawtio with `docker exec standalone supervisorctl restart hawtio`.
Set `HAWTIO_TLS_KEYSTORE` to another PKCS#12 file to use a different certificate, or to
`none` to fall back to plain HTTP.

## Exposed ports

| Port | Service |
|---|---|
| `7443` | Data Mover — HTTPS Data Portal |
| `7022` | Data Mover — SFTP |
| `8883` | Data Mover — MQTTS (MQTT over TLS) |
| `8443` | Monitor — HTTPS UI |
| `9640` | Master — ECpds CLI |
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
    The standalone image includes demo Hawtio and Jolokia credentials and exposes
    unauthenticated Prometheus exporter endpoints. The command above binds Hawtio and
    metrics ports to localhost only. Do not publish these ports on an untrusted network,
    and replace the demo `HAWTIO_USER`, `HAWTIO_PASSWORD`, `JOLOKIA_USER`, and
    `JOLOKIA_PASSWORD` values for any shared environment.

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

1. Open `https://localhost:8443` and log in as `admin` / `admin2021`.
2. On the main page, in the **Transmission** section, select **Destinations**.
3. Click a destination's name (e.g. `hourly_aq`) in the table to open its **Data** page.
4. Click the green **Start Destination** (▶) button above the transfers table.

Once started, the destination retrieves files from its source host and makes them
available through the Data Portal, so the commands below have something to list and download.

## Try the protocols

All examples use the pre-configured `test / test2021` account.

### HTTPS — browser or curl

Open `https://localhost:7443` in a browser to access the Data Portal UI, or use curl:

```bash
# List available destinations as JSON
curl -k -u test:test2021 -H "Accept: application/json" \
  https://localhost:7443/ecpds/data/list/
```

Example output:

```json
[{"name":"efas_iconeu_opendata","size":null,"time":"2025-09-29T13:56:23.598Z","directory":true},{"name":"hourly_aq","size":null,"time":"2025-09-29T14:02:53.465Z","directory":true},{"name":"wis2_sbo","size":null,"time":"2025-09-29T13:58:05.020Z","directory":true}]
```

```bash
# List files in a destination
curl -k -u test:test2021 -H "Accept: application/json" \
  https://localhost:7443/ecpds/data/list/hourly_aq
```

Example output (trimmed):

```json
[{"name":"HourlyData_2026091600.dat","size":758823,"time":"2026-09-16T14:50:54.098Z","directory":false},{"name":"HourlyData_2026091601.dat","size":757791,"time":"2026-09-16T14:50:55.191Z","directory":false}, ... ,{"name":"HourlyData_2026091705.dat","size":746933,"time":"2026-09-17T08:15:20.546Z","directory":false}]
```

### SFTP

```bash
sftp -P 7022 -o StrictHostKeyChecking=no test@localhost
# Password: test2021
```

### S3

Using [AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html):

```bash
aws configure set aws_access_key_id test
aws configure set aws_secret_access_key test2021
aws s3 ls s3:// --endpoint-url https://localhost:7443/s3 --no-verify-ssl
```

Using [rclone](https://rclone.org/):

```bash
rclone lsd :s3: \
  --s3-provider=Other \
  --s3-endpoint=https://localhost:7443/s3 \
  --s3-access-key-id=test \
  --s3-secret-access-key=test2021 \
  --s3-v2-auth \
  --no-check-certificate
```

> **Note:** `--s3-v2-auth` selects AWS Signature V2 authentication. Use this setting with the rclone example to avoid `SignatureDoesNotMatch` errors from incompatible V4 request signing.

### WebDAV

Using [curl](https://curl.se/):

```bash
# List root directory (PROPFIND)
curl -k -u test:test2021 -X PROPFIND https://localhost:7443/webdav/
```

Using [rclone](https://rclone.org/):

```bash
rclone lsd :webdav: \
  --webdav-url=https://localhost:7443/webdav \
  --webdav-vendor=other \
  --webdav-user=test \
  --webdav-pass=$(rclone obscure test2021) \
  --no-check-certificate
```

Using [cadaver](https://notroj.github.io/cadaver/) (interactive WebDAV client):

```bash
# cadaver does not support self-signed certs natively — set SSL_CERT_FILE or
# configure ~/.cadaverrc: use-ssl-noverify=yes
cadaver https://localhost:7443/webdav
# Username: test  Password: test2021
dav:/webdav/> ls
```

Or mount directly from your OS file manager:

- **macOS Finder:** Go → **Connect to Server…** (⌘K), enter `https://localhost:7443/webdav`, click **Connect**, then log in with `test` / `test2021`.
- **Windows Explorer:** Right-click **This PC → Map network drive**, choose a drive letter, enter `https://localhost:7443/webdav` as the folder, check **Connect using different credentials** and log in with `test` / `test2021`.
- **Linux (GNOME Files / Nautilus):** Go → **Connect to Server…**, enter `davs://localhost:7443/webdav`, log in with `test` / `test2021`.
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
  -u test -P test2021 \
  --insecure \
  -t '#'
```

### REST API

The REST API is served by the Monitor on port `8443`. All examples use the pre-configured
`api / api2021` super-user (full permissions). See the [REST API Reference](../rest-api.md)
for the complete endpoint list.

```bash
# Check server version (no auth required)
curl -k https://localhost:8443/ecpds/v1/version
```

```json
{ "status": "ok", "version": "8.3.0-16112026" }
```

```bash
# List all destinations
curl -k -u api:api2021 https://localhost:8443/ecpds/v1/destination/list
```

```bash
# Get details for a specific destination
curl -k -u api:api2021 https://localhost:8443/ecpds/v1/destination/hourly_aq
```

```bash
# List all incoming (data) users
curl -k -u api:api2021 https://localhost:8443/ecpds/v1/incoming/user/list
```

```bash
# Create a new incoming user (portalService is optional, defaults to
# "standard-login"; other values are "open-access" and "self-service").
# active=true is required here since a new incoming user is inactive by
# default, and incoming/association/add fails with "User newuser not
# found/active" until the user is active.
curl -k -u api:api2021 -X POST \
  "https://localhost:8443/ecpds/v1/incoming/user/add?id=newuser&pass=secret&email=newuser@example.com&iso=gb&portalService=standard-login&active=true"
```

```bash
# Associate a user with a destination
curl -k -u api:api2021 -X POST \
  "https://localhost:8443/ecpds/v1/incoming/association/add?id=newuser&destination=hourly_aq"
```

```bash
# List a user's destination associations
curl -k -u api:api2021 \
  "https://localhost:8443/ecpds/v1/incoming/association/list?id=newuser"
```

```bash
# Remove a user–destination association
curl -k -u api:api2021 -X DELETE \
  "https://localhost:8443/ecpds/v1/incoming/association/del?id=newuser&destination=hourly_aq"
```

```bash
# Deactivate a user
curl -k -u api:api2021 -X DELETE \
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
MQTTS broker and leave the subscription running:

```bash
mqttx sub \
  -h localhost -p 8883 \
  -l mqtts \
  -u test -P test2021 \
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

Enter password `test2021`, accept the host key after checking it is your evaluation
container, then run these commands at the SFTP prompt:

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
curl --fail -k -u test:test2021 \
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

Enter the standalone Data Portal password `test2021`. The `-i` flag shows the
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

Enter password `test2021`, verify and accept the host key, then upload:

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

## Next steps

- Explore the [Monitoring UI](https://localhost:8443) to see destinations, hosts, and
  transfer history. Three accounts give different perspectives:
    - **`admin` / `admin2021`** — full administrator view with access to all destinations,
      hosts, transfer queues, users, and system configuration.
    - **`monitor` / `monitor2021`** — restricted user view showing only a limited set of
      destinations and features, as a regular monitoring user would experience it.
    - **`operator` / `operator2021`** — operator view: all destinations and the Operations
      section, without the administration pages.

  See [Monitor UI overview](../monitor-ui/overview.md) for how the three profiles differ.
- Read [Data Acquisition](../use-cases/acquisition.md) and [Data Dissemination](../use-cases/dissemination.md) to understand the two active services.
- Explore the [Data Portal](../use-cases/data-portal.md) for how remote users push and pull data.
- Ready for a real deployment? See [Installation](installation.md) → [First Run](first-run.md).
