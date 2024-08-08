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
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * This class implements the Broker interface. It uses the Hibernate framework
 * to interact with a database.
 *
 * The class defines some private fields and constants, including a Hibernate
 * session, a cache, and a MariaDB connection. It also has some methods that
 * deal with the MariaDB connection, such as getMariadbConnection and
 * setReadOnly.
 *
 * The class implements the methods clearCache and executeQuery from the Broker
 * interface. clearCache clears the cache used by the Hibernate session, while
 * executeQuery executes a given SQL query and returns a ResultSet.
 *
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLTransactionRollbackException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.persistence.criteria.CriteriaQuery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Cache;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.TransactionException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.SessionImpl;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.metamodel.spi.MetamodelImplementor;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;

import com.mchange.v2.c3p0.impl.NewProxyConnection;

import ecmwf.common.technical.Cnf;

/**
 * The Class BrokerHibernate.
 */
final class BrokerHibernate implements Broker {

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(BrokerHibernate.class);

    /** The Constant FETCH_SIZE. */
    private static final int FETCH_SIZE = Cnf.at("DataBase", "fetchSize", 500);

    /** The Constant QUERY_CACHEABLE. */
    private static final boolean QUERY_CACHEABLE = Cnf.at("DataBase", "queryCacheable", false);

    /** The Constant GET_MARIADB_CONNECTION. */
    private static final boolean GET_MARIADB_CONNECTION = Cnf.at("DataBase", "getMariadbConnection", true);

    /** The Constant SET_AUTO_COMMIT. */
    private static final boolean SET_AUTO_COMMIT = Cnf.at("DataBase", "setAutoCommit", true);

    /** The Constant SET_READ_ONLY. */
    private static final boolean SET_READ_ONLY = Cnf.at("DataBase", "setReadOnly", true);

    /** The Constant RETRY_TRANSACTION_COUNT. */
    private static final int RETRY_TRANSACTION_COUNT = Cnf.at("DataBase", "retryTransactionCount", 2);

    /** The Constant RETRY_TRANSACTION_DELAY. */
    private static final long RETRY_TRANSACTION_DELAY = Cnf.at("DataBase", "retryTransactionDelay", 1000);

    /** The Constant DEBUG_CACHE. */
    private static final boolean DEBUG_CACHE = Cnf.at("DataBase", "debugCache", false);

    /** The Constant DEBUG_POOL. */
    private static final boolean DEBUG_POOL = Cnf.at("DataBase", "debugPool", false);

    /** The Constant DEBUG_FREQUENCY. */
    private static final int DEBUG_FREQUENCY = Cnf.at("DataBase", "debugFrequency", 1_000_000);

    /** The Constant activities. */
    private static final Map<String, NodeActivity> activities = new ConcurrentHashMap<>();

    /** The session. */
    private final Session session;

    /** The Hibernate cache. */
    private final Cache cache;

    /** The mariadbConnection. */
    private final org.mariadb.jdbc.Connection mariadbConnection;

    /**
     * The ExecuteOperation.
     */
    private enum ExecuteOperation {

        /** The save or update. */
        SAVE_OR_UPDATE,
        /** The save. */
        SAVE,
        /** The update. */
        UPDATE,
        /** The delete. */
        DELETE
    }

    /**
     * This code is creating an instance of the BrokerHibernate class, which has a constructor that initializes some
     * fields. It first obtains a SessionFactory from the HibernateSessionFactory class and uses it to open a new
     * session with sessionFactory.openSession(). It also gets a reference to the cache associated with the session
     * using sessionFactory.getCache(). Finally, it calls the getMariadbConnection() method to obtain a reference to a
     * MariaDB connection, which is assigned to the mariadbConnection field. Note that if the JDBC driver being used is
     * not MariaDB, then mariadbConnection will be null.
     *
     * @throws BrokerException
     *             Signals that a broker exception has occurred.
     */
    BrokerHibernate() throws BrokerException {
        final var sessionFactory = HibernateSessionFactory.getSessionFactory();
        if (sessionFactory == null) {
            throw new BrokerException("Hibernate session initialization failed");
        }
        session = sessionFactory.openSession();
        // If not a MariaDB driver, this is null!
        mariadbConnection = getMariadbConnection(session);
        cache = sessionFactory.getCache();
    }

