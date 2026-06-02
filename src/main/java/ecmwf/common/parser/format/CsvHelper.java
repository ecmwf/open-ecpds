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
}