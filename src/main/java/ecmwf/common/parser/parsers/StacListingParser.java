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

import java.util.HashMap;
import java.util.Map;

import ecmwf.common.ftp.FtpParser;
import ecmwf.common.parser.core.ParserConfig;

public class StacListingParser extends JsonListingParser {

    @Override
    public String name() {
        return "stac";
    }

    @Override
    public boolean supports(String hint) {

        return "stac".equalsIgnoreCase(hint) || (hint != null && hint.contains("geojson"));
    }

    @Override
    public FtpParser.FileEntry[] parse(String input, ParserConfig config) throws Exception {
        Map<String, String> options = new HashMap<>(Map.of("arrayPath", "features", "nameField", "id", "urlField",
                "assets.data.href", "timeField", "properties.start_datetime"));
        options.putAll(config.getProperties());
        String urlField = options.get("urlField");
        String assetPath = urlField.endsWith(".href") ? urlField.substring(0, urlField.length() - 5) : "assets.data";
        options.putIfAbsent("sizeField", assetPath + ".file:size");
        return super.parse(input, new ParserConfig(config.getType(), options));
    }
}