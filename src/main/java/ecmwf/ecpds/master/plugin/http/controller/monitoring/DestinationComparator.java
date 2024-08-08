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
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Daniel Varela Santoalla <sy8@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.Comparator;

import ecmwf.ecpds.master.plugin.http.model.transfer.Destination;

/**
 * The Class DestinationComparator.
 */
public class DestinationComparator implements Comparator<Destination> {

    /** The ascending. */
    private final boolean ascending;

    /**
     * Instantiates a new destination comparator.
     *
     * @param field
     *            the field
     * @param ascending
     *            the ascending
     */
    public DestinationComparator(final String field, final boolean ascending) {
        this.ascending = ascending;
    }

    /**
     * Compare.
     *
     * @param d1
     *            the d 1
     * @param d2
     *            the d 2
     *
     * @return the int
     */
    @Override
    public int compare(final Destination d1, final Destination d2) {
        final var value1 = d1.getName();
        final var value2 = d2.getName();
        return ascending ? value1.compareTo(value2) : value2.compareTo(value1);
    }
}
