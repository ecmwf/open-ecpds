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

package ecmwf.common.text;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * The Class DateUtil.
 */
public class DateUtil {

    /**
     * Gets the end of day.
     *
     * @param date
     *            the date
     *
     * @return the end of day
     */
    public static Date getEndOfDay(final Date date) {
        final var localDateTime = dateToLocalDateTime(date);
        final var endOfDay = localDateTime.with(LocalTime.MAX);
        return localDateTimeToDate(endOfDay);
    }

    /**
     * Gets the start of day.
     *
     * @param date
     *            the date
     *
     * @return the start of day
     */
    public static Date getStartOfDay(final Date date) {
        final var localDateTime = dateToLocalDateTime(date);
        final var startOfDay = localDateTime.with(LocalTime.MIN);
        return localDateTimeToDate(startOfDay);
    }

    /**
     * Local date time to date.
     *
     * @param startOfDay
     *            the start of day
     *
     * @return the date
     */
    private static Date localDateTimeToDate(final LocalDateTime startOfDay) {
        return Date.from(startOfDay.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Date to local date time.
     *
     * @param date
     *            the date
     *
     * @return the local date time
     */
    private static LocalDateTime dateToLocalDateTime(final Date date) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), ZoneId.systemDefault());
    }
}
