package ecmwf.common.parser.discovery;

import java.util.List;
import java.util.Map;

import ecmwf.common.ftp.FtpParser;

public class ParserPreview {

    private String parserType;

    private Map<String, String> suggestedConfig;

    private FtpParser.FileEntry[] previewEntries;

    private List<String> warnings;

    private double confidence;

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

    public FtpParser.FileEntry[] getPreviewEntries() {
        return previewEntries;
    }

    public void setPreviewEntries(FtpParser.FileEntry[] previewEntries) {
        this.previewEntries = previewEntries;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }
}