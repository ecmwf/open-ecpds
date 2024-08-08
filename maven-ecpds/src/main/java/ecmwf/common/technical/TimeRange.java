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

package ecmwf.common.technical;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.time.LocalTime;
import java.time.format.DateTimeParseException;

/**
 * The Class TimeRange.
 */
public class TimeRange {

    /** The Constant SEPARATOR. */
    private static final String SEPARATOR = "-";

    /** The start time. */
    private final LocalTime startTime;

    /** The end time. */
    private final LocalTime endTime;

    /**
     * Instantiates a new time range.
     *
     * @param startTime
     *            the start time
     * @param endTime
     *            the end time
     */
    public TimeRange(final LocalTime startTime, final LocalTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * Within.
     *
     * @param current
     *            the current
     *
     * @return true, if successful
     */
    public boolean within(final LocalTime current) {
        return current.isAfter(getStartTime()) && current.isBefore(getEndTime());
    }

    /**
     * Parses the.
     *
     * @param timeRange
     *            the time range
     *
     * @return the time range
     *
     * @throws DateTimeParseException
     *             the date time parse exception
     */
    public static TimeRange parse(final String timeRange) throws DateTimeParseException {
        final var range = timeRange.split(SEPARATOR);
        if (range.length == 2) {
            return new TimeRange(LocalTime.parse(range[0]), LocalTime.parse(range[1]));
        }
        throw new DateTimeParseException("Expected format is {LocalTime}-{LocalTime} (e.g. 15:00-16:30)", timeRange, 0);
    }

    /**
     * Gets the start time.
     *
     * @return the start time
     */
    public LocalTime getStartTime() {
        return startTime;
    }

    /**
     * Gets the end time.
     *
     * @return the end time
     */
    public LocalTime getEndTime() {
        return endTime;
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return startTime + SEPARATOR + endTime;
    }
}
