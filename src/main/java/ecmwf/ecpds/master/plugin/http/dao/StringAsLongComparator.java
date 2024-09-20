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

package ecmwf.ecpds.master.plugin.http.dao;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.Comparator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Class StringAsLongComparator.
 */
public class StringAsLongComparator implements Comparator<String> {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(StringAsLongComparator.class);

    /**
     * Compare.
     *
     * @param s1
     *            the s 1
     * @param s2
     *            the s 2
     *
     * @return the int
     */
    @Override
    public int compare(final String s1, final String s2) {
        try {
            return Long.valueOf(s1).compareTo(Long.valueOf(s2));
        } catch (final Exception e) {
            log.warn("Problem comparing '" + s1 + "' and '" + s2 + "' as integers", e);
            return 0;
        }
    }
}