    /**
     * This method takes a Session object as a parameter and returns a MariaDB jdbc Connection object. The method first
     * checks if the flag GET_MARIADB_CONNECTION is set to true, and if so, it checks if the Session object is an
     * instance of SessionImpl and if its physical connection is a NewProxyConnection that wraps a MariaDB jdbc
     * Connection. If all of these conditions are true, the method returns the unwrapped MariaDB jdbc Connection object.
     * If any of the conditions are false or if an exception is thrown during the process, the method returns null.
     *
     * @param session
     *            the session
     *
     * @return the mariadb connection
     */
    private static org.mariadb.jdbc.Connection getMariadbConnection(final Session session) {
        try {
            if (GET_MARIADB_CONNECTION && session instanceof final SessionImpl hibernateSession
                    && hibernateSession.getJdbcCoordinator().getLogicalConnection()
                            .getPhysicalConnection() instanceof final NewProxyConnection proxy
                    && proxy.isWrapperFor(org.mariadb.jdbc.Connection.class) && proxy.unwrap(
                            org.mariadb.jdbc.Connection.class) instanceof final org.mariadb.jdbc.Connection mariadbConnection) {
                return mariadbConnection;
            }
        } catch (final SQLException e) {
            _log.debug("Getting underlying JDBC connection", e);
        }
        return null;
    }

    /**
     * This method is used to get the identifier of a given object in Hibernate. It takes two parameters - the object
     * and a Hibernate Session.
     *
     * If the object is a Hibernate proxy, it returns the identifier of the underlying persistent object. If not, it
     * checks whether the Session's Metamodel is an instance of MetamodelImplementor (for Hibernate version 5) and the
     * Session is an instance of SessionImplementor. If so, it obtains the entity persister for the object's class name
     * and gets its identifier using the sessionImplementor. If none of these conditions are met, it throws a
     * HibernateException.
     *
     * The return value is a Serializable object representing the identifier of the given object. *
     *
     * @param object
     *            the object
     * @param session
     *            the session
     *
     * @return the object identifier
     *
     * @throws HibernateException
     *             the hibernate exception
     */
    private static Serializable getObjectIdentifier(final Object object, final Session session) {
        // For Hibernate6 replace: MetamodelImplementor -> MappingMetamodel &&
        // implementor.entityPersister(...) -> implementor.getEntityDescriptor(...) and
        // return Object instead of Serializable.
        // Also add the following imports:
        // import org.hibernate.metamodel.MappingMetamodel;
        // import jakarta.persistence.criteria.CriteriaQuery;
        if (object instanceof final HibernateProxy proxy) {
            return proxy.getHibernateLazyInitializer().getIdentifier();
        }
        if (session.getEntityManagerFactory().getMetamodel() instanceof final MetamodelImplementor implementor
                && session instanceof final SessionImplementor sessionImplementor) {
            return implementor.entityPersister(object.getClass().getName()).getIdentifier(object, sessionImplementor);
        } else {
            throw new HibernateException("Getting object identifier");
        }
    }

    /**
     * This method is setting the read-only mode and auto-commit mode of a MariaDB database connection. In
     * load-balancing or replication mode, this allow spreading the connections across the nodes.It first checks if a
     * MariaDB jdbc Connection object is available and then sets the read-only and auto-commit mode using the
     * setReadOnly() and setAutoCommit() methods, respectively. If the DEBUG_POOL flag is set, it also updates a
     * NodeActivity object with the current read/write activity of the connection. If there is any SQLException during
     * the process, it logs an error message with the read-only mode value.
     *
     * @param read
     *            the new read only
     */
    private void setReadOnly(final boolean read) {
        if (mariadbConnection != null) {
            try {
                if (SET_READ_ONLY) {
                    mariadbConnection.setReadOnly(read);
                }
                if (SET_AUTO_COMMIT && !read) {
                    mariadbConnection.setAutoCommit(true);
                }
            } catch (final SQLException e) {
                _log.error("setReadOnly/setAutoCommit: read={}", read, e);
            }
            if (DEBUG_POOL && _log.isDebugEnabled()) {
                activities.computeIfAbsent(mariadbConnection.__test_host(), NodeActivity::new).update(read);
            }
        }
    }

