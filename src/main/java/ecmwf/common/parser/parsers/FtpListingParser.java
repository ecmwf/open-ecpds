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