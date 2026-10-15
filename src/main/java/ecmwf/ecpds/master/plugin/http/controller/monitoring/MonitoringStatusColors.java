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

package ecmwf.ecpds.master.plugin.http.controller.monitoring;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.ecpds.master.DataBaseInterface;
import ecmwf.ecpds.master.MasterManager;

/**
 * Configurable colors for the status scales shown throughout the Monitor UI.
 *
 * <p>
 * Overrides are stored in SYS_CONFIG under the {@code MonitoringColors} group. The short-lived cache avoids a remote
 * database lookup for every status badge; changes made in this Monitor are applied immediately, and other Monitor
 * instances pick them up on their next refresh.
 */
public final class MonitoringStatusColors {

    /** SYS_CONFIG group containing the configured colors. */
    public static final String CONFIG_GROUP = "MonitoringColors";

    private static final Logger LOG = LogManager.getLogger(MonitoringStatusColors.class);
    private static final long CACHE_MILLIS = 15_000L;
    private static final Pattern HEX_COLOR = Pattern.compile("#[0-9a-fA-F]{6}");
    private static final List<ColorSetting> SETTINGS = createSettings();
    private static final Map<String, ColorSetting> SETTINGS_BY_KEY = indexSettings();

    private static volatile Map<String, String> overrides = Collections.emptyMap();
    private static volatile long lastRefresh;

    private MonitoringStatusColors() {
    }

    /**
     * Returns the settings in display order.
     */
    public static List<ColorSetting> settings() {
        return SETTINGS;
    }

