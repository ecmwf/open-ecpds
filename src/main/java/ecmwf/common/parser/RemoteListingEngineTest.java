package ecmwf.common.parser;

import java.util.Map;

import ecmwf.common.ftp.FtpParser;
import ecmwf.common.parser.core.ParserConfig;
import ecmwf.common.parser.core.RemoteListingEngine;

/**
 * Simple manual test harness for RemoteListingEngine.
 *
 * <p>
 * This class allows quick validation of all parsers without JUnit. Useful during development and refactoring phases.
 * </p>
 */
public class RemoteListingEngineTest {

    public static void main(String[] args) throws Exception {

        // ------------------------------------------------------------
        // 1. Build registry + engine
        // ------------------------------------------------------------
        RemoteListingEngine engine = new RemoteListingEngine();

        System.out.println("====================================");
        System.out.println("REMOTE LISTING ENGINE MANUAL TEST");
        System.out.println("====================================");

        testFtp(engine);
        testCsv(engine);
        testJson(engine);
        testXml(engine);
        testStac(engine);
        testS3(engine);
        testMetadata(engine);
        testPreviewConfig(engine);
        testHttpMetadata();
        testNotificationProfiles();
        testMqttNotificationListing("script");
        testMqttNotificationListing("wis2");
        testMqttNotificationListing("wis2", true);

        System.out.println("====================================");
        System.out.println("ALL TESTS COMPLETED");
        System.out.println("====================================");
    }

    // ------------------------------------------------------------
    // FTP
    // ------------------------------------------------------------
    private static void testFtp(RemoteListingEngine engine) throws Exception {

        System.out.println("\n[FTP TEST]");

        String input = "-rw-r--r-- 1 root root 1234 Sep 01 10:00 test.nc";

        ParserConfig config = new ParserConfig("ftp", Map.of("system", "UNIX"));

        FtpParser.FileEntry[] result = engine.process(config, input);

        require(result.length == 1 && result[0].size == 1234, "FTP byte size must remain unchanged");
        print(result);
    }

    // ------------------------------------------------------------
    // CSV
    // ------------------------------------------------------------
    private static void testCsv(RemoteListingEngine engine) throws Exception {

        System.out.println("\n[CSV TEST]");

        String input = "name,date,url\n" + "file1.nc,2024-01-01,https://host/file1.nc";

        ParserConfig config = new ParserConfig("csv",
                Map.of("delimiter", ",", "nameCol", "0", "timeCol", "1", "urlCol", "2"));

        FtpParser.FileEntry[] result = engine.process(config, input);

        print(result);
    }

    // ------------------------------------------------------------
    // JSON
    // ------------------------------------------------------------
    private static void testJson(RemoteListingEngine engine) throws Exception {

        System.out.println("\n[JSON TEST]");

        String input = """
                {
                  "items": [
                    {
                      "name": "file1.nc",
                      "url": "https://host/file1.nc",
                      "time": "2024-01-01T10:00:00Z"
                    }
                  ]
                }
                """;

        ParserConfig config = new ParserConfig("json",
                Map.of("arrayPath", "items", "nameField", "name", "urlField", "url", "timeField", "time"));

        FtpParser.FileEntry[] result = engine.process(config, input);

        print(result);
    }

    // ------------------------------------------------------------
    // XML
    // ------------------------------------------------------------
    private static void testXml(RemoteListingEngine engine) throws Exception {

        System.out.println("\n[XML TEST]");

        String input = """
                <items>
                    <item>
                        <name>file1.nc</name>
                        <url>https://host/file1.nc</url>
                        <time>2024-01-01T10:00:00Z</time>
                    </item>
                </items>
                """;

        ParserConfig config = new ParserConfig("xml",
                Map.of("arrayPath", "items.item", "nameField", "name", "urlField", "url", "timeField", "time"));

        FtpParser.FileEntry[] result = engine.process(config, input);

        print(result);
    }

