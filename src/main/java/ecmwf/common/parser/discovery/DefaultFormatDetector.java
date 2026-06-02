package ecmwf.common.parser.discovery;

import ecmwf.common.parser.core.DetectionResult;

public class DefaultFormatDetector implements FormatDetector {

    @Override
    public DetectionResult detect(String sample) {

        String s = sample.trim();

        // JSON / STAC
        if (s.startsWith("{") || s.startsWith("[")) {

            if (s.contains("\"features\"") && s.contains("\"assets\"")) {

                return new DetectionResult("stac", 0.95);
            }

            return new DetectionResult("json", 0.90);
        }

        // XML
        if (s.startsWith("<")) {

            return new DetectionResult("xml", 0.90);
        }

        // CSV
        if (s.contains(",") && s.contains("http")) {

            return new DetectionResult("csv", 0.80);
        }

        // FTP LIST
        if (s.contains("rw-") || s.startsWith("-")) {

            return new DetectionResult("ftp", 0.75);
        }

        return new DetectionResult("unknown", 0.0);
    }
}