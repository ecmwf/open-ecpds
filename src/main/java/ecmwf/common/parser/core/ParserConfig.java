/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * In applying the License, ECMWF does not waive the privileges and immunities
 * granted to it by virtue of its status as an inter-governmental organization
 * nor does it submit to any jurisdiction.
 */

package ecmwf.common.parser.core;

import java.util.Map;
import java.util.HashMap;
import java.util.Locale;

public class ParserConfig {

    public static ParserConfig forHttpListing(String type, String options) {
        type = type.toLowerCase(Locale.ROOT);
        Map<String, String> values = new HashMap<>();
        if ("csv".equals(type)) {
            values.putAll(Map.of("delimiter", ",", "nameCol", "0", "timeCol", "1", "urlCol", "2"));
        } else if (!java.util.Set.of("json", "xml", "stac", "s3").contains(type)) {
            throw new IllegalArgumentException(
                    "Parse Preview supports CSV, JSON, XML, STAC and S3 inventory; configured parser: " + type);
        }
        if (options != null && !options.isBlank()) {
            for (String pair : options.split(",")) {
                int eq = pair.indexOf('=');
                if (eq > 0) {
                    values.put(pair.substring(0, eq).trim(), pair.substring(eq + 1).trim());
                }
            }
        }
        return new ParserConfig(type, values);
    }

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