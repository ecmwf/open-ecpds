package ecmwf.common.parser.mapping;

import java.util.Map;

/**
 * Suggestion produced by the ingestion system to help the user configure field extraction from a remote listing.
 *
 * <p>
 * This object is used by the OpenECPDS UI during "auto-detection + preview" to propose a mapping between source fields
 * and {@code FileEntry} attributes.
 * </p>
 */
public class MappingSuggestion {

    /**
     * Detected parser type (csv, json, stac, xml, ftp, s3...)
     */
    private String parserType;

    /**
     * Suggested configuration key/value pairs.
     *
     * Example: - nameCol=0 - urlField=assets.data.href - timeField=properties.start_datetime
     */
    private Map<String, String> suggestedConfig;

    /**
     * Confidence score of detection (0.0 → 1.0)
     */
    private double confidence;

    /**
     * Human-readable explanation of why this mapping was suggested.
     */
    private String explanation;

    public MappingSuggestion() {
    }

    public MappingSuggestion(String parserType, Map<String, String> suggestedConfig, double confidence,
            String explanation) {
        this.parserType = parserType;
        this.suggestedConfig = suggestedConfig;
        this.confidence = confidence;
        this.explanation = explanation;
    }

    public String getParserType() {
        return parserType;
    }

    public void setParserType(String parserType) {
        this.parserType = parserType;
    }

    public Map<String, String> getSuggestedConfig() {
        return suggestedConfig;
    }

    public void setSuggestedConfig(Map<String, String> suggestedConfig) {
        this.suggestedConfig = suggestedConfig;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}