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
 * The Interface Broker. Generic interface to allow plugging any object mapping
 * broker (e.g. OJB, Hibernate).
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.sql.SQLException;
import java.util.List;

import ecmwf.common.technical.CloseableIterator;

/**
 * The Interface Broker.
 */
interface Broker extends AutoCloseable {

    /**
     * Clear cache for the specified entities.
     *
     * @param tableName
     *            the table name
     * @param columnName
     *            the column name
     * @param primaryKeys
     *            the primary keys
     */
    void clearCache(final String tableName, final String columnName, final List<Object> primaryKeys);

    /**
     * Clear cache for the specified entities.
     *
     * @param <T>
     *            the generic type
     * @param clazz
     *            the clazz
     * @param primaryKeys
     *            the primary keys
     */
    <T extends DataBaseObject> void clearCache(final Class<T> clazz, final List<Object> primaryKeys);

    /**
     * Clear cache for all entities.
     */
    void clearCache();

    /**
     * Delete.
     *
     * @param object
     *            the object
     */
    void delete(DataBaseObject object);

    /**
     * Execute query.
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
     */
    CloseableResultSetWrapper executeQuery(boolean release, String sql) throws BrokerException, SQLException;

    /**
     * Execute update.
     *
     * @param sql
     *            the sql
     *
     * @return the number of fields processed
     *
     * @throws BrokerException
     *             the broker exception
     * @throws SQLException
     *             the SQL exception
     */
    int executeUpdate(String sql) throws BrokerException, SQLException;

    /**
     * Gets the.
     *
     * @param <T>
     *            the generic type
     * @param object
     *            the object
     *
     * @return the t
     */
    <T extends DataBaseObject> T get(T object);

    /**
     * Gets the iterator.
     *
     * @param <T>
     *            the generic type
     * @param target
     *            the target
     *
     * @return the iterator
     *
     * @throws BrokerException
     *             the broker exception
     */
    <T extends DataBaseObject> CloseableIterator<T> getIterator(Class<T> target) throws BrokerException;

    /**
     * Gets the iterator.
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
     * @throws BrokerException
     *             the broker exception
     */
    <T extends DataBaseObject> CloseableIterator<T> getIterator(Class<T> target, String sql) throws BrokerException;

    /**
     * Gets the primary key values.
     *
     * @param object
     *            the object
     *
     * @return the primary key values
     *
     * @throws Exception
     *             the exception
     * @throws SecurityException
     *             the security exception
     * @throws IllegalArgumentException
     *             the illegal argument exception
     */
    Object[] getPrimaryKeyValues(DataBaseObject object) throws Exception;

    /**
     * Checks if is closed.
     *
     * @return true, if is closed
     */
    boolean isClosed();

    /**
     * Called by the try-catch-resource.
     */
    void close();

    /**
     * Called by the DBIterator and DBResultSet.
     */
    void release();

    /**
     * Store the object in the database. If update is true then it does an update of an existing object, otherwise the
     * object is created. If required, the primary key is automatically generated.
     *
     * @param object
     *            the object
     * @param update
     *            the update
     */
    void store(DataBaseObject object, boolean update);
}
