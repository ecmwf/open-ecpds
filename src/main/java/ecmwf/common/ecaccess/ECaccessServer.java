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

import java.io.IOException;
import java.sql.SQLException;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.timer.Timer;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.database.Activity;
import ecmwf.common.database.DataBase;
import ecmwf.common.database.DataBaseException;
import ecmwf.common.database.ECUser;
import ecmwf.common.database.Event;
import ecmwf.common.mbean.MBeanManager;
import ecmwf.common.monitor.MonitorManager;
import ecmwf.common.security.SecretWriting;
import ecmwf.common.starter.Starter;
import ecmwf.common.technical.Cnf;
import ecmwf.common.text.Format;

/**
 * The Class ECaccessServer.
 */
public abstract class ECaccessServer extends StarterServer {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -3153455519323024730L;

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(ECaccessServer.class);

    /** The data base. */
    private final DataBase dataBase;

    /**
     * Instantiates a new ecaccess server.
     *
     * @param starter
     *            the starter
     *
     * @throws java.sql.SQLException
     *             the SQL exception
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     * @throws java.lang.IllegalAccessException
     *             the illegal access exception
     * @throws java.lang.InstantiationException
     *             the instantiation exception
     * @throws java.lang.ClassNotFoundException
     *             the class not found exception
     * @throws javax.management.InstanceAlreadyExistsException
     *             the instance already exists exception
     * @throws javax.management.MBeanRegistrationException
     *             the MBean registration exception
     * @throws javax.management.NotCompliantMBeanException
     *             the not compliant m bean exception
     * @throws javax.management.MalformedObjectNameException
     *             the malformed object name exception
     * @throws javax.management.InstanceNotFoundException
     *             the instance not found exception
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public ECaccessServer(final Starter starter) throws SQLException, IOException, IllegalAccessException,
            InstantiationException, ClassNotFoundException, InstanceAlreadyExistsException, MBeanRegistrationException,
            NotCompliantMBeanException, MalformedObjectNameException, InstanceNotFoundException, DataBaseException {
        this(new DataBase(), starter);
    }

    /**
     * Instantiates a new ecaccess server.
     *
     * @param dataBase
     *            the data base
     * @param starter
     *            the starter
     *
     * @throws java.sql.SQLException
     *             the SQL exception
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     * @throws java.lang.IllegalAccessException
     *             the illegal access exception
     * @throws java.lang.InstantiationException
     *             the instantiation exception
     * @throws java.lang.ClassNotFoundException
     *             the class not found exception
     * @throws javax.management.InstanceAlreadyExistsException
     *             the instance already exists exception
     * @throws javax.management.MBeanRegistrationException
     *             the MBean registration exception
     * @throws javax.management.NotCompliantMBeanException
     *             the not compliant m bean exception
     * @throws javax.management.MalformedObjectNameException
     *             the malformed object name exception
     * @throws javax.management.InstanceNotFoundException
     *             the instance not found exception
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    protected ECaccessServer(final DataBase dataBase, final Starter starter)
            throws SQLException, IOException, IllegalAccessException, InstantiationException, ClassNotFoundException,
            InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException,
            MalformedObjectNameException, InstanceNotFoundException, DataBaseException {
        super(starter);
        final var initCount = Cnf.at("DataBase", "initCount", 1);
        final var initSleep = Cnf.at("DataBase", "initSleep", 30);
        this.dataBase = dataBase;
        for (var i = 0; i < initCount; i++) {
            if (i != 0) {
                try {
                    final var sleep = initSleep * Timer.ONE_SECOND;
                    if (_log.isInfoEnabled()) {
                        _log.info("Sleeping {}", Format.formatDuration(sleep));
                    }
                    Thread.sleep(sleep);
                } catch (final InterruptedException e) {
                }
            }
            var success = false;
            try {
                _log.info("Initializing the DataBase connection");
                dataBase.initialize(Cnf.at("DataBase", "brokerFactory", "ecmwf.common.database.BrokerFactoryOJB"),
                        Cnf.at("DataBase", "driver"), Cnf.at("DataBase", "level"), Cnf.at("DataBase", "protocol"),
                        Cnf.at("DataBase", "subProtocol"), Cnf.at("DataBase", "alias"), Cnf.at("DataBase", "user"),
                        Cnf.at("DataBase", "password"), Cnf.at("DataBase", "dbms"), Cnf.at("DataBase", "server"),
                        Cnf.at("DataBase", "repository"), Cnf.at("DataBase", "validation"),
                        Cnf.at("DataBase", "logEvents", true), Cnf.at("DataBase", "debugSql", false));
                _log.debug("DataBase initialized");
                success = true;
                break;
            } catch (SQLException | IllegalAccessException | InstantiationException | ClassNotFoundException
                    | DataBaseException e1) {
                _log.error("Cannot initialize the DataBase", e1);
                if (i + 1 == initCount) {
                    throw e1;
                }
            } catch (final Throwable t) {
                _log.error("Cannot initialize the DataBase", t);
                if (i + 1 == initCount) {
                    throw new DataBaseException("Cannot initialize the DataBase");
                }
            } finally {
                if (!success && i + 1 == initCount) {
                    _log.error("DataBase not initialized");
                }
            }
        }
        if (Cnf.at("Security", "check", false)) {
            boolean check;
            try {
                check = "check".equals(SecretWriting.decrypt(SecretWriting.encrypt("check")));
            } catch (final Exception e) {
                _log.fatal("decrypt", e);
                check = false;
            }
            if (!check) {
                throw new IOException("keystore verification failed");
            }
        }
        new MBeanManager("ECaccess:service=DataBase", dataBase);
        for (final String time : Cnf.listAt("Scheduler", "updateECUsersTime")) {
            getMBeanCenter().scheduleNotifications(this, Cnf.getTime(time, null),
                    Cnf.at("Scheduler", "updateECUsers", 24) * Timer.ONE_HOUR, "updateECUsers");
        }
        for (final String time : Cnf.listAt("Scheduler", "purgeDataBaseTime")) {
            getMBeanCenter().scheduleNotifications(this, Cnf.getTime(time, null),
                    Cnf.at("Scheduler", "purgeDataBase", 24) * Timer.ONE_HOUR, "purgeDataBase");
        }
    }

    /**
     * Purge the database.
     */
    public synchronized void purgeDataBase() {
        final var milliseconds = Cnf.at("DataBase", "purge", 360) * Timer.ONE_HOUR;
        if (_log.isDebugEnabled()) {
            _log.debug("Purge from database entries older than {}", Format.formatDuration(0, milliseconds));
        }
        purgeDataBase(milliseconds);
    }

