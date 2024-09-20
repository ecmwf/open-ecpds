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

package ecmwf.common.ecaccess;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import static ecmwf.common.text.Util.isNotEmpty;

import java.net.InetAddress;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.management.timer.Timer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.monitor.MonitorCallback;
import ecmwf.common.monitor.MonitorException;
import ecmwf.common.monitor.MonitorManager;
import ecmwf.common.monitor.MonitorThread;
import ecmwf.common.technical.Cnf;
import ecmwf.common.technical.TimeRange;
import ecmwf.common.technical.WakeupThread;

/**
 * The Class ECaccessScheduler.
 */
public abstract class ECaccessScheduler extends WakeupThread implements MonitorCallback {

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(ECaccessScheduler.class);

    /** The Constant DEBUG_FREQUENCY. */
    private static final long MINIMUM_WAIT = Cnf.durationAt("ECaccessScheduler", "minimumWait", Timer.ONE_SECOND);

    /** The Constant SCHEDULER_STATE_ONLINE. */
    public static final int SCHEDULER_STATE_ONLINE = 0;

    /** The Constant SCHEDULER_STATE_OPENING. */
    public static final int SCHEDULER_STATE_OPENING = 1;

    /** The Constant SCHEDULER_STATE_CLOSING. */
    public static final int SCHEDULER_STATE_CLOSING = 2;

    /** The Constant SCHEDULER_STATE_OFFLINE. */
    public static final int SCHEDULER_STATE_OFFLINE = 3;

    /** The Constant SCHEDULER_STATE_JAMMED. */
    public static final int SCHEDULER_STATE_JAMMED = 4;

    /** The Constant SCHEDULER_STATE_ONHOLD. */
    public static final int SCHEDULER_STATE_ONHOLD = 5;

    /** The Constant NEXT_STEP_INIT. */
    public static final int NEXT_STEP_INIT = 0;

    /** The Constant NEXT_STEP_CONTINUE. */
    public static final int NEXT_STEP_CONTINUE = 1;

    /** The Constant NEXT_STEP_DELAY. */
    public static final int NEXT_STEP_DELAY = 2;

    /** The Constant NEXT_STEP_ABORT. */
    public static final int NEXT_STEP_ABORT = 3;

    /** The Constant states. */
    protected static final String[] states = { "SCHEDULER_STATE_ONLINE", "SCHEDULER_STATE_OPENING",
            "SCHEDULER_STATE_CLOSING", "SCHEDULER_STATE_OFFLINE", "SCHEDULER_STATE_JAMMED", "SCHEDULER_STATE_ONHOLD" };

    /** The Constant steps. */
    protected static final String[] steps = { "NEXT_STEP_INIT", "NEXT_STEP_CONTINUE", "NEXT_STEP_DELAY",
            "NEXT_STEP_ABORT" };

    /** The timeRanges. */
    private final List<TimeRange> timeRanges = new ArrayList<>();

    /** The time. */
    private long time = System.currentTimeMillis();

    /** The last step time. */
    private long lastStepTime = 0;

    /** The run. */
    private boolean run = true;

    /** The state. */
    private int state = SCHEDULER_STATE_OFFLINE;

    /** The step. */
    private int step = NEXT_STEP_DELAY;

    /** The delay. */
    private long delay = 5 * Timer.ONE_SECOND;

    /** The jammed. */
    private long jammed = 5 * Timer.ONE_MINUTE;

    /** The start date. */
    private Date startDate = null;

    /** The monitor. */
    private boolean monitor = true;

    /** The initialized. */
    private boolean initialized = false;

    /**
     * Shutdown.
     */
    public void shutdown() {
        state = SCHEDULER_STATE_CLOSING;
        run = false;
        wakeup();
        if (monitor && MonitorManager.isActivated()) {
            try {
                MonitorThread.getInstance().unSubscribe(getThreadName());
            } catch (final MonitorException e) {
                _log.debug(e);
            }
        }
    }

    /**
     * Checks if is initialized.
     *
     * @return true, if is initialized
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Checks if is running.
     *
     * @return true, if is running
     */
    public boolean isRunning() {
        return run;
    }

    /**
     * Checks if is jammed.
     *
     * @return true, if is jammed
     */
    public boolean isJammed() {
        return state == SCHEDULER_STATE_ONLINE && !isSleeping() && !isRestricted() && getStepTime() > jammed;
    }

    /**
     * Checks if is on hold.
     *
     * @return true, if is on hold
     */
    @Override
    public boolean isOnHold() {
        return state == SCHEDULER_STATE_ONLINE && super.isOnHold();
    }

    /**
     * Wakeup.
     */
    @Override
    public void wakeup() {
        time = System.currentTimeMillis();
        super.wakeup();
    }

    /**
     * Sets the onhold.
     *
     * @param hold
     *            the new onhold
     */
    @Override
    public void setOnhold(final boolean hold) {
        super.setOnhold(hold);
        if (!hold) {
            wakeup();
        }
    }

    /**
     * Sets the list of time ranges when it should be running.
     *
     * @param timeRanges
     *            the time ranges list
     */
    public void setTimeRanges(final List<TimeRange> timeRanges) {
        this.timeRanges.clear();
        this.timeRanges.addAll(timeRanges);
    }