    /**
     * Remove objects from the the Hibernate second level cache regions of the specified Class with the specified
     * primary keys or all objects if no primary keys are specified.
     *
     * @param <T>
     *            the generic type
     * @param mappedClass
     *            the mapped class
     * @param primaryKeys
     *            the primary keys
     */
    @Override
    public <T extends DataBaseObject> void clearCache(final Class<T> mappedClass, final List<Object> primaryKeys) {
        clearCache(HibernateSessionFactory.getPersistenClass(mappedClass), primaryKeys);
    }

    /**
     * Remove objects from the the Hibernate second level cache regions of the specified table name with the specified
     * primary keys or all objects if no primary keys are specified.
     *
     * @param tableName
     *            the table name
     * @param columnName
     *            the column name
     * @param primaryKeys
     *            the primary keys
     */
    @Override
    public void clearCache(final String tableName, final String columnName, final List<Object> primaryKeys) {
        final var persistentClass = HibernateSessionFactory.getPersistenClass(tableName);
        final var columns = persistentClass.getTable().getPrimaryKey().getColumns();
        if (columns.size() == 1 && columns.get(0).getName().equals(columnName)) {
            clearCache(persistentClass, primaryKeys);
        }
    }

    /**
     * Remove objects from the the Hibernate second level cache regions of the specified table name with the specified
     * primary keys or all objects if no primary keys are specified.
     *
     * @param persistentClass
     *            the persistent class
     * @param primaryKeys
     *            the primary keys
     */
    private void clearCache(final PersistentClass persistentClass, final List<Object> primaryKeys) {
        final Class<?> mappedClass = persistentClass.getMappedClass();
        if (primaryKeys == null || primaryKeys.isEmpty()) {
            cache.evict(mappedClass);
            if (DEBUG_CACHE) {
                _log.debug("Cache {} cleared for all entities", mappedClass.getName());
            }
        } else {
            final Class<?> hibernateClass = persistentClass.getIdentifier().getType().getReturnedClass();
            for (final Object primaryKey : primaryKeys) {
                if (primaryKey instanceof final Serializable serializableKey) {
                    final var hibernateKey = convert(serializableKey, hibernateClass);
                    if (cache.contains(mappedClass, hibernateKey)) {
                        cache.evict(mappedClass, hibernateKey);
                        if (DEBUG_CACHE) {
                            _log.debug("Cache {} cleared for entity {}", mappedClass, hibernateKey);
                        }
                    }
                }
            }
        }
    }

    /**
     * Convert from the JDBC type to the Hibernate type.
     *
     * @param value
     *            the value
     * @param toClass
     *            the to class
     *
     * @return the value converted in the requested class
     */
    private Serializable convert(final Serializable value, final Class<?> toClass) {
        if (value instanceof final BigDecimal bigDecimalValue) {
            if (Long.class.equals(toClass)) {
                return bigDecimalValue.longValueExact();
            }
            if (Integer.class.equals(toClass)) {
                return bigDecimalValue.intValueExact();
            }
        }
        if (value.getClass() != toClass) {
            _log.warn("No converter available for: {} -> {}", value.getClass().getName(), toClass.getName());
        }
        return value;
    }

    /**
     * Remove all objects from the the Hibernate second level cache regions.
     *
     * @see ecmwf.common.database.Broker#clearCache()
     */
    @Override
    public void clearCache() {
        cache.evictAllRegions(); // Clear all caches (first and second level)
        _log.debug("Cache cleared for all entities");
    }

    /**
     * This method takes an SQL query as input and returns a ResultSet.
     *
     * Before executing the query, it sets the connection to read-only mode and sets the session's default read-only
     * mode to true. Then, it executes the query using the doReturningWork method of the session, which allows the query
     * to be executed on the underlying JDBC connection.
     *
     * If the query execution results in an exception, the method catches the exception, logs it, and throws a
     * BrokerException.
     *
     * @param sql
     *            the sql
     *
     * @return the result set
     *
     * @throws BrokerException
     *             the broker exception
     * @throws SQLException
     *             the SQL exception
     *
     * @see ecmwf.common.database.Broker#executeQuery(java.lang.String)
     */
    @Override
    public ResultSet executeQuery(final String sql) throws BrokerException, SQLException {
        setReadOnly(true);
        session.setDefaultReadOnly(true);
        try {
            return session.doReturningWork(connection -> {
                final var statement = connection.createStatement();
                statement.setFetchSize(FETCH_SIZE);
                return statement.executeQuery(sql);
            });
        } catch (final Exception e) {
            _log.error("executeQuery: {}", sql, e);
            throw new BrokerException(e);
        }
    }

