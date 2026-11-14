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

import java.io.IOException;
import java.sql.SQLException;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import ecmwf.common.database.DBIterator;
import ecmwf.common.database.DataTransfer;
import ecmwf.common.mbean.MBeanManager;
import ecmwf.common.technical.Cnf;

/**
 * The Class DissDownloadScheduler.
 */
public final class DissDownloadScheduler extends DownloadScheduler {
    private final MasterServer _master;

    /** The _cache ratio. */
    public int _cacheRatio = Cnf.at("Scheduler", "cacheRatio", 40);

    /**
     * Instantiates a new diss download scheduler.
     *
     * @param name
     *            the name
     */
    public DissDownloadScheduler(final MasterServer _master, final String name) {
        // Bind the Master before superclass construction and field initialization.
        this._master = _master;
        super(_master, name,
                "The DissDownloadScheduler is implementing the retrieval mechanism for the Dissemination files.");
    }

    /**
     * Gets the MBean info.
     *
     * @return the MBean info
     */
    @Override
    public MBeanInfo getMBeanInfo() {
        return MBeanManager.addMBeanInfo(super.getMBeanInfo(), null, new MBeanAttributeInfo[] { new MBeanAttributeInfo(
                "CacheRatio", "java.lang.Integer",
                "CacheRatio: multiplier on MaximumDownloadThreads sizing the batch of DataTransfers pre-fetched from the database.",
                true, true, false) }, new MBeanOperationInfo[] {});
    }

    /**
     * Gets the attribute.
     *
     * @param attributeName
     *            the attribute name
     *
     * @return the attribute
     *
     * @throws AttributeNotFoundException
     *             the attribute not found exception
     * @throws MBeanException
     *             the MBean exception
     */
    @Override
    public Object getAttribute(final String attributeName) throws AttributeNotFoundException, MBeanException {
        try {
            if ("CacheRatio".equals(attributeName)) {
                return _cacheRatio;
            }
        } catch (final Exception e) {
            MasterServer._log.warn("Getting an MBean attribute", e);
            throw new MBeanException(e);
        }
        return super.getAttribute(attributeName);
    }

    /**
     * Sets the attribute.
     *
     * @param name
     *            the name
     * @param value
     *            the value
     *
     * @return true, if successful
     *
     * @throws InvalidAttributeValueException
     *             the invalid attribute value exception
     * @throws MBeanException
     *             the MBean exception
     */
    @Override
    public boolean setAttribute(final String name, final Object value)
            throws InvalidAttributeValueException, MBeanException {
        if ("CacheRatio".equals(name)) {
            _cacheRatio = (Integer) value;
            return true;
        }
        return super.setAttribute(name, value);
    }

    /**
     * Gets the data transfers to download iterator.
     *
     * @return the data transfers to download iterator
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws SQLException
     *             the SQL exception
     */
    @Override
    public DBIterator<DataTransfer> getDataTransfersToDownloadIterator() throws IOException, SQLException {
        return _master.getECpdsBase()
                .getDisseminationDataTransfersToDownloadIterator(_cacheRatio * getMaxDownloadThreads());
    }
}