    /**
     * Check if we are supposed to run or not according to the time ranges! If there is no time range specified then by
     * default no restrictions are in place.
     *
     * @return true if it should not process
     */
    private boolean isRestricted() {
        if (!timeRanges.isEmpty()) {
            final var now = LocalTime.now();
            return timeRanges.stream().noneMatch(period -> period.within(now));
        }
        return false;
    }

    /**
     * Sets the jammed timeout.
     *
     * @param timeout
     *            the new jammed timeout
     */
    public void setJammedTimeout(final long timeout) {
        jammed = timeout;
    }

    /**
     * Gets the jammed timeout.
     *
     * @return the jammed timeout
     */
    public long getJammedTimeout() {
        return jammed;
    }

    /**
     * Gets the delay.
     *
     * @return the delay
     */
    public long getDelay() {
        return delay;
    }

    /**
     * Gets the start date.
     *
     * @return the start date
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * Gets the monitor name.
     *
     * @return the monitor name
     */
    public String getMonitorName() {
        return getThreadName();
    }

    /**
     * Gets the scheduler state.
     *
     * @return the scheduler state
     */
    public int getSchedulerState() {
        return isJammed() ? SCHEDULER_STATE_JAMMED : isOnHold() ? SCHEDULER_STATE_ONHOLD : state;
    }

    /**
     * Gets the step time.
     *
     * @return the step time
     */
    public long getStepTime() {
        return System.currentTimeMillis() - time;
    }

    /**
     * Gets the next step.
     *
     * @return the next step
     */
    public int getNextStep() {
        return step;
    }

    /**
     * Gets the last step time.
     *
     * @return the last step time
     */
    public long getLastStepTime() {
        return lastStepTime;
    }

    /**
     * Gets the activity.
     *
     * @return the activity
     */
    public String getActivity() {
        return null;
    }

    /**
     * Gets the monitor manager.
     *
     * @param name
     *            the name
     *
     * @return the monitor manager
     *
     * @throws MonitorException
     *             the monitor exception
     */
    @Override
    public MonitorManager getMonitorManager(final String name) throws MonitorException {
        final var context = getActivity();
        // The name is the name of the scheduler (e.g. TransferScheduler)!
        try {
            return new MonitorManager(
                    Cnf.at("Login", "service", Cnf.at("Login", "hostName", InetAddress.getLocalHost().getHostName())),
                    "Scheduler: " + name,
                    isOnHold() ? MonitorManager.YELLOW : isJammed() ? MonitorManager.RED : MonitorManager.GREEN,
                    (isOnHold() || isRestricted() ? "ONHOLD" : isJammed() ? "JAMMED" : "ONLINE")
                            + (isNotEmpty(context) ? " (" + context + ")" : ""));
        } catch (final Throwable t) {
            throw new MonitorException(t.getMessage());
        }
    }

    /**
     * Initialize.
     */
    public void initialize() {
    }

    /**
     * On hold.
     */
    public void onHold() {
    }

    /**
     * Sets the delay.
     *
     * @param delay
     *            the new delay
     */
    public void setDelay(final long delay) {
        this.delay = delay;
    }

    /**
     * Deactivate monitor.
     */
    public void deactivateMonitor() {
        monitor = false;
    }

    /**
     * Start.
     */
    public void start() {
        startDate = new Date();
        state = SCHEDULER_STATE_OPENING;
        if (monitor && MonitorManager.isActivated()) {
            final var name = getMonitorName();
            try {
                MonitorThread.getInstance().subscribe(name, "Scheduler:" + name, this);
            } catch (final MonitorException e) {
                _log.warn("Subscribing {}", name, e);
            }
        }
        execute();
    }

    /**
     * Next step.
     *
     * @return the int
     */
    public abstract int nextStep();

    /**
     * Configurable run.
     */
    @Override
    public void configurableRun() {
        _log.info("Scheduler {} initialized (frequence of {}ms)", getThreadName(), delay);
        initialize();
        initialized = true;
        var onHold = false;
        state = SCHEDULER_STATE_ONLINE;
        step = NEXT_STEP_DELAY;
        while (run) {
            if (isRestricted() && delay > 0) {
                waitFor(delay);
                continue;
            }
            if (step == NEXT_STEP_DELAY && delay > 0) {
                waitFor(delay);
            } else if (step == NEXT_STEP_CONTINUE && MINIMUM_WAIT > 0) {
                waitFor(MINIMUM_WAIT); // Minimum wait to avoid high CPU usage and DB overload!
            }
            if (!isOnHold()) {
                onHold = false;
            } else {
                if (!onHold) {
                    _log.info("Scheduler {} switch to onHold mode", getThreadName());
                    onHold = true;
                    onHold();
                }
                if (waitFor(Timer.ONE_HOUR)) {
                    continue;
                }
            }
            time = System.currentTimeMillis();
            try {
                step = nextStep();
            } catch (final Throwable t) {
                _log.warn("Moving to next step", t);
            }
            lastStepTime = getStepTime();
            if (step == NEXT_STEP_ABORT) {
                break;
            }
        }
        state = SCHEDULER_STATE_OFFLINE;
        _log.info("Scheduler {} done", getThreadName());
    }
}