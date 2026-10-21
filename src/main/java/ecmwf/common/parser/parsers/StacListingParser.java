package ecmwf.common.parser.parsers;

import java.util.HashMap;
import java.util.Map;

import ecmwf.common.ftp.FtpParser;
import ecmwf.common.parser.core.ParserConfig;

public class StacListingParser extends JsonListingParser {

    @Override
    public String name() {
        return "stac";
    }

    @Override
    public boolean supports(String hint) {

        return "stac".equalsIgnoreCase(hint) || (hint != null && hint.contains("geojson"));
    }

    @Override
    public FtpParser.FileEntry[] parse(String input, ParserConfig config) throws Exception {
        Map<String, String> options = new HashMap<>(Map.of("arrayPath", "features", "nameField", "id", "urlField",
                "assets.data.href", "timeField", "properties.start_datetime"));
        options.putAll(config.getProperties());
        String urlField = options.get("urlField");
        String assetPath = urlField.endsWith(".href") ? urlField.substring(0, urlField.length() - 5) : "assets.data";
        options.putIfAbsent("sizeField", assetPath + ".file:size");
        return super.parse(input, new ParserConfig(config.getType(), options));
    }
}