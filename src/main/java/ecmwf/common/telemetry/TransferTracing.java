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
package ecmwf.common.telemetry;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.database.DataFile;
import ecmwf.common.database.DataTransfer;
import ecmwf.common.database.Host;
import ecmwf.common.technical.ProxyEvent;
import ecmwf.common.technical.Cnf;
import ecmwf.common.version.Version;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.data.StatusData;
import io.opentelemetry.sdk.trace.export.SpanExporter;

/** Optional, sampled traces for dissemination, retrieval and incoming portal transfers. */
public final class TransferTracing {
    private static final Logger LOG = LogManager.getLogger(TransferTracing.class);
    private static final W3CTraceContextPropagator PROPAGATOR = W3CTraceContextPropagator.getInstance();
    private static final AtomicLong LAST_EXPORT_WARNING = new AtomicLong();
    private static final TextMapGetter<DataTransfer> GETTER = new TextMapGetter<>() {
        @Override
        public Iterable<String> keys(final DataTransfer carrier) {
            return List.of("traceparent", "tracestate");
        }

        @Override
        public String get(final DataTransfer carrier, final String key) {
            return "traceparent".equals(key) ? carrier.getTraceParent()
                    : "tracestate".equals(key) ? carrier.getTraceState() : null;
        }
    };
    private static OpenTelemetrySdk sdk;
    private static volatile Tracer tracer;
    private static volatile MoverTracing.Delivery imported;

    private TransferTracing() {
    }

    public static synchronized void start(final String role) {
        if (sdk != null || !Cnf.at("OpenTelemetry", "tracesEnabled", false)) {
            return;
        }
        final var disabled = new AtomicBoolean();
        final var exporters = new ArrayList<SpanExporter>();
        sdk = AutoConfiguredOpenTelemetrySdk.builder()
                .addPropertiesSupplier(() -> Map.of("otel.service.name", "open-ecpds-" + role,
                        "otel.exporter.otlp.protocol", "http/protobuf", "otel.traces.exporter", "otlp",
                        "otel.traces.sampler", "parentbased_traceidratio", "otel.traces.sampler.arg",
                        Cnf.at("OpenTelemetry", "tracesSampleRatio", "0.01"), "otel.bsp.max.queue.size",
                        Cnf.at("OpenTelemetry", "tracesMaxQueueSize", "2048"), "otel.bsp.max.export.batch.size",
                        Cnf.at("OpenTelemetry", "tracesMaxExportBatchSize", "512"), "otel.bsp.schedule.delay",
                        Cnf.at("OpenTelemetry", "tracesScheduleDelayMillis", "1000")))
                .addPropertiesCustomizer(properties -> {
                    disabled.set(properties.getBoolean("otel.sdk.disabled", false));
                    return Map.of("otel.logs.exporter", "none", "otel.metrics.exporter", "none");
                })
                .addResourceCustomizer(
                        (resource, _) -> Resource
                                .create(Attributes.builder().put("service.version", Version.getFullVersion())
                                        .put("service.instance.id",
                                                Cnf.at("Login", "root", Cnf.at("Login", "hostName", role)))
                                        .build())
                                .merge(resource))
                .addSpanExporterCustomizer((exporter, properties) -> TelemetryTls.configure(exporter, properties))
                .addSpanExporterCustomizer((exporter, _) -> {
                    final var monitored = new SpanExporter() {
                        @Override
                        public CompletableResultCode export(final Collection<SpanData> spans) {
                            final var result = exporter.export(spans);
                            result.whenComplete(() -> {
                                if (!result.isSuccess()) {
                                    final var now = System.nanoTime();
                                    final var previous = LAST_EXPORT_WARNING.get();
                                    if ((previous == 0 || now - previous >= TimeUnit.MINUTES.toNanos(1))
                                            && LAST_EXPORT_WARNING.compareAndSet(previous, now)) {
                                        LOG.warn("OpenTelemetry trace export failed; spans may be lost. "
                                                + "Check Collector availability and SDK diagnostics");
                                    }
                                }
                            });
                            return result;
                        }

                        @Override
                        public CompletableResultCode flush() {
                            return exporter.flush();
                        }

                        @Override
                        public CompletableResultCode shutdown() {
                            return exporter.shutdown();
                        }
                    };
                    exporters.add(monitored);
                    return monitored;
                }).disableShutdownHook().build().getOpenTelemetrySdk();
        if (disabled.get()) {
            LOG.info("OpenTelemetry transfer tracing disabled by OTEL_SDK_DISABLED");
            return;
        }
        tracer = sdk.getTracer("ecmwf.ecpds.transfers");
        if (!exporters.isEmpty()) {
            // The SDK owns these exporters. Import must never flush or close them.
            final var exporter = SpanExporter.composite(exporters);
            imported = new MoverTracing.Delivery(spans -> {
                final var result = exporter.export(spans.stream().map(TransferTracing::spanData).toList());
                // Keep exactly one batch in flight, even if the exporter never completes.
                // Delivery shutdown interrupts this daemon; it never waits for it.
                while (!result.isDone() && !Thread.currentThread().isInterrupted()) {
                    result.join(1, TimeUnit.SECONDS);
                }
                if (!result.isSuccess()) {
                    throw new IllegalStateException("Imported transfer span export failed");
                }
            }, "master");
        }
        LOG.info("OpenTelemetry transfer tracing enabled for {}", role);
    }

