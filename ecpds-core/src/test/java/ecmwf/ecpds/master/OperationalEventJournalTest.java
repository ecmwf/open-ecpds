package ecmwf.ecpds.master;

import static ecmwf.ecpds.master.OperationalEvents.field;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.sun.net.httpserver.HttpServer;

import ecmwf.common.database.ECpdsBase;
import ecmwf.common.database.HibernateSessionFactory;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor;
import io.opentelemetry.sdk.resources.Resource;

/**
 * Standalone regression checks. An optional JDBC URL enables isolated MariaDB/OTLP integration checks. Run only against
 * an empty test database; the integration check creates its journal table.
 */
public final class OperationalEventJournalTest {
    public static void main(final String[] args) throws Exception {
        checkRecovery();
        checkAdmissionFailure();
        checkAcknowledgementFailure();
        checkCorruptRecord();
        if (args.length > 0) {
            checkDatabaseAndOtlp(args[0]);
        }
        System.out.println("PASS: durable admission, outage/restart recovery, typed replay, acknowledgement "
                + "failure, corrupt-row retention and fail-open fallback");
    }

    private static void checkRecovery() throws Exception {
        final var store = new MemoryStore();
        final var failed = new TestExporter();
        failed.result = CompletableResultCode.ofFailure();
        final var first = journal(store, failed);
        final var timestamp = Timestamp.from(Instant.parse("2026-10-23T14:00:00.123456789Z"));
        try (final var provider = provider(first)) {
            final var events = new OperationalEvents(() -> false, _ -> {
            }, provider.get("journal-test"));
            events.emit("INH", field("TimeStamp", timestamp), field("FileSize", Long.MAX_VALUE),
                    field("Monitored", true), field("Rate", 12.5), field("NonFinite", Double.NaN),
                    field("FileName", "quoted;'\\\n\u00e9\uD83D\uDE80"));
            require(store.rows.size() == 1 && failed.records.isEmpty(), "Persist synchronously before export");
            first.deliver();
            first.deliver();
            require(store.rows.size() == 1, "Endpoint failure must retain event");
        }
        final var id = store.rows.keySet().iterator().next();
        final var recovered = new TestExporter();
        recovered.result = new CompletableResultCode();
        final var second = journal(store, recovered);
        second.deliver();
        second.deliver();
        require(store.rows.size() == 1 && recovered.records.size() == 1,
                "No deletion or duplicate export while acknowledgement is outstanding");
        final var replay = recovered.records.getFirst();
        require(replay.getTimestampEpochNanos() == TimeUnit.SECONDS.toNanos(timestamp.toInstant().getEpochSecond())
                + timestamp.toInstant().getNano(), "Original nanosecond timestamp survives restart");
        require(id.equals(replay.getAttributes().get(AttributeKey.stringKey("ecpds.event.id"))),
                "Stable event ID survives retry/restart");
        require("ecpds.INH".equals(replay.getEventName()), "Event name survives replay");
        require(replay.getAttributes().get(AttributeKey.longKey("FileSize")) == Long.MAX_VALUE, "Integer precision");
        require(Boolean.TRUE.equals(replay.getAttributes().get(AttributeKey.booleanKey("Monitored"))),
                "Boolean type survives replay");
        require(Double.valueOf(12.5).equals(replay.getAttributes().get(AttributeKey.doubleKey("Rate"))),
                "Double type survives replay");
        require(Double.isNaN(replay.getAttributes().get(AttributeKey.doubleKey("NonFinite"))),
                "Non-finite floating-point attributes survive JSON replay");
        require("quoted;'\\\n\u00e9\uD83D\uDE80".equals(replay.getAttributes().get(AttributeKey.stringKey("FileName"))),
                "No delimiter, escape or Unicode corruption");
        require("original-master".equals(replay.getResource().getAttribute(AttributeKey.stringKey("service.name"))),
                "Original resource survives replay on a different SDK instance");
        require(List.of("one", "two")
                .equals(replay.getResource().getAttribute(AttributeKey.stringArrayKey("test.array"))),
                "Array resource attributes survive replay");
        recovered.result.succeed();
        second.deliver();
        require(store.rows.isEmpty(), "Delete only after endpoint acknowledgement");
        second.shutdown();
    }

    private static void checkAdmissionFailure() {
        final var store = new MemoryStore();
        store.failAppend = true;
        final var fallback = new TestExporter();
        final var journal = journal(store, fallback);
        try (final var provider = provider(journal)) {
            new OperationalEvents(() -> false, _ -> {
            }, provider.get("test")).emit("ERR", field("Message", "database unavailable"));
            require(store.rows.isEmpty() && fallback.records.size() == 1,
                    "Database admission failure continues with best-effort delivery");
        }
    }

