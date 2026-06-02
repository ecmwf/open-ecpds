package ecmwf.common.parser.format;

import org.apache.commons.csv.CSVFormat;

import ecmwf.common.parser.core.ParserConfig;

public final class CsvFormatFactory {

    private CsvFormatFactory() {
    }

    public static CSVFormat create(ParserConfig config) {

        String delimiter = config.getOrDefault("delimiter", ",");
        boolean header = config.getBoolean("header", true);
        boolean trim = config.getBoolean("trim", true);

        CSVFormat.Builder builder = CSVFormat.DEFAULT.builder().setDelimiter(delimiter.charAt(0));

        if (header) {
            builder.setHeader();
            builder.setSkipHeaderRecord(true);
        }

        if (trim) {
            builder.setTrim(true);
        }

        return builder.build();
    }
}