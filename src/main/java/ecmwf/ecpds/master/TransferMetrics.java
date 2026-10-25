/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * In applying the License, ECMWF does not waive the privileges and immunities
 * granted to it by virtue of its status as an inter-governmental organization
 * nor does it submit to any jurisdiction.
 */
package ecmwf.ecpds.master;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.technical.Cnf;
import ecmwf.common.version.Version;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;

/** Optional Master-side operational metrics, independent of log and trace export. */
public final class TransferMetrics {
    private static final Logger LOG = LogManager.getLogger(TransferMetrics.class);
    private static final AttributeKey<String> OUTCOME = AttributeKey.stringKey("outcome");
    private static final AttributeKey<String> DIRECTION = AttributeKey.stringKey("direction");
    private static final AttributeKey<String> HOST_TYPE = AttributeKey.stringKey("host.type");
    private static final Set<String> HOST_TYPES = Set.copyOf(TrafficAccounting.TYPES);
    private static final TransferMetrics INSTANCE = new TransferMetrics();

    private volatile OpenTelemetrySdk sdk;
    private volatile LongCounter transferAttempts;
    private volatile LongCounter transfersCompleted;
    private volatile LongHistogram transferDuration;
    private volatile LongHistogram transferBytes;
    private volatile LongCounter retrievalAttempts;
    private volatile LongCounter retrievalsCompleted;
    private volatile LongHistogram retrievalDuration;
    private volatile LongHistogram retrievalBytes;
    private volatile LongCounter portalTransfers;
    private volatile LongHistogram portalDuration;
    private volatile LongHistogram portalBytes;

    private TransferMetrics() {
    }

    public static synchronized void start(final MasterServer master) {
        if (INSTANCE.sdk != null || !Cnf.at("OpenTelemetry", "metricsEnabled", false)) {
            return;
        }
        final var disabled = new AtomicBoolean();
        INSTANCE.sdk = AutoConfiguredOpenTelemetrySdk.builder()
                .addPropertiesSupplier(() -> Map.of("otel.service.name", "open-ecpds-master",
                        "otel.exporter.otlp.protocol", "http/protobuf", "otel.metrics.exporter", "otlp",
                        "otel.metric.export.interval", Cnf.at("OpenTelemetry", "metricsExportIntervalMillis", "60000"),
                        "otel.metric.export.timeout", Cnf.at("OpenTelemetry", "metricsExportTimeoutMillis", "30000")))
                .addPropertiesCustomizer(properties -> {
                    disabled.set(properties.getBoolean("otel.sdk.disabled", false));
                    return Map.of("otel.logs.exporter", "none", "otel.traces.exporter", "none");
                })
                .addResourceCustomizer((resource, _) -> Resource
                        .create(io.opentelemetry.api.common.Attributes.builder()
                                .put("service.version", Version.getFullVersion())
                                .put("service.instance.id",
                                        Cnf.at("Login", "root", Cnf.at("Login", "hostName", "master")))
                                .build())
                        .merge(resource))
                .disableShutdownHook().build().getOpenTelemetrySdk();
        if (disabled.get()) {
            LOG.info("OpenTelemetry metrics disabled by OTEL_SDK_DISABLED");
            return;
        }
        final var meter = INSTANCE.sdk.getMeter("ecmwf.ecpds.master.metrics");
        INSTANCE.transferAttempts = counter(meter, "ecpds.transfer.attempts", "{attempt}");
        INSTANCE.transfersCompleted = counter(meter, "ecpds.transfer.completions", "{transfer}");
        INSTANCE.transferDuration = histogram(meter, "ecpds.transfer.duration", "ms");
        INSTANCE.transferBytes = histogram(meter, "ecpds.transfer.bytes", "By");
        INSTANCE.retrievalAttempts = counter(meter, "ecpds.retrieval.mover.attempts", "{attempt}");
        INSTANCE.retrievalsCompleted = counter(meter, "ecpds.retrieval.completions", "{retrieval}");
        INSTANCE.retrievalDuration = histogram(meter, "ecpds.retrieval.duration", "ms");
        INSTANCE.retrievalBytes = histogram(meter, "ecpds.retrieval.bytes", "By");
        INSTANCE.portalTransfers = counter(meter, "ecpds.portal.transfers", "{transfer}");
        INSTANCE.portalDuration = histogram(meter, "ecpds.portal.transfer.duration", "ms");
        INSTANCE.portalBytes = histogram(meter, "ecpds.portal.transfer.bytes", "By");
        registerKpiGauges(meter, master);
        LOG.info("OpenTelemetry Master metrics enabled");
    }

