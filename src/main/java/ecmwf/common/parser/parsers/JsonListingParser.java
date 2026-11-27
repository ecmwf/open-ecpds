/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * In applying the License, ECMWF does not waive the privileges and immunities
 * granted to it by virtue of its status as an inter-governmental organization
 * nor does it submit to any jurisdiction.
 */

package ecmwf.common.parser.parsers;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ecmwf.common.ftp.FtpParser;
import ecmwf.common.parser.core.ParserConfig;
import ecmwf.common.parser.core.RemoteListingParser;
import ecmwf.common.parser.format.JsonPath;
import ecmwf.common.parser.format.ListingMetadata;

public class JsonListingParser implements RemoteListingParser {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String name() {
        return "json";
    }

    @Override
    public boolean supports(String hint) {
        return "json".equalsIgnoreCase(hint);
    }

    @Override
    public FtpParser.FileEntry[] parse(String input, ParserConfig config) throws Exception {

        ListingMetadata metadata = new ListingMetadata(config);
        JsonNode root = mapper.readTree(input);

        String arrayPath = config.getOrDefault("arrayPath", "items");

        JsonNode array = JsonPath.get(root, arrayPath);

        if (array == null || !array.isArray()) {
            return new FtpParser.FileEntry[0];
        }

        String nameField = config.getOrDefault("nameField", "name");

        String urlField = config.getOrDefault("urlField", "url");

        String timeField = config.getOrDefault("timeField", "time");
        String sizeField = config.getOrDefault("sizeField", "size");

        List<FtpParser.FileEntry> out = new ArrayList<>();

        for (JsonNode n : array) {

            FtpParser.FileEntry e = new FtpParser.FileEntry();

            e.name = JsonPath.text(n, nameField);

            e.link = JsonPath.text(n, urlField);

            e.line = n.toString();

            e.time = ListingMetadata.time(JsonPath.text(n, timeField));
            e.size = metadata.size(JsonPath.text(n, sizeField));

            out.add(e);
        }

        return out.toArray(new FtpParser.FileEntry[0]);
    }
}