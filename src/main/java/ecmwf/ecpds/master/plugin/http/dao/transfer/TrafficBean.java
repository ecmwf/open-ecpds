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
 * @author Laurent Gougeon <sy8iecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import ecmwf.common.text.Format;
import ecmwf.ecpds.master.plugin.http.model.transfer.Traffic;
import ecmwf.web.dao.ModelBeanBase;

/**
 * The Class TrafficBean.
 */
public class TrafficBean extends ModelBeanBase implements Traffic {

    /** The traffic. */
    private final ecmwf.common.database.Traffic traffic;

    /**
     * {@inheritDoc}
     *
     * Gets the bean interface name.
     */
    @Override
    public String getBeanInterfaceName() {
        return Traffic.class.getName();
    }

    /**
     * Instantiates a new traffic bean.
     *
     * @param traffic
     *            the traffic
     */
    protected TrafficBean(final ecmwf.common.database.Traffic traffic) {
        this.traffic = traffic;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the date.
     */
    @Override
    public String getDate() {
        return traffic.getDate();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the rate.
     */
    @Override
    public double getRate() {
        return Format.getMBitsPerSeconds(traffic.getBytes(), traffic.getDuration());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the formatted rate.
     */
    @Override
    public String getFormattedRate() {
        return Format.formatRate(traffic.getBytes(), traffic.getDuration());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the bytes.
     */
    @Override
    public long getBytes() {
        return traffic.getBytes();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the formatted bytes.
     */
    @Override
    public String getFormattedBytes() {
        return Format.formatSize(traffic.getBytes());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the duration.
     */
    @Override
    public long getDuration() {
        return traffic.getDuration();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the formatted duration.
     */
    @Override
    public String getFormattedDuration() {
        return Format.formatDuration(traffic.getDuration());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the files.
     */
    @Override
    public int getFiles() {
        return traffic.getFiles();
    }
}
