package ecmwf.common.parser.parsers;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ecmwf.common.ftp.FtpParser;
import ecmwf.common.parser.core.ParserConfig;
import ecmwf.common.parser.core.RemoteListingParser;
import ecmwf.common.parser.format.JsonPath;

public class JsonListingParser implements RemoteListingParser {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String name() {
        return "json";
    }

    @Override
    public boolean supports(String hint) {
        return "json".equalsIgnoreCase(hint);
    }

    @Override
    public FtpParser.FileEntry[] parse(String input, ParserConfig config) throws Exception {

        JsonNode root = mapper.readTree(input);

        String arrayPath = config.getOrDefault("arrayPath", "items");

        JsonNode array = JsonPath.get(root, arrayPath);

        if (array == null || !array.isArray()) {
            return new FtpParser.FileEntry[0];
        }

        String nameField = config.getOrDefault("nameField", "name");

        String urlField = config.getOrDefault("urlField", "url");

        String timeField = config.getOrDefault("timeField", "time");

        List<FtpParser.FileEntry> out = new ArrayList<>();

        for (JsonNode n : array) {

            FtpParser.FileEntry e = new FtpParser.FileEntry();

            e.name = JsonPath.text(n, nameField);

            e.link = JsonPath.text(n, urlField);

            e.line = n.toString();

            try {

                String t = JsonPath.text(n, timeField);

                if (t != null) {
                    e.time = Instant.parse(t).toEpochMilli();
                }

            } catch (Exception ignored) {
            }

            out.add(e);
        }

        return out.toArray(new FtpParser.FileEntry[0]);
    }
}