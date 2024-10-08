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

package ecmwf.ecpds.master.plugin.http.model.datafile;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * Every one of the files that is going to be transfered by the dissemination
 * system. This is the unit for transfers, so far we know nothing about
 * products.
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.Collection;
import java.util.Date;

import ecmwf.ecpds.master.plugin.http.model.CollectionSizeBean;
import ecmwf.ecpds.master.plugin.http.model.transfer.DataTransfer;
import ecmwf.web.model.ModelBean;
import ecmwf.web.model.users.User;

/**
 * The Interface DataFile.
 */
public interface DataFile extends ModelBean, CollectionSizeBean {

    /**
     * Gets the arrived time.
     *
     * @return the arrived time
     */
    Date getArrivedTime();

    /**
     * Gets the product time.
     *
     * @return the product time
     */
    Date getProductTime();

    /**
     * Gets the product generation time.
     *
     * @return the product generation time
     */
    Date getProductGenerationTime();

    /**
     * Gets the checksum.
     *
     * @return the checksum
     */
    String getChecksum();

    /**
     * Gets the index.
     *
     * @return the index
     */
    int getIndex();

    /**
     * Gets the caller.
     *
     * @return the caller
     */
    String getCaller();

    /**
     * Gets the formatted caller.
     *
     * @return the formatted caller
     */
    String getFormattedCaller();

    /**
     * Gets the remote host.
     *
     * @return the remote host
     */
    String getRemoteHost();

    /**
     * Gets the gets the host.
     *
     * @return the gets the host
     */
    String getGetHost();

    /**
     * Gets the gets the complete duration.
     *
     * @return the gets the complete duration
     */
    String getGetCompleteDuration();

    /**
     * Gets the gets the duration.
     *
     * @return the gets the duration
     */
    String getGetDuration();

    /**
     * Gets the gets the protocol overhead.
     *
     * @return the gets the protocol overhead
     */
    String getGetProtocolOverhead();

    /**
     * Gets the ecauth host.
     *
     * @return the ecauth host
     */
    String getEcauthHost();

    /**
     * Gets the ecauth user.
     *
     * @return the ecauth user
     */
    String getEcauthUser();

    /**
     * Gets the storage path.
     *
     * @return the storage path
     */
    String getStoragePath();

    /**
     * Gets the original.
     *
     * @return the original
     */
    String getOriginal();

    /**
     * Gets the formatted original.
     *
     * @return the formatted original
     */
    String getFormattedOriginal();

    /**
     * Gets the source.
     *
     * @return the source
     */
    String getSource();

    /**
     * Gets the size.
     *
     * @return the size
     */
    long getSize();

    /**
     * Gets the formatted size.
     *
     * @return the formatted size
     */
    String getFormattedSize();

    /**
     * Gets the time step.
     *
     * @return the time step
     */
    long getTimeStep();

    /**
     * Gets the delete original.
     *
     * @return the delete original
     */
    boolean getDeleteOriginal();

    /**
     * Gets the deleted.
     *
     * @return the deleted
     */
    boolean getDeleted();

    /**
     * Gets the removed.
     *
     * @return the removed
     */
    boolean getRemoved();

    /**
     * Gets the meta data.
     *
     * @return the meta data
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.datafile.DataFileException
     *             the data file exception
     */
    Collection<MetaData> getMetaData() throws DataFileException;

    /**
     * Gets the data transfers.
     *
     * @return the data transfers
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.datafile.DataFileException
     *             the data file exception
     */
    Collection<DataTransfer> getDataTransfers() throws DataFileException;

    /**
     * Gets the meta time.
     *
     * @return The time of the product. Eg "00", "12", etc
     */
    String getMetaTime();

    /**
     * Gets the meta stream.
     *
     * @return The stream (data stream) of the product. Eg "OPER", "WAVE", etc
     */
    String getMetaStream();

    /**
     * Gets the meta target.
     *
     * @return The target (dissemination stream) of the product. Eg "IT1", "IT2", etc
     */
    String getMetaTarget();

    /**
     * Gets the meta type.
     *
     * @return The type of the product. Eg "G" for Global, "M" for Mediterranean
     */
    String getMetaType();

    // This are the DATABASE values. To force a calculation, use
    // ArrivalMonitoringParameters interface (extended by DataTransfer)

    /**
     * Gets the earliest time.
     *
     * @return the earliest time
     */
    Date getEarliestTime();

    /**
     * Gets the latest time.
     *
     * @return the latest time
     */
    Date getLatestTime();

    /**
     * Gets the predicted time.
     *
     * @return the predicted time
     */
    Date getPredictedTime();

    /**
     * Sets the user.
     *
     * @param user
     *            the new user
     */
    void setUser(User user);
}
