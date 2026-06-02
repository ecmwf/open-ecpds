package ecmwf.common.parser.parsers;

public class StacListingParser extends JsonListingParser {

    @Override
    public String name() {
        return "stac";
    }

    @Override
    public boolean supports(String hint) {

        return "stac".equalsIgnoreCase(hint) || (hint != null && hint.contains("geojson"));
    }
}