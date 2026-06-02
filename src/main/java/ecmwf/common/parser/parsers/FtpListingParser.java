package ecmwf.common.parser.parsers;

import ecmwf.common.ftp.FtpParser;
import ecmwf.common.parser.core.ParserConfig;
import ecmwf.common.parser.core.RemoteListingParser;

public class FtpListingParser implements RemoteListingParser {

    @Override
    public String name() {
        return "ftp";
    }

    @Override
    public boolean supports(String hint) {

        return "ftp".equalsIgnoreCase(hint) || "unix".equalsIgnoreCase(hint) || "windows".equalsIgnoreCase(hint);
    }

    @Override
    public FtpParser.FileEntry[] parse(String input, ParserConfig config) throws Exception {

        String system = config.getOrDefault("system", "UNIX");

        String regex = config.get("regex");

        String[] lines = input.split("\n");

        return FtpParser.parseDir(regex, system, null, null, "en", null, null, lines);
    }
}