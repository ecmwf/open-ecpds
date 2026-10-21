package ecmwf.common.parser.parsers;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import ecmwf.common.ftp.FtpParser;
import ecmwf.common.parser.core.ParserConfig;
import ecmwf.common.parser.core.RemoteListingParser;
import ecmwf.common.parser.format.JsonPath;
import ecmwf.common.parser.format.ListingMetadata;

public class XmlListingParser implements RemoteListingParser {

    private final XmlMapper mapper = new XmlMapper();

    @Override
    public String name() {
        return "xml";
    }

    @Override
    public boolean supports(String hint) {

        return "xml".equalsIgnoreCase(hint);
    }

    @Override
    public FtpParser.FileEntry[] parse(String input, ParserConfig config) throws Exception {

        ListingMetadata metadata = new ListingMetadata(config);
        JsonNode root = mapper.readTree(input);

        String arrayPath = config.getOrDefault("arrayPath", "items.item");

        JsonNode array = JsonPath.get(root, arrayPath);

        if (array == null) {
            array = JsonPath.get(root, "item");
        }

        if (array == null) {
            array = JsonPath.get(root, "items");
        }

        if (array == null || array.isNull()) {
            return new FtpParser.FileEntry[0];
        }

        List<JsonNode> nodes = new ArrayList<>();

        if (array.isArray()) {
            array.forEach(nodes::add);
        } else {
            nodes.add(array);
        }

        String nameField = config.getOrDefault("nameField", "name");
        String urlField = config.getOrDefault("urlField", "url");
        String timeField = config.getOrDefault("timeField", "time");
        String sizeField = config.getOrDefault("sizeField", "size");

        List<FtpParser.FileEntry> out = new ArrayList<>();

        for (JsonNode n : nodes) {

            FtpParser.FileEntry e = new FtpParser.FileEntry();

            e.name = JsonPath.text(n, nameField);
            e.link = JsonPath.text(n, urlField);
            e.line = n.toString();

            e.time = ListingMetadata.time(JsonPath.text(n, timeField));
            e.size = metadata.size(JsonPath.text(n, sizeField));

            out.add(e);
        }

        return out.toArray(new FtpParser.FileEntry[0]);
    }
}