    /**
     * Returns the palette section names in display order.
     */
    public static List<String> groups() {
        final var result = new ArrayList<String>();
        for (final var setting : SETTINGS) {
            if (!result.contains(setting.getGroup())) {
                result.add(setting.getGroup());
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Returns the complete configured palette, including defaults for values without a SYS_CONFIG override.
     */
    public static Map<String, String> colors() {
        return colors(configuredOverrides());
    }

    /**
     * Returns the complete palette using a previously loaded SYS_CONFIG group.
     */
    public static Map<String, String> colors(final Map<String, String> configured) {
        final var result = new LinkedHashMap<String, String>();
        for (final var setting : SETTINGS) {
            final var value = configured == null ? null : configured.get(setting.getKey());
            result.put(setting.getKey(), isValidColor(value) ? value.toUpperCase() : setting.getDefaultColor());
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * Returns the color for a known setting key.
     */
    public static String color(final String key) {
        final var setting = SETTINGS_BY_KEY.get(key);
        if (setting == null) {
            throw new IllegalArgumentException("Unknown monitoring status color: " + key);
        }
        return colors().get(key);
    }

    /**
     * Returns the transfer/arrival status color as a Java AWT color.
     */
    public static Color arrivalTransferColor(final int status) {
        return Color.decode(color("arrivalTransfer." + status));
    }

    /**
     * Builds the CSS custom properties consumed by the Monitor's shared status styles.
     */
    public static String cssVariables() {
        final var palette = colors();
        final var css = new StringBuilder(":root{");
        for (final var setting : SETTINGS) {
            final var value = palette.get(setting.getKey());
            css.append(setting.getCssVariable()).append(':').append(value).append(';');
            if (setting.getCssTextVariable() != null) {
                css.append(setting.getCssTextVariable()).append(':').append(contrastText(value)).append(';');
            }
        }
        return css.append('}').toString();
    }

    /**
     * Invalidates the local cache after a successful configuration change.
     */
    public static void invalidateCache() {
        lastRefresh = System.currentTimeMillis() - CACHE_MILLIS;
    }

    /**
     * Validates and normalizes an HTML color value.
     *
     * @throws IllegalArgumentException
     *             if the value is not a six-digit hexadecimal color
     */
    public static String normalizeColor(final String value) {
        if (!isValidColor(value)) {
            throw new IllegalArgumentException("Color must be in #RRGGBB format.");
        }
        return value.toUpperCase();
    }

    private static Map<String, String> configuredOverrides() {
        final var now = System.currentTimeMillis();
        if (now - lastRefresh < CACHE_MILLIS) {
            return overrides;
        }
        synchronized (MonitoringStatusColors.class) {
            if (System.currentTimeMillis() - lastRefresh < CACHE_MILLIS) {
                return overrides;
            }
            try {
                final var fromDb = MasterManager.getDB().getMonitorConfigGroup(CONFIG_GROUP);
                final var accepted = new LinkedHashMap<String, String>();
                for (final var entry : fromDb.entrySet()) {
                    if (SETTINGS_BY_KEY.containsKey(entry.getKey()) && isValidColor(entry.getValue())) {
                        accepted.put(entry.getKey(), entry.getValue().toUpperCase());
                    }
                }
                overrides = Collections.unmodifiableMap(accepted);
            } catch (final Exception e) {
                LOG.warn("Unable to load monitoring status colors from SYS_CONFIG; using cached/default colors", e);
            } finally {
                lastRefresh = System.currentTimeMillis();
            }
            return overrides;
        }
    }

    private static boolean isValidColor(final String value) {
        return value != null && HEX_COLOR.matcher(value).matches();
    }

    private static String contrastText(final String hex) {
        final var rgb = Integer.parseInt(hex.substring(1), 16);
        final var red = linear((rgb >> 16) & 0xff);
        final var green = linear((rgb >> 8) & 0xff);
        final var blue = linear(rgb & 0xff);
        final var luminance = 0.2126 * red + 0.7152 * green + 0.0722 * blue;
        return luminance > 0.179 ? "#212529" : "#ffffff";
    }

    private static double linear(final int channel) {
        final var value = channel / 255.0;
        return value <= 0.04045 ? value / 12.92 : Math.pow((value + 0.055) / 1.055, 2.4);
    }

    private static List<ColorSetting> createSettings() {
        final var result = new ArrayList<ColorSetting>();
        final String[] generationLabels = { "No data / pending", "Received >30 minutes ahead",
                "Received 15-30 minutes ahead", "Received 0-15 minutes ahead", "On schedule / slightly late",
                "Not received / overdue", "Transitional / other", "Other status" };
        final String[] generationColors = { "#adb5bd", "#20c997", "#28a745", "#fd7e14", "#ffc107", "#dc3545", "#6f42c1",
                "#6f42c1" };
        for (var status = 0; status < generationLabels.length; status++) {
            result.add(new ColorSetting("product." + status, "Product generation", generationLabels[status],
                    generationColors[status], "--mon-product-status-" + status, null));
        }

        final String[] openViewLabels = { "Success", "Warning", "Critical", "Unknown" };
        final String[] openViewColors = { "#28a745", "#ffc107", "#dc3545", "#0d6efd" };
        for (var status = 0; status < openViewLabels.length; status++) {
            result.add(new ColorSetting("openView." + status, "OpenView status", openViewLabels[status],
                    openViewColors[status], "--mon-openview-status-" + status, null));
        }

        final String[] transferLabels = { "No data yet", "Before target / >75 minutes ahead",
                "Before predicted time (on time)", "Behind prediction / slightly late",
                "Late / beyond 2x predicted range", "Very late / beyond 4x predicted range",
                "Critical / beyond 6x predicted range" };
        final String[] transferColors = { "#adb5bd", "#00ffff", "#00db00", "#0000e7", "#ffff00", "#ffb800", "#ff0000" };
        for (var status = 0; status < transferLabels.length; status++) {
            result.add(new ColorSetting("arrivalTransfer." + status, "Arrival and transfer", transferLabels[status],
                    transferColors[status], "--mon-arrival-transfer-status-" + status,
                    "--mon-arrival-transfer-text-" + status));
        }
        return Collections.unmodifiableList(result);
    }

    private static Map<String, ColorSetting> indexSettings() {
        final var result = new LinkedHashMap<String, ColorSetting>();
        for (final var setting : SETTINGS) {
            result.put(setting.getKey(), setting);
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * One editable status color.
     */
    public static final class ColorSetting {
        private final String key;
        private final String group;
        private final String label;
        private final String defaultColor;
        private final String cssVariable;
        private final String cssTextVariable;

        private ColorSetting(final String key, final String group, final String label, final String defaultColor,
                final String cssVariable, final String cssTextVariable) {
            this.key = key;
            this.group = group;
            this.label = label;
            this.defaultColor = defaultColor;
            this.cssVariable = cssVariable;
            this.cssTextVariable = cssTextVariable;
        }

        public String getKey() {
            return key;
        }

        public String getGroup() {
            return group;
        }

        public String getLabel() {
            return label;
        }

        public String getDefaultColor() {
            return defaultColor;
        }

        private String getCssVariable() {
            return cssVariable;
        }

        private String getCssTextVariable() {
            return cssTextVariable;
        }
    }
}
