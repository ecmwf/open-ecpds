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

package ecmwf.common.database;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.File;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.service.ServiceRegistry;

/**
 * A factory for creating HibernateSession objects.
 */
public class HibernateSessionFactory {
    /** The constant _log. */
    private static final Logger _log = LogManager.getLogger(HibernateSessionFactory.class);

    /** The Constant HIBERNATE_CFG. */
    private static final String HIBERNATE_CFG = "hibernate.cfg.xml";

    /** The Constant HIBERNATE_HBM. */
    private static final String HIBERNATE_HBM = "hibernate.hbm.xml";

    /** The sessionFactory. */
    private static SessionFactory sessionFactory = null;

    /** The metadata. */
    private static Metadata metadata = null;

    /** The configuration. */
    private static Configuration configuration = null;

    /** The persistent class per table name list. */
    private static HashMap<String, PersistentClass> persistentClassPerTableNameList = new HashMap<>();

    /** The persistent class per mapped class list. */
    private static HashMap<Class<?>, PersistentClass> persistentClassPerMappedClassList = new HashMap<>();

    /**
     * Utility class, which is a collection of static members, and is not meant to be instantiated.
     */
    private HibernateSessionFactory() {
    }

    /**
     * Gets the session factory.
     *
     * @return the session factory
     *
     * @throws HibernateException
     *             the hibernate exception
     */
    static synchronized SessionFactory getSessionFactory() throws HibernateException {
        if (sessionFactory == null) {
            try {
                final ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                        .applySettings(getConfiguration().getProperties()).build();
                final var sources = new MetadataSources(serviceRegistry);
                sources.addFile(getFile(HIBERNATE_HBM));
                metadata = sources.getMetadataBuilder().build();
                sessionFactory = metadata.getSessionFactoryBuilder().build();
                for (final PersistentClass persistentClass : metadata.getEntityBindings()) {
                    persistentClassPerTableNameList.put(persistentClass.getTable().getName(), persistentClass);
                    persistentClassPerMappedClassList.put(persistentClass.getMappedClass(), persistentClass);
                }
            } catch (final Throwable e) {
                _log.error("Failed to create session Factory", e);
            }
        }
        return sessionFactory;
    }

    /**
     * Gets the metadata.
     *
     * @return the metadata
     */
    public static Metadata getMetadata() {
        return metadata;
    }

    /**
     * Allow getting the PersistentClass for a given table name. the mapped class and returned class for the primary key
     * can be retrieved with the following: persistentClass.getMappedClass() -> e.g. ecmwf.common.database.Destination
     * persistentClass.getIdentifier().getType().getReturnedClass() -> e.g. java.lang.Integer
     *
     * @param tableName
     *            the table name
     *
     * @return the persistent class
     */
    static final PersistentClass getPersistenClass(final String tableName) {
        final var persitentClass = persistentClassPerTableNameList.get(tableName);
        if (persitentClass != null) {
            return persitentClass;
        }
        throw new HibernateException("Getting entity class: " + tableName);
    }

    /**
     * Allow getting the PersistentClass for a given mapped class. The returned class for the primary key can be
     * retrieved with the following: persistentClass.getIdentifier().getType().getReturnedClass() -> e.g.
     * java.lang.Integer
     *
     * @param mappedClass
     *            the mapped class
     *
     * @return the persistent class
     */
    static final PersistentClass getPersistenClass(final Class<?> mappedClass) {
        final var persitentClass = persistentClassPerMappedClassList.get(mappedClass);
        if (persitentClass != null) {
            return persitentClass;
        }
        throw new HibernateException("Getting entity class: " + mappedClass.getName());
    }

    /**
     * Shutdown.
     */
    public static void shutdown() {
        final var factory = getSessionFactory();
        if (factory != null) {
            factory.close();
        }
        _log.debug("Shutting down session factory");
    }

    /**
     * Gets the configuration.
     *
     * @return the configuration
     */
    static synchronized Configuration getConfiguration() {
        if (configuration == null) {
            if (_log.isDebugEnabled()) {
                _log.debug("Loading hibernate configuation from {}", System.getProperty(HIBERNATE_CFG));
            }
            configuration = new Configuration().configure(getFile(HIBERNATE_CFG));
            _log.debug("Creating Configuration from ecmwf.properties");
        }

        return configuration;
    }

    /**
     * Inits the.
     *
     * @param driver
     *            the driver
     * @param level
     *            the level
     * @param protocol
     *            the protocol
     * @param subProtocol
     *            the sub protocol
     * @param alias
     *            the alias
     * @param user
     *            the user
     * @param password
     *            the password
     * @param dbms
     *            the dbms
     * @param validation
     *            the validation
     */
    public static synchronized void init(final String driver, final String level, final String protocol,
            final String subProtocol, final String alias, final String user, final String password, final String dbms,
            final String validation) {
        final var conf = new Configuration().setProperty("hibernate.connection.driver_class", driver)
                .setProperty("hibernate.connection.url", protocol + ":" + subProtocol + ":" + alias)
                .setProperty("hibernate.connection.username", user)
                .setProperty("hibernate.connection.password", password);
        conf.addFile(getFile(HIBERNATE_HBM));
        conf.configure(getFile(HIBERNATE_CFG));
        conf.setEntityNotFoundDelegate((entityName, id) -> _log.error("Entity not found: {}#{}", id, entityName));
        configuration = conf;
    }

    /**
     * Utility method to get a File from a system property.
     *
     * @param parameterName
     *            the parameter name
     *
     * @return the file
     */
    private static File getFile(final String parameterName) {
        final var fileName = System.getProperty(parameterName);
        if (fileName == null) {
            throw new NullPointerException("Property " + parameterName + " not found");
        }
        return new File(fileName);
    }

}