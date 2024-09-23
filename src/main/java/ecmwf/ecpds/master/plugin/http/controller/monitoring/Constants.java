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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import ecmwf.web.controller.ECMWFActionFormException;

/**
 * The Class Constants.
 */
public class Constants {

    /** The Constant ISO_FORMAT. */
    public static final String ISO_FORMAT = "yyyy-MM-dd";

    /** The Constant TIMELINE_STEP_WIDTH. */
    public static final Integer TIMELINE_STEP_WIDTH = 30;

    /** The Constant LAST_PRODUCT_SESSION_KEY. */
    public static final String LAST_PRODUCT_SESSION_KEY = "ecmwf_last_product";

    /** The Constant LAST_TIME_SESSION_KEY. */
    public static final String LAST_TIME_SESSION_KEY = "ecmwf_last_time";

    /** The Constant DAYS_BACK. */
    public static final int DAYS_BACK = 5;

    /** The Constant TIMES. */
    public static final ArrayList<String> TIMES;

    static {
        TIMES = new ArrayList<>(3);
        TIMES.add("00");
        TIMES.add("06");
        TIMES.add("12");
        TIMES.add("18");
    }

    /**
     * Gets the date options.
     *
     * @return the date options
     */
    public static final Collection<String> getDateOptions() {
        final var N = DAYS_BACK + 1;
        final List<String> l = new ArrayList<>(N + 1);
        final var c = Calendar.getInstance();
        c.setTime(new Date());
        l.add(new SimpleDateFormat(ISO_FORMAT).format(c.getTime()));
        for (var i = 0; i < N; i++) {
            c.add(Calendar.DATE, -1);
            l.add(new SimpleDateFormat(ISO_FORMAT).format(c.getTime()));
        }
        return l;
    }

    /**
     * Gets the date.
     *
     * @param date
     *            the date
     *
     * @return the date
     *
     * @throws ecmwf.web.controller.ECMWFActionFormException
     *             the ECMWF action form exception
     */
    public static final Date getDate(final String date) throws ECMWFActionFormException {
        try {
            return new SimpleDateFormat(ISO_FORMAT).parse(date);
        } catch (final Exception e) {
            throw new ECMWFActionFormException("Error parsing date", e);
        }
    }
}
