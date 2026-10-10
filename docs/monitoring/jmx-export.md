# JMX monitoring with Hawtio and Prometheus

OpenECPDS services expose application and JVM information through JMX. Administrators
can make that information available to two optional tools:

| Tool | Purpose |
|---|---|
| [Hawtio](https://hawt.io), through Jolokia | Browse service MBeans and inspect attributes or invoke operations in a web browser. |
| [Prometheus JMX exporter](https://github.com/prometheus/jmx_exporter) | Publish metrics for Prometheus to collect and Grafana to display. |

These integrations are configured independently. Enable only the one(s) you need.

## For administrators

### Hawtio access with Jolokia

Enable Jolokia for each service by setting its port in that service's configuration
before starting it. For example, in `monitor.cnf`:

```sh
JOLOKIA_PORT=3062
JOLOKIA_USER="jolokia"
JOLOKIA_PASSWORD="replace-with-a-secret"
```

Use the corresponding `master.cnf` or `mover.cnf` for those services. Jolokia has no
authentication by default. Set both `JOLOKIA_USER` and `JOLOKIA_PASSWORD` to enable it.
The agent listens on all interfaces by default; restrict access with a firewall or set
`JOLOKIA_HOST=127.0.0.1` when Hawtio is on the same host. Do not expose an
unauthenticated Jolokia endpoint to an untrusted network.

Run the Hawtio gateway separately and configure its login and the Jolokia endpoints it
should offer. For example, in `hawtio.cnf`:

```sh
HAWTIO_USER="hawtio"
HAWTIO_PASSWORD_FILE="/etc/ecpds/hawtio/password"
HAWTIO_NGINX_WORKER_PROCESSES=2
HAWTIO_PRESET_CONNECTIONS="master=http://localhost:2062/jolokia,monitor=http://localhost:3062/jolokia,mover=http://localhost:4062/jolokia"
JAVA_OPTS="-Dhawtio.proxyAllowlist=localhost,127.0.0.1"
```

Use a protected password file, or configure `HAWTIO_PASSWORD` instead. The gateway
login and Jolokia credentials are separate. Update endpoint hosts and the proxy
allowlist when services are on other machines. The gateway defaults to port `8080`;
`HAWTIO_GATEWAY_PORT` changes it. Configure HTTPS at the gateway or use a trusted
network/SSH tunnel. The gateway root shows links to configured connections; the full
Hawtio interface is at `/hawtio/`.
`HAWTIO_NGINX_WORKER_PROCESSES` controls nginx's worker count (default `2`); set a
positive integer in `hawtio.cnf` and restart the container to apply it.
`HAWTIO_ACCESS_LOG` controls the gateway access log. It is `off` by default because
Hawtio's polling is verbose and the container's stdout is not rotated. Set it to
`/dev/stdout` or to an absolute file path (rotate it yourself) to enable it; errors are
always written to stderr.

The Hawtio container reads `/etc/ecpds/default/hawtio.cnf` followed by
`/etc/ecpds/hawtio.cnf`; the latter overrides defaults. Protect configuration files
that contain passwords, then restart the affected container or service after changes.

#### HTTPS gateway with HTTP Jolokia endpoints

When a browser connects to Hawtio over HTTPS but Hawtio proxies to Jolokia over HTTP,
Jolokia may reject the forwarded secure origin. For this deliberate TLS-termination
setup, configure a Jolokia access policy on each affected service, allowing only the
exact Hawtio origin:

```xml
<restrict>
  <cors>
    <allow-origin>https://hawtio.example.com:6082</allow-origin>
    <strict-checking/>
    <ignore-scheme/>
  </cors>
</restrict>
```

Set the origin to the browser-visible scheme, hostname and port. Enable the policy in
the service's `.cnf` file, for example:

```sh
JOLOKIA_POLICY_LOCATION="file:/etc/ecpds/monitor/jolokia-access.xml"
```

The policy is opt-in; keep Jolokia authentication and network restrictions enabled.
The backend HTTP connection is not encrypted. For remote services, prefer end-to-end
HTTPS rather than TLS termination.

### Prometheus and Grafana

Enable the exporter for a service by setting `USE_JCONSOLE=true` and a metrics port in
its `.cnf` file. For example:

```sh
USE_JCONSOLE=true
PROMETHEUS_JMX_PORT=9404
```

The exporter uses the platform MBean server selected by `USE_JCONSOLE`. Starter mapping
configurations are provided at `etc/master/conf/jmx_exporter.yml`,
`etc/mover/conf/jmx_exporter.yml`, and `etc/monitor/conf/jmx_exporter.yml`; refine the
mapping to expose only the metrics needed. Restrict access to the metrics port to
Prometheus and other trusted clients.

Add each enabled endpoint to Prometheus, using the actual host and port:

```yaml
scrape_configs:
  - job_name: ecpds-master
    static_configs:
      - targets: ["master-host:9404"]
  - job_name: ecpds-monitor
    static_configs:
      - targets: ["monitor-host:9404"]
```

Grafana dashboards can then query the collected metrics through the Prometheus data
source.

## For users

Open the Hawtio gateway URL provided by your administrator and sign in with the gateway
account. The landing page shows responsive cards for configured service presets only;
each opens its Hawtio connection in a new tab, keeping the service selector available.
There is no generic Connect / Remote shortcut on this page (Hawtio itself still
provides its connection interface). Choose a service or open its Hawtio connection link,
such as `/hawtio/?con=master`. Hawtio lets you browse that service's MBeans and view
their attributes. Some MBeans also provide operations; invoking them can change service
state, so use those controls only when authorized.

Prometheus and Grafana provide a metrics-oriented view of the services. Use the
dashboards available to you to follow trends and compare measurements over time; they
do not provide the interactive MBean browsing and operations available in Hawtio.
