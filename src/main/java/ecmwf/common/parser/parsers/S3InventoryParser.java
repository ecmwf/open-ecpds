package ecmwf.common.parser.parsers;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import ecmwf.common.ftp.FtpParser;
import ecmwf.common.parser.core.ParserConfig;
import ecmwf.common.parser.core.RemoteListingParser;

public class S3InventoryParser implements RemoteListingParser {

    @Override
    public String name() {
        return "s3";
    }

    @Override
    public boolean supports(String hint) {
        return hint != null
                && (hint.equalsIgnoreCase("s3") || hint.contains("amazonaws") || hint.contains("s3-inventory"));
    }

    @Override
    public FtpParser.FileEntry[] parse(String input, ParserConfig config) throws Exception {

        CSVFormat format = CSVFormat.DEFAULT.builder().setTrim(true).setIgnoreEmptyLines(true).setHeader()
                .setSkipHeaderRecord(true).build();

        CSVParser parser = CSVParser.parse(input, format);

        // Column mapping (configurable, but safe defaults)
        String keyCol = config.getOrDefault("keyCol", "key");
        String sizeCol = config.getOrDefault("sizeCol", "size");
        String timeCol = config.getOrDefault("timeCol", "last_modified");
        String bucketCol = config.getOrDefault("bucketCol", "bucket");

        // Optional base URL for HTTP access
        String baseUrl = config.getOrDefault("baseUrl", null);

        List<FtpParser.FileEntry> out = new ArrayList<>();

        for (CSVRecord r : parser) {

            FtpParser.FileEntry e = new FtpParser.FileEntry();

            // --- NAME (S3 object key) ---
            e.name = safeGet(r, keyCol);

            // --- SIZE ---
            try {
                String sizeStr = safeGet(r, sizeCol);
                e.size = sizeStr != null ? Long.parseLong(sizeStr) : -1;
            } catch (Exception ignored) {
                e.size = -1;
            }

            // --- TIME ---
            try {
                String timeStr = safeGet(r, timeCol);
                if (timeStr != null) {
                    e.time = parseTime(timeStr);
                }
            } catch (Exception ignored) {
                e.time = -1;
            }

            // --- LINK (derived) ---
            String bucket = safeGet(r, bucketCol);

            if (baseUrl != null && e.name != null) {
                e.link = baseUrl + "/" + e.name;
            } else if (bucket != null && e.name != null) {
                e.link = "s3://" + bucket + "/" + e.name;
            } else if (e.name != null) {
                e.link = "s3://" + e.name;
            }

            // raw debug line
            e.line = r.toString();

            out.add(e);
        }

        return out.toArray(new FtpParser.FileEntry[0]);
    }

    private String safeGet(CSVRecord r, String col) {
        try {
            if (col == null)
                return null;

            // allow both header-based and index-based configs
            if (isNumeric(col)) {
                return r.get(Integer.parseInt(col));
            }
            return r.isMapped(col) ? r.get(col) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isNumeric(String s) {
        return s != null && s.matches("\\d+");
    }

    private long parseTime(String value) {
        try {
            // ISO-8601 (S3 standard)
            return Instant.parse(value).toEpochMilli();
        } catch (Exception e) {
            // fallback: epoch millis
            try {
                return Long.parseLong(value);
            } catch (Exception ex) {
                return -1;
            }
        }
    }
}