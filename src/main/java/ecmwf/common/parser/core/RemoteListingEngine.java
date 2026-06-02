package ecmwf.common.parser.core;

import ecmwf.common.ftp.FtpParser;
import ecmwf.common.parser.parsers.CsvListingParser;
import ecmwf.common.parser.parsers.FtpListingParser;
import ecmwf.common.parser.parsers.JsonListingParser;
import ecmwf.common.parser.parsers.S3InventoryParser;
import ecmwf.common.parser.parsers.StacListingParser;
import ecmwf.common.parser.parsers.XmlListingParser;

/**
 * Generic remote listing ingestion engine.
 *
 * <p>
 * This engine provides a unified framework for parsing and normalizing heterogeneous remote catalog/listing formats
 * into a common {@link FtpParser.FileEntry} model.
 * </p>
 *
 * <p>
 * The goal of this framework is to allow OpenECPDS users to dynamically configure remote listing integrations through
 * the UI without requiring custom parser development or software recompilation.
 * </p>
 *
 * <h2>Supported formats</h2>
 *
 * <ul>
 * <li>FTP directory listings (UNIX, Windows, etc.)</li>
 * <li>CSV feeds</li>
 * <li>Generic JSON feeds</li>
 * <li>STAC catalogs (SpatioTemporal Asset Catalog)</li>
 * <li>XML feeds</li>
 * <li>S3 inventory listings</li>
 * </ul>
 *
 * <h2>Architecture</h2>
 *
 * <p>
 * The engine delegates the parsing work to specialized {@link RemoteListingParser} implementations registered in the
 * {@link RemoteListingParserRegistry}.
 * </p>
 *
 * <p>
 * Each parser is responsible for:
 * </p>
 *
 * <ul>
 * <li>understanding a specific remote format</li>
 * <li>extracting file metadata</li>
 * <li>mapping extracted values into {@link FtpParser.FileEntry}</li>
 * </ul>
 *
 * <p>
 * The extraction logic is driven at runtime through the {@link ParserConfig} configuration object, allowing users to
 * customize:
 * </p>
 *
 * <ul>
 * <li>field mappings</li>
 * <li>CSV delimiters</li>
 * <li>JSON paths</li>
 * <li>XML node mappings</li>
 * <li>FTP parser settings</li>
 * <li>STAC asset selection</li>
 * </ul>
 *
 * <h2>Example usage</h2>
 *
 * <pre>{@code
 * ParserConfig config = new ParserConfig("csv",
 *         Map.of("delimiter", ",", "header", "false", "nameCol", "0", "timeCol", "2", "urlCol", "4"));
 *
 * String content = remoteConnection.download(url);
 *
 * FtpParser.FileEntry[] entries = new RemoteListingEngine().process(config, content);
 * }</pre>
 *
 * <h2>Typical OpenECPDS workflow</h2>
 *
 * <ol>
 * <li>User configures remote parser type and mapping options in the UI</li>
 * <li>Configuration is stored in the OpenECPDS database</li>
 * <li>Remote content is downloaded dynamically at runtime</li>
 * <li>The engine selects the appropriate parser</li>
 * <li>The parser extracts and normalizes the entries</li>
 * <li>The resulting {@link FtpParser.FileEntry} objects are used by OpenECPDS workflows</li>
 * </ol>
 *
 * <h2>Extensibility</h2>
 *
 * <p>
 * New remote formats can easily be supported by implementing the {@link RemoteListingParser} interface and registering
 * the parser in the registry.
 * </p>
 *
 * <h2>Thread safety</h2>
 *
 * <p>
 * The engine itself is stateless and thread-safe assuming parser implementations are also thread-safe.
 * </p>
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF
 *
 * @since 8.0.0
 */
public class RemoteListingEngine {
    private static final RemoteListingParserRegistry registry = new RemoteListingParserRegistry();

    static {
        registry.register(new FtpListingParser());
        registry.register(new CsvListingParser());
        registry.register(new JsonListingParser());
        registry.register(new XmlListingParser());
        registry.register(new StacListingParser());
        registry.register(new S3InventoryParser());
    }

    public FtpParser.FileEntry[] process(ParserConfig config, String input) throws Exception {
        RemoteListingParser parser = registry.find(config.getType());
        return parser.parse(input, config);
    }
}