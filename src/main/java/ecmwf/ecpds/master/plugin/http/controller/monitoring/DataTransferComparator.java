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

package ecmwf.ecpds.master.plugin.http.controller.monitoring;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.Comparator;
import java.util.Date;

import ecmwf.ecpds.master.plugin.http.model.transfer.DataTransfer;

/**
 * The Class DataTransferComparator.
 */
public class DataTransferComparator implements Comparator<DataTransfer> {

    /** The field. */
    private final String field;

    /** The ascending. */
    private final boolean ascending;

    /**
     * Instantiates a new data transfer comparator.
     *
     * @param field
     *            the field
     * @param ascending
     *            the ascending
     */
    public DataTransferComparator(final String field, final boolean ascending) {
        this.field = field;
        this.ascending = ascending;
    }

    /**
     * {@inheritDoc}
     *
     * Compare.
     */
    @Override
    public int compare(final DataTransfer o1, final DataTransfer o2) {
        Date value1;
        Date value2;
        if ("startDate".equals(this.field)) {
            value1 = o1.getStartTime();
            value2 = o2.getStartTime();
            if (value1 == null) {
                value1 = o1.getScheduledTime();
            }
            if (value2 == null) {
                value2 = o2.getScheduledTime();
            }
        } else if ("scheduledDate".equals(this.field)) {
            value1 = o1.getScheduledTime();
            value2 = o2.getScheduledTime();
        } else {
            return 0;
        }
        if (value1 == null) {
            return -1;
        }
        if (value2 == null) {
            return 1;
        }
        return ascending ? value1.compareTo(value2) : value2.compareTo(value1);
    }
}
