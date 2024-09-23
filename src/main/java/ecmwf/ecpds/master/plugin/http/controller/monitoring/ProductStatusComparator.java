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
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.Comparator;

import ecmwf.ecpds.master.plugin.http.model.monitoring.ProductStatus;

/**
 * The Class ProductStatusComparator.
 */
public class ProductStatusComparator implements Comparator<ProductStatus> {

    /**
     * {@inheritDoc}
     *
     * Compare.
     */
    @Override
    public int compare(final ProductStatus p1, final ProductStatus p2) {
        final var d1 = p1.getScheduledTime();
        final var d2 = p2.getScheduledTime();
        if (d1 != null && d2 != null) {
            return d1.compareTo(d2);
        }
        if (d1 != null && d2 == null) {
            return 1;
        } else if (d1 == null && d2 != null) {
            return -1;
        } else {
            return 0;
        }
    }
}
