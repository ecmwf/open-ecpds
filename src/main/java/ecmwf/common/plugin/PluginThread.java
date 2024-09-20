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

package ecmwf.common.plugin;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.mbean.MBeanService;
import ecmwf.common.technical.ThreadService.ConfigurableRunnable;
import ecmwf.common.text.Format;

/**
 * The Class PluginThread.
 */
public abstract class PluginThread extends ConfigurableRunnable implements MBeanService {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(PluginThread.class);

    /** The _caller. */
    private static Object _caller = null;

    /** The _ref. */
    private final String _ref;

    /** The _params. */
    protected final Map<String, String> _params = new HashMap<>();

    /** The _container. */
    private PluginContainer _container = null;

    /** The _events names. */
    private final List<String> _eventsNames = Collections.synchronizedList(new ArrayList<>());

    /** The _events total. */
    private long _eventsTotal = 0;

    /**
     * Instantiates a new plugin thread.
     *
     * @param ref
     *            the ref
     * @param params
     *            the params
     */
    public PluginThread(final String ref, final Map<String, String> params) {
        _params.putAll(params);
        _ref = ref;
    }

    /**
     * Caller back.
     *
     * @param reset
     *            the reset
     */
    public void callerBack(final boolean reset) {
        _log.info(reset ? "Caller has been restarted" : "Caller is back");
    }

    /**
     * Caller gone.
     */
    public void callerGone() {
        _log.info("Caller is gone");
    }

    /**
     * Gets the caller.
     *
     * @param <T>
     *            the generic type
     * @param clazz
     *            the class
     *
     * @return the caller
     */
    public static <T> T getCaller(final Class<T> clazz) {
        return clazz.cast(_caller);
    }

    /**
     * Gets the plugin name.
     *
     * @return the plugin name
     */
    public abstract String getPluginName();

    /**
     * Gets the param.
     *
     * @param name
     *            the name
     *
     * @return the param
     */
    public final String getParam(final String name) {
        return _params.get(name);
    }

    /**
     * Gets the ref.
     *
     * @return the ref
     */
    public final String getRef() {
        return _ref;
    }

    /**
     * Gets the version.
     *
     * @return the version
     */
    public abstract String getVersion();

    /**
     * Sets the caller.
     *
     * @param caller
     *            the new caller
     */
    public static final void setCaller(final Object caller) {
        _caller = caller;
    }

    /**
     * Handle.
     *
     * @param event
     *            the event
     */
    public void handle(final PluginEvent<?> event) {
    }

    /**
     * Handle.
     *
     * @param events
     *            the events
     */
    protected final void handle(final PluginEvent<?>[] events) {
        if (_eventsNames == null || _eventsNames.size() == 0 || events == null) {
            return;
        }
        for (var i = 0; events != null && i < events.length; i++) {
            final PluginEvent<?> event = events[i];
            if (_eventsNames.contains(event.getName())) {
                final var start = System.currentTimeMillis();
                handle(event);
                final var stop = System.currentTimeMillis();
                final var init = event.getCreationTime();
                final var duration = stop - init;
                if (duration > 1000L) {
                    final var source = event.getSource();
                    _log.debug("Event " + event.getName() + " completed after " + Format.formatDuration(duration)
                            + " (run: " + Format.formatDuration(start, stop) + ", idle: "
                            + Format.formatDuration(init, start) + (source != null ? ", from: " + source : "") + ")");
                }
                _eventsTotal++;
            }
        }
    }

    /**
     * Subscribe.
     *
     * @param eventName
     *            the event name
     */
    public final void subscribe(final String eventName) {
        if (!_eventsNames.contains(eventName)) {
            _log.info("Subscribing for " + eventName);
            _eventsNames.add(eventName);
        }
    }

    /**
     * Un subscribe.
     *
     * @param eventName
     *            the event name
     */
    public final void unSubscribe(final String eventName) {
        _log.info("Unsubscribing for " + eventName);
        _eventsNames.remove(eventName);
    }

    /**
     * Start.
     *
     * @return true, if successful
     */
    public abstract boolean start();

