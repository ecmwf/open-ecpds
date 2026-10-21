package ecmwf.common.parser.parsers;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVParser;

import ecmwf.common.ftp.FtpParser;
import ecmwf.common.parser.core.ParserConfig;
import ecmwf.common.parser.core.RemoteListingParser;
import ecmwf.common.parser.format.CsvHelper;
import ecmwf.common.parser.format.ListingMetadata;

public class CsvListingParser implements RemoteListingParser {

    @Override
    public String name() {
        return "csv";
    }

    @Override
    public boolean supports(String hint) {

        return "csv".equalsIgnoreCase(hint) || "cmr".equalsIgnoreCase(hint) || "nasa".equalsIgnoreCase(hint);
    }

    @Override
    public FtpParser.FileEntry[] parse(String input, ParserConfig config) throws Exception {

        String nameCol = config.getOrDefault("nameCol", "0");
        String timeCol = config.getOrDefault("timeCol", "-1");
        String urlCol = config.getOrDefault("urlCol", "-1");
        String sizeCol = config.get("sizeCol");
        ListingMetadata metadata = new ListingMetadata(config);

        List<FtpParser.FileEntry> out = new ArrayList<>();

        try (CSVParser parser = CsvHelper.parse(input, config)) {
            for (var r : parser) {

                FtpParser.FileEntry e = new FtpParser.FileEntry();

                e.name = CsvHelper.value(r, nameCol);
                e.link = CsvHelper.value(r, urlCol);
                e.line = r.toString();
                e.size = metadata.size(CsvHelper.value(r, sizeCol));
                e.time = ListingMetadata.time(CsvHelper.value(r, timeCol));

                out.add(e);
            }
        }

        return out.toArray(new FtpParser.FileEntry[0]);
    }

}