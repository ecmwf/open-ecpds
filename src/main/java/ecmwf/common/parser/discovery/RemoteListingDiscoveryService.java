package ecmwf.common.parser.discovery;

import ecmwf.common.ftp.FtpParser;
import ecmwf.common.parser.core.DetectionResult;
import ecmwf.common.parser.core.ParserConfig;
import ecmwf.common.parser.core.RemoteListingEngine;
import ecmwf.common.parser.mapping.FieldMappingEngine;
import ecmwf.common.parser.mapping.MappingSuggestion;

import java.util.List;
import java.util.Map;

/**
 * Discovery service responsible for auto-detecting remote listing formats, inferring mappings, and producing preview
 * results for OpenECPDS UI.
 *
 * <p>
 * This service is the entry point for "auto-detection + preview" workflow:
 * </p>
 *
 * <ol>
 * <li>Analyze sample content</li>
 * <li>Detect format (CSV / JSON / STAC / XML / FTP / S3)</li>
 * <li>Infer field mapping suggestions</li>
 * <li>Build ParserConfig</li>
 * <li>Execute parser via RemoteListingEngine</li>
 * <li>Return preview result for UI validation</li>
 * </ol>
 */
public class RemoteListingDiscoveryService {

    private final RemoteListingEngine engine;

    private final FieldMappingEngine mappingEngine = new FieldMappingEngine();

    private final FormatDetector detector = new DefaultFormatDetector();

    public RemoteListingDiscoveryService(RemoteListingEngine engine) {
        this.engine = engine;
    }

    /**
     * Main discovery entry point.
     *
     * @param sampleContent
     *            partial content (first KB of remote listing)
     */
    public ParserPreview discover(String sampleContent) throws Exception {

        // ------------------------------------------------------------
        // 1. Detect format
        // ------------------------------------------------------------
        DetectionResult detection = detector.detect(sampleContent);

        String parserType = detection.getParserType();

        // ------------------------------------------------------------
        // 2. Infer mapping configuration
        // ------------------------------------------------------------
        MappingSuggestion suggestion = mappingEngine.infer(parserType, sampleContent);

        Map<String, String> configMap = suggestion.getSuggestedConfig();

        ParserConfig config = new ParserConfig(parserType, configMap);

        // ------------------------------------------------------------
        // 3. Run parser to generate preview
        // ------------------------------------------------------------
        FtpParser.FileEntry[] entries;

        try {
            entries = engine.process(config, sampleContent);
        } catch (Exception e) {
            entries = new FtpParser.FileEntry[0];
        }

        // Limit preview size (UI-friendly)
        FtpParser.FileEntry[] preview = limit(entries, 10);

        // ------------------------------------------------------------
        // 4. Build response
        // ------------------------------------------------------------
        ParserPreview result = new ParserPreview();

        result.setParserType(parserType);
        result.setConfidence(detection.getConfidence());
        result.setSuggestedConfig(configMap);
        result.setPreviewEntries(preview);
        result.setWarnings(buildWarnings(suggestion, entries));

        return result;
    }

    // ------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------

    private FtpParser.FileEntry[] limit(FtpParser.FileEntry[] entries, int max) {

        if (entries == null || entries.length <= max) {
            return entries;
        }

        FtpParser.FileEntry[] out = new FtpParser.FileEntry[max];

        System.arraycopy(entries, 0, out, 0, max);

        return out;
    }

    private List<String> buildWarnings(MappingSuggestion suggestion, FtpParser.FileEntry[] entries) {

        if (entries == null || entries.length == 0) {
            return List.of("No entries extracted - check mapping configuration");
        }

        if (suggestion.getConfidence() < 0.5) {
            return List.of("Low confidence detection - manual review recommended");
        }

        return List.of();
    }
}