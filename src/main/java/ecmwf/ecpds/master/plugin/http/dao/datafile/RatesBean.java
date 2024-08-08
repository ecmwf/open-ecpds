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

package ecmwf.ecpds.master.plugin.http.dao.datafile;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon <sy8iecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import ecmwf.common.text.Format;
import ecmwf.ecpds.master.plugin.http.model.datafile.Rates;
import ecmwf.web.dao.ModelBeanBase;

/**
 * The Class RatesBean.
 */
public class RatesBean extends ModelBeanBase implements Rates {

    /** The rates. */
    private final ecmwf.common.database.Rates rates;

    /**
     * Gets the bean interface name.
     *
     * @return the bean interface name
     */
    @Override
    public String getBeanInterfaceName() {
        return Rates.class.getName();
    }

    /**
     * Instantiates a new rates bean.
     *
     * @param rates
     *            the rates
     */
    protected RatesBean(final ecmwf.common.database.Rates rates) {
        this.rates = rates;
    }

    /**
     * Gets the date.
     *
     * @return the date
     */
    @Override
    public String getDate() {
        return rates.getDate();
    }

    /**
     * Gets the rate.
     *
     * @return the rate
     */
    @Override
    public double getRate() {
        return Format.getMBitsPerSeconds(rates.getSize(), rates.getGetDuration());
    }

    /**
     * Gets the formatted rate.
     *
     * @return the formatted rate
     */
    @Override
    public String getFormattedRate() {
        return Format.formatRate(rates.getSize(), rates.getGetDuration());
    }

    /**
     * Gets the count.
     *
     * @return the count
     */
    @Override
    public long getCount() {
        return rates.getCount();
    }

    /**
     * Gets the bytes.
     *
     * @return the bytes
     */
    @Override
    public long getBytes() {
        return rates.getSize();
    }

    /**
     * Gets the formatted bytes.
     *
     * @return the formatted bytes
     */
    @Override
    public String getFormattedBytes() {
        return Format.formatSize(rates.getSize());
    }

    /**
     * Gets the duration.
     *
     * @return the duration
     */
    @Override
    public long getDuration() {
        return rates.getGetDuration();
    }

    /**
     * Gets the formatted duration.
     *
     * @return the formatted duration
     */
    @Override
    public String getFormattedDuration() {
        return Format.formatDuration(rates.getGetDuration());
    }

    /**
     * Gets the transfer server name.
     *
     * @return the transfer server name
     */
    @Override
    public String getTransferServerName() {
        return rates.getGetHost();
    }

    /**
     * Gets the transfer group name.
     *
     * @return the transfer group name
     */
    @Override
    public String getTransferGroupName() {
        return rates.getTransferGroupName();
    }

    /**
     * Gets the file system.
     *
     * @return the file system
     */
    @Override
    public String getFileSystem() {
        return String.valueOf(rates.getFileSystem());
    }
}
