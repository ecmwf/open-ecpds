package ecmwf.ecpds.master;

import static ecmwf.ecpds.master.OperationalEvents.field;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.sun.net.httpserver.HttpServer;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.layout.PatternLayout;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor;

/** Standalone regression checks, requiring no external Collector or test framework. */
public final class OperationalEventsTest {
    public static void main(final String[] args) throws Exception {
        final var mode = args.length == 0 ? "delivery" : args[0];
        final var config = Files.createTempFile("ecpds-event-test-", ".properties");
        Files.writeString(config, """
                [Login]
                root=event-test-master
                [OperationalEvents]
                splunkEnabled=no
                openTelemetryEnabled=%s
                """.formatted("disabled".equals(mode) ? "no" : "yes"));
        System.setProperty("ecmwf.properties", config.toString());
        try {
            System.setProperty("otel.service.name", "event-test-service");
            if ("disabled".equals(mode) || "invalid".equals(mode)) {
                System.setProperty("otel.exporter.otlp.protocol", "invalid-test-protocol");
                if ("disabled".equals(mode)) {
                    OperationalEvents.start();
                    require(!OperationalEvents.getInstance().isEnabled(),
                            "Both outputs disabled without initializing SDK");
                } else {
                    try {
                        OperationalEvents.start();
                        throw new AssertionError("Invalid enabled exporter must fail startup");
                    } catch (final io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException expected) {
                        // Configuration errors must propagate, not silently disable event export.
                    }
                }
                System.out.println("PASS: " + mode + " startup configuration");
                return;
            }
            checkSinkModes();
            checkOtlpDelivery("reject".equals(mode));
            System.out.println("PASS: all sink combinations, typed fields, legacy format and OTLP " + mode);
        } finally {
            OperationalEvents.shutdown();
            System.clearProperty("otel.exporter.otlp.protocol");
            Files.delete(config);
        }
    }

    private static void checkSinkModes() {
        final var timestamp = Timestamp.from(Instant.parse("2026-10-23T14:00:00.123456789Z"));
        for (final var legacy : List.of(false, true)) {
            for (final var telemetry : List.of(false, true)) {
                final var lines = new ArrayList<String>();
                final var records = new ArrayList<LogRecordData>();
                try (final var provider = SdkLoggerProvider.builder()
                        .addLogRecordProcessor(SimpleLogRecordProcessor.create(new CapturingExporter(records)))
                        .build()) {
                    final var events = new OperationalEvents(() -> legacy, lines::add,
                            telemetry ? provider.get("test") : null);
                    require(events.isEnabled() == (legacy || telemetry), "Independent sink enablement");
                    for (final var category : List.of("PRS", "RET", "UPH", "INH", "ERR", "CPY", "DEA")) {
                        events.emit(category, field("TimeStamp", timestamp), field("Monitored", true),
                                field("FileSize", 133662002L), field("FileName", "wave;a=b.grib"),
                                field("Message", "value={}"), field("Optional", null));
                    }
                    require(lines.size() == (legacy ? 7 : 0), "Only enabled legacy sink writes");
                    require(records.size() == (telemetry ? 7 : 0), "Only enabled telemetry sink writes");
                    if (legacy) {
                        require(lines.getFirst().equals("PRS;TimeStamp=" + timestamp
                                + ";Monitored=true;FileSize=133662002;FileName=wave;a=b.grib;Message=value={};Optional=null"),
                                "Legacy field order, format and literal values unchanged");
                    }
                    if (telemetry) {
                        final var record = records.getFirst();
                        require("ecpds.PRS".equals(record.getEventName()), "Native event name");
                        require("PRS"
                                .equals(record.getAttributes().get(AttributeKey.stringKey("ecpds.event.category"))),
                                "Event category");
                        require(Boolean.TRUE.equals(record.getAttributes().get(AttributeKey.booleanKey("Monitored"))),
                                "Native boolean");
                        require(Long.valueOf(133662002).equals(
                                record.getAttributes().get(AttributeKey.longKey("FileSize"))), "Native integer");
                        require("wave;a=b.grib".equals(record.getAttributes().get(AttributeKey.stringKey("FileName"))),
                                "Attributes do not parse or split legacy delimiters");
                        require(record.getTimestampEpochNanos() == TimeUnit.SECONDS
                                .toNanos(timestamp.toInstant().getEpochSecond()) + timestamp.toInstant().getNano(),
                                "Original event timestamp retained");
                    }
                }
            }
        }
    }

