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

package ecmwf.common.database;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.util.Vector;

/**
 * The Class ReceptionExt.
 */
public class ReceptionExt extends Reception {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -2884284638518728128L;

    /** The jobs count. */
    protected int jobsCount = 0;

    /** The distinct status. */
    protected final Vector<String> distinctStatus = new Vector<>();

    /**
     * Instantiates a new reception ext.
     */
    public ReceptionExt() {
    }

    /**
     * Instantiates a new reception ext.
     *
     * @param name
     *            the name
     */
    public ReceptionExt(final String name) {
        super(name);
    }

    /**
     * Gets the distinct status.
     *
     * @return the distinct status
     */
    public Vector<String> getDistinctStatus() {
        return distinctStatus;
    }

    /**
     * Gets the jobs count.
     *
     * @return the jobs count
     */
    public int getJobsCount() {
        return jobsCount;
    }
}
