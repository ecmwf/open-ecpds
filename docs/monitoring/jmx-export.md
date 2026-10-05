# Exporting JMX Metrics to Grafana and Hawtio

Every long-running OpenECPDS process (`MasterServer`, `MoverServer`, `MonitorServer`, and
everything they manage through `ecmwf.common.mbean.MBeanService`) already exposes its state as standard
JMX MBeans, registered on an in-process `MBeanServer` created by
`ecmwf.common.starter.Starter`. A local `jconsole` attach always shows the JVM's own
standard beans, but only shows these application beans when `USE_JCONSOLE` is set (see
below). This page documents two optional, independent JVM agents you can attach to get a
modern remote-access alternative, **without changing any application code**: both hook
into the exact same `MBeanService` beans already registered by the server.

(Older versions of OpenECPDS also shipped a legacy Sun JDMK `HtmlAdaptorServer` — a basic
HTML+JMX browser — bound to a dedicated port and a `starter.properties` user+password
pair. That adaptor, the `jmxtools` dependency it needed, its dedicated port, and
`starter.properties` have all been removed; Jolokia/Hawtio is its replacement, see below.)

| Agent | Gives you | Depends on `USE_JCONSOLE` |
|---|---|---|
| [Jolokia](https://jolokia.org) | JMX-over-HTTP/JSON, browsable/operable from [Hawtio](https://hawt.io) | No |
| [Prometheus JMX exporter](https://github.com/prometheus/jmx_exporter) | `/metrics` endpoint for Prometheus, graphed in Grafana | **Yes** |

Both are opt-in via environment variables read by `etc/master/sh/master`,
`etc/mover/sh/mover`, and `etc/monitor/sh/monitor` — leaving the variables unset means no
JMX agent is attached at all.

---

## Why `USE_JCONSOLE` matters

The Prometheus JMX exporter javaagent only reads the JVM's **platform MBeanServer**
(`ManagementFactory.getPlatformMBeanServer()`). By default, `Starter` creates a separate,
dedicated `MBeanServer` instead (`useJConsole=false`), so the exporter would see nothing.
Setting `USE_JCONSOLE=true` switches `Starter` to register everything on the platform
MBeanServer instead — the same switch that already lets you attach a plain `jconsole`.

Jolokia does not have this limitation: it merges every `MBeanServer` it can find via
`MBeanServerFactory.findMBeanServer(null)`, platform or not, so it works with or without
`USE_JCONSOLE`.

---

## Where the agent jars come from

Both jars are fetched automatically during the Maven build — no manual download step.
`ecpds-core/pom.xml`'s `maven-dependency-plugin` configuration has a `copy-jmx-export-agents`
execution (alongside the existing `copy-dependencies` one) that pulls them straight from
Maven Central into `lib/agents/` at the repository root:

| Jar | Coordinates pinned in `ecpds-core/pom.xml` |
|---|---|
| `jolokia-agent-jvm-javaagent.jar` | `org.jolokia:jolokia-agent-jvm:2.6.2` (classifier `javaagent`) |
| `jmx_prometheus_javaagent.jar` | `io.prometheus.jmx:jmx_prometheus_javaagent:1.0.1` |

`lib/` is the same shared directory the `ecpds-common` RPM already packages wholesale into
`/usr/local/ecpds/lib` (see `ecpds-common/pom.xml`), so `lib/agents/` ships inside that RPM
automatically — no separate RPM mapping was needed — and ends up on disk at
`/usr/local/ecpds/lib/agents/`, which is exactly where
`etc/{master,mover,monitor}/sh/{master,mover,monitor}` look for them (`$dir_lib/agents/...`).
This also means it's present in the Docker images,
since those install the RPM. To bump either version, edit the `<version>` in that
`copy-jmx-export-agents` execution and rebuild — nothing else needs to change.

Note on versions: `jmx_prometheus_javaagent` only publishes up to `1.0.1` on Maven Central;
newer releases (the project is currently at 1.6.x) are GitHub-release-only artifacts. If you
want a newer version, you'll need to fetch it by URL instead of by Maven coordinate (e.g. a
`wget`/`curl` step), since `copy` resolves from a Maven repository.

## Jolokia + Hawtio

1. Set `JOLOKIA_PORT` (and optionally `JOLOKIA_HOST`, default `0.0.0.0`) before starting
   the service, e.g. `JOLOKIA_PORT=8778`.
2. **Also set `JOLOKIA_USER`/`JOLOKIA_PASSWORD`.** Jolokia has no authentication at all by
   default — anyone who can reach `JOLOKIA_PORT` can read and invoke every MBean. Setting
   both turns on HTTP basic auth for that agent (`etc/{master,mover,monitor}/sh/*` only
   add `user=`/`password=` to the `-javaagent` line when `JOLOKIA_USER` is non-empty).

   For local testing, nothing needs setting at all: the dev argfiles
   (`run/etc/ecpds/{master,mover,monitor}/argfile`, used by the Eclipse/VS Code launch
   configs) and the dev compose files (below) already default to the fixed credentials
   `jolokia` / `jolokia2021` — the same convention as the monitor UI's `admin` /
   `admin2021`. Export `JOLOKIA_USER`/`JOLOKIA_PASSWORD` yourself to override that for
   anything beyond local, trusted-network use.
3. Run [Hawtio](https://hawt.io) as its own standalone container. The official Hawtio
   project doesn't publish a Docker image itself — it's distributed via JBang, whose
   launcher shell script mangles the double-quoted JSON we'd need to pass preset
   connections through (see below), so instead of a third-party image (e.g.
   `boolivar/hawtio`) this project builds its own: `docker/ecpds/hawtio/Dockerfile`, on
   top of the project's own multi-arch `ecpds/java:graalvm` base image. It resolves
   `hawtio-embedded` and its runtime dependencies via plain Maven (the
   `copy-hawtio-deps` execution in `ecpds-core/pom.xml`, staged into
   `docker/ecpds/hawtio/lib` by `make -C docker get-hawtio`/`build-hawtio`) and drives
   `io.hawt.embedded.Main` directly from a small `HawtioLauncher.java` wrapper — no
   JBang involved. `make start-hawtio` (from the repo root) builds and starts it; if
   the Hawtio jars haven't been staged yet, it fails with a reminder to run
   `mvn package -pl ecpds-core` first. Hawtio gives a modern web UI for browsing every
   exposed attribute and invoking operations, at the `/hawtio` path.

   **Hawtio itself has no authentication by default**, and `HawtioLauncher` doesn't
   enable it (`io.hawt.embedded.Main.call()` leaves `hawtio.authenticationEnabled`
   unset/`false`) — so its own login can't be used. Instead, the image includes an
   nginx HTTP Basic Auth gateway: nothing reaches Hawtio without passing that first.
   Both the deployment and local development Compose files use this integrated gateway.

   **Single-container deployment:** nginx is now installed inside `ecpds/hawtio`.
   Its authenticated gateway defaults to port **8080** (`HAWTIO_GATEWAY_PORT`); Jetty is always bound to
   **127.0.0.1:8081**, regardless of supplied `HAWTIO_HOST`/`HAWTIO_PORT` settings.
   The entrypoint supervises both processes and stops the container if either exits.
   Standalone deployments must set `HAWTIO_USER` and either `HAWTIO_PASSWORD` or
   `HAWTIO_PASSWORD_FILE` (a readable, mounted secret file); missing credentials
   prevent startup. Use TLS termination or an SSH tunnel outside trusted networks.

   **Configuration files:** like Master, Monitor and Mover, the container sources
   `/etc/ecpds/default/hawtio.cnf` first, then `/etc/ecpds/hawtio.cnf` at each startup.
   These are trusted Bash configuration files: assignments override environment values,
   and the local file overrides defaults. No `export` is needed. For example:

   ```sh
   HAWTIO_USER="hawtio"
   HAWTIO_GATEWAY_PORT=8080
   HAWTIO_PASSWORD="replace-with-a-strong-password"
   HAWTIO_PRESET_CONNECTIONS="master=http://localhost:2062/jolokia,monitor=http://localhost:3062/jolokia,mover=http://localhost:4062/jolokia"
   JAVA_OPTS="-Dhawtio.proxyAllowlist=localhost,127.0.0.1"
   ```

   Protect files containing passwords with mode `0600`. Alternatively, set
   `HAWTIO_PASSWORD_FILE` to a path **inside the container**; when non-empty, its
   contents take precedence over `HAWTIO_PASSWORD`. Set it to `""` to disable an
   inherited password-file setting when switching to a password in the `.cnf`.
   Adjust the preset hosts and allowlist for servers on other machines.

   Development Compose files mount `${ECPDS_ROOT_PATH}/etc/ecpds` read-only at
   `/etc/ecpds` (default host path: `/etc/ecpds`); the deployment Compose file uses
   the `etc-ecpds-hawtio` volume. ECaccess-J's `hawtio-update` mounts
   `/ecpds/etc` at `/etc/ecpds`, configurable through `ECPDS_CONF_DIR`, and reads
   the same files for installation settings. Existing `.cnf` credentials are preserved
   rather than replaced with a generated password file.
   After editing settings, restart the Hawtio container/service; no image rebuild is
   needed for subsequent configuration changes.

   To use another gateway port with host networking, set `HAWTIO_GATEWAY_PORT=6082`
   in `hawtio.cnf` and restart. Valid ports are 1–65535 except private Jetty port 8081.
   With bridged networking, also update the published container port in Compose
   (for example `"6082:6082"`), or leave the gateway at 8080 and publish `"6082:8080"`.

   **HTTPS with the Monitor certificate:** when Hawtio uses the same DNS name covered
   by the Monitor certificate, it can reuse the Monitor PKCS#12 keystore. Set these
   in `hawtio.cnf` (paths are inside the container):

   ```sh
   HAWTIO_GATEWAY_PORT=6082
   HAWTIO_TLS_KEYSTORE="/etc/ecpds/monitor/ecpds-monitor.pfx"
   KEYSTORE_PASSWORD_FILE="/etc/ecpds/hawtio/keystore-password"
   ```

   Put the existing Monitor keystore password in that protected file, or set
   `KEYSTORE_PASSWORD` locally in `hawtio.cnf`. The password file takes precedence.
   Hawtio does not source `monitor.cnf`; supply the password explicitly.
   nginx serves **HTTPS only** on the selected port with TLS 1.2/1.3 and Basic Auth.
   The entrypoint extracts the certificate chain and private key into `/run/hawtio`
   without changing the source keystore; the extracted key is mode `0600`.
   Missing files, incorrect passwords or invalid certificates prevent startup rather
   than falling back to HTTP. The browser must trust the certificate's issuing CA.
   Restart Hawtio after replacing the Monitor keystore to reload the certificate.
   Leave `HAWTIO_TLS_KEYSTORE` unset or empty to retain HTTP.
   If OpenSSL reports an unsupported keystore algorithm such as `RC2-40-CBC`, set
   `HAWTIO_TLS_LEGACY=true` in `hawtio.cnf` to enable its legacy PKCS#12 reader.
   This does not weaken the HTTPS TLS protocols or change the source keystore.
   It defaults to `false`; prefer a modern-encrypted PKCS#12 file for new deployments.

   When upgrading from the two-container setup, stop/remove the old `hawtio-proxy`
   container (and disable its systemd service, if present) before starting the new image,
   since it otherwise occupies port 8080. Compose users can use
   `docker compose -f <compose-file> up -d --remove-orphans hawtio` after rebuilding.
   ECaccess-J uses the same single-container layout; its deployment needs just one
   `ecpds-hawtio` systemd service, with gateway credentials passed to the Hawtio container.

   These compose files default `HAWTIO_USER`/`HAWTIO_PASSWORD` to `hawtio` / `hawtio2021`,
   and `JOLOKIA_USER`/`JOLOKIA_PASSWORD` (for `master`/`monitor`/`mover`) to `jolokia` /
   `jolokia2021`, read from your shell environment but never committed — export any of
   them yourself to override. Once the stack is up (`http://<host>:8080/hawtio`, login
   `hawtio`/`hawtio2021` unless overridden), the Connect/Remote page
   (`/hawtio/connect/remote`) comes pre-filled with a connection for each of
   `master`/`mover`/`monitor` — no manual entry needed. This is driven by the
   `HAWTIO_PRESET_CONNECTIONS` environment variable on the `hawtio` service, a
   comma-separated list of `name=url` pairs (e.g.
   `master=http://ecpds-master:2062/jolokia,mover=http://ecpds-mover:4062/jolokia`),
   which `HawtioLauncher` parses and feeds to `Main.setConnections(...)`. No extra
   port needs to be published to the host for Jolokia, since Hawtio reaches the other
   containers directly over the `backbone` network (or `localhost`, for the Linux dev
   stack, which uses host networking throughout). On every platform, Jetty listens on
   loopback port `8081` and the integrated gateway defaults to `8080`.
   Note: this is upstream Hawtio behaviour, not something this image controls — on every
   fresh visit to the Hawtio home page (i.e. whenever there's no `?con=` in the URL),
   Hawtio doesn't just list the preset connections, it immediately opens each one as a
   browser tab (the first by navigating the current tab, the rest via `window.open`). If
   that's not what you want, navigate directly to `/hawtio/connect/remote` and connect
   manually instead of hitting `/hawtio` first.
   Also note: Hawtio's server-side proxy only allows connecting to hosts listed in
   `-Dhawtio.proxyAllowlist` (already set to the right hostnames, via `JAVA_OPTS`, in
   these compose files); add any other hostname there before trying to connect to it.
   On macOS, `make start-hawtio` points the preset connections at `host.docker.internal`
   when no `master`/`monitor`/`mover` containers are running (e.g. servers launched from
   an IDE). Override with `HAWTIO_JOLOKIA_HOST=<host> make start-hawtio` if needed.

### Explicit HTTPS termination with HTTP Jolokia endpoints

When the browser uses HTTPS but Hawtio connects to an HTTP Jolokia endpoint, Jolokia
rejects the forwarded HTTPS Origin with `Secure origin ... should not be processed
over HTTP`. This can appear in an HTTP-200 JSON response and be presented by Hawtio
as an incorrect-password error. Do not disable authentication to address it.

For a trusted backend connection, explicitly enable TLS termination using a Jolokia
policy. Each service ships `conf/jolokia-access.xml.example`, **disabled by default**.
Copy it into the mounted configuration directory, for example on the ECaccess-J host:

```sh
install -m 0644 /usr/local/ecpds/monitor/conf/jolokia-access.xml.example \
  /ecpds/etc/monitor/jolokia-access.xml
```

The source template is inside the installed service/container; if it is not available
on the host, create the destination file using the XML below. Do not overwrite an
existing policy: merge the CORS settings while retaining its other restrictions.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<restrict>
  <cors>
    <allow-origin>https://hawtio.example.com:6082</allow-origin>
    <strict-checking/>
    <ignore-scheme/>
  </cors>
</restrict>
```

Replace the origin with the exact browser scheme, hostname and port. Add explicit
`allow-origin` entries for additional trusted browser origins; avoid wildcards.
Strict checking also rejects requests **without** an Origin header. The example
adds origin restrictions, not MBean operation restrictions; authentication and network
access controls remain essential.

In `monitor.cnf`, enable the file via its **container path**:

```sh
export JOLOKIA_POLICY_LOCATION="file:/etc/ecpds/monitor/jolokia-access.xml"
```

Use corresponding `master` and `mover` paths/settings for those services. This option
accepts readable local `file:/absolute/path` URLs without spaces or special characters.
Rebuild/redeploy the service RPMs/images once to obtain the updated startup scripts,
then restart each Java service after enabling or editing its policy. Hawtio itself
does not need rebuilding for this policy change. Keep preset URLs using `http://`.
Existing Compose mounts already expose `/etc/ecpds`; supply the policy through the
service's mounted host directory or configuration volume. IDE argfiles bypass these
startup scripts: add `policyLocation=file:/absolute/path` to their Jolokia agent options
explicitly if using an IDE launch.

The `<ignore-scheme/>` setting permits this deliberate TLS termination; it does not
encrypt the backend HTTP connection. For same-host services, prefer `JOLOKIA_HOST=127.0.0.1`
with localhost presets when host networking is used. For remote hosts, restrict agent
ports to trusted traffic; end-to-end HTTPS is preferable for remote Movers.
Keep gateway HTTPS, Jolokia Basic Auth and Hawtio's host allowlist enabled.

ECaccess-J's `hawtio-login-check` sends the gateway Origin on its version POSTs to
reproduce browser behavior, including policy rejection. Check the JSON `status` and
`error`, not just the HTTP status.

## Prometheus JMX exporter + Grafana

1. A starter mapping config is provided at `etc/master/conf/jmx_exporter.yml` /
   `etc/mover/conf/jmx_exporter.yml` / `etc/monitor/conf/jmx_exporter.yml` — it exports
   every MBean as-is (`pattern: ".*"`). Narrow it once you've decided which attributes you
   actually want graphed.
2. Set `USE_JCONSOLE=true` and `PROMETHEUS_JMX_PORT` (e.g. `9404`, and optionally
   `PROMETHEUS_JMX_HOST`, default `0.0.0.0`) before starting the service.
3. Point Prometheus at it:
   ```yaml
   scrape_configs:
     - job_name: ecpds-master
       static_configs:
         - targets: ["master-host:9404"]
     - job_name: ecpds-mover
       static_configs:
         - targets: ["mover-host:9404"]
     - job_name: ecpds-monitor
       static_configs:
         - targets: ["monitor-host:9404"]
   ```
   then add a Grafana dashboard against that Prometheus data source.

!!! note
    A few attributes are still exposed only as a multi-item text report rather than a
    single scalar — `ThreadList`, `QueueStatus`, `ConnectionsList`, `CurrentTransfers` — since
    they genuinely list several items at once. Each has a numeric sibling that graphs the
    size of that list directly (`ThreadCount`, `QueueSize`, `ConnectionsCount`/
    `ConnectionsActiveCount`, `PendingTransferCount`/`TransferCount`), so no custom parsing
    is needed to track them in Grafana. Durations that used to be exposed only as a
    formatted string (e.g. `ConnectionsDurationAve`, `StepTimeCurrent`, `Inactivity`) now
    also have a `*Millis` sibling (`ConnectionsDurationAveMillis`, `StepTimeCurrentMillis`,
    `InactivityMillis`, …) with the raw numeric value.
