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
package ecmwf.common.database;

import java.util.UUID;

/** Existing ECPDS version identity, usable on both Masters and Movers without server initialization. */
public final class DataFileVersion {
    private DataFileVersion() {
    }

    public static String etag(final DataFile file) {
        final var instance = file.getFileInstance();
        return new UUID(file.getId(), instance == null ? 0 : instance).toString();
    }
}
