package ecmwf.common.parser.core;

import ecmwf.common.ftp.FtpParser;

public interface RemoteListingParser {

    String name();

    boolean supports(String hint);

    FtpParser.FileEntry[] parse(String input, ParserConfig config) throws Exception;
}