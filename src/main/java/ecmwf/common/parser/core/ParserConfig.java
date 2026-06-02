package ecmwf.common.parser.core;

import java.util.Map;

public class ParserConfig {

    private final String type;
    private final Map<String, String> properties;

    public ParserConfig(String type, Map<String, String> properties) {
        this.type = type;
        this.properties = properties;
    }

    public String getType() {
        return type;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public String get(String key) {
        return properties.get(key);
    }

    public String getOrDefault(String key, String defaultValue) {
        String v = properties.get(key);
        return v != null ? v : defaultValue;
    }

    public int getInt(String key, int defaultValue) {
        String v = properties.get(key);
        if (v == null)
            return defaultValue;
        try {
            return Integer.parseInt(v);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String v = properties.get(key);
        if (v == null)
            return defaultValue;
        return Boolean.parseBoolean(v);
    }
}