package ecmwf.common.parser.discovery;

import ecmwf.common.parser.core.DetectionResult;

public interface FormatDetector {

    DetectionResult detect(String sample);
}