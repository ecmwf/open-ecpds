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
 * ECMWF Product Data Store (OpenPDS) Project
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
     * Gets the bean interface name.
     *
     * @return the bean interface name
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
     * Gets the date.
     *
     * @return the date
     */
    @Override
    public String getDate() {
        return traffic.getDate();
    }

    /**
     * Gets the rate.
     *
     * @return the rate
     */
    @Override
    public double getRate() {
        return Format.getMBitsPerSeconds(traffic.getBytes(), traffic.getDuration());
    }

    /**
     * Gets the formatted rate.
     *
     * @return the formatted rate
     */
    @Override
    public String getFormattedRate() {
        return Format.formatRate(traffic.getBytes(), traffic.getDuration());
    }

    /**
     * Gets the bytes.
     *
     * @return the bytes
     */
    @Override
    public long getBytes() {
        return traffic.getBytes();
    }

    /**
     * Gets the formatted bytes.
     *
     * @return the formatted bytes
     */
    @Override
    public String getFormattedBytes() {
        return Format.formatSize(traffic.getBytes());
    }

    /**
     * Gets the duration.
     *
     * @return the duration
     */
    @Override
    public long getDuration() {
        return traffic.getDuration();
    }

    /**
     * Gets the formatted duration.
     *
     * @return the formatted duration
     */
    @Override
    public String getFormattedDuration() {
        return Format.formatDuration(traffic.getDuration());
    }

    /**
     * Gets the files.
     *
     * @return the files
     */
    @Override
    public int getFiles() {
        return traffic.getFiles();
    }
}
