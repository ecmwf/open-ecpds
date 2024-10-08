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

package ecmwf.ecpds.master.plugin.http.model.transfer;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon <sy8iecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import ecmwf.web.model.ModelBean;

/**
 * The Interface Traffic.
 */
public interface Traffic extends ModelBean {

    /**
     * Gets the date.
     *
     * @return the date
     */
    String getDate();

    /**
     * Gets the rate.
     *
     * @return the rate
     */
    double getRate();

    /**
     * Gets the formatted rate.
     *
     * @return the formatted rate
     */
    String getFormattedRate();

    /**
     * Gets the bytes.
     *
     * @return the bytes
     */
    long getBytes();

    /**
     * Gets the formatted bytes.
     *
     * @return the formatted bytes
     */
    String getFormattedBytes();

    /**
     * Gets the duration.
     *
     * @return the duration
     */
    long getDuration();

    /**
     * Gets the formatted duration.
     *
     * @return the formatted duration
     */
    String getFormattedDuration();

    /**
     * Gets the files.
     *
     * @return the files
     */
    int getFiles();
}
