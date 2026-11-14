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

package ecmwf.ecpds.master;

import static ecmwf.common.ectrans.ECtransGroups.Module.HOST_ACQUISITION;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_ACTION;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_DATEDELTA;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_DATEFORMAT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_DATEPATTERN;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_DATESOURCE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_DEBUG;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_DEFAULT_DATE_FORMAT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_DELETEORIGINAL;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_EVENT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_FILEAGE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_FILESIZE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_GROUPBY;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_INTERRUPT_SLOW;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_LIFETIME;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_LIST_MAX_THREADS;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_LIST_MAX_WAITING;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_LIST_PARALLEL;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_LIST_SYNCHRONOUS;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_MAXIMUM_DURATION;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_METADATA;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_NORETRIEVAL;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_ONLY_VALID_TIME;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_PAYLOAD_EXTENSION;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_PRIORITY;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_RECENT_DATE_FORMAT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_REGEX_FORMAT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_REGEX_PATTERN;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_REMOVE_PARAMETERS;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_REQUEUEON;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_REQUEUEONSAMESIZE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_REQUEUEONUPDATE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_SERVER_LANGUAGE_CODE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_SERVER_TIME_ZONE_ID;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_SHORT_MONTH_NAMES;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_STANDBY;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_SYSTEM_KEY;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_TARGET;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_TRANSFERGROUP;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_UNIQUE_BY_NAME_AND_TIME;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_UNIQUE_BY_TARGET_ONLY;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_USE_SYMLINK;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_USE_TARGET_AS_UNIQUE_NAME;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_WILDCARD_FILTER;
import static ecmwf.common.text.Util.isEmpty;
import static ecmwf.common.text.Util.isNotEmpty;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.PatternSyntaxException;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.timer.Timer;
import javax.script.ScriptException;
import ecmwf.common.database.DataBaseException;
import ecmwf.common.database.DataTransfer;
import ecmwf.common.database.Destination;
import ecmwf.common.database.ECpdsBase;
import ecmwf.common.database.Host;
import ecmwf.common.ecaccess.MBeanScheduler;
import ecmwf.common.ectrans.ECtransOptions;
import ecmwf.common.ectrans.notification.AcquisitionListing;
import ecmwf.common.ectrans.notification.AcquisitionNotification;
import ecmwf.common.ectrans.ECtransSetup;
import ecmwf.common.ftp.FtpParser;
import ecmwf.common.ftp.FtpParser.FileEntry;
import ecmwf.common.mbean.MBeanManager;
import ecmwf.common.technical.ByteSize;
import ecmwf.common.technical.Cnf;
import ecmwf.common.technical.ExecutorManager;
import ecmwf.common.technical.ExecutorRunnable;
import ecmwf.common.technical.ScriptManager;
import ecmwf.common.technical.StreamPlugThread;
import ecmwf.common.technical.ThreadService.ConfigurableRunnable;
import ecmwf.common.text.Format;
import ecmwf.ecpds.master.plugin.ecpds.ECpdsClient;
import ecmwf.ecpds.master.transfer.HostOption;
import ecmwf.ecpds.master.transfer.StatusFactory;
import ecmwf.ecpds.master.transfer.TransferManagement;
import ecmwf.ecpds.master.transfer.TransferScheduler;
import ecmwf.ecpds.master.transfer.TransferScheduler.AcquisitionResult;

/**
 * The Class AcquisitionScheduler.
 */
public final class AcquisitionScheduler extends MBeanScheduler {
    private final MasterServer _master;

    /** The _max acquisition threads. */
    private int _maxAcquisitionThreads = Cnf.at("Scheduler", "maxAcquisitionThreads", 100);

    /** The _maximum duration. */
    private long _maximumDuration = Cnf.durationAt("Scheduler", "maximumDurationAcquisitionThread",
            10 * Timer.ONE_MINUTE);

    /** The _interrupt slow. */
    public boolean _interruptSlow = Cnf.at("Scheduler", "interruptSlowAcquisitionThread", true);

    /** The _acquisition threads. */
    final Map<String, AcquisitionThread> _acquisitionThreads = new ConcurrentHashMap<>();

    /** The _to remove. */
    final List<String> _toRemove = Collections.synchronizedList(new ArrayList<>());

    /** The _current key. */
    private String _currentKey = "[none]";

    /** The _pause. */
    private boolean _pause = false;

    /**
     * Instantiates a new acquisition scheduler.
     *
     * @param name
     *            the name
     */
    public AcquisitionScheduler(final MasterServer _master, final String name) {
        // Bind the Master before superclass construction and field initialization.
        this._master = _master;
        super(name);
        setDelay(Cnf.durationAt("Scheduler", "acquisitionScheduler", Timer.ONE_SECOND));
        setJammedTimeout(Cnf.durationAt("Scheduler", "acquisitionSchedulerJammedTimeout", 10 * Timer.ONE_MINUTE));
        setTimeRanges(Cnf.listOfTimeRangesAt("Scheduler", "acquisitionSchedulerTimeRanges"));
    }

    /**
     * Gets the download threads count.
     *
     * @return the download threads count
     */
    public int getDownloadThreadsCount() {
        return _acquisitionThreads.size() - _toRemove.size();
    }

    /**
     * Gets the activity.
     *
     * @return the activity
     */
    @Override
    public String getActivity() {
        return "lastHost=" + _currentKey + ",acquisitionThreads=" + _acquisitionThreads.size();
    }

    /**
     * Gets the key.
     *
     * @param host
     *            the host
     *
     * @return the string
     */
    private String _getKey(final Host host) {
        return host.getName();
    }

    /**
     * Gets the thread list.
     *
     * @return the thread list
     */
    public String getThreadList() {
        final var status = new StringBuilder();
        final var currentTime = System.currentTimeMillis();
        for (final AcquisitionThread thread : _acquisitionThreads.values()) {
            final String key;
            if (thread != null && !_toRemove.contains(key = _getKey(thread._host))) {
                status.append((thread._desName + " Host " + key + " ("
                        + Format.formatDuration(thread._time, currentTime) + ")").replace(' ', '_').replace('-', '_'))
                        .append(" ");
            }
        }
        return status.toString().trim();
    }

    /**
     * Interrupt acquisition for.
     *
     * @param host
     *            the host
     *
     * @return true, if successful
     */
    public boolean interruptAcquisitionFor(final Host host) {
        final var thread = _acquisitionThreads.get(_getKey(host));
        if (thread != null && thread.isAlive()) {
            MasterServer._log.debug("Interrupting AcquisitionThread for Host-{}", host.getName());
            try {
                // Signal the thread's loop checks to stop
                thread._running = false;
                thread._time = System.currentTimeMillis();
                // Give the thread a short grace period to exit cleanly on its own.
                // join(timeout) returns normally on timeout — it does NOT throw — so we
                // must check isAlive() after it returns rather than relying on a catch block.
                thread.join(10 * Timer.ONE_SECOND);
                if (thread.isAlive()) {
                    // Still running after grace period: forcibly interrupt it.
                    // This is needed when the thread is blocked inside a synchronous
                    // RMI call (e.g. mover.listAsByteArray) which _running=false cannot
                    // unblock on its own.
                    MasterServer._log.debug(
                            "AcquisitionThread for Host-{} still alive after grace period, interrupting",
                            host.getName());
                    thread.interrupt();
                }
                return true;
            } catch (final Throwable t) {
                MasterServer._log.warn("Interrupting AcquisitionThread", t);
            }
        }
        return false;
    }

