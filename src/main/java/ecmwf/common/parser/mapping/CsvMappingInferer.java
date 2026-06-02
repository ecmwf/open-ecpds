package ecmwf.common.parser.mapping;

import java.util.HashMap;
import java.util.Map;

public class CsvMappingInferer {

    public Map<String, String> infer(String firstLine) {

        String[] cols = firstLine.split(",");

        Map<String, String> m = new HashMap<>();

        for (int i = 0; i < cols.length; i++) {

            String c = cols[i].toLowerCase();

            if (c.contains("name") || c.contains("file")) {

                m.put("nameCol", "" + i);
            }

            if (c.contains("url") || c.contains("href")) {

                m.put("urlCol", "" + i);
            }

            if (c.contains("time") || c.contains("date")) {

                m.put("timeCol", "" + i);
            }
        }

        return m;
    }
}