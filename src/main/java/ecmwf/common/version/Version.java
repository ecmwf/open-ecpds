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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;

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
    private static final String VERSION_NUMBER;

    /** The Constant build (ddmmyyyy). */
    private static final String BUILD_NUMBER;

    static {
        // Attempt to read the version and build numbers
        final String[] versionInfo = readVersionInfo("VERSION");
        if (versionInfo.length != 2) {
            VERSION_NUMBER = "1.0.0";
            BUILD_NUMBER = LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("ddMMyyyy"));
        } else {
            VERSION_NUMBER = versionInfo[0];
            BUILD_NUMBER = versionInfo[1];
        }
    }

    /**
     * Attempts to read the version information from the specified resource. First checks the classpath, then falls back
     * to the current directory.
     *
     * @param resourceName
     *            the name of the resource file to read from
     *
     * @return an array containing the version and build numbers, or an empty array if not found
     */
    private static String[] readVersionInfo(String resourceName) {
        // Try to read from the classpath
        try (var inputStream = Version.class.getResourceAsStream("/" + resourceName)) {
            if (inputStream != null) {
                try (var reader = new BufferedReader(new InputStreamReader(inputStream))) {
                    return reader.readLine().split("-");
                }
            }
        } catch (IOException e) {
            // Ignored
        }
        // If not found, try to read from the current directory
        try (var fileReader = new BufferedReader(new FileReader(resourceName))) {
            return fileReader.readLine().split("-");
        } catch (IOException e) {
            // Ignored
        }
        // Return an empty array if neither source was successful
        return new String[0];
    }

    /**
     * Gets the version.
     *
     * @return the version
     */
    public static String getVersion() {
        return VERSION_NUMBER;
    }

    /**
     * Gets the build number.
     *
     * @return the build number
     */
    public static String getBuild() {
        return BUILD_NUMBER;
    }

    /**
     * Gets the full version.
     *
     * @return the full version
     */
    public static String getFullVersion() {
        return VERSION_NUMBER + "-" + BUILD_NUMBER;
    }

    /**
     * Gets the version number.
     *
     * @param version
     *            the version
     *
     * @return the version number
     */
    public static int getVersionNumber(String version) {
        try {
            return Integer.parseInt(version.substring(0, 1)) * 100 + Integer.parseInt(version.substring(2, 3)) * 10
                    + Integer.parseInt(version.substring(4, 5));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Print the full version on standard output.
     *
     * @param args
     *            the arguments
     */
    public static void main(String[] args) {
        System.out.println(getFullVersion());
    }
}