    /**
     * Gets the MBean info.
     *
     * @return the MBean info
     */
    @Override
    public MBeanInfo getMBeanInfo() {
        return MBeanManager.addMBeanInfo(super.getMBeanInfo(),
                "The AcquisitionScheduler is used to manage the acquisition threads.",
                new MBeanAttributeInfo[] {
                        new MBeanAttributeInfo("ThreadCount", "java.lang.Integer",
                                "ThreadCount: number of acquisition thread(s) currently running.", true, false, false),
                        new MBeanAttributeInfo("ThreadList", "java.lang.String",
                                "ThreadList: active acquisition thread(s) shown as destination, Host name and elapsed running time.",
                                true, false, false),
                        new MBeanAttributeInfo("MaximumAcquisitionThreads", "java.lang.Integer",
                                "MaximumAcquisitionThreads: maximum number of parallel acquisitions.", true, true,
                                false),
                        new MBeanAttributeInfo("MaximumDuration", "java.lang.Long",
                                "MaximumDuration: maximum duration for an acquisition in milliseconds.", true, true,
                                false),
                        new MBeanAttributeInfo("InterruptSlowEnabled", "java.lang.Boolean",
                                "InterruptSlowEnabled: automatically interrupt slow acquisitions.", true, true,
                                false) },
                new MBeanOperationInfo[0]);
    }

    /**
     * Gets the attribute.
     *
     * @param attributeName
     *            the attribute name
     *
     * @return the attribute
     *
     * @throws AttributeNotFoundException
     *             the attribute not found exception
     * @throws MBeanException
     *             the MBean exception
     */
    @Override
    public Object getAttribute(final String attributeName) throws AttributeNotFoundException, MBeanException {
        try {
            if ("ThreadCount".equals(attributeName)) {
                return _acquisitionThreads.size();
            }
            if ("ThreadList".equals(attributeName)) {
                return getThreadList();
            }
            if ("MaximumAcquisitionThreads".equals(attributeName)) {
                return _maxAcquisitionThreads;
            }
            if ("MaximumDuration".equals(attributeName)) {
                return _maximumDuration;
            }
            if ("InterruptSlowEnabled".equals(attributeName)) {
                return _interruptSlow;
            }
        } catch (final Exception e) {
            MasterServer._log.warn("Getting an MBean attribute", e);
            throw new MBeanException(e);
        }
        return super.getAttribute(attributeName);
    }

    /**
     * Sets the attribute.
     *
     * @param name
     *            the name
     * @param value
     *            the value
     *
     * @return true, if successful
     *
     * @throws InvalidAttributeValueException
     *             the invalid attribute value exception
     * @throws MBeanException
     *             the MBean exception
     */
    @Override
    public boolean setAttribute(final String name, final Object value)
            throws InvalidAttributeValueException, MBeanException {
        if ("MaximumAcquisitionThreads".equals(name)) {
            _maxAcquisitionThreads = (Integer) value;
            return true;
        }
        if ("MaximumDuration".equals(name)) {
            _maximumDuration = (Long) value;
            return true;
        }
        if ("InterruptSlowEnabled".equals(name)) {
            _interruptSlow = (Boolean) value;
            return true;
        }
        return super.setAttribute(name, value);
    }

    /**
     * Sets the pause.
     *
     * @param pause
     *            the new pause
     */
    public void setPause(final boolean pause) {
        _pause = pause;
    }

    /**
     * Gets the pause.
     *
     * @return the pause
     */
    public boolean getPause() {
        return _pause;
    }

    /**
     * Next step.
     *
     * @return the int
     */
    @Override
    public int nextStep() {
        var processed = 0;
        try {
            final var start = System.currentTimeMillis();
            final var destinations = _master.getECpdsBase().getDestinationsAndHostsForType(HostOption.ACQUISITION,
                    2 * _maxAcquisitionThreads);
            for (final String desName : destinations.keySet()) {
                for (final Host host : destinations.get(desName).toArray(new Host[0])) {
                    if (_pause || !isRunning() || getDownloadThreadsCount() >= _maxAcquisitionThreads) {
                        break;
                    }
                    synchronized (_acquisitionThreads) {
                        if (!_acquisitionThreads.containsKey(_currentKey = _getKey(host))) {
                            final var thread = new AcquisitionThread(desName, host);
                            thread.setThreadNameAndCookie(null, desName, "Host-" + host.getName(), null);
                            thread.execute(true);
                            _acquisitionThreads.put(_currentKey, thread);
                            processed++;
                        }
                    }
                }
            }
            synchronized (_toRemove) {
                for (final String key : _toRemove) {
                    final var thread = _acquisitionThreads.remove(key);
                    MasterServer._log.debug("AcquisitionThread " + key + " removed (" + (thread != null) + ")");
                }
                _toRemove.clear();
            }
            // Check the expired DownloadThreads
            if (_interruptSlow) {
                for (final AcquisitionThread thread : _acquisitionThreads.values()) {
                    if (thread != null && thread._localInterruptSlow) {
                        final var currentTime = System.currentTimeMillis();
                        final var duration = currentTime - thread._time;
                        final var maximumDuration = thread._localMaximumDuration;
                        if (duration > maximumDuration) {
                            MasterServer._log.debug("Interrupting slow acquisition: " + thread._host.getName() + " ("
                                    + Format.formatDuration(duration) + ")");
                            try {
                                thread._running = false;
                                thread.interrupt();
                                thread._time = System.currentTimeMillis();
                            } catch (final Throwable t) {
                                MasterServer._log.warn("Interrupting AcquisitionThread", t);
                            }
                        }
                    }
                }
            }
            final var duration = System.currentTimeMillis() - start;
            if (MasterServer._log.isDebugEnabled() && duration > 60000) {
                MasterServer._log.debug("Step completed in {}: {} Host(s) processed for acquisition",
                        Format.formatDuration(duration), processed);
            }
        } catch (final Throwable t) {
            MasterServer._log.warn("Error in scheduler", t);
        }
        // If there are still Destinations to be processed then there is no
        // reason to wait!
        return processed == 0 ? NEXT_STEP_DELAY : NEXT_STEP_CONTINUE;
    }

    /**
     * The Class AcquisitionThread.
     */
    final class AcquisitionThread extends ConfigurableRunnable implements StatusUpdate {

        /** The Constant LINE_SEPARATOR. */
        private static final String LINE_SEPARATOR = "------------------------------------------------------------------------";

        /** The _des name. */
        private final String _desName;

        /** The _gzip. */
        private final OutputStream _tmpOut;

        /** The _out. */
        private final File _fileOutputTmp;

        /** The _out. */
        private final File _fileOutputDone;

        /** The _out. */
        private final FileOutputStream _out;

        /** The _options list. */
        private final Map<String, String> _optionsList = new ConcurrentHashMap<>();

        /** The _time. */
        private long _time = System.currentTimeMillis();

        /** The _localMaximumDuration. */
        private long _localMaximumDuration = _maximumDuration;

        /** The _localInterruptSlow. */
        private boolean _localInterruptSlow = _interruptSlow;

        /** The _host. */
        private Host _host = null;

