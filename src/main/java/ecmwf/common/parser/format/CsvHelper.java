package ecmwf.common.parser.format;

import org.apache.commons.csv.*;

import ecmwf.common.parser.core.ParserConfig;

import java.io.StringReader;

public final class CsvHelper {

    private CsvHelper() {
    }

    public static CSVParser parse(String input, ParserConfig config) throws Exception {

        CSVFormat format = CsvFormatFactory.create(config);

        return format.parse(new StringReader(input));
    }

    public static String value(CSVRecord record, String column) {
        if (column == null || column.isBlank() || "-1".equals(column)) {
            return null;
        }
        if (column.matches("\\d+")) {
            int index = Integer.parseInt(column);
            return index < record.size() ? record.get(index) : null;
        }
        return record.isMapped(column) && record.isSet(column) ? record.get(column) : null;
    }
}