package ecmwf.common.parser.format;

import com.fasterxml.jackson.databind.JsonNode;

public final class XmlPath {

    private XmlPath() {
    }

    public static String text(JsonNode node, String path) {
        return JsonPath.text(node, path);
    }
}