        /** The _debug. */
        private boolean _debug = false;

        /** The _setup. */
        private ECtransSetup _setup = null;

        /** The _size. */
        private int _size = 0;

        /** The summary. */
        final StringBuilder _summary = new StringBuilder();

        /** The _running. */
        boolean _running = true;

        /**
         * Instantiates a new acquisition thread.
         *
         * @param desName
         *            the des name
         * @param host
         *            the host
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        private AcquisitionThread(final String desName, final Host host) throws IOException {
            _fileOutputTmp = MasterServer._getHostOutputFile(host, true);
            _fileOutputDone = MasterServer._getHostOutputFile(host, false);
            _out = new FileOutputStream(_fileOutputTmp);
            if (_fileOutputTmp.getName().endsWith(".gz")) {
                _tmpOut = new GZIPOutputStream(_out, Deflater.BEST_COMPRESSION);
            } else {
                _tmpOut = _out;
            }
            _desName = desName;
            _host = host;
        }

        /**
         * _compare.
         *
         * @param op
         *            the op
         * @param localValue
         *            the local value
         * @param remoteValue
         *            the remote value
         *
         * @return true, if successful
         */
        private boolean _compare(final String op, final long localValue, final long remoteValue) {
            if ("==".equals(op)) {
                return remoteValue == localValue;
            }
            if (">=".equals(op)) {
                return remoteValue >= localValue;
            }
            if ("<=".equals(op)) {
                return remoteValue <= localValue;
            }
            if (">>".equals(op)) {
                return remoteValue > localValue;
            }
            if ("<<".equals(op)) {
                return remoteValue < localValue;
            }
            if ("!=".equals(op)) {
                return remoteValue != localValue;
            }
            // Operation no recognized
            return false;
        }

        /**
         * _check size.
         *
         * @param remoteSize
         *            the remote size
         *
         * @return true, if successful
         *
         * @throws ScriptException
         *             the script exception
         */
        private boolean _checkSize(final long remoteSize) throws ScriptException {
            var value = _optionsList.get(HOST_ACQUISITION_FILESIZE.getName());
            if (value == null && (value = _setup.getString(HOST_ACQUISITION_FILESIZE)) != null) {
                if (value.indexOf("$size") != -1) {
                    // This is a string to evaluate!
                    return ScriptManager.exec(Boolean.class, ScriptManager.JS,
                            Format.replaceAll(value, "$size", remoteSize));
                }
                final var option = _getValue(value);
                if (option != null && "".equals(option.name)) {
                    value = option.op2 + option.value;
                }
            }
            return value == null || value.length() < 3
                    || _compare(value.substring(0, 2), ByteSize.parse(value.substring(2)).size(), remoteSize);
        }

        /**
         * _check age.
         *
         * @param remoteAge
         *            the remote age
         *
         * @return true, if successful
         *
         * @throws ScriptException
         *             the script exception
         */
        private boolean _checkAge(final long remoteAge) throws ScriptException {
            var value = _optionsList.get(HOST_ACQUISITION_FILEAGE.getName());
            if (value == null && (value = _setup.getString(HOST_ACQUISITION_FILEAGE)) != null) {
                if (value.indexOf("$age") != -1) {
                    // This is a string to evaluate!
                    return ScriptManager.exec(Boolean.class, ScriptManager.JS,
                            Format.replaceAll(value, "$age", remoteAge));
                }
                final var option = _getValue(value);
                if (option != null && "".equals(option.name)) {
                    value = option.op2 + option.value;
                }
            }
            return value == null || value.length() < 3
                    || _compare(value.substring(0, 2), Format.getDuration(value.substring(2)), remoteAge);
        }

        /**
         * Gets the boolean.
         *
         * @param option
         *            the option
         *
         * @return true, if successful
         */
        private boolean _getBoolean(final ECtransOptions option) {
            try {
                final var result = _getString(null, true, option.getName(), option.getDefaultString());
                return "true".equalsIgnoreCase(result) || "yes".equalsIgnoreCase(result);
            } catch (final Throwable t) {
                return option.getDefaultBoolean();
            }
        }

        /**
         * Gets the int.
         *
         * @param option
         *            the option
         *
         * @return the int
         */
        private int _getInt(final ECtransOptions option) {
            try {
                return Integer.parseInt(_getString(null, true, option.getName(), option.getDefaultString()));
            } catch (final Throwable t) {
                return option.getDefaultInteger();
            }
        }

        /**
         * By default get the options from the line and the global host configuration only.
         *
         * @param option
         *            the option
         * @param defaultValue
         *            the default value
         *
         * @return the string
         */
        private String _getString(final ECtransOptions option, final String defaultValue) {
            return _getString(null, true, option.getName(), defaultValue);
        }

        /**
         * Gets the string.
         *
         * @param option
         *            the option
         *
         * @return the string
         */
        private String _getString(final ECtransOptions option) {
            return _getString(null, true, option.getName(), option.getDefaultString());
        }

        /**
         * Gets the string.
         *
         * @param params
         *            the params
         * @param useLineOption
         *            the use line option
         * @param option
         *            the option
         * @param defaultValue
         *            the default value
         *
         * @return the string
         */
        private String _getString(final Map<String, String> params, final boolean useLineOption, final String option,
                final String defaultValue) {
            // First check the option in the parameter itself e.g.
            // $date[dateformat=YYYY,...]
            var value = params != null ? params.get(option) : null;
            // If there is no option found then search the options shared
            // with the line e.g. [dateformat=YYYY,...]/mydir/{test(.*).tmp}
            // unless asked not to do so!
            value = value == null ? useLineOption ? _optionsList.get(option) : null : value;
            if (value != null && value.length() > 2) {
                // We found the option let's remove the operator
                return value.substring(2);
            }
            // Not found let's search the host configuration
            return _setup.get(option, defaultValue);
        }

        /**
         * Gets the string.
         *
         * @param params
         *            the params
         * @param useLineOption
         *            the use line option
         * @param option
         *            the option
         *
         * @return the string
         */
        private String _getString(final Map<String, String> params, final boolean useLineOption,
                final ECtransOptions option) {
            return _getString(params, useLineOption, option.getName(), option.getDefaultString());
        }