    /**
     * This method takes a SQL query as input and executes it against the database. It sets the session to be not
     * read-only, then creates a StringBuilder instance to keep track of the number of rows affected by the query. It
     * then executes the query by creating a Statement instance from the database connection and calling its
     * executeUpdate method, which returns an integer representing the number of rows affected. Finally, it clears the
     * session and returns the number of rows as an integer. If any errors occur during the execution of the query, it
     * logs an error message and throws a SQLException. *
     *
     * @param sql
     *            the sql
     *
     * @return the int
     *
     * @throws BrokerException
     *             the broker exception
     * @throws SQLException
     *             the SQL exception
     *
     * @see ecmwf.common.database.Broker#executeUpdate(java.lang.String)
     */
    @Override
    public int executeUpdate(final String sql) throws BrokerException, SQLException {
        setReadOnly(false);
        final var numberOfRows = new StringBuilder();
        try {
            session.doWork(connection -> {
                try (final var statement = connection.createStatement()) {
                    numberOfRows.append(statement.executeUpdate(sql));
                } finally {
                    session.clear();
                }
            });
        } catch (final Exception e) {
            _log.error("executeUpdate: {}", sql, e);
            throw new SQLException(e);
        }
        return Integer.parseInt(numberOfRows.toString());
    }

    /**
     * This is a method that retrieves a specific object from the database using its primary key. It takes an object as
     * a parameter and returns an object of the same type.
     *
     * It first sets the session to read-only mode to prevent any modifications to the object. Then, it retrieves the
     * primary key value of the input object using the getObjectIdentifier method. Finally, it uses the session.get
     * method to retrieve the object with the specified primary key from the database. If the object is not found, it
     * returns null.
     *
     * @param <T>
     *            the generic type
     * @param object
     *            the object
     *
     * @return the t
     *
     * @see ecmwf.common.database.Broker#get(ecmwf.common.database.DataBaseObject)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends DataBaseObject> T get(final T object) {
        setReadOnly(true);
        session.setDefaultReadOnly(true);
        try {
            return (T) session.get(object.getClass(), getObjectIdentifier(object, session));
        } catch (final HibernateException e) {
            _log.error("get", e);
            throw e;
        }
    }

    /**
     * This method returns an iterator for all instances of a given class (specified by the target parameter) that are
     * currently persisted in the database. It sets the session to read-only mode and creates a CriteriaQuery object to
     * build the query. Then, it creates a Query object from the CriteriaQuery and executes the query to get the list of
     * results. Finally, it returns an iterator for the list of results. If there is any error, it logs the error and
     * throws a HibernateException.
     *
     * @param <T>
     *            the generic type
     * @param target
     *            the target
     *
     * @return the iterator
     *
     * @see ecmwf.common.database.Broker#getIterator(java.lang.Class)
     */
    @Override
    public <T extends DataBaseObject> Iterator<T> getIterator(final Class<T> target) {
        setReadOnly(true);
        session.setDefaultReadOnly(true);
        try {
            final CriteriaQuery<T> cq = session.getCriteriaBuilder().createQuery(target);
            final Query<T> query = session.createQuery(cq.select(cq.from(target)));
            query.setFetchSize(FETCH_SIZE);
            query.setCacheable(QUERY_CACHEABLE);
            return query.list().iterator();
        } catch (final HibernateException e) {
            _log.error("getIterator: {}", target.getName(), e);
            throw e;
        }
    }

