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

import org.apache.commons.csv.CSVParser;

import ecmwf.common.ftp.FtpParser;
import ecmwf.common.parser.core.ParserConfig;
import ecmwf.common.parser.core.RemoteListingParser;
import ecmwf.common.parser.format.CsvHelper;
import ecmwf.common.parser.format.ListingMetadata;

public class CsvListingParser implements RemoteListingParser {

    @Override
    public String name() {
        return "csv";
    }

    @Override
    public boolean supports(String hint) {

        return "csv".equalsIgnoreCase(hint) || "cmr".equalsIgnoreCase(hint) || "nasa".equalsIgnoreCase(hint);
    }

    @Override
    public FtpParser.FileEntry[] parse(String input, ParserConfig config) throws Exception {

        String nameCol = config.getOrDefault("nameCol", "0");
        String timeCol = config.getOrDefault("timeCol", "-1");
        String urlCol = config.getOrDefault("urlCol", "-1");
        String sizeCol = config.get("sizeCol");
        ListingMetadata metadata = new ListingMetadata(config);

        List<FtpParser.FileEntry> out = new ArrayList<>();

        try (CSVParser parser = CsvHelper.parse(input, config)) {
            for (var r : parser) {

                FtpParser.FileEntry e = new FtpParser.FileEntry();

                e.name = CsvHelper.value(r, nameCol);
                e.link = CsvHelper.value(r, urlCol);
                e.line = r.toString();
                e.size = metadata.size(CsvHelper.value(r, sizeCol));
                e.time = ListingMetadata.time(CsvHelper.value(r, timeCol));

                out.add(e);
            }
        }

        return out.toArray(new FtpParser.FileEntry[0]);
    }

}