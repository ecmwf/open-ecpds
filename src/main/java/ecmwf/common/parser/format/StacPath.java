package ecmwf.common.parser.format;

import com.fasterxml.jackson.databind.JsonNode;

public final class StacPath {

    private StacPath() {
    }

    public static String get(JsonNode node, String path) {
        return JsonPath.text(node, path);
    }
}