    /** Validate, detach and enqueue only: no collector I/O on the RMI/REST thread. */
    public static void accept(final List<TransferSpan> spans) {
        final var current = imported;
        if (current == null) {
            throw new IllegalStateException("Master transfer span import is disabled");
        }
        current.accept(spans);
    }

    public static synchronized void shutdown() {
        tracer = null;
        final var current = imported;
        imported = null;
        if (current != null) {
            current.close();
        }
        if (sdk != null) {
            if (!sdk.shutdown().join(10, TimeUnit.SECONDS).isSuccess()) {
                LOG.warn("OpenTelemetry trace shutdown failed or timed out; pending spans may be lost");
            }
            sdk = null;
        }
    }

    public static Attempt dispatch(final DataTransfer transfer) {
        transfer.setTraceParent(null);
        transfer.setTraceState(null);
        transfer.setTraceAttemptId(null);
        if (tracer == null) {
            return new Attempt(Span.getInvalid(), Context.root());
        }
        transfer.setTraceAttemptId(UUID.randomUUID().toString());
        return start(transfer, "ecpds.transfer.dispatch", Context.root(), SpanKind.PRODUCER);
    }

    public static Attempt execution(final DataTransfer transfer) {
        return start(transfer, "ecpds.transfer.execute", context(transfer), SpanKind.CONSUMER);
    }

    public static Attempt completion(final DataTransfer transfer) {
        return start(transfer, "ecpds.transfer.complete", context(transfer), SpanKind.CONSUMER);
    }

    public static Retrieval retrieval(final DataFile file, final DataTransfer transfer, final Host source) {
        file.setRetrievalTraceParent(null);
        file.setRetrievalTraceState(null);
        file.setRetrievalTraceId(null);
        file.setRetrievalAttempt(0);
        final var currentTracer = tracer;
        if (currentTracer == null) {
            return new Retrieval(Span.getInvalid(), Context.root(), null);
        }
        final var id = UUID.randomUUID().toString();
        final var builder = currentTracer.spanBuilder("ecpds.retrieval").setSpanKind(SpanKind.INTERNAL)
                .setAttribute("ecpds.retrieval.id", id).setAttribute("ecpds.file.id", file.getId())
                .setAttribute("ecpds.transfer.id", transfer.getId());
        if (transfer.getDestinationName() != null) {
            builder.setAttribute("ecpds.destination.name", transfer.getDestinationName());
        }
        if (source != null && source.getName() != null) {
            builder.setAttribute("ecpds.host.id", source.getName());
        }
        final var span = builder.startSpan();
        if (!span.getSpanContext().isValid()) {
            return new Retrieval(span, Context.root(), null);
        }
        final var context = Context.root().with(span);
        inject(context, file);
        file.setRetrievalTraceId(id);
        return new Retrieval(span, context, id);
    }

