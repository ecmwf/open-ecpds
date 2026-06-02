package ecmwf.common.parser.core;

public class DetectionResult {

    private final String parserType;
    private final double confidence;

    public DetectionResult(String parserType, double confidence) {

        this.parserType = parserType;
        this.confidence = confidence;
    }

    public String getParserType() {
        return parserType;
    }

    public double getConfidence() {
        return confidence;
    }
}