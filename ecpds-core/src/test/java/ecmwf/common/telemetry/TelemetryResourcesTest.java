package ecmwf.common.telemetry;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.data.SpanData;

public final class TelemetryResourcesTest {
    private static final List<String> SIGNALS = List.of("logs", "metrics", "traces");

    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            environment("jvm".equals(args[0]));
            return;
        }
        final Map<String, String> settings = new HashMap<>();
        SIGNALS.forEach(signal -> settings.put(key(signal), ""));
        final var baseline = resource("logs", settings);
        check("shared".equals(value(baseline, "routing.dataset")), "Shared baseline without overrides");
        check("default-instance".equals(value(baseline, "service.instance.id")), "Service default retained");
        check("shared-version".equals(value(baseline, "service.version")), "Shared setting overrides default");
        for (final var signal : SIGNALS) {
            settings.put(key(signal), "routing.dataset=" + signal + ",signal.only=" + signal + ",service.name=custom-"
                    + signal + ",service.version=signal-version");
        }
        for (final var signal : SIGNALS) {
            final var resource = resource(signal, settings);
            check(signal.equals(value(resource, "routing.dataset")), "Signal overrides shared key");
            check(signal.equals(value(resource, "signal.only")), "Signals remain isolated");
            check(("custom-" + signal).equals(value(resource, "service.name")), "Signal overrides service name");
            check("signal-version".equals(value(resource, "service.version")), "Signal overrides shared version");
            check("retained".equals(value(resource, "shared.only")), "Non-overridden shared attribute retained");
            check("default-instance".equals(value(resource, "service.instance.id")), "Default retained");
        }
        forwardedSpan();
        for (final var mode : List.of("environment", "jvm")) {
            final var process = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                    "-cp", System.getProperty("java.class.path"), TelemetryResourcesTest.class.getName(), mode);
            for (final var signal : SIGNALS) {
                process.environment().put(
                        "ECPDS_OTEL_" + signal.toUpperCase(java.util.Locale.ROOT) + "_RESOURCE_ATTRIBUTES",
                        "routing.dataset=env-" + signal + ",environment.only=kept");
            }
            check(process.inheritIO().start().waitFor() == 0, "Environment/JVM precedence: " + mode);
        }
        System.out.println(
                "PASS: per-signal isolation, shared/default merging, environment/JVM precedence and forwarded Mover resources");
    }

    private static void environment(boolean jvm) {
        for (final var signal : SIGNALS) {
            if (jvm) {
                System.setProperty(key(signal), "routing.dataset=jvm-" + signal);
            }
            final var resource = resource(signal, Map.of());
            check(((jvm ? "jvm-" : "env-") + signal).equals(value(resource, "routing.dataset")),
                    "Environment or JVM setting used");
            check(jvm ? value(resource, "environment.only") == null
                    : "kept".equals(value(resource, "environment.only")), "JVM replaces whole environment setting");
            check("retained".equals(value(resource, "shared.only")), "Shared baseline still retained");
        }
    }

    private static Resource resource(String signal, Map<String, String> settings) {
        final var captured = new AtomicReference<Resource>();
        final var configured = AutoConfiguredOpenTelemetrySdk.builder().addPropertiesCustomizer(_ -> {
            final Map<String, String> properties = new HashMap<>(settings);
            properties.put("otel.logs.exporter", "none");
            properties.put("otel.metrics.exporter", "none");
            properties.put("otel.traces.exporter", "none");
            properties.put("otel.resource.attributes",
                    "routing.dataset=shared,shared.only=retained,service.version=shared-version");
            return properties;
        }).addResourceCustomizer((resource,
                _) -> Resource.create(Attributes.builder().put("service.instance.id", "default-instance")
                        .put("service.version", "default-version").build()).merge(resource))
                .addResourceCustomizer(
                        (resource, properties) -> resource.merge(TelemetryResources.overrides(properties, signal)))
                .addResourceCustomizer((resource, _) -> {
                    captured.set(resource);
                    return resource;
                }).disableShutdownHook().build();
        configured.getOpenTelemetrySdk().close();
        return captured.get();
    }

    private static void forwardedSpan() throws Exception {
        final var span = new TransferSpan();
        span.setTraceId("11111111111111111111111111111111");
        span.setSpanId("2222222222222222");
        span.setParentSpanId("3333333333333333");
        span.setStartEpochNanos(1);
        span.setEndEpochNanos(2);
        span.setAttributes(Map.of("ecpds.transfer.id", 1L, "ecpds.transfer.attempt.id", "test"));
        span.setResource(Map.of("service.name", "open-ecpds-mover", "service.instance.id", "mover-a"));
        final var convert = TransferTracing.class.getDeclaredMethod("spanData", TransferSpan.class, Resource.class);
        convert.setAccessible(true);
        final var original = ((SpanData) convert.invoke(null, span.snapshot(), Resource.empty())).getResource();
        check("open-ecpds-mover".equals(value(original, "service.name")), "Mover identity retained by default");
        final var overrides = Resource.create(
                Attributes.builder().put("routing.dataset", "traces").put("service.name", "custom-mover").build());
        final var resource = ((SpanData) convert.invoke(null, span.snapshot(), overrides)).getResource();
        check("traces".equals(value(resource, "routing.dataset")), "Forwarded spans get trace routing");
        check("custom-mover".equals(value(resource, "service.name")), "Explicit trace override wins");
        check("mover-a".equals(value(resource, "service.instance.id")), "Mover instance retained");
    }

    private static String key(String signal) {
        return "ecpds.otel." + signal + ".resource.attributes";
    }

    private static String value(Resource resource, String key) {
        return resource.getAttribute(AttributeKey.stringKey(key));
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
