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

package ecmwf.common.version;

/**
 * ECMWF Product Data Store (OpenECPDS) Project.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 *
 * @version 6.7.7
 *
 * @since 2024-07-01
 */
public final class Version {
    /** The Constant version. */
    private static final String VERSION = "6.7.7";

    /** The Constant build (ddmmyyyy). */
    private static final String BUILD = "14052024";

    /**
     * Gets the version.
     *
     * @return the version
     */
    public static String getVersion() {
        return VERSION;
    }

    /**
     * Gets the builds the.
     *
     * @return the builds the
     */
    public static String getBuild() {
        return BUILD;
    }

    /**
     * Gets the full version.
     *
     * @return the full version
     */
    public static String getFullVersion() {
        return VERSION + "-" + BUILD;
    }

    /**
     * Gets the version number.
     *
     * @param version
     *            the version
     *
     * @return the version number
     */
    public static int getVersionNumber(final String version) {
        try {
            return Integer.parseInt(version.substring(0, 1)) * 100 + Integer.parseInt(version.substring(2, 3)) * 10
                    + Integer.parseInt(version.substring(4, 5));
        } catch (final NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Print the full version on standard output.
     *
     * @param args
     *            the arguments
     */
    public static void main(final String[] args) {
        System.out.println(getFullVersion());
    }
}
