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
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import static ecmwf.common.text.Util.isNotEmpty;

import java.io.Closeable;

import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.timer.Timer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.mbean.MBeanManager;
import ecmwf.common.mbean.MBeanService;
import ecmwf.common.text.Format;

/**
 * The Class MBeanScheduler.
 */
public abstract class MBeanScheduler extends ECaccessScheduler implements MBeanService, Closeable {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(MBeanScheduler.class);

    /** The _manager. */
    private final MBeanManager _manager;

    /** The _name. */
    private final String _name;

    /**
     * Instantiates a new m bean scheduler.
     *
     * @param group
     *            the group
     * @param name
     *            the name
     */
    public MBeanScheduler(final String group, final String name) {
        setDelay(Timer.ONE_SECOND);
        MBeanManager manager = null;
        _name = name;
        if (isNotEmpty(name)) {
            setThreadNameAndCookie(name, null, null, null);
            // Make sure the name is not using invalid characters for the
            // MBeanManager!
            final var formattedName = name.replace(':', '_').replace('=', '_');
            try {
                manager = new MBeanManager(group + "=" + formattedName, this);
            } catch (final Throwable e) {
                _log.warn("Registering " + name + " (" + formattedName + ")", e);
            }
        }
        _manager = manager;
    }

    /**
     * Instantiates a new m bean scheduler.
     *
     * @param name
     *            the name
     */
    public MBeanScheduler(final String name) {
        this("ECaccess:service", name);
    }

    /**
     * Gets the monitor name.
     *
     * @return the monitor name
     */
    @Override
    public String getMonitorName() {
        return _name;
    }

    /**
     * Gets the MBean info.
     *
     * @return the MBean info
     */
    @Override
    public MBeanInfo getMBeanInfo() {
        return new MBeanInfo(this.getClass().getName(),
                "The MBeanScheduler is used to manage and monitor " + "a runtime ECaccessScheduler.",
                new MBeanAttributeInfo[] {
                        new MBeanAttributeInfo("StartDate", "java.util.Date",
                                "StartDate: when the MBeanScheduler has been started.", true, false, false),
                        new MBeanAttributeInfo("Monitor", "java.lang.String",
                                "Monitor: current status sent to Monitor.", true, false, false),
                        new MBeanAttributeInfo("ThreadName", "java.lang.String", "ThreadName: current thread name.",
                                true, false, false),
                        new MBeanAttributeInfo("Activity", "java.lang.String", "Activity: activity of this scheduler.",
                                true, false, false),
                        new MBeanAttributeInfo("StepTimeCurrent", "java.lang.String",
                                "StepTimeCurrent: duration of the current step.", true, false, false),
                        new MBeanAttributeInfo("StepTimeLast", "java.lang.String",
                                "StepTimeLast: duration of the last step.", true, false, false),
                        new MBeanAttributeInfo("SchedulerState", "int", "SchedulerState: state of the running thread.",
                                true, false, false),
                        new MBeanAttributeInfo("NextStepLast", "java.lang.String",
                                "NextStepLast: last next step result.", true, false, false),
                        new MBeanAttributeInfo("ThreadPriority", "int", "ThreadPriority: priority of the thread.", true,
                                true, false),
                        new MBeanAttributeInfo("IsSleeping", "boolean", "IsSleeping: the thread is sleeping.", true,
                                false, false),
                        new MBeanAttributeInfo("StepPeriodicity", "long",
                                "StepPeriodicity: delay between 2 steps in milliseconds.", true, true, false) },
                new MBeanConstructorInfo[0],
                new MBeanOperationInfo[] {
                        new MBeanOperationInfo("wakeup", "wakeup: wakeup the thread.", new MBeanParameterInfo[0],
                                "void", MBeanOperationInfo.ACTION),
                        new MBeanOperationInfo("hold", "hold: hold the thread.", new MBeanParameterInfo[0], "void",
                                MBeanOperationInfo.ACTION),
                        new MBeanOperationInfo("interrupt", "interrupt: interrupt the thread.",
                                new MBeanParameterInfo[0], "void", MBeanOperationInfo.ACTION) },
                new MBeanNotificationInfo[0]);
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
            if ("NextStepLast".equals(attributeName)) {
                return steps[getNextStep()];
            }
            if ("ThreadName".equals(attributeName)) {
                return getThreadName();
            }
            if ("IsSleeping".equals(attributeName)) {
                return isSleeping();
            }
            if ("StepTimeCurrent".equals(attributeName)) {
                return Format.formatDuration(getStepTime());
            }
            if ("Activity".equals(attributeName)) {
                return getActivity();
            }
            if ("StepTimeLast".equals(attributeName)) {
                return Format.formatDuration(getLastStepTime());
            }
            if ("StartDate".equals(attributeName)) {
                return getStartDate();
            }
            if ("ThreadPriority".equals(attributeName)) {
                return getPriority();
            }
            if ("StepPeriodicity".equals(attributeName)) {
                return getDelay();
            }
            if ("SchedulerState".equals(attributeName)) {
                return states[getSchedulerState()];
            }
            if ("Monitor".equals(attributeName)) {
                return getMonitorManager(null).toString();
            }
        } catch (final Exception e) {
            _log.warn("Getting an MBean attribute", e);
            throw new MBeanException(e);
        }
        throw new AttributeNotFoundException(
                "Cannot find " + attributeName + " attribute in " + this.getClass().getName());
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
        if ("ThreadPriority".equals(name)) {
            setPriority((Integer) value);
            return true;
        }
        if ("StepPeriodicity".equals(name)) {
            setDelay((Long) value);
            return true;
        }
        return false;
    }

    /**
     * Invoke.
     *
     * @param operationName
     *            the operation name
     * @param params
     *            the params
     * @param signature
     *            the signature
     *
     * @return the object
     *
     * @throws NoSuchMethodException
     *             the no such method exception
     * @throws MBeanException
     *             the MBean exception
     */
    @Override
    public Object invoke(final String operationName, final Object[] params, final String[] signature)
            throws NoSuchMethodException, MBeanException {
        try {
            if ("wakeup".equals(operationName) && signature.length == 0) {
                if (isOnHold()) {
                    setOnhold(false);
                } else {
                    wakeup();
                }
                return Boolean.TRUE;
            }
            if ("hold".equals(operationName) && signature.length == 0) {
                setOnhold(true);
                return Boolean.TRUE;
            }
            if ("interrupt".equals(operationName) && signature.length == 0) {
                interrupt();
                return Boolean.TRUE;
            }
        } catch (final Exception e) {
            _log.warn("Invoking the {} MBean method", operationName, e);
            throw new MBeanException(e);
        }
        throw new NoSuchMethodException(operationName);
    }

    /**
     * Close.
     */
    @Override
    public synchronized void close() {
        if (_manager != null) {
            _manager.unregister();
        }
    }

    /**
     * Shutdown.
     */
    @Override
    public void shutdown() {
        close();
        super.shutdown();
    }
}
