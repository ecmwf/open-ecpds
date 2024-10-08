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

package ecmwf.ecpds.master.plugin.http.dao.transfer;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import static ecmwf.ecpds.master.DataFilePath.getPath;

import java.util.Collection;
import java.util.Date;

import ecmwf.common.database.DataBaseObject;
import ecmwf.common.text.Format;
import ecmwf.ecpds.master.plugin.http.dao.OjbImplementedBean;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.ecpds.master.plugin.http.home.datafile.MetaDataHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.DataTransferHome;
import ecmwf.ecpds.master.plugin.http.model.datafile.DataFile;
import ecmwf.ecpds.master.plugin.http.model.datafile.DataFileException;
import ecmwf.ecpds.master.plugin.http.model.datafile.MetaData;
import ecmwf.ecpds.master.plugin.http.model.transfer.DataTransfer;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferException;
import ecmwf.web.dao.ModelBeanBase;
import ecmwf.web.model.users.User;

/**
 * The Class DataFileBean.
 */
public class DataFileBean extends ModelBeanBase implements DataFile, OjbImplementedBean {

    /** The datafile. */
    private final ecmwf.common.database.DataFile datafile;

    /** The user. */
    private User user;

    /**
     * Instantiates a new data file bean.
     *
     * @param datafile
     *            the datafile
     */
    protected DataFileBean(final ecmwf.common.database.DataFile datafile) {
        this.datafile = datafile;
    }

    /**
     * {@inheritDoc}
     *
     * Sets the user.
     */
    @Override
    public void setUser(final User user) {
        this.user = user;
    }

