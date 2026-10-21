package ecmwf.common.parser.mapping;

import java.util.HashMap;
import java.util.Map;
import java.util.Locale;

import ecmwf.common.parser.core.ParserConfig;
import ecmwf.common.parser.format.CsvHelper;

public class CsvMappingInferer {

    public Map<String, String> infer(String firstLine) {

        Map<String, String> m = new HashMap<>();
        try (var parser = CsvHelper.parse(firstLine, new ParserConfig("csv", Map.of()))) {
            for (var column : parser.getHeaderMap().entrySet()) {
                int i = column.getValue();
                String c = column.getKey().toLowerCase(Locale.ROOT);

                if (c.contains("name") || c.contains("file")) {

                    m.put("nameCol", "" + i);
                }

                if (c.contains("url") || c.contains("href") || c.contains("link")) {

                    m.put("urlCol", "" + i);
                }

                if (c.contains("time") || c.contains("date")) {

                    m.put("timeCol", "" + i);
                }
                if (c.contains("size") || c.equals("bytes") || c.equals("length")) {
                    m.put("sizeCol", "" + i);
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to infer CSV column mappings", e);
        }

        return m;
    }
}