        /**
         * Display the data according to the options.
         *
         * @param params
         *            the params
         * @param useOptions
         *            the use options
         *
         * @return the string
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        private String _getFormatedDate(final Map<String, String> params, final boolean useOptions) throws IOException {
            // How should we display the date?
            final var format = _getString(params, useOptions, HOST_ACQUISITION_DATEFORMAT);
            // Do we have a delta to apply to the date?
            final var delta = _getString(params, useOptions, HOST_ACQUISITION_DATEDELTA);
            // If we want to parse the date from a source what pattern
            // should we use?
            final var pattern = _getString(params, useOptions, HOST_ACQUISITION_DATEPATTERN.getName(), format);
            // Do we have a source to parse the date from?
            final var source = _getString(params, useOptions, HOST_ACQUISITION_DATESOURCE);
            final long date;
            if (isNotEmpty(source)) {
                // Let's parse the date from the source according to the
                // provided pattern!
                final var simpleFormat = new SimpleDateFormat(pattern);
                try {
                    date = simpleFormat.parse(source).getTime();
                } catch (final Throwable t) {
                    throw new IOException("parsing date in " + source);
                }
            } else {
                // There is no source to parse the date from so let's use
                // the current time!
                date = System.currentTimeMillis();
            }
            // Return the formated date according to the delta and format
            // found
            return Format.formatTime(format, date + Format.getDuration(delta));
        }

        /**
         * Check if there are no parameters configured after the $date or $dirdate option .e.g $date[dateformat=YYYY].
         *
         * @param paramName
         *            the param name
         * @param string
         *            the string
         * @param useOptions
         *            the use options
         *
         * @return the string
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        private String _replaceDate(final String paramName, String string, final boolean useOptions)
                throws IOException {
            final var result = new StringBuilder();
            int index;
            while ((index = string.toLowerCase().indexOf(paramName)) != -1) {
                Map<String, String> params = null;
                result.append(string.substring(0, index));
                string = string.substring(index + paramName.length());
                if (string.startsWith("[") && (index = string.indexOf("]")) != -1) {
                    params = _getParams(string.substring(1, index), false);
                    string = string.substring(index + 1);
                }
                string = _getFormatedDate(params, useOptions) + string;
            }
            return result.append(string).toString();
        }

        /**
         * The Class Option.
         */
        private final class Option {

            /** The name. */
            String name;

            /** The value. */
            String value;

            /** The op1. */
            String op1; // Original operation (might be 1 character only)

            /** The op2. */
            String op2; // Updated operation (always 2 characters)
        }

        /**
         * Gets the value.
         *
         * @param option
         *            the option
         *
         * @return the option
         */
        private Option _getValue(String option) {
            final var result = new Option();
            // If the option is in the form name="value" then let's remove
            // the '="' and '"' at the end!
            final var pos1 = option.indexOf("=\"");
            if (pos1 > 0 && option.endsWith("\"") && pos1 + 2 < option.length()) {
                option = option.substring(0, pos1 + 1) + option.substring(pos1 + 2, option.length() - 1);
            }
            // Now we know we have an option in the format name=value,
            // name<=value,name>=value ...
            for (String op : new String[] { "==", ">=", "<=", "!=", "=", "<", ">" }) {
                final var pos2 = option.indexOf(op);
                if (pos2 >= 0) {
                    // We might have an empty name!
                    result.name = option.substring(0, pos2);
                    var value = option.substring(pos2 + op.length());
                    // If '>', '<' or '=' then set '>>',
                    // '<<' or '==' to encode the operation
                    // on 2 characters
                    result.op1 = op;
                    if (op.length() == 1) {
                        op += op;
                    }
                    result.op2 = op;
                    // If the value is between quotes then let's remove
                    // them!
                    if (value.startsWith("\"") && value.endsWith("\"") && value.length() > 1) {
                        value = value.substring(1, value.length() - 1);
                    }
                    result.value = value;
                    return result;
                }
            }
            // Parsing error?
            return null;
        }

        /**
         * Update status.
         *
         * @param messages
         *            the messages
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         * @throws DataBaseException
         *             Signals that a DataBase exception has occurred.
         */
        @Override
        public void info(final String... messages) throws IOException, DataBaseException {
            final var sb = new StringBuilder();
            for (final String message : messages) {
                sb.append(sb.isEmpty() ? "" : "\n").append(getCurrentTime()).append(message);
            }
            _update(sb);
        }

        /**
         * Update status.
         *
         * @param messages
         *            the messages
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         * @throws DataBaseException
         *             Signals that a DataBase exception has occurred.
         */
        @Override
        public void warn(final String... messages) throws IOException, DataBaseException {
            final var sb = new StringBuilder();
            for (final String message : messages) {
                sb.append(sb.isEmpty() ? "" : "\n").append("err:").append(getCurrentTime()).append(message);
            }
            _update(sb);
        }

        /**
         * Update status.
         *
         * @param progress
         *            the progress
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        private void _update(final StringBuilder progress) throws IOException {
            _master._updateHostOutput(_host,
                    Format.compressBase64(_summary.append(_summary.isEmpty() ? "" : "\n").append(progress)));
        }

        /**
         * Adds the.
         *
         * @param value
         *            the value
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        public void _add(final String value) throws IOException {
            synchronized (_tmpOut) {
                try {
                    _tmpOut.write(("\n" + value).getBytes());
                } catch (final IOException e) {
                    // Ignore!
                }
            }
            if (_debug) {
                MasterServer._log.debug(value);
            }
        }

        /**
         * Adds the.
         *
         * @param name
         *            the name
         * @param value
         *            the value
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        private void _add(final String name, final String value) throws IOException {
            _add(name + ": " + value);
        }

        /**
         * Look for string inside delimiters.
         *
         * @param delimiter1
         *            the delimiter1
         * @param delimiter2
         *            the delimiter2
         * @param string
         *            the string
         *
         * @return the int
         */
        private int _getNext(final char delimiter1, final char delimiter2, final String string) {
            if (string.charAt(0) == delimiter1) {
                var index = 0;
                var in = 0;
                for (final char c : string.toCharArray()) {
                    index++;
                    if (c == delimiter1) {
                        in++;
                    }
                    if (c == delimiter2) {
                        in--;
                    }
                    if (in == 0) {
                        return index;
                    }
                }
            }
            return -1;
        }

