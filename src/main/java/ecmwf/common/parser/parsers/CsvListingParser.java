package ecmwf.common.parser.parsers;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import ecmwf.common.ftp.FtpParser;
import ecmwf.common.parser.core.ParserConfig;
import ecmwf.common.parser.core.RemoteListingParser;
import ecmwf.common.parser.format.CsvHelper;

public class CsvListingParser implements RemoteListingParser {

    @Override
    public String name() {
        return "csv";
    }

    @Override
    public boolean supports(String hint) {

        return "csv".equalsIgnoreCase(hint) || "cmr".equalsIgnoreCase(hint) || "nasa".equalsIgnoreCase(hint);
    }

    private String safeGet(CSVRecord r, int idx) {
        if (idx < 0 || idx >= r.size()) {
            return null;
        }
        return r.get(idx);
    }

    @Override
    public FtpParser.FileEntry[] parse(String input, ParserConfig config) throws Exception {

        CSVParser parser = CsvHelper.parse(input, config);

        int nameCol = config.getInt("nameCol", 0);
        int timeCol = config.getInt("timeCol", -1);
        int urlCol = config.getInt("urlCol", -1);

        List<FtpParser.FileEntry> out = new ArrayList<>();

        for (CSVRecord r : parser) {

            FtpParser.FileEntry e = new FtpParser.FileEntry();

            e.name = safeGet(r, nameCol);
            e.link = safeGet(r, urlCol);
            e.line = r.toString();

            // safer time parsing
            if (timeCol >= 0) {
                String t = safeGet(r, timeCol);
                if (t != null) {
                    e.time = parseTimeSafe(t);
                }
            }

            out.add(e);
        }

        return out.toArray(new FtpParser.FileEntry[0]);
    }

    private long parseTimeSafe(String value) {

        if (value == null) {
            return -1;
        }

        value = value.trim();

        try {
            return Instant.parse(value).toEpochMilli();
        } catch (Exception ignored) {
        }

        try {
            return LocalDate.parse(value).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
        } catch (Exception ignored) {
        }

        try {
            return Long.parseLong(value);
        } catch (Exception ignored) {
        }

        return -1;
    }
}