    /**
     * Stop.
     */
    public abstract void stop();

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
            if ("Version".equals(attributeName)) {
                return getVersion();
            }
            if ("Ref".equals(attributeName)) {
                return getRef();
            }
            if ("EventsTotal".equals(attributeName)) {
                return _eventsTotal;
            }
            if ("Name".equals(attributeName)) {
                return getPluginName();
            }
            if ("Status".equals(attributeName)) {
                return getPluginContainer().getPluginStatus(getRef());
            }
            if ("StatusUpdateDate".equals(attributeName)) {
                return getPluginContainer().getPluginStatusUpdateDate(getRef());
            }
        } catch (final Exception e) {
            _log.warn("Getting an MBean attribute", e);
            throw new MBeanException(e);
        }
        throw new AttributeNotFoundException(
                "Cannot find " + attributeName + " attribute in " + this.getClass().getName());
    }

    /**
     * Gets the MBean info.
     *
     * @return the MBean info
     */
    @Override
    public MBeanInfo getMBeanInfo() {
        return new MBeanInfo(this.getClass().getName(),
                "This MBean provides informations concerning the plugin " + getRef()
                        + ", as well as operations to monitor (status) and manage " + "(start and stop) the plugin.",
                new MBeanAttributeInfo[] {
                        new MBeanAttributeInfo("Version", "java.lang.String", "Version: plugin version number.", true,
                                false, false),
                        new MBeanAttributeInfo("Ref", "java.lang.String", "Ref: plugin reference name.", true, false,
                                false),
                        new MBeanAttributeInfo("Name", "java.lang.String", "Name: plugin name.", true, false, false),
                        new MBeanAttributeInfo("EventsTotal", "java.lang.Integer",
                                "EventsTotal: total number of events.", true, false, false),
                        new MBeanAttributeInfo("Status", "java.lang.String", "Name: plugin status.", true, false,
                                false),
                        new MBeanAttributeInfo("StatusUpdateDate", "java.util.Date",
                                "StatusUpdateDate: last update of the status.", true, false, false) },
                new MBeanConstructorInfo[0],
                new MBeanOperationInfo[] {
                        new MBeanOperationInfo("stop", "stop(): stop the " + getRef() + " plugin", null, "void",
                                MBeanOperationInfo.ACTION),
                        new MBeanOperationInfo("start", "start(): start the " + getRef() + " plugin", null, "void",
                                MBeanOperationInfo.ACTION),
                        new MBeanOperationInfo("callerGone",
                                "callerGone(): notify the " + getRef() + " plugin the caller has gone", null, "void",
                                MBeanOperationInfo.ACTION),
                        new MBeanOperationInfo("callerBack",
                                "callerBack(isReseted): notify the " + getRef() + " plugin the caller is back",
                                new MBeanParameterInfo[] { new MBeanParameterInfo("isReseted", "java.lang.Boolean",
                                        "caller has been restarted") },
                                "void", MBeanOperationInfo.ACTION) },
                new MBeanNotificationInfo[0]);
    }

    /**
     * Gets the plugin container.
     *
     * @return the plugin container
     */
    public PluginContainer getPluginContainer() {
        return _container;
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
            if ("stop".equals(operationName)) {
                getPluginContainer().stopPlugin(getRef());
                return Boolean.TRUE;
            }
            if ("start".equals(operationName)) {
                getPluginContainer().startPlugin(getRef());
                return Boolean.TRUE;
            }
            if ("callerBack".equals(operationName) && signature.length == 1
                    && "java.lang.Boolean".equals(signature[0])) {
                callerBack(((Boolean) params[0]));
                return Boolean.TRUE;
            }
            if ("callerGone".equals(operationName)) {
                callerGone();
                return Boolean.TRUE;
            }
        } catch (final Exception e) {
            _log.warn("Invoking the " + operationName + " MBean method", e);
            throw new MBeanException(e);
        }
        throw new NoSuchMethodException(operationName);
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
        return false;
    }

    /**
     * Sets the plugin container.
     *
     * @param container
     *            the new plugin container
     */
    public void setPluginContainer(final PluginContainer container) {
        _container = container;
    }

    /**
     * Configurable run.
     */
    @Override
    public void configurableRun() {
        _log.warn("Not implemented");
    }
}
