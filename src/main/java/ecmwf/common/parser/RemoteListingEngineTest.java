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
}