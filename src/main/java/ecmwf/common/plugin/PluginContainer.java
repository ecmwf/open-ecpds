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
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.net.InetAddress;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.AttributeNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.mbean.MBeanManager;
import ecmwf.common.mbean.MBeanService;
import ecmwf.common.monitor.MonitorCallback;
import ecmwf.common.monitor.MonitorException;
import ecmwf.common.monitor.MonitorManager;
import ecmwf.common.monitor.MonitorThread;
import ecmwf.common.starter.StarterLoader;
import ecmwf.common.technical.Cnf;
import ecmwf.common.technical.ThreadService.ConfigurableRunnable;

/**
 * The Class PluginContainer.
 */
public final class PluginContainer implements MBeanService, MonitorCallback {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(PluginContainer.class);

    /** The _monitor. */
    private final MonitorThread _monitor;

    /** The _plugins. */
    private final Map<String, PluginThread> _plugins = new ConcurrentHashMap<>();

    /** The _status. */
    private final Map<String, Object[]> _status = new ConcurrentHashMap<>();

    /** The _m beans. */
    private final Map<String, MBeanManager> _mBeans = new ConcurrentHashMap<>();

    /** The _caller. */
    private final Object _caller;

    /** The _sync notify. */
    private final Object _syncNotify = new Object();

    /** The _sync plugin. */
    private final Object _syncPlugin = new Object();

    /** The _caller gone. */
    private boolean _callerGone = false;

    /**
     * Instantiates a new plugin container.
     *
     * @param caller
     *            the caller
     */
    public PluginContainer(final Object caller) {
        _caller = caller;
        MonitorThread monitor = null;
        if (MonitorManager.isActivated()) {
            try {
                monitor = MonitorThread.getInstance();
            } catch (final MonitorException e) {
                _log.debug(e);
            }
        }
        _monitor = monitor;
    }

