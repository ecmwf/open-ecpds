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
 * ECMWF Product Data Store (OpenECPDS) Project.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 *
 * @version 6.7.7
 *
 * @since 2024-07-01
 */
public interface ProgressInterface {
    /**
     * Gets the data file id.
     *
     * @return the data file id
     */
    long getDataFileId();

    /**
     * Gets the root.
     *
     * @return the root
     */
    String getRoot();

    /**
     * Gets the byte sent.
     *
     * @return the byte sent
     */
    long getByteSent();

    /**
     * Update progress.
     *
     * @param root
     *            the root
     * @param byteSent
     *            the byte sent
     */
    void update(final String root, final long byteSent);

    /**
     * Gets the name of the Destination this retrieval is for, if applicable. Used by the "Live ECPDS Earth" globe
     * visualisation to label/attribute Acquisition retrieval samples; not every implementation is tied to a
     * Destination.
     *
     * @return the destination name, or {@code null} if not applicable/known
     */
    default String getDestinationName() {
        return null;
    }

    /**
     * Gets the source Host used for this retrieval (e.g. the Acquisition Host it is being pulled from), if applicable.
     * Used by the "Live ECPDS Earth" globe visualisation to geolocate/label Acquisition retrieval samples.
     *
     * @return the source host, or {@code null} if not applicable/known
     */
    default ecmwf.common.database.Host getSourceHost() {
        return null;
    }

    /**
     * Whether this retrieval is a genuine Acquisition (i.e. from an Acquisition Host), as opposed to e.g. a
     * Dissemination-side backup/source pull which is not shown as an Acquisition on the "Live ECPDS Earth" globe.
     *
     * @return true, if this is an Acquisition retrieval
     */
    default boolean isAcquisition() {
        return false;
    }

    /**
     * Gets the total duration, in milliseconds, since this retrieval started.
     *
     * @return the duration, or {@code 0} if not applicable/known
     */
    default long getDuration() {
        return 0;
    }

    /**
     * Gets the total size, in bytes, of the file being retrieved.
     *
     * @return the file size, or {@code -1} if not applicable/known
     */
    default long getFileSize() {
        return -1;
    }
}
