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

package ecmwf.ecpds.master.plugin.http.dao.transfer;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * Strategy to calculate Earliest, Latest, Predicted arrivals for a Data
 * Transfer
 *
 * @author Daniel Varela Santoalla <sy8@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.ecpds.master.plugin.http.model.datafile.DataFile;
import ecmwf.ecpds.master.plugin.http.model.transfer.DataTransfer;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferException;

/**
 * The Class EarliestLatestPredictedArrivalStrategy.
 */
public class EarliestLatestPredictedArrivalStrategy {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(EarliestLatestPredictedArrivalStrategy.class);

    /** The dt. */
    private final DataTransfer dt;

    /** The df. */
    private final DataFile df;

    /** The earliest. */
    private Date earliest;

    /** The latest. */
    private Date latest;

    /** The predicted. */
    private Date predicted;

    /**
     * Instantiates a new earliest latest predicted arrival strategy.
     *
     * @param t
     *            the t
     * @param f
     *            the f
     */
    public EarliestLatestPredictedArrivalStrategy(final DataTransfer t, final DataFile f) {
        this.dt = t;
        this.df = f;
    }

    /**
     * Gets the earliest.
     *
     * @return the earliest
     */
    public Date getEarliest() {
        return this.earliest;
    }

    /**
     * Gets the latest.
     *
     * @return the latest
     */
    public Date getLatest() {
        return this.latest;
    }

    /**
     * Gets the predicted.
     *
     * @return the predicted
     */
    public Date getPredicted() {
        return this.predicted;
    }

    /**
     * Calculate.
     *
     * @param c
     *            the c
     */
    public void calculate(final Collection<DataTransfer> c) {
        try {
            var N = c.size();
            final var times = new long[N];
            var earliest = Long.MAX_VALUE;
            var earliestPos = -1;
            var latest = Long.MIN_VALUE;
            var latestPos = -1;
            var total = 0D;
            var i = 0;
            for (DataTransfer transfer : c) {
                final var datafile = transfer.getDataFile();
                if (datafile.getArrivedTime() != null && datafile.getProductTime() != null) {
                    // The arrival times are normalised as time from TIME_BASE
                    // to the actual arrival, to be able to compare different
                    // days
                    final var difference = datafile.getArrivedTime().getTime() - datafile.getProductTime().getTime();
                    if (difference > latest) {
                        latest = difference;
                        latestPos = i;
                    }
                    if (difference < earliest) {
                        earliest = difference;
                        earliestPos = i;
                    }
                    total += difference;
                    times[i] = difference;
                    i++;
                } else if (log.isDebugEnabled()) {
                    log.debug("Ignoring data transfer '" + transfer.getId() + "' because of null fields");
                }
            }
            N = i;
            // If there are more than 4 values we will deduct the earliest
            // and latest from the total before calculating the average;
            var removedN = 0;
            var removedTime = 0L;
            if (N > 4 && earliestPos != latestPos) {
                removedN = 2;
                removedTime = earliest + latest;
                times[earliestPos] = -1;
                times[latestPos] = -1;
            }
            if (N > 0) {
                final var average = (total - removedTime) / (N - removedN);
                // Then get deviations, ignoring removed elements
                total = 0;
                for (i = 0; i < N; i++) {
                    if (removedN == 0 || i != earliestPos && i != latestPos) {
                        total += Math.pow(average - times[i], 2);
                    }
                }

                // Now std deviation
                final var variance = total / (N - removedN);
                final var standardDeviation = Math.sqrt(variance);
                // Add the std deviation to the TargetTime
                // if (log.isDebugEnabled() && standardDeviation != 0)
                // log.debug("Standard deviation for arrival of file '" +
                // df.getOriginal() + "' is " + standardDeviation);
                final var predicted = getDateFromDatePlusMillisecs(df.getProductTime(), average + standardDeviation);
                this.predicted = predicted;
                final var earliestD = getDateFromDatePlusMillisecs(df.getProductTime(), earliest);
                this.earliest = earliestD;
                final var latestD = getDateFromDatePlusMillisecs(df.getProductTime(), latest);
                this.latest = latestD;
            } else {
                // No history: Take scheduled time as a minor evil;
                final var defaultDate = this.dt.getScheduledTime() != null ? this.dt.getScheduledTime() : new Date();
                this.earliest = defaultDate;
                this.latest = defaultDate;
                this.predicted = defaultDate;
            }
        } catch (final TransferException e) {
            log.error("Problem trying to predict arrival time. You know, this is quite hard ;-)", e);
        } catch (final Exception e) {
            log.error("Problem trying to save predicted transfer time", e);
        }
    }

    /**
     * Gets the date from date plus millisecs.
     *
     * @param d
     *            the d
     * @param millisecs
     *            the millisecs
     *
     * @return the date from date plus millisecs
     */
    private static final Date getDateFromDatePlusMillisecs(final Date d, final double millisecs) {
        final var cal = Calendar.getInstance();
        cal.setTime(d);
        final var secondsShift = (int) (millisecs / 1000);
        cal.add(Calendar.SECOND, secondsShift);
        final var millisecsShift = (int) (millisecs / 1000 - (int) (millisecs / 1000)) * 1000;
        cal.add(Calendar.MILLISECOND, millisecsShift);
        return cal.getTime();
    }
}