    public static synchronized void shutdown() {
        final var sdk = INSTANCE.sdk;
        INSTANCE.sdk = null;
        INSTANCE.transferAttempts = null;
        INSTANCE.transfersCompleted = null;
        INSTANCE.transferDuration = null;
        INSTANCE.transferBytes = null;
        INSTANCE.retrievalAttempts = null;
        INSTANCE.retrievalsCompleted = null;
        INSTANCE.retrievalDuration = null;
        INSTANCE.retrievalBytes = null;
        INSTANCE.portalTransfers = null;
        INSTANCE.portalDuration = null;
        INSTANCE.portalBytes = null;
        if (sdk != null && !sdk.shutdown().join(10, TimeUnit.SECONDS).isSuccess()) {
            LOG.warn("OpenTelemetry metrics shutdown failed or timed out; pending measurements may be lost");
        }
    }

    public static void transferAttempt(final Context context) {
        add(INSTANCE.transferAttempts, 1, Attributes.empty(), context);
    }

    public static void transferCompleted(final String outcome, final long durationMillis, final long bytes,
            final Context context) {
        final var attributes = Attributes.of(OUTCOME, outcome);
        add(INSTANCE.transfersCompleted, 1, attributes, context);
        record(INSTANCE.transferDuration, durationMillis, attributes, context);
        record(INSTANCE.transferBytes, bytes, attributes, context);
    }

    public static void retrievalAttempt(final boolean successful, final Context context) {
        add(INSTANCE.retrievalAttempts, 1, Attributes.of(OUTCOME, successful ? "success" : "failure"), context);
    }

    public static void retrievalCompleted(final boolean successful, final long durationMillis, final long bytes,
            final Context context) {
        final var attributes = Attributes.of(OUTCOME, successful ? "success" : "failure");
        add(INSTANCE.retrievalsCompleted, 1, attributes, context);
        record(INSTANCE.retrievalDuration, durationMillis, attributes, context);
        if (successful) {
            record(INSTANCE.retrievalBytes, bytes, attributes, context);
        }
    }

    public static void portalTransfer(final boolean upload, final boolean successful, final long durationMillis,
            final long bytes, final Context context) {
        final var attributes = Attributes.of(DIRECTION, upload ? "upload" : "download", OUTCOME,
                successful ? "success" : "failure");
        final var metricContext = context != null ? context : Context.root();
        add(INSTANCE.portalTransfers, 1, attributes, metricContext);
        record(INSTANCE.portalDuration, durationMillis, attributes, metricContext);
        record(INSTANCE.portalBytes, bytes, attributes, metricContext);
    }

    private static LongCounter counter(final Meter meter, final String name, final String unit) {
        return meter.counterBuilder(name).setUnit(unit).build();
    }

    private static LongHistogram histogram(final Meter meter, final String name, final String unit) {
        return meter.histogramBuilder(name).ofLongs().setUnit(unit).build();
    }

    private static void add(final LongCounter counter, final long value, final Attributes attributes,
            final Context context) {
        if (counter != null) {
            counter.add(value, attributes, context != null ? context : Context.current());
        }
    }

    private static void record(final LongHistogram histogram, final long value, final Attributes attributes,
            final Context context) {
        if (histogram != null && value >= 0) {
            histogram.record(value, attributes, context != null ? context : Context.current());
        }
    }