    /**
     * This method returns an iterator over a list of database objects of a specific type, based on a given SQL query.
     * The SQL query is passed as a string parameter, and the type of the objects to be retrieved is passed as a generic
     * type parameter.
     *
     * Inside the method, it sets the session to be read-only and creates a native SQL query using the
     * createNativeQuery() method of the session object, passing the SQL string and the target class. It sets the fetch
     * size and cacheable properties of the query, and returns an iterator over the list of results obtained by
     * executing the query using the list() method of the query object.
     *
     * If any HibernateException occurs, it logs an error message and throws the exception.
     *
     * @param <T>
     *            the generic type
     * @param target
     *            the target
     * @param sql
     *            the sql
     *
     * @return the iterator
     *
     * @see ecmwf.common.database.Broker#getIterator(java.lang.Class, java.lang.String)
     */
    @Override
    public <T extends DataBaseObject> Iterator<T> getIterator(final Class<T> target, final String sql) {
        setReadOnly(true);
        session.setDefaultReadOnly(true);
        try {
            final NativeQuery<T> query = session.createNativeQuery(sql, target);
            query.setFetchSize(FETCH_SIZE);
            query.setCacheable(QUERY_CACHEABLE);
            return query.list().iterator();
        } catch (final HibernateException e) {
            _log.error("getIterator: {} -> {}", target.getName(), sql, e);
            throw e;
        }
    }