    private static void checkAcknowledgementFailure() throws Exception {
        final var store = new MemoryStore();
        final var exporter = new TestExporter();
        final var first = journal(store, exporter);
        try (final var provider = provider(first)) {
            new OperationalEvents(() -> false, _ -> {
            }, provider.get("test")).emit("UPH", field("FileSize", 1L));
            first.deliver();
            store.failAcknowledge = true;
            first.deliver();
            require(store.rows.size() == 1, "Database delete failure must not forget an event");
        }
        store.failAcknowledge = false;
        final var restarted = journal(store, exporter);
        restarted.deliver();
        restarted.deliver();
        require(store.rows.isEmpty() && exporter.records.size() == 2,
                "Crash after endpoint acceptance can duplicate, not lose, an event");
        final var key = AttributeKey.stringKey("ecpds.event.id");
        require(exporter.records.get(0).getAttributes().get(key)
                .equals(exporter.records.get(1).getAttributes().get(key)),
                "Duplicate deliveries retain deduplication identity");
        restarted.shutdown();
    }

    private static void checkCorruptRecord() {
        final var store = new MemoryStore();
        store.rows.put("corrupt-test", "{\"version\":99}");
        final var exporter = new TestExporter();
        final var journal = journal(store, exporter);
        journal.deliver();
        require(store.rows.size() == 1 && exporter.records.isEmpty(), "Undecodable rows are never silently deleted");
        journal.shutdown();
    }

    private static OperationalEventJournal journal(final MemoryStore store, final TestExporter exporter) {
        return new OperationalEventJournal(store, exporter, SimpleLogRecordProcessor.create(exporter), false);
    }

    private static SdkLoggerProvider provider(final OperationalEventJournal journal) {
        return SdkLoggerProvider.builder()
                .setResource(Resource.create(Attributes.builder().put("service.name", "original-master")
                        .put(AttributeKey.stringArrayKey("test.array"), List.of("one", "two")).build()))
                .addLogRecordProcessor(journal).build();
    }