    /** Records the protocol-reported duration of a completed incoming transfer and returns its log context. */
    public static Context portalTransfer(final DataTransfer transfer, final ProxyEvent event,
            final boolean successful) {
        return portalTransfer(transfer, event.getUpload(), event.getProtocol(), event.getLocalHost(),
                event.getStartTime(), event.getDuration(), event.getSent(), successful);
    }

    public static Context portalTransfer(final DataTransfer transfer, final boolean upload, final String protocol,
            final String server, final long startTime, final long durationMillis, final long bytes,
            final boolean successful) {
        final var currentTracer = tracer;
        if (currentTracer == null) {
            return Context.root();
        }
        final var start = startTime > 0 ? startTime : System.currentTimeMillis();
        final var duration = Math.max(0, durationMillis);
        final var end = duration > Long.MAX_VALUE - start ? Long.MAX_VALUE : start + duration;
        final var builder = currentTracer.spanBuilder("ecpds.portal.transfer").setSpanKind(SpanKind.INTERNAL)
                .setStartTimestamp(start, TimeUnit.MILLISECONDS).setAttribute("ecpds.file.id", transfer.getDataFileId())
                .setAttribute("ecpds.portal.action", upload ? "upload" : "download")
                .setAttribute("ecpds.portal.bytes", bytes).setAttribute("ecpds.portal.duration_ms", duration);
        if (protocol != null) {
            builder.setAttribute("ecpds.portal.protocol", protocol);
        }
        if (server != null) {
            builder.setAttribute("ecpds.portal.server", server);
        }
        final var span = builder.startSpan();
        if (!successful) {
            span.setStatus(StatusCode.ERROR);
        }
        final var context = Context.root().with(span);
        span.end(end, TimeUnit.MILLISECONDS);
        return context;
    }

    private static void inject(final Context context, final DataFile file) {
        PROPAGATOR.inject(context, file, (carrier, key, value) -> {
            if ("traceparent".equals(key)) {
                carrier.setRetrievalTraceParent(value);
            } else if ("tracestate".equals(key)) {
                carrier.setRetrievalTraceState(value);
            }
        });
    }

    public static final class Retrieval implements AutoCloseable {
        private final Span span;
        private final Context context;
        private final String id;
        private final AtomicBoolean ended = new AtomicBoolean();

        private Retrieval(final Span span, final Context context, final String id) {
            this.span = span;
            this.context = context;
            this.id = id;
        }

        public Context context() {
            return context;
        }

        public String id() {
            return id;
        }

        public RetrievalAttempt moverAttempt(final DataFile file, final Host source, final String mover,
                final int number) {
            final var currentTracer = tracer;
            if (currentTracer == null || !span.getSpanContext().isValid()) {
                return new RetrievalAttempt(Span.getInvalid());
            }
            final var builder = currentTracer.spanBuilder("ecpds.retrieval.mover").setParent(context)
                    .setSpanKind(SpanKind.CLIENT).setAttribute("ecpds.retrieval.id", id)
                    .setAttribute("ecpds.file.id", file.getId()).setAttribute("ecpds.retrieval.attempt", number)
                    .setAttribute("ecpds.mover.name", mover);
            if (source != null && source.getName() != null) {
                builder.setAttribute("ecpds.host.id", source.getName());
            }
            final var attempt = builder.startSpan();
            if (attempt.getSpanContext().isValid()) {
                inject(Context.root().with(attempt), file);
                file.setRetrievalTraceId(id);
                file.setRetrievalAttempt(number);
            }
            return new RetrievalAttempt(attempt);
        }

        public void finish(final boolean successful) {
            if (ended.compareAndSet(false, true)) {
                if (!successful) {
                    span.setStatus(StatusCode.ERROR);
                }
                span.end();
            }
        }

