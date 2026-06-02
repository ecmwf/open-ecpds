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
                Map.of("delimiter", ",", "keyField", "key", "sizeField", "size", "timeField", "last_modified"));

        FtpParser.FileEntry[] result = engine.process(config, input);

        print(result);
    }

    // ------------------------------------------------------------
    // Helper
    // ------------------------------------------------------------
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