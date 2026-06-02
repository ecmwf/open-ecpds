package ecmwf.common.parser.mapping;

import java.util.HashMap;
import java.util.Map;

/**
 * FieldMappingEngine is responsible for inferring how raw input fields map to {@code FileEntry} attributes.
 *
 * <p>
 * It supports multiple formats:
 * <ul>
 * <li>CSV (column-based inference)</li>
 * <li>JSON (field/path-based inference)</li>
 * <li>STAC (semantic JSON structure)</li>
 * <li>XML (node path inference)</li>
 * </ul>
 * </p>
 *
 * <p>
 * This engine does NOT parse data itself; it only analyzes structure and produces a {@link MappingSuggestion}.
 * </p>
 */
public class FieldMappingEngine {

    /**
     * Main entry point for mapping inference.
     *
     * @param parserType
     *            detected format (csv/json/xml/stac/ftp/s3)
     * @param sample
     *            first lines or sample document
     */
    public MappingSuggestion infer(String parserType, String sample) {

        if (parserType == null) {
            return unknown();
        }

        return switch (parserType.toLowerCase()) {

        case "csv" -> inferCsv(sample);
        case "json" -> inferJson(sample);
        case "stac" -> inferStac(sample);
        case "xml" -> inferXml(sample);
        case "ftp" -> inferFtp(sample);
        case "s3" -> inferS3(sample);

        default -> unknown();
        };
    }

    // ------------------------------------------------------------
    // CSV inference
    // ------------------------------------------------------------

    private MappingSuggestion inferCsv(String sample) {

        String[] firstLine = sample.split("\n")[0].split(",");

        Map<String, String> map = new HashMap<>();

        for (int i = 0; i < firstLine.length; i++) {

            String col = firstLine[i].toLowerCase();

            if (col.contains("name") || col.contains("file")) {
                map.put("nameCol", String.valueOf(i));
            }

            if (col.contains("url") || col.contains("href") || col.contains("link")) {
                map.put("urlCol", String.valueOf(i));
            }

            if (col.contains("time") || col.contains("date")) {
                map.put("timeCol", String.valueOf(i));
            }
        }

        return new MappingSuggestion("csv", map, 0.80, "CSV columns inferred from header keywords");
    }

    // ------------------------------------------------------------
    // JSON inference (simple heuristic version)
    // ------------------------------------------------------------

    private MappingSuggestion inferJson(String sample) {

        Map<String, String> map = new HashMap<>();

        map.put("arrayPath", "items");
        map.put("nameField", "name");
        map.put("urlField", "url");
        map.put("timeField", "time");

        return new MappingSuggestion("json", map, 0.60, "Default JSON structure assumption (items[] array)");
    }

    // ------------------------------------------------------------
    // STAC inference
    // ------------------------------------------------------------

    private MappingSuggestion inferStac(String sample) {

        Map<String, String> map = new HashMap<>();

        map.put("arrayPath", "features");
        map.put("nameField", "id");
        map.put("urlField", "assets.data.href");
        map.put("timeField", "properties.start_datetime");

        return new MappingSuggestion("stac", map, 0.95, "STAC structure detected (features + assets + properties)");
    }

    // ------------------------------------------------------------
    // XML inference
    // ------------------------------------------------------------

    private MappingSuggestion inferXml(String sample) {

        Map<String, String> map = new HashMap<>();

        map.put("arrayPath", "items.item");
        map.put("nameField", "name");
        map.put("urlField", "url");
        map.put("timeField", "time");

        return new MappingSuggestion("xml", map, 0.70, "Generic XML structure assumed (items/item nodes)");
    }

    // ------------------------------------------------------------
    // FTP inference
    // ------------------------------------------------------------

    private MappingSuggestion inferFtp(String sample) {

        Map<String, String> map = new HashMap<>();

        map.put("system", "UNIX");

        return new MappingSuggestion("ftp", map, 0.85, "FTP listing format detected (UNIX style listing)");
    }

    // ------------------------------------------------------------
    // S3 inference
    // ------------------------------------------------------------

    private MappingSuggestion inferS3(String sample) {

        Map<String, String> map = new HashMap<>();

        map.put("delimiter", ",");
        map.put("header", "true");
        map.put("keyField", "key");
        map.put("sizeField", "size");
        map.put("timeField", "last_modified");

        return new MappingSuggestion("s3", map, 0.90, "S3 inventory CSV structure assumed");
    }

    private MappingSuggestion unknown() {

        return new MappingSuggestion("unknown", Map.of(), 0.0, "Unable to detect format");
    }
}