    // ------------------------------------------------------------
    // STAC
    // ------------------------------------------------------------
    private static void testStac(RemoteListingEngine engine) throws Exception {

        System.out.println("\n[STAC TEST]");

        String input = """
                {
                  "features": [
                    {
                      "id": "S1",
                      "properties": {
                        "start_datetime": "2024-01-01T10:00:00Z"
                      },
                      "assets": {
                        "data": {
                          "href": "https://host/file1.nc"
                        }
                      }
                    }
                  ]
                }
                """;

        ParserConfig config = new ParserConfig("stac", Map.of("arrayPath", "features", "nameField", "id", "urlField",
                "assets.data.href", "timeField", "properties.start_datetime"));

        FtpParser.FileEntry[] result = engine.process(config, input);

        print(result);
    }

    // ------------------------------------------------------------
    // S3
    // ------------------------------------------------------------
    private static void testS3(RemoteListingEngine engine) throws Exception {

        System.out.println("\n[S3 TEST]");

        String input = "key,size,last_modified\n" + "file1.nc,1234,2024-01-01T10:00:00Z";

        ParserConfig config = new ParserConfig("s3",
                Map.of("delimiter", ",", "keyCol", "key", "sizeCol", "size", "timeCol", "last_modified"));

        FtpParser.FileEntry[] result = engine.process(config, input);

        print(result);
    }

