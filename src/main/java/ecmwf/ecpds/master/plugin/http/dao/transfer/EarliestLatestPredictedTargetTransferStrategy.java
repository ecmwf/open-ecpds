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
 * The Class EarliestLatestPredictedTargetTransferStrategy.
 */
public class EarliestLatestPredictedTargetTransferStrategy {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(EarliestLatestPredictedTargetTransferStrategy.class);

    /** The dt. */
    private final DataTransfer dt;

    /** The df. */
    private final DataFile df;

    /** The target. */
    private Date earliest, latest, predicted, target;

    /**
     * Instantiates a new earliest latest predicted target transfer strategy.
     *
     * @param t
     *            the t
     * @param f
     *            the f
     */
    public EarliestLatestPredictedTargetTransferStrategy(final DataTransfer t, final DataFile f) {
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
     * Gets the target.
     *
     * @return the target
     */
    public Date getTarget() {
        return this.target;
    }

    /**
     * Calculate.
     *
     * @param c
     *            the c
     */
    public void calculate(final Collection<DataTransfer> c) {
        // if (log.isDebugEnabled())
        // log.debug("Calculating Transfer Earliest, Latest, Target And
        // Predicted Time for transfer (" + dt.getId() + ") of file '" +
        // df.getOriginal() + "' DT:"+this.dt+", DF:"+this.df);
        try {
            final var N = c.size();
            final var times = new long[N];
            var earliest = Long.MAX_VALUE;
            var earliestPos = -1;
            var latest = Long.MIN_VALUE;
            var latestPos = -1;
            var total = 0D;
            var i = 0;
            for (DataTransfer transfer : c) {
                final var datafile = transfer.getDataFile();
                // If the transfer has FINISHED it will be counted in, if
                // not it WONT, yielding different values.
                // By some funky reason the calculation is called both
                // from the EVENT and from the display
                // Maybe the value is not flushed fast enough ?
                if (datafile.getProductTime() != null && transfer.getFinishTime() != null) {
                    final var difference = transfer.getFinishTime().getTime() - datafile.getProductTime().getTime();
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
            final var finishedN = i;
            // If there are more than 4 values we will deduct the earliest
            // and latest from the total before calculating the average;
            // If there are more than 4 values we will deduct the earliest
            // and latest from the total before calculating the average;
            var removedN = 0;
            var removedTime = 0L;
            if (finishedN > 4 && earliestPos != latestPos) {
                removedN = 2;
                removedTime = earliest + latest;
                times[earliestPos] = -1;
                times[latestPos] = -1;
            }
            if (N > 1 && finishedN > 0) {
                final var average = (total - removedTime) / (finishedN - removedN);
                // Then get deviations
                total = 0;
                for (i = 0; i < finishedN; i++) {
                    if (removedN == 0 || i != earliestPos && i != latestPos) {
                        total += Math.pow(average - times[i], 2);
                    }
                }
                // Now std deviation
                final var variance = total / (finishedN - removedN);
                final var standardDeviation = Math.sqrt(variance);
                // Add the std deviation to the TargetTime
                if (log.isDebugEnabled() && standardDeviation != 0) {
                    log.debug("Calculations for transfer '" + dt.getId() + "' std dev: " + standardDeviation + ", var: "
                            + variance + ", avg: " + average + ", N: " + finishedN + "-" + removedN + ", times: "
                            + getAsString(times) + ", earliest: " + earliestPos + ", latest: " + latestPos);
                }
                // Add TWO standard deviations for the Second Standard
                // Deviation
                final var predicted = getDateFromDatePlusMillisecs(df.getProductTime(),
                        average + standardDeviation * 2);
                this.predicted = predicted;
                final var earliestD = getDateFromDatePlusMillisecs(df.getProductTime(), earliest);
                this.earliest = earliestD;
                final var latestD = getDateFromDatePlusMillisecs(df.getProductTime(), latest);
                this.latest = latestD;
                // Target time
                final var d = this.dt.getDestination();
                final var bytesPerMillisecond = d.getBandWidth();
                if (bytesPerMillisecond > 0) {
                    final var size = this.df.getSize();
                    final var targetMilliseconds = size / bytesPerMillisecond;
                    final var targetD = getDateFromDatePlusMillisecs(this.dt.getScheduledTime(), targetMilliseconds);
                    this.target = targetD;
                } else {
                    this.target = this.dt.getScheduledTime();
                }
            } else {
                // No history: Take scheduled time as a lesser evil;
                if (log.isDebugEnabled()) {
                    log.debug("Data Transfer '" + this.dt.getId() + "' doesn't have any finished history (History: " + N
                            + ", Finished: " + finishedN + "). We'll take scheduled time (" + this.dt.getScheduledTime()
                            + ") for monitoring values");
                }
                if (this.dt.getScheduledTime() == null) {
                    log.warn("Scheduled time for Transfer " + this.dt.getId() + " IS NULL");
                }
                final var defaultDate = this.dt.getScheduledTime() != null ? this.dt.getScheduledTime() : new Date();
                this.earliest = defaultDate;
                this.latest = defaultDate;
                this.predicted = defaultDate;
                this.target = defaultDate;
            }
        } catch (final TransferException e) {
            log.error("Problem trying to predict transfer time. You know, this is quite hard ;-)", e);
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

    /**
     * Gets the as string.
     *
     * @param arr
     *            the arr
     *
     * @return the as string
     */
    protected static final String getAsString(final long[] arr) {
        final var out = new StringBuilder("[");
        for (var i = 0; i < arr.length; i++) {
            out.append(arr[i]);
            if (i < arr.length - 1) {
                out.append(",");
            }
        }
        return out.append("]").toString();
    }
}