        /**
         * Parse options in the form filesize=">1234";fileage>=24h,dateformat=yyyyMMdd;datedelta="-1d"
         * ;metadata="dir=$date[dateformat=yyyy]/test".
         *
         * @param options
         *            the options
         * @param log
         *            the log
         *
         * @return the hashtable
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        private Map<String, String> _getParams(final String options, final boolean log) throws IOException {
            final Map<String, String> params = new ConcurrentHashMap<>();
            final var current = new StringBuilder();
            Character delimiter = null;
            var in = 0;
            for (final char c : options.toCharArray()) {
                if (delimiter != null && c == delimiter) {
                    delimiter = null;
                    continue;
                }
                if (delimiter == null) {
                    if (c == '"' || c == '\'') {
                        delimiter = c;
                        continue;
                    } else if (in == 0 && (c == ';' || c == ',')) {
                        _addParam(params, current.toString(), log);
                        current.setLength(0);
                        continue;
                    } else if (c == '[') {
                        in++;
                    } else if (c == ']') {
                        in--;
                    }
                }
                current.append(c);
            }
            if (current.length() > 0) {
                _addParam(params, current.toString(), log);
            }
            return params;
        }

        /**
         * Process the option and add it to the list.
         *
         * @param params
         *            the params
         * @param string
         *            the string
         * @param log
         *            the log
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        private void _addParam(final Map<String, String> params, final String string, final boolean log)
                throws IOException {
            final var option = _getValue(string);
            if (option != null) {
                params.put(option.name, option.op2 + option.value);
                if (log) {
                    _add("Opt" + (++_size), option.name + option.op1 + option.value);
                }
            } else {
                MasterServer._log.debug("Error parsing option: " + string);
                if (log) {
                    _add("err:Opt" + (++_size) + ": " + string + " (parsing error)");
                }
            }
        }

        /**
         * Parses an acquisition file entry.
         *
         * @param base
         *            the base
         * @param ar
         *            the ar
         * @param copy
         *            the copy
         * @param namePattern
         *            the name pattern
         * @param currentPath
         *            the current path
         * @param destination
         *            the destination
         * @param entry
         *            the entry
         * @param body
         *            the body
         * @param message
         *            the message
         *
         * @throws ScriptException
         *             the script exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        private void _parseEntry(final ECpdsBase base, final AcquisitionResult ar, final Host copy,
                final String namePattern, final String currentPath, final Destination destination,
                final FileEntry entry, final String body, final String message,
                final AcquisitionNotification notification) throws ScriptException, IOException {
            String outcome;
            if (entry.exception == null) {
                // File entry found with no error
                final var symlink = FtpParser.SYMBOLIC_LINK_TYPE == entry.type;
                final var useSymlink = _getBoolean(HOST_ACQUISITION_USE_SYMLINK);
                if (!useSymlink && symlink) {
                    // We are not allowed to use the symbolic links!
                    outcome = "not-selected: symbolic link (useSymlink=no)";
                } else if (!symlink && FtpParser.FILE_TYPE != entry.type) {
                    // Don't select directories or files with an unknown
                    // type
                    outcome = "not-selected: directory or unknown type";
                } else {
                    final var onlyValidTime = _getBoolean(HOST_ACQUISITION_ONLY_VALID_TIME);
                    if (onlyValidTime && entry.time == -1) {
                        outcome = "not-selected: date/time could not be parsed";
                    } else {
                        // It is a valid data/time or we didn't check!
                        try {
                            if ("".equals(namePattern) || entry.name.matches(namePattern)) {
                                // It does match the required pattern!
                                outcome = _processValidEntry(base, ar, copy, currentPath, destination, entry, symlink,
                                        body, message, notification);
                            } else {
                                outcome = "not-selected: wrong pattern";
                            }
                        } catch (final PatternSyntaxException e) {
                            MasterServer._log.warn("Pattern matching {} -> {}", entry.name, namePattern, e);
                            outcome = "not-selected: syntax error in regex pattern";
                        }
                    }
                }
            } else {
                // Error parsing the file entry
                outcome = "error: " + Format.getMessage(entry.exception);
            }
            // Display the current line
            _add((outcome.startsWith("selected") ? "log:" : "err:") + entry.line + " (" + outcome + ")");
        }

        /**
         * A match has been found with a file entry, so let's process it.
         *
         * @param base
         *            the base
         * @param ar
         *            the ar
         * @param copy
         *            the copy
         * @param currentPath
         *            the current path
         * @param destination
         *            the destination
         * @param entry
         *            the entry
         * @param symlink
         *            the symlink
         * @param body
         *            the body
         * @param message
         *            the message
         *
         * @return the string
         *
         * @throws ScriptException
         *             the script exception
         */
        String _processValidEntry(final ECpdsBase base, final AcquisitionResult ar, final Host copy,
                final String currentPath, final Destination destination, final FileEntry entry, final boolean symlink,
                final String body, final String message, final AcquisitionNotification notification)
                throws ScriptException, IOException {
            // Check if this is an absolute or relative entry name
            final var name = Format.cleanTextContent(entry.name);
            final var lower = name.toLowerCase();
            final String initialTarget;
            final String original;
            var windows = false;
            var url = false;
            if ((url = lower.startsWith("http://") || lower.startsWith("https://")) // URL
                    || lower.startsWith("/") // UNIX
                    || (windows = lower.startsWith("\\"))) // Windows
            {
                // This is an absolute entry name (forget currentPath)
                original = name;
                if (url) {
                    // If this is a symlink (e.g. from http.urlIsFileName), the link field
                    // holds the forced filename — use it directly as the target.
                    if (symlink && isNotEmpty(entry.link)) {
                        initialTarget = entry.link;
                    } else {
                        final var urlName = new File(name).getName();
                        if (_getBoolean(HOST_ACQUISITION_REMOVE_PARAMETERS)) {
                            final var index = urlName.indexOf("?");
                            // If we detect a ? then let's remove the parameters!
                            initialTarget = index != -1 ? urlName.substring(0, index) : urlName;
                        } else {
                            initialTarget = urlName;
                        }
                    }
                } else {
                    // Is it UNIX or Windows?
                    // When a symlink target is a plain filename (no path separator), it
                    // represents the desired output filename (e.g. set via http.urlIsFileName)
                    // — use it directly rather than extracting the last path segment of name.
                    if (symlink && isNotEmpty(entry.link) && !entry.link.contains("/") && !entry.link.contains("\\")) {
                        initialTarget = entry.link;
                    } else {
                        final var index = name.lastIndexOf(windows ? "\\" : "/");
                        // If we detect a file separator then let's remove the
                        // parent directory!
                        initialTarget = index != -1 ? name.substring(index + 1) : name;
                    }
                }
            } else {
                // This is a relative entry name
                initialTarget = name;
                original = currentPath + initialTarget;
            }
            // Which target should be used effectively?
            var effectiveTarget = _getString(HOST_ACQUISITION_TARGET);
            final var filename = new File(initialTarget).getName();
            try {
                effectiveTarget = Format.replaceAllExt(effectiveTarget, "$destination", destination.getName());
                effectiveTarget = Format.replaceAllExt(effectiveTarget, "$name", filename);
                effectiveTarget = Format.replaceAllExt(effectiveTarget, "$target", initialTarget);
                effectiveTarget = Format.replaceAllExt(effectiveTarget, "$original", original);
                effectiveTarget = Format.replaceAllExt(effectiveTarget, "$link", entry.link);
                effectiveTarget = _replaceDate("$dirdate", effectiveTarget, true);
                effectiveTarget = _replaceDate("$date", effectiveTarget, false);
                effectiveTarget = Format.replaceAll(effectiveTarget, "$timestamp", entry.time / 1000L * 1000L);
            } catch (final IOException _) {
            }
            // Let's check the size and age options. We obviously only check
            // the time if it is valid!
            if ((symlink || _checkSize(entry.size))
                    && (entry.time == -1 || _checkAge(System.currentTimeMillis() - entry.time))) {
                // Match the size or age option. What should we do with this
                // selection?
                final var action = _getString(HOST_ACQUISITION_ACTION).toLowerCase();
                if ("delete".equals(action)) {
                    // This is a delete request!
                    try {
                        TransferScheduler.del(ar.server, copy, original);
                        // The file has been selected and deleted
                        // successfully
                        return "selected: deleted";
                    } catch (final Throwable t) {
                        // Error when trying to delete the data file
                        return "not-selected: delete error (" + Format.getMessage(t) + ")";
                    }
                } else {
                    // If we have a data file with the same name but a
                    // different file time do we consider it as a single
                    // file or as different files? If we don't know the time
                    // then we just use the original name.
                    final var uniqueName = notification != null ? notification.uniqueName()
                            : (_getBoolean(HOST_ACQUISITION_UNIQUE_BY_TARGET_ONLY) ? ""
                                    : _getBoolean(HOST_ACQUISITION_USE_TARGET_AS_UNIQUE_NAME) ? initialTarget
                                            : original)
                                    + (entry.time == -1 || !_getBoolean(HOST_ACQUISITION_UNIQUE_BY_NAME_AND_TIME) ? ""
                                            : "." + entry.time);
                    if ("queue".equals(action)) {
                        // This is a queue request. Check if the file is not
                        // already in the database?
                        final var standby = _getBoolean(HOST_ACQUISITION_STANDBY);
                        final var transfers = base.getScheduledDataTransfer(
                                TransferManagement.getUniqueKey(standby, _desName, effectiveTarget, uniqueName),
                                _desName);
                        final var found = transfers.length > 0;
                        var force = false;
                        if (found) {
                            // The datafile is already in the database so
                            // let's find the time and size and check if it
                            // should be requeued or not?
                            final var dataFile = transfers[0].getDataFile();
                            // Let's be compatible with the old parameters
                            // if the new parameter requeueon is not found!
                            final var requeueonupdate = _getBoolean(HOST_ACQUISITION_REQUEUEONUPDATE);
                            final var requeueonsamesize = _getBoolean(HOST_ACQUISITION_REQUEUEONSAMESIZE);
                            var requeueon = _getString(HOST_ACQUISITION_REQUEUEON, requeueonupdate
                                    ? requeueonsamesize ? "$time2 > $time1" : "$time2 > $time1 && $size2 != $size1"
                                    : "false");
                            try {
                                requeueon = Format.replaceAll(requeueon, "$time1", dataFile.getTimeFile().getTime());
                                requeueon = Format.replaceAll(requeueon, "$size1", dataFile.getSize());
                                requeueon = Format.replaceAll(requeueon, "$time2", entry.time / 1000L * 1000L);
                                requeueon = Format.replaceAll(requeueon, "$size2", entry.size >= 0 ? entry.size : -1);
                                requeueon = Format.replaceAllExt(requeueon, "$destination", destination.getName());
                                requeueon = Format.replaceAllExt(requeueon, "$target", initialTarget);
                                requeueon = Format.replaceAllExt(requeueon, "$original", original);
                                // Should we requeue it?
                                force = ScriptManager.exec(Boolean.class, ScriptManager.JS, requeueon);
                            } catch (final Throwable t) {
                                return "not-selected: requeueon error (" + Format.getMessage(t) + ")";
                            }
                        }
                        var failedOnly = false;
                        if (found && !force) {
                            for (final DataTransfer transfer : transfers) {
                                final var status = transfer.getStatusCode();
                                if (StatusFactory.FAIL.equals(status)) {
                                    // The file has not been retrieved so
                                    // let's re-queue it
                                    MasterServer._log.debug("Requeuing DataFile: " + transfer.getDataFileId()
                                            + " (retrieval didn't succeed last time)");
                                    failedOnly = true;
                                    force = true;
                                    break;
                                }
                            }
                        }
                        if (found && !force) {
                            // The file has already been recorded in the
                            // database
                            return "not-selected: exists DatafileId=" + transfers[0].getDataFileId();
                        } else {
                            // The file has not been found in the
                            // database or should be re-queued. Let's
                            // schedule it for retrieval
                            var metadata = _getString(HOST_ACQUISITION_METADATA);
                            try {
                                metadata = Format.replaceAllExt(metadata, "$destination", destination.getName());
                                metadata = Format.replaceAllExt(metadata, "$name", filename);
                                metadata = Format.replaceAllExt(metadata, "$target", initialTarget);
                                metadata = Format.replaceAllExt(metadata, "$original", original);
                                metadata = Format.replaceAllExt(metadata, "$link", entry.link);
                                metadata = _replaceDate("$dirdate", metadata, true);
                                metadata = _replaceDate("$date", metadata, false);
                                metadata = Format.replaceAll(metadata, "$timestamp", entry.time / 1000L * 1000L);
                                final var hasInlineBody = notification == null ? isNotEmpty(body) : body != null;
                                final String fileMetadata;
                                if (notification != null) {
                                    if (_getBoolean(HOST_ACQUISITION_NORETRIEVAL))
                                        throw new IOException(
                                                "Notification profiles require acquisition.noRetrieval=no");
                                    fileMetadata = (isEmpty(metadata) ? "" : metadata + ",") + notification.metadata();
                                    if (hasInlineBody && notification.integrity() != null)
                                        notification.integrity().verifyContent(Base64.getDecoder().decode(body));
                                } else {
                                    fileMetadata = metadata;
                                }
                                final long dataFileId;
                                final long notificationId;
                                if (!isEmpty(message)) {
                                    // Let's push the notification message with the correct extension!
                                    final var payLoadName = effectiveTarget
                                            + _getString(HOST_ACQUISITION_PAYLOAD_EXTENSION);
                                    notificationId = ECpdsClient.put(
                                            "Notification from Acquisition Host=" + _host.getName() + " ("
                                                    + _host.getNickname() + ") on DataMover=" + ar.server.getName()
                                                    + " for Destination=" + _desName,
                                            _host.getLogin(), destination, _host.getName(), metadata, payLoadName,
                                            payLoadName, payLoadName, entry.time, -1, false,
                                            _getInt(HOST_ACQUISITION_PRIORITY), _getString(HOST_ACQUISITION_LIFETIME),
                                            null, standby, force, failedOnly, false,
                                            _getString(HOST_ACQUISITION_TRANSFERGROUP),
                                            Base64.getDecoder().decode(message));
                                } else {
                                    notificationId = -1;
                                }
                                if (!hasInlineBody) {
                                    // Retrieval is going to be scheduled
                                    dataFileId = ECpdsClient.put(
                                            "Scheduled from Acquisition Host=" + _host.getName() + " ("
                                                    + _host.getNickname() + ") on DataMover=" + ar.server.getName()
                                                    + " for Destination=" + _desName,
                                            _host.getLogin(), destination, _host.getName(), fileMetadata, original,
                                            uniqueName, effectiveTarget, entry.time, entry.size >= 0 ? entry.size : -1,
                                            _getBoolean(HOST_ACQUISITION_EVENT), _getInt(HOST_ACQUISITION_PRIORITY),
                                            _getString(HOST_ACQUISITION_LIFETIME), null, standby,
                                            _getString(HOST_ACQUISITION_GROUPBY,
                                                    "ACQ_" + _desName + "_" + _host.getName()),
                                            _getBoolean(HOST_ACQUISITION_NORETRIEVAL), force, failedOnly,
                                            _getBoolean(HOST_ACQUISITION_DELETEORIGINAL),
                                            _getString(HOST_ACQUISITION_TRANSFERGROUP));
                                    // The file has been selected and registered in the database for later retrieval
                                    return "selected: " + (force ? "re-" : "") + "scheduled with DatafileId="
                                            + dataFileId + (notificationId >= 0
                                                    ? " - notification DatafileId=" + notificationId : "");
                                } else {
                                    // Sending the body
                                    dataFileId = ECpdsClient.put(
                                            "Received from Acquisition Host=" + _host.getName() + " ("
                                                    + _host.getNickname() + ") on DataMover=" + ar.server.getName()
                                                    + " for Destination=" + _desName,
                                            _host.getLogin(), destination, _host.getName(), fileMetadata, original,
                                            uniqueName, effectiveTarget, entry.time, entry.size >= 0 ? entry.size : -1,
                                            _getBoolean(HOST_ACQUISITION_EVENT), _getInt(HOST_ACQUISITION_PRIORITY),
                                            _getString(HOST_ACQUISITION_LIFETIME), null, standby, force, failedOnly,
                                            _getBoolean(HOST_ACQUISITION_DELETEORIGINAL),
                                            _getString(HOST_ACQUISITION_TRANSFERGROUP),
                                            Base64.getDecoder().decode(body));
                                    // The file has been registered in the database and sent to the mover
                                    return "selected: " + (force ? "re-" : "") + "received with DatafileId="
                                            + dataFileId + (notificationId >= 0
                                                    ? " - notification DatafileId=" + notificationId : "");
                                }
                            } catch (final Throwable t) {
                                // Error when trying to register the
                                // data file
                                final var error = Format.getMessage(t);
                                if (error.indexOf("DataFile already exist") != -1) {
                                    // already exists and submitted between
                                    // the check and the insert!
                                    return "not-selected:" + error.substring(8);
                                } else {
                                    MasterServer._log.warn("Registration error", t);
                                    return "not-selected: registration error (" + error + ")";
                                }
                            }
                        }
                    } else {
                        // Unknown action requested!
                        return "not-selected: unknown action requested (" + action + ")";
                    }
                }
            }
            if (symlink) {
                return "not-selected: wrong age on symbolic link (time=" + Format.formatTime(entry.time) + ")";
            } else {
                return "not-selected: wrong size/age (time=" + Format.formatTime(entry.time) + ",size=" + entry.size
                        + ")";
            }
        }