        @Override
        public void close() {
            finish(false);
        }
    }

    public static final class RetrievalAttempt implements AutoCloseable {
        private final Span span;
        private final AtomicBoolean ended = new AtomicBoolean();

        private RetrievalAttempt(final Span span) {
            this.span = span;
        }

        public Context context() {
            return span.getSpanContext().isValid() ? Context.root().with(span) : Context.root();
        }

        public void finish(final boolean successful) {
            if (ended.compareAndSet(false, true)) {
                if (!successful) {
                    span.setStatus(StatusCode.ERROR);
                }
                span.end();
            }
        }

        @Override
        public void close() {
            finish(false);
        }
    }

    private static Attempt start(final DataTransfer transfer, final String name, final Context parent,
            final SpanKind kind) {
        final var currentTracer = tracer;
        if (currentTracer == null || transfer.getTraceAttemptId() == null
                || !"ecpds.transfer.dispatch".equals(name) && !Span.fromContext(parent).getSpanContext().isValid()) {
            return new Attempt(Span.getInvalid(), parent);
        }
        final var builder = currentTracer.spanBuilder(name).setParent(parent).setSpanKind(kind)
                .setAttribute("ecpds.transfer.id", transfer.getId())
                .setAttribute("ecpds.transfer.attempt.id", transfer.getTraceAttemptId())
                .setAttribute("ecpds.transfer.start_count", transfer.getStartCount())
                .setAttribute("ecpds.transfer.requeue_count", transfer.getRequeueHistory());
        builder.setAttribute("ecpds.file.id", transfer.getDataFileId());
        if (transfer.getDestinationName() != null) {
            builder.setAttribute("ecpds.destination.name", transfer.getDestinationName());
        }
        if (transfer.getHostName() != null) {
            builder.setAttribute("ecpds.host.id", transfer.getHostName());
        }
        final var span = builder.startSpan();
        if (!span.getSpanContext().isValid()) {
            if ("ecpds.transfer.dispatch".equals(name)) {
                transfer.setTraceAttemptId(null);
            }
            return new Attempt(span, parent);
        }
        final var context = Context.root().with(span);
        transfer.setTraceState(null);
        PROPAGATOR.inject(context, transfer, (carrier, key, value) -> {
            if ("traceparent".equals(key)) {
                carrier.setTraceParent(value);
            } else if ("tracestate".equals(key)) {
                carrier.setTraceState(value);
            }
        });
        return new Attempt(span, context);
    }

    public static Context context(final DataTransfer transfer) {
        if (transfer.getTraceParent() == null) {
            return Context.root();
        }
        if (transfer.getTraceParent().length() > 55
                || transfer.getTraceState() != null && transfer.getTraceState().length() > 512
                || transfer.getTraceAttemptId() == null || transfer.getTraceAttemptId().length() > 64) {
            LOG.warn("Invalid trace carrier for transfer {}; ignoring trace context", transfer.getId());
            return Context.root();
        }
        final var context = PROPAGATOR.extract(Context.root(), transfer, GETTER);
        if (!Span.fromContext(context).getSpanContext().isValid()) {
            LOG.warn("Invalid traceparent for transfer {}; ignoring trace context", transfer.getId());
        }
        return context;
    }

    public static void copy(final DataTransfer source, final DataTransfer target) {
        target.setTraceParent(source.getTraceParent());
        target.setTraceState(source.getTraceState());
        target.setTraceAttemptId(source.getTraceAttemptId());
    }

    /** Only compare known attempt IDs, preserving interoperability with uninstrumented services. */
    public static boolean isStale(final DataTransfer local, final DataTransfer incoming) {
        return local != null && local.getTraceAttemptId() != null && incoming.getTraceAttemptId() != null
                && !local.getTraceAttemptId().equals(incoming.getTraceAttemptId());
    }