    private static void checkOtlpDelivery(final boolean reject) throws Exception {
        final var requests = new CopyOnWriteArrayList<byte[]>();
        final var contentType = new AtomicReference<String>();
        final var warnings = new CopyOnWriteArrayList<String>();
        final var logger = LoggerContext.getContext(false).getLogger(OperationalEvents.class.getName());
        final var previousLevel = logger.getLevel();
        final var appender = new AbstractAppender("event-export-test", null, PatternLayout.createDefaultLayout(), false,
                Property.EMPTY_ARRAY) {
            @Override
            public void append(final LogEvent event) {
                warnings.add(event.getMessage().getFormattedMessage());
            }
        };
        appender.start();
        logger.addAppender(appender);
        logger.setLevel(Level.WARN);
        final var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/logs", exchange -> {
            contentType.set(exchange.getRequestHeaders().getFirst("Content-Type"));
            requests.add(exchange.getRequestBody().readAllBytes());
            exchange.sendResponseHeaders(reject ? 400 : 200, -1);
            exchange.close();
        });
        server.start();
        try {
            System.setProperty("otel.exporter.otlp.logs.endpoint",
                    "http://127.0.0.1:" + server.getAddress().getPort() + "/v1/logs");
            System.setProperty("otel.exporter.otlp.logs.protocol", "http/protobuf");
            System.setProperty("otel.exporter.otlp.logs.timeout", "2000");
            OperationalEvents.start();
            final var events = OperationalEvents.getInstance();
            require(events.isEnabled(), "Telemetry works even with Splunk disabled");
            events.emit("DEA", field("Message", "test;native=value"), field("UserId", "synthetic-user"));
            OperationalEvents.shutdown();
            require(!events.isEnabled(), "Disabled Splunk remains off after telemetry shutdown");
            require(!requests.isEmpty(), "Shutdown flush sends pending OTLP records");
            require("application/x-protobuf".equals(contentType.get()), "OTLP protobuf content type");
            final var payload = new String(requests.getFirst(), StandardCharsets.ISO_8859_1);
            require(payload.contains("ecpds.DEA") && payload.contains("test;native=value")
                    && payload.contains("event-test-master") && payload.contains("event-test-service"),
                    "Native attributes and configured/default resources reach receiver");
            require(!payload.contains("DEA;Message="), "OTLP is not a forwarded legacy log line");
            require(warnings.stream().anyMatch(message -> message.contains("OpenTelemetry export failed")) == reject,
                    "Receiver rejection must be explicitly reported");
        } finally {
            server.stop(0);
            logger.removeAppender(appender);
            logger.setLevel(previousLevel);
            appender.stop();
            System.clearProperty("otel.exporter.otlp.logs.endpoint");
            System.clearProperty("otel.exporter.otlp.logs.protocol");
            System.clearProperty("otel.exporter.otlp.logs.timeout");
            System.clearProperty("otel.service.name");
        }
    }

    private static void require(final boolean condition, final String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private record CapturingExporter(List<LogRecordData> records) implements LogRecordExporter {
        @Override
        public CompletableResultCode export(final Collection<LogRecordData> logs) {
            records.addAll(logs);
            return CompletableResultCode.ofSuccess();
        }

        @Override
        public CompletableResultCode flush() {
            return CompletableResultCode.ofSuccess();
        }

        @Override
        public CompletableResultCode shutdown() {
            return CompletableResultCode.ofSuccess();
        }
    }
}