        /**
         * The Class ListThread.
         */
        // Listing meant to be used in multiple instances in parallel!
        final class ListThread extends ExecutorRunnable {

            /** The base. */
            final ECpdsBase _base;

            /** The ar. */
            final AcquisitionResult _ar;

            /** The copy. */
            final Host _copy;

            /** The name pattern. */
            final String _namePattern;

            /** The current path. */
            final String _currentPath;

            /** The destination. */
            final Destination _destination;

            /** The entry. */
            final FileEntry _entry;

            /** The body. */
            final String _body;

            /** The message. */
            final String _message;

            final AcquisitionNotification _notification;

            /**
             * Instantiates a new list thread.
             *
             * @param manager
             *            the manager
             * @param base
             *            the base
             * @param ar
             *            the ar
             * @param copy
             *            the copy
             * @param namePattern
             *            the name pattern
             * @param currentPath
             *            the current path
             * @param destination
             *            the destination
             * @param entry
             *            the entry
             * @param body
             *            the body
             * @param message
             *            the message
             */
            ListThread(final ExecutorManager<ListThread> manager, final ECpdsBase base, final AcquisitionResult ar,
                    final Host copy, final String namePattern, final String currentPath, final Destination destination,
                    final FileEntry entry, final String body, final String message,
                    final AcquisitionNotification notification) {
                super(manager);
                _base = base;
                _ar = ar;
                _copy = copy;
                _namePattern = namePattern;
                _currentPath = currentPath;
                _destination = destination;
                _entry = entry;
                _body = body;
                _message = message;
                _notification = notification;
            }

