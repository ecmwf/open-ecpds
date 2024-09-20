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

/**
 * ECMWF Product Data Store (OpenPDS) Project.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 *
 * @version 6.7.7
 *
 * @since 2024-07-01
 */

public final class HostOption {
    /** The Constant networkName. */
    public static final String[] networkName = { "Internet", "RMDCN", "Leased Line", "LAN", "ECaccess" };

    /** The Constant networkCode. */
    public static final String[] networkCode = { "I", "R", "LL", "L", "E" };

    /** The Constant type. */
    public static final String[] type = { "Dissemination", "Acquisition", "Replication", "Source", "Backup", "Proxy" };

    /** The Constant DISSEMINATION. */
    public static final String DISSEMINATION = "Dissemination";

    /** The Constant ACQUISITION. */
    public static final String ACQUISITION = "Acquisition";

    /** The Constant REPLICATION. */
    public static final String REPLICATION = "Replication";

    /** The Constant SOURCE. */
    public static final String SOURCE = "Source";

    /** The Constant BACKUP. */
    public static final String BACKUP = "Backup";

    /** The Constant PROXY. */
    public static final String PROXY = "Proxy";

    /**
     * Get the label for a type.
     *
     * @param type
     *            the type
     *
     * @return the label for the type
     */
    public static String getLabel(final String type) {
        for (var i = 0; i < networkName.length; i++) {
            if (networkCode[i].equals(type)) {
                return networkName[i];
            }
        }
        return "Unknown";
    }
}