    /**
     * Gets the plugin infos.
     *
     * @return the plugin infos
     */
    public PluginInfo[] getPluginInfos() {
        final var plugins = _plugins.values().toArray(new PluginThread[0]);
        final var infos = new PluginInfo[plugins.length];
        for (var i = 0; i < plugins.length; i++) {
            infos[i] = new PluginInfo(plugins[i]);
        }
        return infos;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the monitor manager.
     */
    @Override
    public MonitorManager getMonitorManager(final String name) throws MonitorException {
        String ref = null;
        final var status = name.startsWith("plugin_") ? getPluginStatus(ref = name.substring(7)) : null;
        if (status == null) {
            throw new MonitorException("No MonitorManager for " + name);
        }
        // The ref is the name of the plugin (e.g. ssh, telnet, http, ftp)!
        try {
            return new MonitorManager(
                    Cnf.at("Login", "service", Cnf.at("Login", "hostName", InetAddress.getLocalHost().getHostName())),
                    "Plugin: " + ref, "ON".equals(status) ? MonitorManager.GREEN : MonitorManager.RED, status);
        } catch (final Throwable t) {
            throw new MonitorException(t.getMessage());
        }
    }

    /**
     * Gets the plugin status.
     *
     * @param ref
     *            the ref
     *
     * @return the plugin status
     */
    public String getPluginStatus(final String ref) {
        final var status = _status.get(ref);
        if (status != null && status.length == 2) {
            return String.valueOf(status[0]);
        }
        return null;
    }

    /**
     * Load plugin.
     *
     * @param ref
     *            the ref
     *
     * @return true, if successful
     */
    private boolean _loadPlugin(final String ref) {
        final var values = Cnf.listAt("PluginList", ref);
        if (values.size() > 0) {
            String loader = null;
            var name = values.get(0);
            int pos;
            if ((pos = name.indexOf("@")) != -1) {
                loader = name.substring(pos + 1);
                name = name.substring(0, pos);
            }
            final Map<String, String> params = new ConcurrentHashMap<>();
            for (var i = 1; i < values.size(); i++) {
                final var value = values.get(i);
                if ((pos = value.indexOf('=')) > 0) {
                    params.put(value.substring(0, pos), value.substring(pos + 1));
                }
            }
            try {
                final var current = PluginContainer.class.getClassLoader();
                final var classLoader = loader != null ? StarterLoader.getClassLoader(loader, current) : current;
                final var plug = (PluginThread) classLoader.loadClass(name).getConstructor(String.class, Map.class)
                        .newInstance(ref, params);
                plug.setPluginContainer(this);
                PluginThread.setCaller(_caller);
                _registerPlugin(plug);
                return true;
            } catch (final Throwable t) {
                _log.error("Loading plugin " + ref, t);
            }
        } else {
            _log.warn("Plugin list is empty for " + ref);
        }
        return false;
    }

    /**
     * Load plugins.
     */
    public void loadPlugins() {
        for (final String key : Cnf.keysAt("PluginList")) {
            _loadPlugin(key);
        }
    }

    /**
     * Notify caller back.
     *
     * @param reset
     *            the reset
     */
    public void notifyCallerBack(final boolean reset) {
        _log.info("Notifying caller is " + (reset ? "reseted" : "back"));
        int i;
        synchronized (_syncNotify) {
            _callerGone = false;
            final var plugins = _plugins.values().toArray(new PluginThread[0]);
            for (i = 0; i < plugins.length; i++) {
                final PluginThread plugin;
                if ((plugin = plugins[i]) != null) {
                    new PluginActionThread(plugin,
                            reset ? PluginActionThread.NOTIFY_CALLER_RESETED : PluginActionThread.NOTIFY_CALLER_BACK)
                                    .execute();
                }
            }
        }
        _log.info(i + " plugin(s) notified");
    }

    /**
     * Notify caller gone.
     */
    public void notifyCallerGone() {
        _log.info("Notifying caller is gone");
        int i;
        synchronized (_syncNotify) {
            _callerGone = true;
            final var plugins = _plugins.values().toArray(new PluginThread[0]);
            for (i = 0; i < plugins.length; i++) {
                final PluginThread plugin;
                if ((plugin = plugins[i]) != null) {
                    new PluginActionThread(plugin, PluginActionThread.NOTIFY_CALLER_GONE).execute();
                }
            }
        }
        _log.info(i + " plugin(s) notified");
    }

    /**
     * Start plugin.
     *
     * @param ref
     *            the ref
     *
     * @return true, if successful
     */
    public boolean startPlugin(final String ref) {
        final PluginThread plugin;
        if ((plugin = _plugins.get(ref)) != null) {
            new PluginActionThread(plugin, PluginActionThread.START).execute();
            return true;
        }
        return false;
    }

    /**
     * Stop plugin.
     *
     * @param ref
     *            the ref
     *
     * @return true, if successful
     */
    public boolean stopPlugin(final String ref) {
        final PluginThread plugin;
        if ((plugin = _plugins.get(ref)) != null) {
            new PluginActionThread(plugin, PluginActionThread.STOP).execute();
            return true;
        }
        return false;
    }

    /**
     * Stop plugins.
     */
    public void stopPlugins() {
        _log.info("Stopping all plugin(s)");
        int i;
        final var plugins = _plugins.values().toArray(new PluginThread[0]);
        for (i = 0; i < plugins.length; i++) {
            final PluginThread plugin;
            if ((plugin = plugins[i]) != null) {
                new PluginActionThread(plugin, PluginActionThread.STOP).execute();
            }
        }
        _log.info(i + " plugin(s) stopped");
    }

    /**
     * Notify.
     *
     * @param events
     *            the events
     */
    public void notify(final PluginEvent<?>[] events) {
        handle(events);
    }

    /**
     * Handle.
     *
     * @param events
     *            the events
     */
    public void handle(final PluginEvent<?>[] events) {
        final var plugins = _plugins.values().toArray(new PluginThread[0]);
        for (final PluginThread plugin2 : plugins) {
            final PluginThread plugin;
            if ((plugin = plugin2) != null) {
                plugin.handle(events);
            }
        }
    }

    /**
     * _update status.
     *
     * @param ref
     *            the ref
     * @param status
     *            the status
     */
    private void _updateStatus(final String ref, final String status) {
        _status.put(ref, new Object[] { status, new Date() });
    }

    /**
     * {@inheritDoc}
     *
     * Gets the attribute.
     */
    @Override
    public Object getAttribute(final String attributeName) throws AttributeNotFoundException, MBeanException {
        try {
            if ("CallerGone".equals(attributeName)) {
                return _callerGone;
            }
            if ("PluginCount".equals(attributeName)) {
                return _plugins.size();
            }
            if ("ActivePluginCount".equals(attributeName)) {
                var active = 0;
                for (final PluginThread thread : _plugins.values()) {
                    active += "ON".equals(getPluginStatus(thread.getRef())) ? 1 : 0;
                }
                return active;
            }
        } catch (final Exception e) {
            _log.warn("Getting an MBean attribute", e);
            throw new MBeanException(e);
        }
        throw new AttributeNotFoundException(
                "Cannot find " + attributeName + " attribute in " + this.getClass().getName());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the MBean info.
     */
    @Override
    public MBeanInfo getMBeanInfo() {
        final var plugins = _plugins.values().toArray(new PluginThread[0]);
        final var ref = new StringBuilder();
        for (var i = 0; i < plugins.length; i++) {
            ref.append(ref.isEmpty() ? "" : i == plugins.length - 1 ? " or " : ", ").append(plugins[i].getRef());
        }
        final var params = new MBeanParameterInfo[] { new MBeanParameterInfo("ref", "java.lang.String",
                "the plugin reference" + (ref.isEmpty() ? "" : " (" + ref.toString() + ")")) };
        return new MBeanInfo(this.getClass().getName(), """
                ECaccess includes a model for the management of "plugin" services. \
                A plugin is a piece of code that handles request/responses flowing \
                through ECaccess. Plugins are activated and managed by the plugin \
                container, which communicates with the ECaccess runtime and provides \
                plugins with access to ECMWF services. This MBean provides operations \
                to manage and monitor the plugins.""",
                new MBeanAttributeInfo[] {
                        new MBeanAttributeInfo("PluginCount", "java.lang.Integer",
                                "PluginCount: number of plugins loaded.", true, false, false),
                        new MBeanAttributeInfo("ActivePluginCount", "java.lang.Integer",
                                "PluginCount: number of active plugins.", true, false, false),
                        new MBeanAttributeInfo("CallerGone", "java.lang.Boolean", "CallerGone: the caller has gone.",
                                true, false, false) },
                new MBeanConstructorInfo[0],
                new MBeanOperationInfo[] {
                        new MBeanOperationInfo("stopPlugin", "stopPlugin(ref): stop the plugin of reference ref",
                                params, "void", MBeanOperationInfo.ACTION),
                        new MBeanOperationInfo("startPlugin", "startPlugin(ref): stop the plugin of reference ref",
                                params, "void", MBeanOperationInfo.ACTION),
                        new MBeanOperationInfo("loadPlugin", "loadPlugin(ref): load the plugin of reference ref",
                                params, "void", MBeanOperationInfo.ACTION),
                        new MBeanOperationInfo("loadPlugins", "loadPlugin(): load all the plugins", null, "void",
                                MBeanOperationInfo.ACTION),
                        new MBeanOperationInfo("notifyCallerBack",
                                "notifyCallerBack(isReseted): notify the plugins the caller is back",
                                new MBeanParameterInfo[] { new MBeanParameterInfo("isReseted", "java.lang.Boolean",
                                        "server has been restarted") },
                                "void", MBeanOperationInfo.ACTION),
                        new MBeanOperationInfo("notifyCallerGone",
                                "notifyCallerGone(): notify the plugins the caller has gone", null, "void",
                                MBeanOperationInfo.ACTION),
                        new MBeanOperationInfo("getPluginStatus",
                                "getPluginStatus(ref): give the status of the plugin of reference ref", params,
                                "java.lang.String", MBeanOperationInfo.ACTION), },
                new MBeanNotificationInfo[0]);
    }

    /**
     * Gets the plugin status update date.
     *
     * @param ref
     *            the ref
     *
     * @return the plugin status update date
     */
    public Date getPluginStatusUpdateDate(final String ref) {
        final var status = _status.get(ref);
        if (status != null && status.length == 2) {
            return (Date) status[1];
        }
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * Invoke.
     */
    @Override
    public Object invoke(final String operationName, final Object[] params, final String[] signature)
            throws NoSuchMethodException, MBeanException {
        try {
            if ("getPluginStatus".equals(operationName) && signature.length == 1
                    && "java.lang.String".equals(signature[0])) {
                final var name = (String) params[0];
                if (!_plugins.containsKey(name)) {
                    throw new PluginContainerException("Plugin " + name + " not found");
                }
                return getPluginStatus(name);
            }
            if ("notifyCallerGone".equals(operationName)) {
                if (_callerGone) {
                    throw new PluginContainerException("Signal CALLER_GONE already sent");
                }
                notifyCallerGone();
                return Boolean.TRUE;
            }
            if ("notifyCallerBack".equals(operationName) && signature.length == 1
                    && "java.lang.Boolean".equals(signature[0])) {
                if (!_callerGone) {
                    throw new PluginContainerException("No prior CALLER_GONE sent");
                }
                notifyCallerBack((Boolean) params[0]);
                return Boolean.TRUE;
            }
            if ("loadPlugins".equals(operationName) && signature.length == 0) {
                stopPlugins();
                unregisterPlugins();
                loadPlugins();
                return Boolean.TRUE;
            }
            if ("loadPlugin".equals(operationName) && signature.length == 1
                    && "java.lang.String".equals(signature[0])) {
                final var name = (String) params[0];
                if (!_plugins.containsKey(name)) {
                    throw new PluginContainerException("Plugin " + name + " not found");
                }
                stopPlugin(name);
                _unregisterPlugin(name);
                _loadPlugin(name);
                return Boolean.TRUE;
            }
            if ("startPlugin".equals(operationName) && signature.length == 1
                    && "java.lang.String".equals(signature[0])) {
                final var name = (String) params[0];
                if (!_plugins.containsKey(name)) {
                    throw new PluginContainerException("Plugin " + name + " not found");
                }
                final var status = getPluginStatus(name);
                if ("ON".equals(status)) {
                    throw new PluginContainerException(
                            status == null ? "Plugin" + name + " not found" : "Plugin " + name + " is already ON");
                }
                startPlugin(name);
                return Boolean.TRUE;
            }
            if ("stopPlugin".equals(operationName) && signature.length == 1
                    && "java.lang.String".equals(signature[0])) {
                final var name = (String) params[0];
                if (!_plugins.containsKey(name)) {
                    throw new PluginContainerException("Plugin " + name + " not found");
                }
                final var status = getPluginStatus(name);
                if (!"ON".equals(status)) {
                    throw new PluginContainerException("Plugin " + name + " not ON");
                }
                stopPlugin(name);
                return Boolean.TRUE;
            }
        } catch (final Exception e) {
            _log.warn("Invoking the {} MBean method", operationName, e);
            throw new MBeanException(e);
        }
        throw new NoSuchMethodException(operationName);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the attribute.
     */
    @Override
    public boolean setAttribute(final String name, final Object value) {
        return false;
    }

    /**
     * Unregister plugins.
     */
    public void unregisterPlugins() {
        for (final String name : _mBeans.keySet()) {
            _unregisterPlugin(name);
        }
    }

    /**
     * _register plugin.
     *
     * @param plugin
     *            the plugin
     */
    private void _registerPlugin(final PluginThread plugin) {
        final var ref = plugin.getRef();
        _log.info("Registering plugin " + ref);
        if (_monitor != null) {
            _monitor.subscribe("plugin_" + ref, this);
        }
        synchronized (_syncPlugin) {
            try {
                _mBeans.put(ref, new MBeanManager("ECPlugin:plugin=" + plugin.getPluginName() + "_" + ref, plugin));
                _updateStatus(ref, "OFF");
                _plugins.put(ref, plugin);
            } catch (final Exception e) {
                _log.error("Registering plugin " + ref, e);
            }
        }
    }

    /**
     * _unregister plugin.
     *
     * @param ref
     *            the ref
     */
    private void _unregisterPlugin(final String ref) {
        _log.info("Unregistering plugin " + ref);
        if (_monitor != null) {
            _monitor.unSubscribe("plugin_" + ref);
        }
        synchronized (_syncPlugin) {
            if (_mBeans.containsKey(ref)) {
                try {
                    _mBeans.remove(ref).unregister();
                } catch (final Exception e) {
                    _log.warn("Unregistering plugin " + ref, e);
                }
            }
            _plugins.remove(ref);
            _status.remove(ref);
        }
    }

    /**
     * Start plugins.
     */
    public void startPlugins() {
        _log.info("Starting all plugin(s)");
        int i;
        final var plugins = _plugins.values().toArray(new PluginThread[0]);
        for (i = 0; i < plugins.length; i++) {
            final PluginThread plugin;
            if ((plugin = plugins[i]) != null) {
                new PluginActionThread(plugin, PluginActionThread.START).execute();
            }
        }
        _log.info(i + " plugin(s) started");
    }

    /**
     * The Class PluginActionThread.
     */
    public final class PluginActionThread extends ConfigurableRunnable {
        /** The Constant START. */
        public static final int START = 0;

        /** The Constant STOP. */
        public static final int STOP = 1;

        /** The Constant NOTIFY_CALLER_BACK. */
        public static final int NOTIFY_CALLER_BACK = 2;

        /** The Constant NOTIFY_CALLER_RESETED. */
        public static final int NOTIFY_CALLER_RESETED = 3;

        /** The Constant NOTIFY_CALLER_GONE. */
        public static final int NOTIFY_CALLER_GONE = 4;

        /** The names. */
        private final String[] _NAMES = { "START", "STOP", "NOTIFY_CALLER_BACK", "NOTIFY_CALLER_RESETED",
                "NOTIFY_CALLER_GONE" };

        /** The _plugin. */
        private final PluginThread _plugin;

        /** The _action. */
        private final int _action;

        /**
         * Gets the action.
         *
         * @param action
         *            the action
         *
         * @return the string
         */
        private String _getAction(final int action) {
            if (action <= 4) {
                return _NAMES[action];
            }
            return "UNDEFINED";
        }

        /**
         * Instantiates a new plugin action thread.
         *
         * @param plugin
         *            the plugin
         * @param action
         *            the action
         */
        public PluginActionThread(final PluginThread plugin, final int action) {
            setThreadNameAndCookie(plugin.getRef() + "(" + _getAction(action) + ")", null, null, null);
            _plugin = plugin;
            _action = action;
        }

        /**
         * Configurable run.
         */
        @Override
        public void configurableRun() {
            var ref = "unknown";
            try {
                ref = _plugin.getRef();
                final var status = getPluginStatus(ref);
                switch (_action) {
                case START: {
                    _log.info("Switching plugin {} on", ref);
                    _updateStatus(ref, "SWITCHING_ON");
                    _updateStatus(ref, _plugin.start() ? "ON" : "ERROR");
                    return;
                }
                case STOP: {
                    _log.info("Switching plugin {} off", ref);
                    _updateStatus(ref, "SWITCHING_OFF");
                    _plugin.stop();
                    _updateStatus(ref, "OFF");
                    return;
                }
                case NOTIFY_CALLER_GONE: {
                    _log.info("Sending plugin {} CALLER_GONE", ref);
                    _updateStatus(ref, "CALLER_GONE");
                    _plugin.callerGone();
                    _updateStatus(ref, status);
                    return;
                }
                case NOTIFY_CALLER_BACK: {
                    _log.info("Sending plugin {} CALLER_BACK", ref);
                    _updateStatus(ref, "CALLER_BACK");
                    _plugin.callerBack(false);
                    _updateStatus(ref, status);
                    return;
                }
                case NOTIFY_CALLER_RESETED: {
                    _log.info("Sending plugin {} CALLER_RESETED", ref);
                    _updateStatus(ref, "CALLER_RESETED");
                    _plugin.callerBack(true);
                    _updateStatus(ref, status);
                }
                }
            } catch (final Throwable t) {
                _log.error("Managing the {} plugin", ref, t);
                _updateStatus(ref, "ERROR");
            }
        }
    }

    /**
     * The Class PluginContainerException.
     */
    private static final class PluginContainerException extends Exception {
        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 7296960761312877990L;

        /**
         * Instantiates a new plugin container exception.
         *
         * @param message
         *            the message
         */
        PluginContainerException(final String message) {
            super(message);
        }
    }
}