            /**
             * Process.
             *
             * @throws ScriptException
             *             the script exception
             * @throws IOException
             *             Signals that an I/O exception has occurred.
             */
            @Override
            public void process() throws ScriptException, IOException {
                _parseEntry(_base, _ar, _copy, _namePattern, _currentPath, _destination, _entry, _body, _message,
                        _notification);
            }
        }

        /**
         * Gets the script.
         *
         * @param host
         *            the host
         *
         * @return the script
         */
        // Provide some parameters
        private String getScript(final Host host) {
            final var sb = new StringBuilder(host.getDir());
            final var method = host.getTransferMethod();
            final var module = method.getECtransModule();
            Format.replaceAll(sb, "$host[name]", host.getName());
            Format.replaceAll(sb, "$host[comment]", host.getComment());
            Format.replaceAll(sb, "$host[host]", host.getHost());
            Format.replaceAll(sb, "$host[login]", host.getLogin());
            Format.replaceAll(sb, "$host[passwd]", host.getPasswd());
            Format.replaceAll(sb, "$host[userMail]", host.getUserMail());
            Format.replaceAll(sb, "$host[networkCode]", host.getNetworkCode());
            Format.replaceAll(sb, "$host[networkName]", host.getNetworkName());
            Format.replaceAll(sb, "$host[nickname]", host.getNickname());
            Format.replaceAll(sb, "$transferMethod[name]", method.getName());
            Format.replaceAll(sb, "$transferMethod[comment]", method.getComment());
            Format.replaceAll(sb, "$transferMethod[value]", method.getValue());
            Format.replaceAll(sb, "$ectransModule[name]", module.getName());
            return sb.toString().trim();
        }

