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