    private static SpanData spanData(final TransferSpan span) {
        final var stateBuilder = TraceState.builder();
        if (span.getTraceState() != null && !span.getTraceState().isEmpty()) {
            final var entries = span.getTraceState().split(",");
            // TraceStateBuilder inserts at the front; retain incoming member order.
            for (int index = entries.length - 1; index >= 0; index--) {
                final var entry = entries[index].strip();
                final var equals = entry.indexOf('=');
                stateBuilder.put(entry.substring(0, equals), entry.substring(equals + 1));
            }
        }
        final var state = stateBuilder.build();
        final var flags = TraceFlags.fromByte((byte) span.getTraceFlags());
        final var context = SpanContext.create(span.getTraceId(), span.getSpanId(), flags, state);
        final var parent = SpanContext.createFromRemoteParent(span.getTraceId(), span.getParentSpanId(), flags, state);
        final var attributes = Attributes.builder();
        span.getAttributes().forEach((key, value) -> {
            if (value instanceof Long number) {
                attributes.put(key, number);
            } else {
                attributes.put(key, (String) value);
            }
        });
        final var resource = Attributes.builder();
        span.getResource().forEach(resource::put);
        return new ImportedSpan(span, context, parent, attributes.build(), Resource.create(resource.build()));
    }

    /** Reconstructed data bypasses SDK sampling and ID/time generation entirely. */
    private record ImportedSpan(TransferSpan span, SpanContext context, SpanContext parent, Attributes attributes,
            Resource resource) implements SpanData {
        @Override
        public String getName() {
            return span.getName();
        }

        @Override
        public SpanKind getKind() {
            return "SERVER".equals(span.getKind()) ? SpanKind.SERVER : SpanKind.CONSUMER;
        }

        @Override
        public SpanContext getSpanContext() {
            return context;
        }

        @Override
        public SpanContext getParentSpanContext() {
            return parent;
        }

        @Override
        public StatusData getStatus() {
            return span.isFailed() ? StatusData.error() : StatusData.unset();
        }

        @Override
        public long getStartEpochNanos() {
            return span.getStartEpochNanos();
        }

        @Override
        public long getEndEpochNanos() {
            return span.getEndEpochNanos();
        }

        @Override
        public Attributes getAttributes() {
            return attributes;
        }

        @Override
        public Resource getResource() {
            return resource;
        }

        @Override
        public List<EventData> getEvents() {
            return List.of();
        }

        @Override
        public List<LinkData> getLinks() {
            return List.of();
        }

        @Override
        public boolean hasEnded() {
            return true;
        }

        @Override
        public int getTotalRecordedEvents() {
            return 0;
        }

        @Override
        public int getTotalRecordedLinks() {
            return 0;
        }

        @Override
        public int getTotalAttributeCount() {
            return attributes.size();
        }

        @SuppressWarnings("deprecation")
        @Override
        public InstrumentationLibraryInfo getInstrumentationLibraryInfo() {
            return InstrumentationLibraryInfo.create("ecmwf.ecpds.transfers", null);
        }

        @Override
        public InstrumentationScopeInfo getInstrumentationScopeInfo() {
            return InstrumentationScopeInfo.create("ecmwf.ecpds.transfers");
        }
    }

    public static final class Attempt implements AutoCloseable {
        private final Span span;
        private final Context context;
        private final AtomicBoolean ended = new AtomicBoolean();

        private Attempt(final Span span, final Context context) {
            this.span = span;
            this.context = context;
        }

        public Scope makeCurrent() {
            return context.makeCurrent();
        }

        public void failed() {
            // Do not export exception messages: transfer modules may include credentials or URLs.
            span.setStatus(StatusCode.ERROR);
        }

        public void finish(final DataTransfer transfer, final boolean successful) {
            if (!ended.compareAndSet(false, true)) {
                return;
            }
            span.setAttribute("ecpds.transfer.bytes_sent", transfer.getSent());
            if (!successful) {
                failed();
            }
            span.end();
        }

        @Override
        public void close() {
            if (ended.compareAndSet(false, true)) {
                span.end();
            }
        }
    }
}