        /**
         * Configurable run.
         */
        @Override
        public void configurableRun() {
            var completed = false;
            var interrupted = false;
            final var key = _getKey(_host);
            MasterServer._log.debug("Starting AcquisitionThread " + key);
            Scanner scanner = null;
            try {
                final var base = _master.getDataBase(ECpdsBase.class);
                final var destination = base.getDestination(_desName);
                if (StatusFactory.STOP.equals(destination.getStatusCode())) {
                    MasterServer._log.debug("AcquisitionThread " + key + " interrupted");
                    _running = false;
                }
                _host = base.getHost(_host.getName());
                _setup = HOST_ACQUISITION.getECtransSetup(_host.getData());
                _localMaximumDuration = _setup
                        .get(HOST_ACQUISITION_MAXIMUM_DURATION, Duration.ofMillis(_localMaximumDuration)).toMillis();
                _localInterruptSlow = _setup.getOptionalBoolean(HOST_ACQUISITION_INTERRUPT_SLOW)
                        .orElse(_localInterruptSlow);
                final var script = getScript(_host);
                final BufferedReader reader;
                if (script.startsWith("$(") && script.endsWith(")")) {
                    // This is a script so we just evaluate it on one of the allocated data-mover
                    // and return the result string!
                    final var er = TransferScheduler.execution(this, _desName, _host,
                            script.substring(2, script.length() - 1));
                    if (!er.complete)
                        return;
                    // We update the string with the result of the code
                    // and we continue the processing!
                    reader = new BufferedReader(new InputStreamReader(er.in));
                } else {
                    reader = new BufferedReader(new StringReader(script));
                }
                _debug = _getBoolean(HOST_ACQUISITION_DEBUG);
                final var start = System.currentTimeMillis();
                final var startDate = Format.formatTime(start);
                _add("Date", startDate + " (start=" + start + " ago)");
                info("Acquisition started (start=" + start + " ago)");
                var listingCount = 0;
                String currentPath;
                // Build a summary to display the progress of the processing!
                final var progress = new StringBuilder();
                ExecutorManager<ListThread> manager = null;
                // Parse the list of directories in the form
                // "[filesize=1234;fileage>=24h;dateformat=yyyyMMdd;datedelta=-1d]/home/test/acq-$date/{filename-regex}"
                while (_running && !(interrupted = interrupted()) && (currentPath = reader.readLine()) != null) {
                    currentPath = currentPath.trim();
                    if (currentPath.isEmpty()) {
                        continue;
                    }
                    _optionsList.clear(); // remove previous values
                    _size = 0; // reset the option count!
                    listingCount++;
                    int pos;
                    if ((pos = _getNext('[', ']', currentPath)) != -1) {
                        final var options = currentPath.substring(1, pos - 1);
                        currentPath = currentPath.substring(pos);
                        _optionsList.putAll(_getParams(options, true));
                    }
                    // Now the current path is in the form
                    // "/home/systems/syi/acq-$date/{filename-regex}"
                    var namePattern = "";
                    if (currentPath.endsWith("}") && (pos = currentPath.indexOf("{")) != -1) {
                        namePattern = currentPath.substring(pos + 1, currentPath.length() - 1);
                        currentPath = currentPath.substring(0, pos);
                    }
                    // Now the current path should be in the form "/home/systems/syi/acq-$date/".
                    // Replace the $date parameter according to the options (format and delta)!
                    currentPath = _replaceDate("$date", currentPath, true);
                    namePattern = _replaceDate("$date", namePattern, true); // {...}
                    var regexPattern = _getString(HOST_ACQUISITION_REGEX_PATTERN);
                    regexPattern = _replaceDate("$date", regexPattern, true);
                    regexPattern = Format.replaceAll(regexPattern, "$namePattern", namePattern);
                    var wildcardFilter = _getString(HOST_ACQUISITION_WILDCARD_FILTER);
                    wildcardFilter = _replaceDate("$date", wildcardFilter, true);
                    final var path = currentPath + wildcardFilter;
                    final var formattedPath = path + (regexPattern.length() > 0 ? " (" + regexPattern + ")" : "");
                    _add("Path", formattedPath);
                    _add("Pattern", namePattern);
                    info("Processing " + formattedPath);
                    // Configure the host for the data mover
                    final var copy = (Host) _host.clone();
                    copy.setDir("");
                    progress.setLength(0);
                    final var ar = TransferScheduler.acquisition(this, _desName, copy, path,
                            regexPattern.length() > 0 ? regexPattern : null,
                            _setup.getBoolean(HOST_ACQUISITION_LIST_SYNCHRONOUS));
                    if (_running && ar.complete) {
                        info("Starting parsing of the listing.", StatusUpdate.IN_PROGRESS);
                        _add(LINE_SEPARATOR);
                        scanner = new Scanner(ar.in);
                        scanner.useDelimiter("\n");
                        var filesCount = 0;
                        // Create the ListManager if required!
                        manager = _setup.getBoolean(HOST_ACQUISITION_LIST_PARALLEL)
                                ? new ExecutorManager<>(_setup.getInteger(HOST_ACQUISITION_LIST_MAX_WAITING),
                                        _setup.getInteger(HOST_ACQUISITION_LIST_MAX_THREADS))
                                : null;
                        // Get the list of files from the remote site
                        while (_running && scanner.hasNext()) {
                            // We don't process the list of files at once as it might be huge and would
                            // consume a lot of memory!
                            final var entry = scanner.next();
                            if (entry.startsWith("err:")) {
                                // This was an error so we just report it!
                                _add(entry);
                            } else {
                                final AcquisitionListing listing;
                                try {
                                    listing = AcquisitionListing.parse(entry);
                                } catch (final IOException e) {
                                    _add("err:Invalid acquisition notification metadata");
                                    MasterServer._log.warn("Invalid acquisition notification metadata", e);
                                    continue;
                                }
                                final var entries = FtpParser.parseDir(_getString(HOST_ACQUISITION_REGEX_FORMAT),
                                        _getString(HOST_ACQUISITION_SYSTEM_KEY).toUpperCase(),
                                        _getString(HOST_ACQUISITION_DEFAULT_DATE_FORMAT),
                                        _getString(HOST_ACQUISITION_RECENT_DATE_FORMAT),
                                        _getString(HOST_ACQUISITION_SERVER_LANGUAGE_CODE).toLowerCase(),
                                        _getString(HOST_ACQUISITION_SHORT_MONTH_NAMES),
                                        _getString(HOST_ACQUISITION_SERVER_TIME_ZONE_ID),
                                        new String[] { listing.line() });
                                // Did we find anything?
                                if (entries.length == 0) {
                                    continue;
                                }
                                if (manager != null) {
                                    // Do we have to start the ListManager?
                                    if (filesCount == 0) {
                                        manager.start();
                                    }
                                    // Let's start the listing!
                                    manager.put(new ListThread(manager, base, ar, copy, namePattern, currentPath,
                                            destination, entries[0], listing.body(), listing.message(),
                                            listing.notification()));
                                } else {
                                    _parseEntry(base, ar, copy, namePattern, currentPath, destination, entries[0],
                                            listing.body(), listing.message(), listing.notification());
                                }
                                filesCount++;
                            }
                        }
                        if (manager != null) {
                            // We don't want to take more jobs!
                            manager.stopRun();
                            // And now we wait for all the Threads to complete!
                            try {
                                manager.join();
                            } catch (final InterruptedException e) {
                            }
                        }
                        // If we have no entries then we print a message!
                        if (filesCount == 0) {
                            _add("err:(empty)");
                        }
                        _add(LINE_SEPARATOR);
                        _add("List", progress.append("Found ").append(filesCount).append(" file(s) using DataMover=")
                                .append(ar.server.getName()).toString());
                        // Let's update the progress on the fly for each new entry!
                        _update(progress.insert(0, getCurrentTime()));
                    } else {
                        // Couldn't connect to the remote host to list the files
                        _add(LINE_SEPARATOR);
                        _add("err:" + progress.append(ar.message).append(ar.server != null
                                ? " using DataMover=" + ar.server.getName() : " (no DataMover available)").toString());
                        _add(LINE_SEPARATOR);
                        // Let's update the progress on the fly for each new entry!
                        _update(progress.insert(0, getCurrentTime()).insert(0, "err:"));
                    }
                }
                final var fullDuration = Format.formatDuration(System.currentTimeMillis() - start);
                if (!interrupted) {
                    // Completed, let's give some information about the overall process!
                    final String message;
                    if (listingCount == 0) {
                        message = "No directory to process, please review the configuration";
                        MasterServer._log.warn(message);
                        warn(message);
                        _add("err:" + message);
                    } else {
                        message = "Completed " + listingCount + " listing(s) in " + fullDuration;
                        MasterServer._log.info(message);
                        info(message);
                        _add(message);
                    }
                    completed = true;
                } else {
                    // The activity was interrupted!
                    final var message = "Acquisition interrupted after " + fullDuration;
                    MasterServer._log.warn(message);
                    warn(message);
                    _add("err:" + message);
                    if (manager != null)
                        manager.stopRun();
                }
                _tmpOut.close();
                _out.close();
                if (!_fileOutputDone.getAbsolutePath().equals(_fileOutputTmp.getAbsolutePath())) {
                    // Tmp and done file names are different, so we need to move tmp to done!
                    _fileOutputDone.delete();
                    _fileOutputTmp.renameTo(_fileOutputDone);
                }
            } catch (final Throwable t) {
                MasterServer._log.warn("Running AcquisitionThread " + key, t);
                try {
                    warn(Format.getMessage(t));
                } catch (final Throwable ignored) {
                }
            } finally {
                StreamPlugThread.closeQuietly(scanner);
                StreamPlugThread.closeQuietly(_tmpOut);
                StreamPlugThread.closeQuietly(_out);
                _toRemove.add(key);
                MasterServer._log
                        .info("Host " + key + " acquisition: " + completed + (interrupted ? " (interrupted)" : ""));
            }
        }
    }

    /**
     * Gets the current time.
     *
     * @return the current time
     */
    private String getCurrentTime() {
        return "time=" + System.currentTimeMillis() + " ";
    }
}
