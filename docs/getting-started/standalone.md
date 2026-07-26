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
  ghcr.io/ecmwf/open-ecpds/standalone:latest
```

The database initialises automatically on first start. The `/data` volume persists
everything across restarts. Wait about 30 seconds for all services to become available.

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
| Monitoring UI | `https://localhost:8443` | `admin` / `admin2021` · `monitor` / `monitor2021` |
| Data Portal (HTTPS) | `https://localhost:7443` | `test` / `test2021` |
| Data Portal (S3) | `https://localhost:7443/s3` | `test` / `test2021` |
| Data Portal (SFTP) | `sftp://localhost:7022` | `test` / `test2021` |
| MQTTS broker | `mqtts://localhost:8883` | `test` / `test2021` |

## Exposed ports

| Port | Service |
|---|---|
| `7443` | Data Mover — HTTPS Data Portal |
| `7022` | Data Mover — SFTP |
| `8883` | Data Mover — MQTTS (MQTT over TLS) |
| `8443` | Monitor — HTTPS UI |
| `9640` | Master — ECpds CLI |

!!! note "FTP not available in standalone"
    OpenECPDS fully supports FTP in production deployments. FTP passive mode (PASV) is
    not compatible with Docker port mapping — the server advertises its internal address
    for data connections, which external clients cannot reach. Use SFTP (port 7022) as a
    drop-in alternative.

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
[
  {
    "name": "hourly_aq",
    "comment": "Hourly Air Quality data",
    "userStatus": "ACTIVE"
  }
]
```

```bash
# List files in a destination
curl -k -u test:test2021 -H "Accept: application/json" \
  https://localhost:7443/ecpds/data/list/hourly_aq
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
  --no-check-certificate
```

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

Using [Mosquitto](https://mosquitto.org/download/) — fetch the self-signed cert first:

```bash
# Fetch the certificate directly from the broker (no container access needed)
openssl s_client -connect localhost:8883 </dev/null 2>/dev/null \
  | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > /tmp/ecpds.pem

# Subscribe to all topics
mosquitto_sub \
  --host localhost --port 8883 \
  --username test --pw test2021 \
  --cafile /tmp/ecpds.pem \
  --insecure \
  --topic '#' -v
```

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

## Next steps

- Explore the [Monitoring UI](https://localhost:8443) to see destinations, hosts, and
  transfer history. Two accounts give different perspectives:
    - **`admin` / `admin2021`** — full administrator view with access to all destinations,
      hosts, transfer queues, users, and system configuration.
    - **`monitor` / `monitor2021`** — restricted user view showing only a limited set of
      destinations and features, as a regular monitoring user would experience it.
- Read [Data Acquisition](../use-cases/acquisition.md) and [Data Dissemination](../use-cases/dissemination.md) to understand the two active services.
- Explore the [Data Portal](../use-cases/data-portal.md) for how remote users push and pull data.
- Ready for a real deployment? See [Installation](installation.md) → [First Run](first-run.md).