    /**
     * Purge the database.
     *
     * @param milliseconds
     *            the milliseconds
     */
    public abstract void purgeDataBase(long milliseconds);

    /**
     * Update the ECusers.
     */
    public void updateECUsers() {
        _log.debug("Update ECUsers");
        final var ecusers = dataBase.getECUserArray();
        for (final ECUser fromDb : ecusers) {
            try {
                final var fromEc = importECUser(fromDb.getName());
                if (fromEc != null && !fromEc.toString().equals(fromDb.toString())) {
                    // Remove associations for this user?
                    final var comment = fromEc.getComment();
                    if (StringUtils.isNotEmpty(comment)) { // Make sure we don't import non printable characters!
                        fromEc.setComment(Format.cleanTextContent(comment));
                    }
                    dataBase.update(fromEc);
                    try {
                        handleECuserUpdate(fromEc);
                    } catch (final Throwable t) {
                        _log.warn("Error on ecuser update: {}", fromEc.getName(), t);
                    }
                }
            } catch (final Exception e) {
                _log.debug("Could not update ECUser {}", fromDb, e);
            }
        }
    }

    /**
     * Handle the ECuser update. To be overwritten by classes that wish to do some special processing when a ECuser is
     * updated.
     *
     * @param ecuser
     *            the ecuser
     */
    public void handleECuserUpdate(final ECUser ecuser) {
    }

    /**
     * Update the ECuser.
     *
     * @param ecuser
     *            the ecuser
     */
    public void updateECUser(final String ecuser) {
        _log.debug("Update ECUser {}", ecuser);
        try {
            final var fromDb = dataBase.getECUser(ecuser);
            final var fromEc = importECUser(fromDb.getName());
            if (fromEc != null) {
                dataBase.update(fromEc);
            }
        } catch (final Exception e) {
            _log.debug(e);
        }
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
            if ("updateECUsers".equals(operationName) && signature.length == 0) {
                updateECUsers();
                return Boolean.TRUE;
            }
            if ("updateECUser".equals(operationName) && signature.length == 1
                    && "java.lang.String".equals(signature[0])) {
                updateECUser((String) params[0]);
                return Boolean.TRUE;
            }
            if ("purgeDataBase".equals(operationName) && signature.length == 1
                    && "java.lang.Integer".equals(signature[0])) {
                purgeDataBase((Integer) params[0] * Timer.ONE_HOUR);
                return Boolean.TRUE;
            }
            if ("purgeDataBase".equals(operationName) && signature.length == 0) {
                purgeDataBase();
                return Boolean.TRUE;
            }
        } catch (final Exception e) {
            _log.warn("Invoking the {} MBean method", operationName, e);
            throw new MBeanException(e);
        }
        return super.invoke(operationName, params, signature);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the MBean info.
     */
    @Override
    public MBeanInfo getMBeanInfo() {
        return MBeanManager.addMBeanInfo(super.getMBeanInfo(), """
                The ECaccess server initialize all the ECaccess software \
                components, including the database, the ECaccess plugins \
                and the Management Bean interfaces.""",
                new MBeanAttributeInfo[] { new MBeanAttributeInfo("MonitorDebug", "java.lang.Boolean",
                        "MonitorDebug: allow debugging the MonitorManager (e.g. Opsview).", true, true, false),
                        new MBeanAttributeInfo("MonitorActivated", "java.lang.Boolean",
                                "MonitorActivated: Monitor activated.", true, false, false) },
                new MBeanOperationInfo[] {
                        new MBeanOperationInfo("updateECUsers", "updateECUsers(): update the ECUsers in the database",
                                new MBeanParameterInfo[0], "void", MBeanOperationInfo.ACTION),
                        new MBeanOperationInfo("updateECUser",
                                "updateECUser(name): update the specified ECUser in the database",
                                new MBeanParameterInfo[] {
                                        new MBeanParameterInfo("name", "java.lang.String", "ECUser name") },
                                "void", MBeanOperationInfo.ACTION),
                        new MBeanOperationInfo("purgeDataBase", "purgeDataBase(hours): purge the database",
                                new MBeanParameterInfo[] {
                                        new MBeanParameterInfo("hours", "java.lang.Integer", "number of hours") },
                                "void", MBeanOperationInfo.ACTION),
                        new MBeanOperationInfo("purgeDataBase", "purgeDataBase(): purge the database",
                                new MBeanParameterInfo[0], "void", MBeanOperationInfo.ACTION) });
    }

