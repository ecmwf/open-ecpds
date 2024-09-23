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

package ecmwf.ecpds.master.transfer;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ecmwf.common.database.Destination;

/**
 * The Class DestinationComparator.
 */
public final class DestinationComparator implements Comparator<Destination> {

    /**
     * {@inheritDoc}
     *
     * Compare.
     */
    @Override
    public int compare(final Destination destination1, final Destination destination2) {
        return _compareDestination(destination1, destination2);
    }

    /**
     * _compare destination.
     *
     * @param destination1
     *            the destination1
     * @param destination2
     *            the destination2
     *
     * @return the int
     */
    private static int _compareDestination(final Destination destination1, final Destination destination2) {
        return destination1.getName().compareTo(destination2.getName());
    }

    /**
     * Gets the destination array.
     *
     * @param destinations
     *            the destinations
     *
     * @return the destination array
     */
    public static Destination[] getDestinationArray(final List<Destination> destinations) {
        Collections.sort(destinations, new DestinationComparator());
        return destinations.toArray(new Destination[destinations.size()]);
    }
}
