package ecmwf.common.parser.format;

import com.fasterxml.jackson.databind.JsonNode;

public final class JsonPath {

    private JsonPath() {
    }

    public static JsonNode get(JsonNode node, String path) {

        if (node == null || path == null)
            return null;

        String[] parts = path.split("\\.");

        JsonNode current = node;

        for (String p : parts) {
            if (current == null)
                return null;
            current = current.get(p);
        }

        return current;
    }

    public static String text(JsonNode node, String path) {
        JsonNode n = get(node, path);
        return n != null && !n.isNull() ? n.asText() : null;
    }
}