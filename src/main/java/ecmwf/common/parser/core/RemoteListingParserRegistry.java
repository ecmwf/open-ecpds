package ecmwf.common.parser.core;

import java.util.ArrayList;
import java.util.List;

public class RemoteListingParserRegistry {

    private final List<RemoteListingParser> parsers = new ArrayList<>();

    public void register(RemoteListingParser parser) {
        parsers.add(parser);
    }

    public RemoteListingParser find(String type) {

        return parsers.stream().filter(p -> p.supports(type)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No parser registered for type: " + type));
    }

    public List<RemoteListingParser> getAll() {
        return parsers;
    }
}