    /**
     * {@inheritDoc}
     *
     * Sets the attribute.
     */
    @Override
    public boolean setAttribute(final String name, final Object value)
            throws InvalidAttributeValueException, MBeanException {
        if ("MonitorDebug".equals(name)) {
            MonitorManager.setDebug((Boolean) value);
            return true;
        }
        return super.setAttribute(name, value);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the attribute.
     */
    @Override
    public Object getAttribute(final String attributeName) throws AttributeNotFoundException, MBeanException {
        try {
            if ("MonitorDebug".equals(attributeName)) {
                return MonitorManager.isDebug();
            }
            if ("MonitorActivated".equals(attributeName)) {
                return MonitorManager.isActivated();
            }
        } catch (final Exception e) {
            _log.warn("Getting an MBean attribute", e);
            throw new MBeanException(e);
        }
        return super.getAttribute(attributeName);
    }

    /**
     * {@inheritDoc}
     *
     * Shutdown.
     */
    @Override
    public synchronized void shutdown() {
        super.shutdown();
        dataBase.close();
    }

    /**
     * Gets the database.
     *
     * @return the data base
     */
    public DataBase getDataBase() {
        if (dataBase == null) {
            throw new NullPointerException("DataBase not initialized yet");
        }
        return dataBase;
    }

    /**
     * Gets the database.
     *
     * @param <T>
     *            the generic type
     * @param clazz
     *            the clazz
     *
     * @return the data base
     */
    public <T extends DataBase> T getDataBase(final Class<T> clazz) {
        return clazz.cast(getDataBase());
    }

    /**
     * New activity.
     *
     * @param user
     *            the user
     * @param plugin
     *            the plugin
     * @param host
     *            the host
     * @param agent
     *            the agent
     * @param action
     *            the action
     * @param comment
     *            the comment
     * @param error
     *            the error
     *
     * @return the activity
     */
    public Activity newActivity(final String user, final String plugin, final String host, final String agent,
            final String action, final String comment, final boolean error) {
        final var ecuser = getECUser(user, true);
        // Return new Activity from database.
        return ecuser != null ? newActivity(ecuser, plugin, host, agent, action, comment, error) : null;
    }

    /**
     * New activity.
     *
     * @param ecuser
     *            the ecuser
     * @param plugin
     *            the plugin
     * @param host
     *            the host
     * @param agent
     *            the agent
     * @param action
     *            the action
     * @param comment
     *            the comment
     * @param error
     *            the error
     *
     * @return the activity
     */
    public Activity newActivity(final ECUser ecuser, final String plugin, final String host, final String agent,
            final String action, final String comment, final boolean error) {
        return dataBase.newActivity(ecuser, plugin, host, agent, action, comment, error);
    }

    /**
     * New event.
     *
     * @param activity
     *            the activity
     * @param action
     *            the action
     * @param comment
     *            the comment
     * @param error
     *            the error
     *
     * @return the event
     */
    public Event newEvent(final Activity activity, final String action, final String comment, final boolean error) {
        return dataBase.newEvent(activity, action, comment, error);
    }

    /**
     * Import ec user.
     *
     * @param user
     *            the user
     *
     * @return the EC user
     *
     * @throws java.lang.Exception
     *             the exception
     */
    public abstract ECUser importECUser(String user) throws Exception;

    /**
     * Gets the ECuser.
     *
     * @param user
     *            the user
     * @param save
     *            the save
     *
     * @return the EC user
     */
    public ECUser getECUser(final String user, final boolean save) {
        ECUser ecuser = null;
        try {
            if ((ecuser = dataBase.getECUserObject(user)) == null && (ecuser = importECUser(user)) != null && save) {
                dataBase.insert(ecuser, false);
            }
        } catch (final Throwable t) {
            _log.debug(t);
        }
        return ecuser;
    }

    /**
     * Checks if it is a registered user.
     *
     * @param user
     *            the user
     *
     * @return true, if is registered user
     */
    public boolean isRegistredUser(final String user) {
        return getECUser(user, true) != null;
    }
}
