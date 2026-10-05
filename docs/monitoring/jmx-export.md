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
   unset/`false`) — so its own login can't be used. Instead, both
   `deploy/kubernetes/docker-compose.yml` and the local dev stack
   (`run/bin/ecpds/{Linux,Darwin}-ecpds/docker-compose.yml`, driven by `make start-hawtio`/
   `stop-hawtio`) put a small `nginx:alpine` sidecar (`hawtio-proxy`) in front of it as an
   HTTP Basic Auth gateway — nothing reaches Hawtio without passing that first. Hawtio
   itself is also no longer published to the host directly; only `hawtio-proxy` is.

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
   stack, which uses host networking throughout — there, Hawtio itself listens on `8081`
   instead of `8080`, since `hawtio-proxy` takes `8080`).
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