    /**
     * This method is retrieving the primary key values of a given DataBaseObject instance. It first gets the Class
     * object of the object and then retrieves the ListIterator of the columns that make up the primary key of the
     * database table associated with the object. It then iterates over the primary key columns and gets the
     * corresponding field from the object using reflection. The value of each field is added to a List of keys, which
     * is then converted to an array of String and returned.
     *
     * @param object
     *            the object
     *
     * @return the primary key values
     *
     * @throws IllegalArgumentException
     *             the illegal argument exception
     * @throws IllegalAccessException
     *             the illegal access exception
     * @throws NoSuchFieldException
     *             the no such field exception
     * @throws SecurityException
     *             the security exception
     *
     * @see ecmwf.common.database.Broker#getPrimaryKeyValues(ecmwf.common.database. DataBaseObject)
     */
    @Override
    public Object[] getPrimaryKeyValues(final DataBaseObject object)
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        final Class<? extends DataBaseObject> clazz = object.getClass();
        final var primaryKeyIterator = HibernateSessionFactory.getMetadata().getEntityBinding(clazz.getName())
                .getTable().getPrimaryKey().getColumns().listIterator();
        final List<Object> keys = new ArrayList<>();
        while (primaryKeyIterator.hasNext()) {
            keys.add(clazz.getDeclaredField(primaryKeyIterator.next().getName()).get(object));
        }
        return keys.toArray(new Object[keys.size()]);
    }

    /**
     * This method performs a delete of the given object.
     *
     * @param object
     *            the object
     *
     * @see ecmwf.common.database.Broker#delete(ecmwf.common.database.DataBaseObject)
     */
    @Override
    public void delete(final DataBaseObject object) {
        retryablePerform(ExecuteOperation.DELETE, object, RETRY_TRANSACTION_COUNT);
    }

    /**
     * This method performs a save (if the object is new) or update (if the object already exists) of the given object.
     *
     * @param object
     *            the object
     *
     * @see ecmwf.common.database.Broker#store(ecmwf.common.database.DataBaseObject)
     */
    @Override
    public void store(final DataBaseObject object) {
        retryablePerform(ExecuteOperation.SAVE_OR_UPDATE, object, RETRY_TRANSACTION_COUNT);
    }

    /**
     * This method performs a save or update of the given object depending on the update parameter.
     *
     * @param object
     *            the object
     * @param update
     *            the update
     *
     * @see ecmwf.common.database.Broker#store(ecmwf.common.database.DataBaseObject, boolean)
     */
    @Override
    public void store(final DataBaseObject object, final boolean update) {
        retryablePerform(update ? ExecuteOperation.UPDATE : ExecuteOperation.SAVE, object, RETRY_TRANSACTION_COUNT);
    }

    /**
     * This method performs a CRUD (Create, Read, Update, Delete) operation on the underlying database object. It begins
     * by setting the connection to read-write mode. It then begins a transaction and executes the specified operation
     * (saveOrUpdate, save, update, or delete) on the specified database object. If an exception occurs during the
     * transaction, the transaction is rolled back and the exception is rethrown. Finally, the Hibernate session is
     * cleared.
     *
     * @param operation
     *            the operation
     * @param object
     *            the object
     */
    private void perform(final ExecuteOperation operation, final DataBaseObject object) {
        setReadOnly(false);
        try {
            // For Hibernate6 replace saveOrUpdate, save and update by persist, merge ...
            session.beginTransaction();
            switch (operation) {
            case SAVE_OR_UPDATE:
                session.saveOrUpdate(object);
                break;
            case SAVE:
                session.persist(object);
                break;
            case UPDATE:
                session.merge(object);
                break;
            case DELETE:
                session.remove(object);
                break;
            }
            session.getTransaction().commit();
        } catch (final HibernateException e) {
            _log.warn("perform", e);
            session.getTransaction().rollback();
            throw e;
        } finally {
            session.clear();
        }
    }

    /**
     * Perform the request and retry if required.
     *
     * @param operation
     *            the operation
     * @param object
     *            the object
     * @param retryCount
     *            the retry count
     */
    private void retryablePerform(final ExecuteOperation operation, final DataBaseObject object, final int retryCount) {
        try {
            perform(operation, object);
        } catch (final TransactionException e) {
            if ((retryCount <= 0) || !worthRetrying(e)) {
                throw e;
            }
            try {
                Thread.sleep(RETRY_TRANSACTION_DELAY);
            } catch (final InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            retryablePerform(operation, object, retryCount - 1);
        }
    }

    /**
     * Worth retrying?
     *
     * @param e
     *            the e
     *
     * @return true, if successful
     */
    private static boolean worthRetrying(final TransactionException e) {
        if (e.getCause() instanceof final SQLTransactionRollbackException rollbackException) {
            final var message = rollbackException.getMessage();
            return message != null && message.indexOf("try restarting transaction") != -1;
        }
        return false;
    }

    /**
     * This method is checking if the underlying hibernate session is closed.
     *
     * @return true, if is closed
     *
     * @see ecmwf.common.database.Broker#isClosed()
     */
    @Override
    public boolean isClosed() {
        return session == null || !session.isOpen();
    }

    /**
     * This method is closing the underlying hibernate session.
     *
     * @param success
     *            the success
     *
     * @see ecmwf.common.database.Broker#release(boolean)
     */
    @Override
    public void release(final boolean success) {
        session.close();
    }

    /**
     * This class is tracking the activity of a database node.
     *
     * The class has several fields, including a DecimalFormat object called "format" (used to format numbers with two
     * decimal points), a "startup" long that stores the system time when the object was created, and two AtomicLong
     * objects called "reads" and "writes" that are used to track the number of read and write operations performed on
     * the node.
     *
     * The constructor for the NodeActivity class takes a "hostAddress" argument, which is used to identify the database
     * node being tracked.
     *
     * The class also has a method called "getAvgPerSec" that takes an AtomicLong object called "count" and returns a
     * string representation of the average number of operations per second for that count, based on the time elapsed
     * since the object was created.
     *
     * The main method in the class is "update", which takes a boolean argument called "read" that indicates whether a
     * read or write operation has been performed. The method updates the appropriate AtomicLong object and, if the
     * number of operations is a multiple of a constant called "DEBUG_FREQUENCY", it logs a message to a logger called
     * "_log" indicating the number of reads and writes, along with their respective average operations per second. The
     * message includes the hostAddress of the node being tracked.
     */
    private static final class NodeActivity {

        /** The Constant format. */
        static final DecimalFormat format = new DecimalFormat("0.00");

        /** The startup. */
        final long startup = System.currentTimeMillis();

        /** The reads. */
        final AtomicLong reads = new AtomicLong(0);

        /** The writes. */
        final AtomicLong writes = new AtomicLong(0);

        /** The host address. */
        final String hostAddress;

        /**
         * Instantiates a new node activity.
         *
         * @param hostAddress
         *            the host address
         */
        NodeActivity(final String hostAddress) {
            this.hostAddress = hostAddress;
        }

        /**
         * Gets the avg per sec.
         *
         * @param count
         *            the count
         *
         * @return the avg per sec
         */
        String getAvgPerSec(final AtomicLong count) {
            return format.format(count.get() / ((System.currentTimeMillis() - startup) / 1000d));
        }

        /**
         * Update.
         *
         * @param read
         *            the read
         */
        void update(final boolean read) {
            if ((read ? reads : writes).updateAndGet(c -> c == Long.MAX_VALUE ? 1 : c + 1) % DEBUG_FREQUENCY == 0
                    && _log.isDebugEnabled()) {
                _log.debug("Database node {}: Reads: {} ({}/sec avg) Writes: {} ({}/sec avg)", hostAddress, reads,
                        getAvgPerSec(reads), writes, getAvgPerSec(writes));
            }
        }
    }
}
