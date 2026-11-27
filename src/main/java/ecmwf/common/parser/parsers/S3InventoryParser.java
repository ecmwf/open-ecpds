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

public class S3InventoryParser implements RemoteListingParser {

    @Override
    public String name() {
        return "s3";
    }

    @Override
    public boolean supports(String hint) {
        return hint != null
                && (hint.equalsIgnoreCase("s3") || hint.contains("amazonaws") || hint.contains("s3-inventory"));
    }

    @Override
    public FtpParser.FileEntry[] parse(String input, ParserConfig config) throws Exception {

        ListingMetadata metadata = new ListingMetadata(config);

        // Column mapping (configurable, but safe defaults)
        String keyCol = config.getOrDefault("keyCol", "key");
        String sizeCol = config.getOrDefault("sizeCol", "size");
        String timeCol = config.getOrDefault("timeCol", "last_modified");
        String bucketCol = config.getOrDefault("bucketCol", "bucket");

        // Optional base URL for HTTP access
        String baseUrl = config.getOrDefault("baseUrl", null);

        List<FtpParser.FileEntry> out = new ArrayList<>();

        try (CSVParser parser = CsvHelper.parse(input, config)) {
            for (var r : parser) {

                FtpParser.FileEntry e = new FtpParser.FileEntry();

                // --- NAME (S3 object key) ---
                e.name = CsvHelper.value(r, keyCol);

                // --- SIZE ---
                e.size = metadata.size(CsvHelper.value(r, sizeCol));

                // --- TIME ---
                e.time = ListingMetadata.time(CsvHelper.value(r, timeCol));

                // --- LINK (derived) ---
                String bucket = CsvHelper.value(r, bucketCol);

                if (baseUrl != null && e.name != null) {
                    e.link = baseUrl + "/" + e.name;
                } else if (bucket != null && e.name != null) {
                    e.link = "s3://" + bucket + "/" + e.name;
                } else if (e.name != null) {
                    e.link = "s3://" + e.name;
                }
                e.line = r.toString();
                out.add(e);
            }
        }

        return out.toArray(new FtpParser.FileEntry[0]);
    }

}