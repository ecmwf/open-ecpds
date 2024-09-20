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

package ecmwf.ecpds.master.plugin.http.dao.monitoring;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.Date;

import ecmwf.ecpds.master.plugin.http.model.monitoring.GenerationMonitoringStatus;
import ecmwf.ecpds.master.plugin.http.model.transfer.Status;

/**
 * The Class ProductStatusStrategy.
 */
public class ProductStatusStrategy {

    /**
     * Gets the generation status from code.
     *
     * @param status
     *            the status
     * @param code
     *            the code
     * @param buffer
     *            the buffer
     *
     * @return the generation status from code
     */
    public static final int getGenerationStatusFromCode(final GenerationMonitoringStatus status, final String code,
            final long buffer) {
        final var minsAhead = getMinutesBeforeSchedule(status.getScheduledTime(), status.getArrivalTime())
                + buffer / 60000;
        status.setGenerationStatusCode(code);
        if (minsAhead >= 30) {
            return GenerationMonitoringStatus.STATUS_MORE_THAN_30_AHEAD;
        }
        if (minsAhead >= 15) {
            return GenerationMonitoringStatus.STATUS_30_15_AHEAD;
        } else if (minsAhead >= 0) {
            return GenerationMonitoringStatus.STATUS_15_0_AHEAD;
        } else if (minsAhead == Integer.MIN_VALUE) {
            return GenerationMonitoringStatus.STATUS_NONE;
        } else if (minsAhead < -60 && Status.INIT.equals(code)) {
            return GenerationMonitoringStatus.STATUS_NOT_RECEIVED;
        } else {
            return GenerationMonitoringStatus.STATUS_0_AHEAD;
        }
    }

    /**
     * Gets the minutes before schedule.
     *
     * @param schedule
     *            the schedule
     * @param arrival
     *            the arrival
     *
     * @return the minutes before schedule
     */
    public static final long getMinutesBeforeSchedule(final Date schedule, final Date arrival) {
        if (schedule != null) {
            final var arrivalOrNow = arrival != null ? arrival : new Date();
            return (schedule.getTime() - arrivalOrNow.getTime()) / 60000;
        }
        return Integer.MIN_VALUE;
    }
}
