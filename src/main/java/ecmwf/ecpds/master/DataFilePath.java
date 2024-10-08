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

package ecmwf.ecpds.master;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.database.DataFile;
import ecmwf.common.text.Format;

/**
 * The Class DataFilePath.
 */
public class DataFilePath {

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(DataFilePath.class);

    /**
     * Gets the path.
     *
     * @param dataFile
     *            the data file
     *
     * @return the path
     */
    public static String getPath(final DataFile dataFile) {
        return getPath(dataFile, dataFile.getFileInstance());
    }

    /**
     * Build the full path name of the DataFile on the storage.
     *
     * @param dataFile
     *            the data file
     * @param instance
     *            the instance
     *
     * @return the path
     */
    public static String getPath(final DataFile dataFile, final Integer instance) {
        try {
            // If the original name is more than 245 + 10 then we use "data" instead as this
            // is a filesystem limitation (not more than 255)!
            final var originalName = new File(dataFile.getOriginal()).getName();
            return getDir(dataFile.getFileSystem(), dataFile.getArrivedTime().getTime()) + File.separator
                    + Format.formatValue(dataFile.getTimeStep(), 10) + File.separator
                    + Format.formatValue(dataFile.getId(), 10) + (originalName.length() > 245 ? "data" : originalName)
                    + (instance != null ? "_" + instance.intValue() : "");
        } catch (final NullPointerException e) {
            _log.warn("Cannot process path for DataFile: {}", dataFile);
            throw e;
        }
    }

    /**
     * Gets the data file id from the target name or -1 if it can't find it.
     *
     * @param target
     *            the target name
     *
     * @return the data file id
     */
    public static long getDataFileId(final String target) {
        try {
            final var separator = target.lastIndexOf("/");
            return Long.parseLong(target.substring(separator + 1, separator + 11));
        } catch (final Throwable t) {
            return -1;
        }
    }

    /**
     * Build the path to access a DataFile on the storage.
     *
     * @param fileSystem
     *            the file system
     * @param arrivedTime
     *            the arrived time
     *
     * @return the dir
     */
    public static String getDir(final int fileSystem, final long arrivedTime) {
        return "volume" + fileSystem + File.separator + Format.formatTime("MMddyyyy", arrivedTime);
    }
}