    /**
     * Gets the user.
     *
     * @return the user
     */
    public User getUser() {
        return user;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the index.
     */
    @Override
    public int getIndex() {
        return datafile.getIndex();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the caller.
     */
    @Override
    public String getCaller() {
        return datafile.getCaller();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the formatted caller.
     */
    @Override
    public String getFormattedCaller() {
        return Util.getFormatted(user, getCaller());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the remote host.
     */
    @Override
    public String getRemoteHost() {
        return datafile.getRemoteHost();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the gets the host.
     */
    @Override
    public String getGetHost() {
        return datafile.getGetHost();
    }

    /**
     * Gets the group by.
     *
     * @return the group by
     */
    public String getGroupBy() {
        return datafile.getGroupBy();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the checksum.
     */
    @SuppressWarnings("null")
    @Override
    public String getChecksum() {
        final var checksum = datafile.getChecksum();
        // Let's check if the Checksum is in the form xxxxxxxx/lbzip2=yyyyyyyy?
        final var index = checksum != null ? checksum.indexOf("/") : -1;
        return index == -1 ? checksum : "plain=" + checksum.substring(0, index) + " " + checksum.substring(index + 1);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the gets the complete duration.
     */
    @Override
    public String getGetCompleteDuration() {
        return Format.formatDuration(datafile.getGetCompleteDuration());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the gets the duration.
     */
    @Override
    public String getGetDuration() {
        return Format.formatDuration(datafile.getGetDuration());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the gets the protocol overhead.
     */
    @Override
    public String getGetProtocolOverhead() {
        return Format.formatDuration(datafile.getGetCompleteDuration() - datafile.getGetDuration());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the ecauth host.
     */
    @Override
    public String getEcauthHost() {
        return datafile.getEcauthHost();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the ecauth user.
     */
    @Override
    public String getEcauthUser() {
        return datafile.getEcauthUser();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the bean interface name.
     */
    @Override
    public String getBeanInterfaceName() {
        return DataFile.class.getName();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the ojb implementation.
     */
    @Override
    public DataBaseObject getOjbImplementation() {
        return datafile;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the id.
     */
    @Override
    public String getId() {
        return Long.toString(datafile.getId());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the arrived time.
     */
    @Override
    public Date getArrivedTime() {
        return datafile.getArrivedTime();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the product time.
     */
    @Override
    public Date getProductTime() {
        return datafile.getTimeBase();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the storage path.
     */
    @Override
    public String getStoragePath() {
        return getPath(datafile);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the product generation time.
     */
    @Override
    public Date getProductGenerationTime() {
        return datafile.getTimeFile();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the earliest time.
     */
    @Override
    public Date getEarliestTime() {
        final var monitoringValue = datafile.getMonitoringValue();
        return monitoringValue != null ? monitoringValue.getEarliestTime() : null;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the latest time.
     */
    @Override
    public Date getLatestTime() {
        final var monitoringValue = datafile.getMonitoringValue();
        return monitoringValue != null ? monitoringValue.getLatestTime() : null;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the predicted time.
     */
    @Override
    public Date getPredictedTime() {
        final var monitoringValue = datafile.getMonitoringValue();
        return monitoringValue != null ? monitoringValue.getPredictedTime() : null;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the collection size.
     */
    @Override
    public int getCollectionSize() {
        return datafile.getCollectionSize();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the deleted.
     */
    @Override
    public boolean getDeleted() {
        return datafile.getDeleted();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the delete original.
     */
    @Override
    public boolean getDeleteOriginal() {
        return datafile.getDeleteOriginal();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the removed.
     */
    @Override
    public boolean getRemoved() {
        return datafile.getRemoved();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the original.
     */
    @Override
    public String getOriginal() {
        return datafile.getOriginal();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the formatted original.
     */
    @Override
    public String getFormattedOriginal() {
        return Util.getFormatted(user, datafile.getOriginal());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the size.
     */
    @Override
    public long getSize() {
        return datafile.getSize();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the formatted size.
     */
    @Override
    public String getFormattedSize() {
        return Format.formatSize(getSize());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the source.
     */
    @Override
    public String getSource() {
        return datafile.getSource();
    }

    /**
     * Gets the time.
     *
     * @return the time
     */
    public Date getTime() {
        return datafile.getArrivedTime();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the time step.
     */
    @Override
    public long getTimeStep() {
        return datafile.getTimeStep();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the meta data.
     */
    @Override
    public Collection<MetaData> getMetaData() throws DataFileException {
        return MetaDataHome.findByDataFile(this);
    }

    /**
     * Sets the removed.
     *
     * @param b
     *            the new removed
     */
    public void setRemoved(final boolean b) {
        datafile.setRemoved(b);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the data transfers.
     */
    @Override
    public Collection<DataTransfer> getDataTransfers() throws DataFileException {
        try {
            return DataTransferHome.findByDataFile(this, true);
        } catch (final TransferException e) {
            throw new DataFileException("Error getting Data Transfers for Data File '" + getId() + "'");
        }
    }

    /**
     * {@inheritDoc}
     *
     * Gets the meta time.
     */
    @Override
    public String getMetaTime() {
        return datafile.getMetaTime();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the meta stream.
     */
    @Override
    public String getMetaStream() {
        return datafile.getMetaStream();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the meta target.
     */
    @Override
    public String getMetaTarget() {
        return datafile.getMetaTarget();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the meta type.
     */
    @Override
    public String getMetaType() {
        return datafile.getMetaType();
    }

    /**
     * {@inheritDoc}
     *
     * Equals.
     */
    @Override
    public boolean equals(final Object o) {
        return o instanceof final DataFileBean dataFileBean && equals(dataFileBean);
    }

    /**
     * Equals.
     *
     * @param d
     *            the d
     *
     * @return true, if successful
     */
    public boolean equals(final DataFileBean d) {
        return getId().equals(d.getId());
    }

    /**
     * {@inheritDoc}
     *
     * Hash code.
     */
    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    /**
     * {@inheritDoc}
     *
     * To string.
     */
    @Override
    public String toString() {
        return getClass().getName() + " { " + datafile + " }";
    }
}
