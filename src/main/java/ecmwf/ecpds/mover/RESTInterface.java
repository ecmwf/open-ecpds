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

package ecmwf.ecpds.mover;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.util.List;

import ecmwf.common.database.DataFile;
import ecmwf.common.database.DataTransfer;
import ecmwf.common.database.ExistingStorageDirectory;
import ecmwf.common.database.Host;
import ecmwf.common.ecaccess.ECauthToken;
import ecmwf.ecpds.mover.service.RESTClient.MonitorRequest;
import ecmwf.ecpds.mover.service.RESTClient.UpdateDataRequest;

/**
 * The Interface RESTInterface.
 */
public interface RESTInterface {
    /**
     * Gets the version of the remote ECaccess software (mover).
     *
     * @return the version
     *
     * @throws Exception
     *             the exception
     */
    String getVersion() throws Exception;

    /**
     * Deletes the physical file associated to the DataFile on the mover.
     *
     * @param dataFile
     *            the data file
     *
     * @throws Exception
     *             the exception
     */
    void del(DataFile dataFile) throws Exception;

    /**
     * Stops the transmission of the DataTransfer on the mover.
     *
     * @param dataTransfer
     *            the data transfer
     *
     * @throws Exception
     *             the exception
     */
    void close(DataTransfer dataTransfer) throws Exception;

    /**
     * Requests a transmission of the DataTransfer with the target name as specified in fileName. Return the name of the
     * DataMover used for the transmission.
     *
     * @param transfer
     *            the transfer
     * @param fileName
     *            the file name
     * @param localPosn
     *            the local posn
     * @param remotePosn
     *            the remote posn
     *
     * @return the string
     *
     * @throws Exception
     *             the exception
     */
    String put(DataTransfer transfer, String fileName, long localPosn, long remotePosn) throws Exception;

    /**
     * Requests an asynchronous purge of the DataFiles on the data mover which are more than the specified date.
     *
     * @param directories
     *            the directories
     *
     * @throws Exception
     *             the exception
     */
    void purge(List<ExistingStorageDirectory> directories) throws Exception;

    /**
     * Requests a report for the specified Host (e.g. traceroute, paping).
     *
     * @param host
     *            the host
     *
     * @return the host report
     *
     * @throws Exception
     *             the exception
     */
    String getHostReport(Host host) throws Exception;

    /**
     * Requests a report from the Data Mover (e.g. df, sar).
     *
     * @return the mover report
     *
     * @throws Exception
     *             the exception
     */
    String getMoverReport() throws Exception;

    /**
     * Requests a ecauth token to allow a connection to a ecauth compliant server.
     *
     * @param user
     *            the user
     *
     * @return the ecauth token
     *
     * @throws Exception
     *             the exception
     */
    ECauthToken getECauthToken(String user) throws Exception;

    /**
     * Live message sent from the ProxyHost to the Master server.
     *
     * @param name
     *            the name
     *
     * @return the long
     *
     * @throws Exception
     *             the exception
     */
    long proxyHostIsAlive(String name) throws Exception;

    /**
     * Requests an update of the data part of the Host on the master (e.g. when the data is updated by the ectrans
     * module).
     *
     * @param request
     *            the request
     *
     * @throws Exception
     *             the exception
     */
    void updateDataRequest(UpdateDataRequest request) throws Exception;

    /**
     * Requests an update of the data part of the Host on the master (e.g. when the data is updated by the ectrans
     * module).
     *
     * @param host
     *            the host
     *
     * @throws Exception
     *             the exception
     */
    void updateData(Host host) throws Exception;

    /**
     * Requests an update of the location part of the Host on the master (e.g. when the IP is updated by the ectrans
     * module).
     *
     * @param host
     *            the host
     *
     * @throws Exception
     *             the exception
     */
    void updateLocation(Host host) throws Exception;

    /**
     * Requests an update of the transfers on the master (e.g. status).
     *
     * @param transfers
     *            the transfers
     *
     * @throws Exception
     *             the exception
     */
    void updateDataTransfers(List<DataTransfer> transfers) throws Exception;

    /**
     * Sends a message to Monitor.
     *
     * @param request
     *            the request
     *
     * @throws Exception
     *             the exception
     */
    void sendMessage(MonitorRequest request) throws Exception;

    /**
     * Checks if the DataFile exists and is not expired.
     *
     * @param dataFileId
     *            the data file id
     *
     * @return true, if is valid data file
     *
     * @throws Exception
     *             the exception
     */
    boolean isValidDataFile(long dataFileId) throws Exception;
}