    private static void checkDatabaseAndOtlp(final String url) throws Exception {
        final var config = Files.createTempFile("ecpds-journal-test-", ".properties");
        final var hibernate = Files.createTempFile("ecpds-journal-hibernate-", ".xml");
        final var owner = "journal-integration-" + java.util.UUID.randomUUID();
        final var otherOwner = owner + ";other'\\\u00e9";
        Files.writeString(config, """
                [Login]
                root=journal-integration
                [DataBase]
                enableStreamingFetch=no
                [OperationalEvents]
                splunkEnabled=no
                openTelemetryEnabled=yes
                journalEnabled=yes
                journalOwner=%s
                """.formatted(owner));
        System.setProperty("ecmwf.properties", config.toString());
        final var status = new AtomicInteger(400);
        final var requests = new CopyOnWriteArrayList<String>();
        var initialized = false;
        final var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/logs", exchange -> {
            requests.add(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.ISO_8859_1));
            exchange.sendResponseHeaders(status.get(), -1);
            exchange.close();
        });
        server.start();
        try (final var connection = DriverManager.getConnection(url, "root", "");
                final var database = new ECpdsBase()) {
            try (final var statement = connection.createStatement()) {
                statement.execute(Files.readString(Path.of("docker/ecpds/database/operational-event-journal.sql")));
            }
            Files.writeString(hibernate, """
                    <?xml version="1.0"?>
                    <!DOCTYPE hibernate-configuration PUBLIC
                      "-//Hibernate/Hibernate Configuration DTD 3.0//EN"
                      "http://www.hibernate.org/dtd/hibernate-configuration-3.0.dtd">
                    <hibernate-configuration><session-factory>
                      <property name="hibernate.dialect">org.hibernate.dialect.MariaDBDialect</property>
                      <property name="hibernate.cache.use_second_level_cache">false</property>
                      <property name="hibernate.hikari.minimumIdle">1</property>
                      <property name="hibernate.hikari.maximumPoolSize">3</property>
                    </session-factory></hibernate-configuration>
                    """);
            System.setProperty("hibernate.cfg.xml", hibernate.toString());
            System.setProperty("hibernate.hbm.xml",
                    Path.of("etc/master/conf/hibernate.hbm.xml").toAbsolutePath().toString());
            System.setProperty("otel.exporter.otlp.logs.endpoint",
                    "http://127.0.0.1:" + server.getAddress().getPort() + "/v1/logs");
            System.setProperty("otel.exporter.otlp.logs.timeout", "1000");
            database.initialize("ecmwf.common.database.BrokerFactoryHibernate", "org.mariadb.jdbc.Driver", "jdbc",
                    "mariadb", url.substring("jdbc:mariadb:".length()), "root", "", null,
                    Path.of("etc/master/conf/sql").toAbsolutePath().toString(), false, false);
            initialized = true;
            final var isolatedId = java.util.UUID.randomUUID().toString();
            database.insertOperationalEvent(otherOwner, isolatedId, "{}");
            OperationalEvents.start(database);
            final var timestamp = Timestamp.from(Instant.parse("2026-10-23T14:01:02.123456789Z"));
            OperationalEvents.getInstance().emit("INH", field("TimeStamp", timestamp),
                    field("FileName", "quote;'\\\n\u00e9\uD83D\uDE80"), field("FileSize", 123456789L));
            final var pending = database.getOperationalEvents(owner, 10);
            require(pending.size() == 1, "Named SQL durably inserts journal record");
            final var id = pending.keySet().iterator().next();
            require(OperationalEventJournal.decode(pending.get(id)).getAttributes()
                    .get(AttributeKey.stringKey("FileName")).equals("quote;'\\\n\u00e9\uD83D\uDE80"),
                    "Named SQL preserves quotes, backslashes, newline and Unicode");
            await(() -> !requests.isEmpty(), "Failed OTLP attempt");
            require(database.getOperationalEvents(owner, 10).size() == 1, "HTTP rejection retains database row");
            status.set(200);
            await(() -> database.getOperationalEvents(owner, 10).isEmpty(),
                    "Automatic retry when endpoint recovers without a Master restart");
            require(requests.stream().allMatch(payload -> payload.contains(id) && payload.contains("ecpds.INH")),
                    "Real protobuf retries retain event identity and category");
            status.set(400);
            OperationalEvents.getInstance().emit("INH", field("Message", "persist across restart"));
            final var restartId = database.getOperationalEvents(owner, 10).keySet().iterator().next();
            await(() -> requests.stream().anyMatch(payload -> payload.contains(restartId)),
                    "Second event exported before restart");
            require(database.getOperationalEvents(owner, 10).size() == 1, "Second rejected event remains durable");
            OperationalEvents.shutdown();
            status.set(200);
            OperationalEvents.start(database);
            await(() -> database.getOperationalEvents(owner, 10).isEmpty(), "Automatic replay after publisher restart");
            require(requests.stream().filter(payload -> payload.contains(restartId)).count() >= 2,
                    "Restart retries preserve event ID");
            require(database.getOperationalEvents(otherOwner, 10).size() == 1, "Other owners are not replayed");
            database.deleteOperationalEvent(owner, isolatedId);
            require(database.getOperationalEvents(otherOwner, 10).size() == 1, "Acknowledgement is owner-scoped");
            database.deleteOperationalEvent(otherOwner, isolatedId);
            OperationalEvents.shutdown();
            for (final var property : List.of("otel.logs.exporter", "otel.sdk.disabled")) {
                System.setProperty(property, "otel.logs.exporter".equals(property) ? "none" : "true");
                try {
                    OperationalEvents.start(database);
                    throw new AssertionError("Journal must not silently start without an active OTLP exporter");
                } catch (final IllegalArgumentException expected) {
                    require(expected.getMessage().contains("Database event journaling"),
                            "Explicit journal configuration error");
                } finally {
                    System.clearProperty(property);
                }
            }
            System.out.println("PASS: MariaDB named SQL, Unicode payloads, owner isolation, real HTTP/protobuf "
                    + "rejection, restart and automatic acknowledgement-based deletion");
        } finally {
            OperationalEvents.shutdown();
            if (initialized) {
                HibernateSessionFactory.shutdown();
            }
            server.stop(0);
            Files.deleteIfExists(config);
            Files.deleteIfExists(hibernate);
        }
    }

    @FunctionalInterface
    private interface Check {
        boolean done() throws Exception;
    }

    private static void await(final Check check, final String message) throws Exception {
        final var deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(20);
        while (!check.done() && System.nanoTime() < deadline) {
            Thread.sleep(100);
        }
        require(check.done(), message);
    }

    private static void require(final boolean condition, final String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static final class MemoryStore implements OperationalEventJournal.Store {
        private final Map<String, String> rows = new LinkedHashMap<>();
        private boolean failAppend;
        private boolean failAcknowledge;

        @Override
        public void append(final String id, final String payload) throws SQLException {
            if (failAppend) {
                throw new SQLException("Synthetic database admission failure");
            }
            rows.put(id, payload);
        }

        @Override
        public Map<String, String> pending(final int limit) {
            final Map<String, String> result = new LinkedHashMap<>();
            rows.entrySet().stream().limit(limit).forEach(entry -> result.put(entry.getKey(), entry.getValue()));
            return result;
        }

        @Override
        public void acknowledge(final String id) throws SQLException {
            if (failAcknowledge) {
                throw new SQLException("Synthetic acknowledgement failure");
            }
            rows.remove(id);
        }
    }

    private static final class TestExporter implements LogRecordExporter {
        private final List<LogRecordData> records = new ArrayList<>();
        private CompletableResultCode result = CompletableResultCode.ofSuccess();

        @Override
        public CompletableResultCode export(final Collection<LogRecordData> logs) {
            records.addAll(logs);
            return result;
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