    // ------------------------------------------------------------
    // Helper
    // ------------------------------------------------------------
    private static void testMetadata(RemoteListingEngine engine) throws Exception {
        long date = java.time.Instant.parse("2024-01-01T00:00:00Z").toEpochMilli();
        var csv = engine.process(
                new ParserConfig("csv",
                        Map.of("nameCol", "name", "urlCol", "url", "timeCol", "date", "sizeCol", "size", "sizeUnit",
                                "MiB")),
                "name,date,url,size\nfile.nc,2024-01-01,https://host/file.nc,6.899173736572266");
        require(csv.length == 1 && csv[0].size == 7234308 && csv[0].time == date,
                "CSV named columns, fractional binary size and UTC date");
        var indexed = engine.process(
                new ParserConfig("csv",
                        Map.of("header", "false", "nameCol", "0", "timeCol", "1", "urlCol", "2", "sizeCol", "3")),
                "empty.nc,1704067200000,https://host/empty.nc,0");
        require(indexed[0].size == 0 && indexed[0].time == date, "Headerless CSV, empty file and epoch milliseconds");

        var json = engine.process(new ParserConfig("json", Map.of("sizeField", "metadata.bytes", "sizeUnit", "KB")),
                "{\"items\":[{\"name\":\"a\",\"url\":\"https://host/a\",\"time\":\"2024-01-01\","
                        + "\"metadata\":{\"bytes\":1.5}}]}");
        require(json[0].size == 1536 && json[0].time == date, "Nested JSON field and ECtrans binary units");
        var xml = engine.process(new ParserConfig("xml", Map.of("sizeField", "length", "sizeUnit", "KiB")),
                "<items><item><name>a</name><url>https://host/a</url><time>1704067200000</time>"
                        + "<length>1.5</length></item></items>");
        require(xml.length == 1 && xml[0].size == 1536 && xml[0].time == date, "XML field and epoch milliseconds");
        var stac = engine.process(new ParserConfig("stac", Map.of("urlField", "assets.image.href")),
                "{\"features\":[{\"id\":\"a\",\"properties\":{\"start_datetime\":\"2024-01-01T00:00:00Z\"},"
                        + "\"assets\":{\"image\":{\"href\":\"https://host/a\",\"file:size\":42}}}]}");
        require(stac[0].size == 42 && stac[0].time == date && "https://host/a".equals(stac[0].link),
                "STAC size must follow the selected asset");
        var s3 = engine
                .process(
                        new ParserConfig("s3", Map.of("header", "false", "keyCol", "1", "sizeCol", "2", "timeCol", "3",
                                "bucketCol", "0", "sizeUnit", "KB", "baseUrl", "https://host")),
                        "bucket,a,1.5,2024-01-01");
        require(s3[0].size == 1536 && s3[0].time == date && "https://host/a".equals(s3[0].link),
                "S3 inventory numeric columns, headerless input and units");

        for (String type : new String[] { "json", "xml", "csv", "s3", "stac" }) {
            String input = switch (type) {
            case "csv" -> "name,url,time,size\na,https://host/a,bad,-1\nb,https://host/b,,\n";
            case "s3" -> "key,bucket,last_modified,size\na,bucket,bad,-1\nb,bucket,,\n";
            case "xml" -> "<items><item><name>a</name><time>bad</time><size>-1</size></item>"
                    + "<item><name>b</name></item></items>";
            default -> "{\"items\":[{\"name\":\"a\",\"time\":\"bad\",\"size\":-1},{\"name\":\"b\"}]}";
            };
            var options = type.equals("csv") ? Map.of("nameCol", "0", "urlCol", "1", "timeCol", "2", "sizeCol", "3")
                    : Map.of("arrayPath", "items", "timeField", "time", "sizeField", "size");
            var entries = engine.process(new ParserConfig(type, options), input);
            require(entries.length == 2 && entries[0].size == -1 && entries[0].time == -1 && entries[1].size == -1
                    && entries[1].time == -1, type + " invalid/missing metadata remains unknown");
        }

        var metadata = new ecmwf.common.parser.format.ListingMetadata(new ParserConfig("csv", Map.of()));
        require(metadata.size("9223372036854775807") == Long.MAX_VALUE, "Exact long size");
        for (String invalid : new String[] { "-1", "NaN", "Infinity", "9223372036854775808", "1e100" }) {
            require(metadata.size(invalid) == -1, "Reject invalid or overflowing size: " + invalid);
        }
        require(metadata.size("1.5") == 2, "Fractional bytes round to nearest byte");
        String[] legacyUnits = { "B", "KB", "MB", "GB", "TB", "PB", "EB" };
        String[] binaryUnits = { "bytes", "KiB", "MiB", "GiB", "TiB", "PiB", "EiB" };
        for (int i = 0; i < legacyUnits.length; i++) {
            long expected = 1L << (10 * i);
            require(ecmwf.common.technical.ByteSize.parse("1" + legacyUnits[i]).size() == expected,
                    "Preserve legacy ECtrans units");
            require(ecmwf.common.technical.ByteSize.parse(" 1 " + binaryUnits[i].toLowerCase(java.util.Locale.ROOT))
                    .size() == expected, "ECtrans accepts binary aliases, spaces and case variations");
            for (String unit : new String[] { legacyUnits[i], binaryUnits[i] }) {
                var conversion = new ecmwf.common.parser.format.ListingMetadata(
                        new ParserConfig("csv", Map.of("sizeUnit", unit)));
                require(conversion.size("1") == expected, "Parser and ECtrans must agree for " + unit);
            }
        }
        require(ecmwf.common.technical.ByteSize.parse("9007199254740993B").size() == 9007199254740993L,
                "Large byte sizes retain integer precision");
        try {
            ecmwf.common.technical.ByteSize.parse("8EiB");
            throw new AssertionError("Overflowing ECtrans byte size must fail");
        } catch (IllegalArgumentException expected) {
            // Overflow must not silently saturate the byte count.
        }
        var scaled = new ecmwf.common.parser.format.ListingMetadata(new ParserConfig("csv", Map.of("sizeUnit", "TiB")));
        require(scaled.size("9223372036854775807") == -1, "Reject size overflow after unit conversion");
        try {
            new ecmwf.common.parser.format.ListingMetadata(new ParserConfig("csv", Map.of("sizeUnit", "bogus")));
            throw new AssertionError("Invalid sizeUnit must fail explicitly");
        } catch (IllegalArgumentException expected) {
            // Invalid configuration must not produce a successful empty listing.
        }
        var mapping = new ecmwf.common.parser.mapping.CsvMappingInferer().infer("\"File,name\",url,time,size\n");
        require("3".equals(mapping.get("sizeCol")) && "0".equals(mapping.get("nameCol")),
                "Discovery must infer size and respect quoted headers");
        var preview = new ecmwf.common.parser.discovery.RemoteListingDiscoveryService(engine)
                .discover("name,url,time,size\na,https://host/a,2024-01-01,42");
        require(preview.getPreviewEntries()[0].size == 42
                && preview.getWarnings().stream().anyMatch(warning -> warning.contains("size unit")),
                "Discovery must preview sizes and warn about assumed units");
        System.out.println("Metadata regression checks passed");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void testPreviewConfig(RemoteListingEngine engine) throws Exception {
        var setup = new ecmwf.common.ectrans.ECtransSetup("http",
                "http.parser = \"csv\"\nhttp.parserOptions = \"nameCol=0,urlCol=1,timeCol=2,sizeCol=3,sizeUnit=KiB\"");
        var config = ParserConfig.forHttpListing(setup.getString(ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_PARSER),
                setup.getString(ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_PARSER_OPTIONS));
        var entries = engine.process(config, "name,url,time,size\n\"a\nb\",https://host/a,2024-01-01,2");
        require(entries.length == 1 && entries[0].size == 2048 && "a\nb".equals(entries[0].name),
                "Preview uses Properties and parses multiline CSV records as a document");
        var defaults = engine.process(ParserConfig.forHttpListing("csv", ""),
                "name,time,url\na,2024-01-01,https://host/a");
        require(defaults[0].time > 0 && "https://host/a".equals(defaults[0].link),
                "Preview and acquisition share HTTP CSV defaults");
        try {
            ParserConfig.forHttpListing("html", "");
            throw new AssertionError("Unsupported preview parser must fail explicitly");
        } catch (IllegalArgumentException expected) {
            require(expected.getMessage().contains("configured parser: html"), "Explain unsupported parser");
        }
    }

    private static void testHttpMetadata() throws Exception {
        var requests = new java.util.concurrent.atomic.AtomicInteger();
        var server = com.sun.net.httpserver.HttpServer.create(new java.net.InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/file.nc")) {
                requests.incrementAndGet();
                exchange.getResponseHeaders().set("Content-Length", "42");
                exchange.getResponseHeaders().set("Last-Modified", "Mon, 01 Jan 2024 00:00:00 GMT");
                exchange.sendResponseHeaders(200, -1);
            } else {
                String input = switch (path) {
                case "/csv" -> "name,time,url,size\nfile.nc,2024-01-01,/file.nc,42";
                case "/missing" -> "name,time,url,size\nfile.nc,2024-01-01,/file.nc,";
                case "/xml" -> "<items><item><name>file.nc</name><time>2024-01-01</time>"
                        + "<url>/file.nc</url><size>42</size></item></items>";
                case "/stac" -> "{\"features\":[{\"id\":\"file.nc\","
                        + "\"properties\":{\"start_datetime\":\"2024-01-01\"},"
                        + "\"assets\":{\"data\":{\"href\":\"/file.nc\",\"file:size\":42}}}]}";
                case "/s3" -> "key,size,last_modified\nfile.nc,42,2024-01-01";
                default -> "{\"items\":[{\"name\":\"file.nc\",\"time\":\"2024-01-01\","
                        + "\"url\":\"/file.nc\",\"size\":42}]}";
                };
                byte[] content = input.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, content.length);
                exchange.getResponseBody().write(content);
            }
            exchange.close();
        });
        server.start();
        try {
            int port = server.getAddress().getPort();
            String baseUrl = "http://127.0.0.1:" + port;
            for (String type : new String[] { "csv", "json", "xml", "stac", "s3", "missing" }) {
                var setup = new ecmwf.common.ectrans.ECtransSetup("http");
                setup.set("port", port);
                setup.set("urldir", "no");
                setup.set("useHead", "yes");
                setup.set("ftpLike", "yes");
                setup.set("parser", type.equals("missing") ? "csv" : type);
                setup.set("parserOptions",
                        type.equals("s3") ? "baseUrl=" + baseUrl : "nameCol=0,timeCol=1,urlCol=2,sizeCol=3");
                var module = new ecmwf.common.ectrans.module.HttpModule();
                try {
                    module.connect(":@127.0.0.1", setup);
                    var listing = module.listAsStringArray(baseUrl + "/" + type, null);
                    require(listing.length == 1 && listing[0].contains("42") && listing[0].contains("file.nc"),
                            type + " HTTP listing must retain metadata");
                    require(requests.get() == (type.equals("missing") ? 1 : 0),
                            type + " HTTP listing must only request missing metadata");
                } finally {
                    module.close();
                }
            }
            var setup = new ecmwf.common.ectrans.ECtransSetup("http");
            setup.set("port", port);
            setup.set("urldir", "no");
            setup.set("parser", "csv");
            setup.set("parserOptions", "sizeCol=3,sizeUnit=bogus");
            var module = new ecmwf.common.ectrans.module.HttpModule();
            try {
                module.connect(":@127.0.0.1", setup);
                try {
                    module.listAsStringArray(baseUrl + "/csv", null);
                    throw new AssertionError("Invalid units must fail HTTP acquisition");
                } catch (java.io.IOException expected) {
                    require(expected.getMessage().contains("sizeUnit"), "HTTP error must explain invalid units");
                }
            } finally {
                module.close();
            }
        } finally {
            server.stop(0);
        }
        System.out.println("HTTP metadata request regression checks passed");
    }

    private static void print(FtpParser.FileEntry[] entries) {

        if (entries == null || entries.length == 0) {
            System.out.println("No entries");
            return;
        }

        for (FtpParser.FileEntry e : entries) {
            System.out.println("name=" + e.name + ", size=" + e.size + ", time=" + e.time + ", link=" + e.link);
        }
    }

    private static void testNotificationProfiles() throws Exception {
        var profile = ecmwf.common.ectrans.notification.NotificationProfiles.resolve("wis2");
        require(ecmwf.common.ectrans.notification.NotificationProfiles.resolve("script") == null,
                "Default script interpretation must remain independent of profile discovery");
        expectIOException(() -> ecmwf.common.ectrans.notification.NotificationProfiles.resolve("missing"));
        var json = new com.fasterxml.jackson.databind.ObjectMapper();
        var wnm = json.readTree(wis2Message());
        var topic = "origin/a/wis2/fr/example/data/core/weather";
        var first = profile.decode(topic, json.writeValueAsBytes(wnm));
        require(first.size() == 5 && first.filename().equals("file.txt")
                && first.notification().integrity().method().equals("SHA-512"),
                "WIS2 fields must become retrieval metadata without changing the digest algorithm");
        var relay = wnm.deepCopy();
        ((com.fasterxml.jackson.databind.node.ObjectNode) relay).put("id", "another-broker-message");
        ((com.fasterxml.jackson.databind.node.ObjectNode) relay.path("properties")).put("global-cache", "cache-one");
        ((com.fasterxml.jackson.databind.node.ObjectNode) relay.path("links").get(0)).put("href",
                "https://mirror/file");
        var second = profile.decode("cache/a/wis2/fr/example/data/core/weather", json.writeValueAsBytes(relay));
        require(first.notification().uniqueName().equals(second.notification().uniqueName()),
                "Broker IDs, cache topics and mirror URLs must not create duplicate file versions");
        var update = wnm.deepCopy();
        var updateLinks = (com.fasterxml.jackson.databind.node.ArrayNode) update.path("links");
        updateLinks.addObject().put("rel", "update").put("href", "https://provider/update").put("length", 5);
        ((com.fasterxml.jackson.databind.node.ObjectNode) update.path("properties")).put("pubtime",
                "2026-11-07T14:00:01Z");
        var corrected = profile.decode(topic, json.writeValueAsBytes(update));
        require(corrected.href().endsWith("/update")
                && !first.notification().uniqueName().equals(corrected.notification().uniqueName()),
                "Update links take precedence and later publications have distinct version identities");
        var invalid = wnm.deepCopy();
        ((com.fasterxml.jackson.databind.node.ObjectNode) invalid.path("properties")).put("pubtime", "not-a-date");
        expectIOException(() -> profile.decode(topic, json.writeValueAsBytes(invalid)));
        expectIOException(() -> profile.decode("other/topic", json.writeValueAsBytes(wnm)));
        expectIOException(
                () -> profile.decode("cache/a/wis2/fr/example/data/core/weather", json.writeValueAsBytes(wnm)));
        expectIOException(() -> profile.decode(topic, "{} {}".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        var badDigest = wnm.deepCopy();
        ((com.fasterxml.jackson.databind.node.ObjectNode) badDigest.path("properties").path("integrity")).put("method",
                "unsupported");
        expectIOException(() -> profile.decode(topic, json.writeValueAsBytes(badDigest)));
        var noLength = wnm.deepCopy();
        ((com.fasterxml.jackson.databind.node.ObjectNode) noLength.path("links").get(0)).remove("length");
        ((com.fasterxml.jackson.databind.node.ObjectNode) noLength.path("properties")).remove("integrity");
        var unspecified = profile.decode(topic, json.writeValueAsBytes(noLength));
        require(unspecified.size() == -1 && unspecified.notification().integrity() == null,
                "Missing length and integrity must remain unknown rather than becoming invented values");
        var fractionalLength = wnm.deepCopy();
        ((com.fasterxml.jackson.databind.node.ObjectNode) fractionalLength.path("links").get(0)).put("length", 5.5);
        expectIOException(() -> profile.decode(topic, json.writeValueAsBytes(fractionalLength)));
        var inline = wnm.deepCopy();
        ((com.fasterxml.jackson.databind.node.ObjectNode) inline.path("properties")).putObject("content")
                .put("encoding", "base64").put("value", "aGVsbG8=");
        require(profile.decode(topic, json.writeValueAsBytes(inline)).body().equals("aGVsbG8="),
                "Valid inline content must retain its complete bytes");
        ((com.fasterxml.jackson.databind.node.ObjectNode) inline.path("links").get(0)).remove("length");
        require(profile.decode(topic, json.writeValueAsBytes(inline)).size() == 5,
                "Inline bytes supply a missing length without fetching the retrieval URL");
        ((com.fasterxml.jackson.databind.node.ObjectNode) inline.path("properties").path("content")).put("value",
                "d3Jvbmc=");
        expectIOException(() -> profile.decode(topic, json.writeValueAsBytes(inline)));
        first.notification().integrity().verifyContent("hello".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        expectIOException(() -> first.notification().integrity()
                .verifyContent("wrong".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        for (var algorithm : new String[] { "SHA-256", "SHA-384" }) {
            var content = "hello".getBytes(java.nio.charset.StandardCharsets.UTF_8);
            var expected = java.util.Base64.getEncoder()
                    .encodeToString(java.security.MessageDigest.getInstance(algorithm).digest(content));
            new ecmwf.common.ectrans.notification.AcquisitionNotification.Integrity(algorithm, expected)
                    .verifyContent(content);
        }

        var longIdentity = "wis2/example/" + "x".repeat(2000) + "/file.txt";
        var full = new ecmwf.common.ectrans.notification.AcquisitionNotification("wis2", "id", longIdentity,
                "2026-11-07T14:00:00+00:00", topic, "origin", "canonical", first.notification().integrity());
        var metadata = new java.util.LinkedHashMap<String, String>();
        for (var pair : full.metadata().split(",")) {
            var parts = pair.split("=", 2);
            require(parts[0].length() <= 25 && parts[1].length() <= 255,
                    "Notification metadata must fit existing database columns");
            metadata.put(parts[0], parts[1]);
        }
        var restored = ecmwf.common.ectrans.notification.AcquisitionNotification.fromMetadata(metadata);
        require(restored.identity().equals(longIdentity) && restored.integrity().equals(full.integrity()),
                "Persistence must preserve the full identity and full expected digest");
        metadata.remove("notify.0");
        expectIOException(() -> ecmwf.common.ectrans.notification.AcquisitionNotification.fromMetadata(metadata));
        expectIOException(() -> ecmwf.common.ectrans.notification.AcquisitionNotification.decode("bnVsbA=="));
        var envelope = ecmwf.common.ectrans.notification.AcquisitionListing.format("file entry", "aGVsbG8=", "", full);
        var listing = ecmwf.common.ectrans.notification.AcquisitionListing.parse(envelope);
        require(listing.notification().equals(full) && listing.body().equals("aGVsbG8="),
                "The Mover-to-Master listing envelope must preserve attribution and inline bytes");
        var empty = ecmwf.common.ectrans.notification.AcquisitionListing
                .parse(ecmwf.common.ectrans.notification.AcquisitionListing.format("file entry", "", "", full));
        var absent = ecmwf.common.ectrans.notification.AcquisitionListing
                .parse(ecmwf.common.ectrans.notification.AcquisitionListing.format("file entry", null, "", full));
        require("".equals(empty.body()) && absent.body() == null,
                "An empty inline file must remain distinct from a URL retrieval");
        expectIOException(
                () -> ecmwf.common.ectrans.notification.AcquisitionListing.parse(envelope.replace(";n1:", ";n2:")));
        var legacy = ecmwf.common.ectrans.notification.AcquisitionListing.parse("file entry [aGVsbG8=;e30=]");
        require(legacy.notification() == null && legacy.body().equals("aGVsbG8=") && legacy.message().equals("e30="),
                "Legacy script body/message listings must retain their original interpretation");

        var root = java.nio.file.Files.createTempDirectory("ecpds-profile-spi-");
        var metaInf = java.nio.file.Files.createDirectory(root.resolve("META-INF"));
        var services = java.nio.file.Files.createDirectory(metaInf.resolve("services"));
        var descriptor = services.resolve(ecmwf.common.ectrans.notification.NotificationProfile.class.getName());
        try {
            java.nio.file.Files.writeString(descriptor, FixtureProfile.class.getName());
            try (var loader = new java.net.URLClassLoader(new java.net.URL[] { root.toUri().toURL() },
                    RemoteListingEngineTest.class.getClassLoader())) {
                require(ecmwf.common.ectrans.notification.NotificationProfiles.resolve("fixture", loader)
                        .decode("topic", new byte[0]) == null,
                        "External stateless profiles must be discovered and invoked through the SPI");
            }
        } finally {
            java.nio.file.Files.deleteIfExists(descriptor);
            java.nio.file.Files.deleteIfExists(services);
            java.nio.file.Files.deleteIfExists(metaInf);
            java.nio.file.Files.deleteIfExists(root);
        }
        System.out.println("Notification profile, identity, integrity and persistence checks passed");
    }

    public static final class FixtureProfile implements ecmwf.common.ectrans.notification.NotificationProfile {
        @Override
        public String name() {
            return "fixture";
        }

        @Override
        public Result decode(String topic, byte[] payload) {
            return null;
        }
    }

    private static String wis2Message() throws Exception {
        var digest = java.util.Base64.getEncoder().encodeToString(java.security.MessageDigest.getInstance("SHA-512")
                .digest("hello".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        return """
                {"id":"notification-one","type":"Feature","properties":{
                  "data_id":"wis2/example/file.txt","pubtime":"2026-11-07T14:00:00Z",
                  "integrity":{"method":"sha512","value":"%s"}},
                  "links":[{"rel":"canonical","href":"https://provider/file.txt","length":5}]}
                """.formatted(digest);
    }

    private static void testMqttNotificationListing(String profile) throws Exception {
        testMqttNotificationListing(profile, false);
    }

    private static void testMqttNotificationListing(String profile, boolean emptyInline) throws Exception {
        var topic = "origin/a/wis2/fr/example/data/core/weather";
        var wnm = new com.fasterxml.jackson.databind.ObjectMapper().readTree(wis2Message());
        if (emptyInline) {
            var properties = (com.fasterxml.jackson.databind.node.ObjectNode) wnm.path("properties");
            properties.remove("integrity");
            properties.putObject("content").put("encoding", "base64").put("value", "");
            ((com.fasterxml.jackson.databind.node.ObjectNode) wnm.path("links").get(0)).remove("length");
        }
        var payload = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsBytes(wnm);
        try (var server = new java.net.ServerSocket(0, 1, java.net.InetAddress.getLoopbackAddress())) {
            server.setSoTimeout(30000);
            var finished = new java.util.concurrent.CompletableFuture<Void>();
            var broker = new Thread(() -> {
                try (var socket = server.accept()) {
                    socket.setSoTimeout(30000);
                    var in = socket.getInputStream();
                    var out = socket.getOutputStream();
                    readMqttPacket(in);
                    writeMqttPacket(out, 0x20, new byte[] { 0, 0, 0 });
                    var subscribe = readMqttPacket(in);
                    writeMqttPacket(out, 0x90, new byte[] { subscribe[1], subscribe[2], 0, 0 });
                    var message = new java.io.ByteArrayOutputStream();
                    var topicBytes = topic.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                    message.write(topicBytes.length >> 8);
                    message.write(topicBytes.length & 255);
                    message.write(topicBytes);
                    message.write(0);
                    message.write(payload);
                    writeMqttPacket(out, 0x30, message.toByteArray());
                    while (true) {
                        var packet = readMqttPacket(in);
                        if ((packet[0] & 0xf0) == 0xe0)
                            break;
                        if ((packet[0] & 0xf0) == 0xc0)
                            writeMqttPacket(out, 0xd0, new byte[0]);
                    }
                    finished.complete(null);
                } catch (Throwable e) {
                    finished.completeExceptionally(e);
                }
            }, "notification-test-broker");
            broker.setDaemon(true);
            broker.start();
            var setup = new ecmwf.common.ectrans.ECtransSetup("http",
                    ecmwf.common.ectrans.ECtransSetup.SEPARATOR
                            + "return { http: { mqttHref: mqttPayload.links[0].href,"
                            + "mqttSize: 5, mqttTime: Date.parse(mqttPayload.properties.pubtime),"
                            + "mqttAlternativeName: 'script-file.txt' } };");
            setup.set("mqttMode", "yes");
            setup.set("mqttProfile", profile);
            setup.set("mqttUrl", "tcp://127.0.0.1:" + server.getLocalPort());
            setup.set("sslValidation", "yes");
            setup.set("mqttAwait", "3s");
            setup.set("mqttQos", 0);
            setup.set("ftpLike", "yes");
            setup.set("mqttAddPayload", "yes");
            if (profile.equals("script")) {
                ecmwf.common.technical.ScriptManager.exec(setup.getScriptLanguage(),
                        Map.of("mqttPayload",
                                new com.fasterxml.jackson.databind.ObjectMapper().readValue(payload, Map.class),
                                "mqttTopic", topic),
                        setup.getScriptContent(), value -> {
                            require(setup.getString(ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_MQTT_HREF, value)
                                    .equals("https://provider/file.txt"),
                                    "Fixture script must resolve the notification URL");
                            return null;
                        });
            }
            var module = new ecmwf.common.ectrans.module.HttpModule();
            try {
                module.connect(":@127.0.0.1", setup);
                var results = module.listAsStringArray(topic, null);
                require(results.length == 1 && !results[0].startsWith("err:"),
                        profile + " MQTT callback must emit a file listing: " + java.util.Arrays.toString(results));
                var listing = ecmwf.common.ectrans.notification.AcquisitionListing.parse(results[0]);
                var entries = FtpParser.parseDir(null, "UNIX", null, null, "en", null, "UTC",
                        new String[] { listing.line() });
                require(entries.length == 1 && entries[0].size == (emptyInline ? 0 : 5),
                        profile + " listing must be understood by the acquisition parser");
                if (emptyInline)
                    require("".equals(listing.body()),
                            "MQTT must preserve an empty inline file without fetching its URL");
                require(java.util.Arrays.equals(java.util.Base64.getDecoder().decode(listing.message()), payload),
                        "Optional raw payload attachment must be unchanged");
                require(profile.equals("wis2") ? listing.notification() != null && !results[0].contains("script-file")
                        : listing.notification() == null && results[0].contains("script-file.txt"),
                        "WIS2 is explicitly selected and default scripts retain their extraction behaviour");
                finished.get(5, java.util.concurrent.TimeUnit.SECONDS);
                broker.join(1000);
                require(!broker.isAlive(), "Test broker must close after disconnect");
            } finally {
                module.close();
            }
        }
        System.out.println(profile + " MQTT transport-to-listing checks passed");
    }

    private static byte[] readMqttPacket(java.io.InputStream in) throws Exception {
        int header = in.read();
        if (header < 0)
            throw new java.io.EOFException();
        int length = 0;
        int multiplier = 1;
        for (int i = 0; i < 4; i++) {
            int next = in.read();
            if (next < 0)
                throw new java.io.EOFException();
            length += (next & 127) * multiplier;
            if ((next & 128) == 0)
                break;
            multiplier *= 128;
        }
        if (length > 1024 * 1024)
            throw new java.io.IOException("Test MQTT packet too large");
        var body = in.readNBytes(length);
        var packet = new byte[body.length + 1];
        packet[0] = (byte) header;
        System.arraycopy(body, 0, packet, 1, body.length);
        return packet;
    }

    private static void writeMqttPacket(java.io.OutputStream out, int header, byte[] body) throws Exception {
        out.write(header);
        int remaining = body.length;
        do {
            int digit = remaining % 128;
            remaining /= 128;
            out.write(remaining > 0 ? digit | 128 : digit);
        } while (remaining > 0);
        out.write(body);
        out.flush();
    }

    @FunctionalInterface
    private interface CheckedAction {
        void run() throws Exception;
    }

    private static void expectIOException(CheckedAction action) throws Exception {
        try {
            action.run();
            throw new AssertionError("Invalid notification must fail explicitly");
        } catch (java.io.IOException expected) {
        }
    }
}