    private static void registerKpiGauges(final Meter meter, final MasterServer master) {
        meter.gaugeBuilder("ecpds.traffic.bytes.24h").ofLongs().setUnit("By")
                .setDescription("Actual bytes transferred in the rolling 24-hour window by Host Type")
                .buildWithCallback(measurement -> {
                    try {
                        master.getActualTrafficTotals()
                                .forEach((type, bytes) -> measurement.record(bytes, Attributes.of(HOST_TYPE, type)));
                    } catch (final java.rmi.RemoteException e) {
                        LOG.warn("Cannot collect rolling 24-hour traffic metrics", e);
                    }
                });

        meter.gaugeBuilder("ecpds.portal.sessions").ofLongs().setUnit("{session}")
                .setDescription("Currently open Data Portal connections")
                .buildWithCallback(measurement -> measurement.record(master.getIncomingConnectionsCount()));
        final var portal = DataPortalActivityRegistry.getInstance();
        meter.gaugeBuilder("ecpds.portal.throughput").setUnit("By/s")
                .setDescription("Rolling Data Portal upload and download throughput").buildWithCallback(measurement -> {
                    measurement.record(Math.max(0, Math.round(portal.getBytesInPerSecond())),
                            Attributes.of(DIRECTION, "upload"));
                    measurement.record(Math.max(0, Math.round(portal.getBytesOutPerSecond())),
                            Attributes.of(DIRECTION, "download"));
                });

        meter.gaugeBuilder("ecpds.storage.bytes").ofLongs().setUnit("By")
                .setDescription("Cached DataMover storage usage; used and capacity are separate measurements")
                .buildWithCallback(measurement -> {
                    var used = 0L;
                    var capacity = 0L;
                    for (final var volumes : master.getMoverVolumeSnapshot(null).values()) {
                        if (volumes == null || volumes.length != 2 || volumes[0] == null || volumes[1] == null) {
                            continue;
                        }
                        final var count = Math.min(volumes[0].length, volumes[1].length);
                        for (var index = 0; index < count; index++) {
                            if (volumes[0][index] >= 0) {
                                used = Math.addExact(used, volumes[0][index]);
                            }
                            if (volumes[1][index] >= 0) {
                                capacity = Math.addExact(capacity, volumes[1][index]);
                            }
                        }
                    }
                    measurement.record(used, Attributes.of(AttributeKey.stringKey("state"), "used"));
                    measurement.record(capacity, Attributes.of(AttributeKey.stringKey("state"), "capacity"));
                });

        meter.gaugeBuilder("ecpds.transfer.active").ofLongs().setUnit("{transfer}")
                .setDescription("Active sampled transfers by Host Type; emitted only while live sampling is enabled")
                .buildWithCallback(measurement -> {
                    if (!LiveTransferRegistry.getInstance().isEnabled()) {
                        return;
                    }
                    final Map<String, Long> counts = new HashMap<>();
                    for (final var sample : LiveTransferRegistry.getInstance().getActiveTransfers()) {
                        counts.merge(hostType(sample), 1L, Long::sum);
                    }
                    counts.forEach((type, count) -> measurement.record(count, Attributes.of(HOST_TYPE, type)));
                });
        meter.gaugeBuilder("ecpds.transfer.active.hosts").ofLongs().setUnit("{host}")
                .setDescription("Distinct active sampled transfer hosts by Host Type")
                .buildWithCallback(measurement -> {
                    if (!LiveTransferRegistry.getInstance().isEnabled()) {
                        return;
                    }
                    final Map<String, Set<String>> hosts = new HashMap<>();
                    for (final var sample : LiveTransferRegistry.getInstance().getActiveTransfers()) {
                        if (sample.getHostName() != null) {
                            hosts.computeIfAbsent(hostType(sample), _ -> new HashSet<>()).add(sample.getHostName());
                        }
                    }
                    hosts.forEach((type, names) -> measurement.record(names.size(), Attributes.of(HOST_TYPE, type)));
                });
        meter.gaugeBuilder("ecpds.transfer.active.throughput").setUnit("By/s")
                .setDescription("Active sampled transfer throughput by Host Type").buildWithCallback(measurement -> {
                    if (!LiveTransferRegistry.getInstance().isEnabled()) {
                        return;
                    }
                    final Map<String, Double> rates = new HashMap<>();
                    for (final var sample : LiveTransferRegistry.getInstance().getActiveTransfers()) {
                        final var bitsPerSecond = sample.getRateBitsPerSecond();
                        if (Double.isFinite(bitsPerSecond) && bitsPerSecond > 0) {
                            rates.merge(hostType(sample), bitsPerSecond / 8, Double::sum);
                        }
                    }
                    rates.forEach((type, bytesPerSecond) -> measurement.record(bytesPerSecond,
                            Attributes.of(HOST_TYPE, type)));
                });
    }

    private static String hostType(final LiveTransferSample sample) {
        final var type = sample.getHostType();
        if (type != null && HOST_TYPES.contains(type)) {
            return type;
        }
        return LiveTransferSample.DIRECTION_ACQUISITION.equals(sample.getDirection()) ? "Acquisition" : "Dissemination";
    }
}
