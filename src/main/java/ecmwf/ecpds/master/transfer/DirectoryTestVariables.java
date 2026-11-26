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

package ecmwf.ecpds.master.transfer;

import java.util.Collection;
import java.util.Date;

import ecmwf.common.database.MetadataValue;
import ecmwf.common.text.Format;

/** Transfer-specific substitutions used by directory tests. */
public final class DirectoryTestVariables {

    private DirectoryTestVariables() {
    }

    public static String timestamp(final Date value) {
        return value != null ? Long.toString(value.getTime()) : "";
    }

    public static void resolveMetadata(final StringBuilder text, final Collection<MetadataValue> values) {
        for (final var value : values) {
            Format.replaceAll(text, "$metadata[" + value.getMetadataAttributeName() + "]", value.getValue());
        }
        int index;
        while ((index = text.indexOf("$metadata[")) != -1) {
            final var end = text.indexOf("]", index + 10);
            if (end == -1) {
                throw new IllegalArgumentException("Malformed metadata in directory");
            }
            text.delete(index, end + 1);
        }
    }
}
