# Monitoring

The Monitoring section provides a real-time overview of the OpenECPDS system.
It is the first page shown after login and the primary operational dashboard.


## Dashboard (admin view)

The dashboard shows all destinations and their current transfer status. Each row represents a destination with colour-coded status indicators for queued, running, done, and failed transfers. The toolbar at the top provides quick access to filtering and refresh controls.


![Dashboard (admin view)](img/dashboard.png)


## Live Earth

**Live Earth** is a real-time 3D globe visualisation of the transfers currently in progress across every Data Mover, available from the Monitoring card on the start page (`/do/monitoring/globe`). Transfers are aggregated per destination Host: each Host with at least one active transfer is drawn as a single pulsing arc/marker from the Master Server to that Host, coloured by status (blue while active, red if any of its transfers is failing, green fading out just after completion), with the arc's thickness reflecting the Host's combined throughput and the marker growing slightly with its number of concurrent transfers. Clicking a Host marker opens a panel summarising its aggregate activity (active transfer count, protocol(s) in use, total throughput, total bytes transferred, longest-running transfer) together with a per-transfer breakdown table (Data Mover, protocol, rate, bytes, status).

The globe is powered by a lightweight WebSocket feed (`/ws/globe`) pushed from the Master Server, which aggregates live samples reported periodically by every Data Mover while at least one globe page is open — no additional load is added when nobody is watching. Transfers relayed through a ProxyHost (a Data Mover with no direct RMI connection to the Master Server, reachable only via another Data Mover's REST interface) are included as well: their samples are relayed to the Master Server through that intermediary Data Mover's REST endpoint, exactly like their transfer status and progress updates already are. Each active ProxyHost is additionally drawn as its own marker (a distinct violet dot) at its resolved geolocation, with the arc for its transfers starting from that marker rather than from the Master Server's own location — making it visually clear that the data left from a different place. Ordinary, directly-connected Data Movers are not shown as separate markers, since they are considered co-located with the Master Server itself. The map imagery and 3D rendering are provided by a self-hosted copy of [CesiumJS](https://cesium.com/platform/cesiumjs/); no external network access or account/token is required.

Above the globe, a small KPI strip shows the number of active transfers and hosts, a combined-throughput speed-meter, and the total bytes transferred over the rolling last 24 hours. The 24h total is maintained on the Master Server itself (not in the browser), so every open globe page — and every reconnect — shows the exact same figure. It is kept in memory for accuracy, but its underlying data is also periodically saved to the database (`SYS_CONFIG` table, every 2 minutes by default, plus once more on a graceful shutdown) and reloaded at startup, so a normal Master Server restart only loses at most a couple of minutes of history instead of resetting the whole 24h figure to zero. This can be disabled with `liveTransferBytesScheduler=no` in the `[Server]` section of the **Master Server's** `ecmwf.properties` (the save interval can also be adjusted with a `liveTransferBytesScheduler` duration entry in the `[Scheduler]` section), in which case the figure reverts to being memory-only.

By default, the globe is centred on the Master Server's own location, auto-detected via a GeoIP lookup of its hostname. If this location is inaccurate or cannot be resolved (for example, when the Master Server runs behind NAT/VPN or in a cloud/datacenter network range), it can be pinned explicitly with a `forced.<ip-or-prefix>` entry in the `[GeoIP]` section of the **Master Server's** `ecmwf.properties` — the same mechanism already used to force the location of any Host (e.g. for the traceroute/Nmap map on a Host's report page), so no separate/duplicate setting is needed. For example:

```ini
[GeoIP]
forced.<master-hostname-or-ip>=51.505,-0.09,Europe,GB,Reading
```

Because the globe's origin is resolved once by the Master Server itself (not by the Monitor plugin), it stays consistent even when several Monitor plugins are connected to the same Master Server.

The base map imagery is the low-resolution "Natural Earth II" imagery bundled with CesiumJS — sharp for a whole-Earth view, but it blurs once zoomed into a country/region, since no extra detail is bundled. Optionally, the Monitor plugin can transparently build and serve a sharper, higher-resolution layer (Natural Earth's 50m-resolution hypsometric/relief/water raster) instead: on the first "Live Earth" page opened after the Monitor starts, it downloads the (public-domain) source raster and tiles it locally — in pure Java, no GDAL or other native dependency required — into a small (a few MB) tile pyramid cached on disk. This happens entirely in the background and never blocks the page: it simply keeps using the bundled low-resolution imagery until the higher-resolution one finishes building, and silently falls back to it again if it can never be built (e.g. no internet access). If it fails (typically because the Monitor server has no internet access at the time), it keeps retrying every 15 minutes indefinitely, so connectivity becoming available only later is still picked up automatically without needing a restart. The cache location defaults to a subdirectory of the JVM's temp directory, and can be pointed at a persistent location (so it survives restarts and isn't rebuilt every time) with a `globeImageryCacheDir` entry in the `[Server]` section of the **Monitor's** `ecmwf.properties`:

```ini
[Server]
globeImageryCacheDir=/var/lib/ecpds/monitor/globe-imagery
```

Country and major city names can similarly be shown on the globe, using the same lazy/background/no-account approach: the Monitor plugin downloads Natural Earth's small (public-domain) country boundary and populated-place datasets on first use and converts them into a compact labels file, cached on disk (a few tens of KB). The default whole-globe view shows no names at all to stay uncluttered; country names join in first as the view zooms in a little, then city names join in even later as it zooms in further still (largest cities first, progressively smaller/less populated ones as it gets closer) - the same progressive-reveal technique used by most web map providers, shown as bold, uppercase, amber-bordered pills for countries versus plainer grey pills for cities so the two kinds of labels are easy to tell apart at a glance. Like the imagery layer, this never blocks the page — labels simply appear once (if ever) the background download finishes — and retries indefinitely every 15 minutes if it fails. They are hidden by default; a small tag icon next to the fullscreen button opens a dropdown with independent "Country names" / "Town names" switches, so each user can show either, both, or neither, remembered separately on their browser between visits. Its cache location can likewise be pointed at a persistent path with a `globeLabelsCacheDir` entry in the same `[Server]` section:

```ini
[Server]
globeLabelsCacheDir=/var/lib/ecpds/monitor/globe-labels
```

