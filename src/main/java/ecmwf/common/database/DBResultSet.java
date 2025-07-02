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

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.technical.Cnf;
import ecmwf.common.technical.ResourceTracker;

/**
 * The Class DBResultSet.
 */
final class DBResultSet implements AutoCloseable {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(DBResultSet.class);

    /** The Constant TRACKER. */
    private static final ResourceTracker TRACKER = new ResourceTracker(DBResultSet.class);

    /** The Constant EMULATE_CALC_FOUND_ROWS. */
    private static final boolean EMULATE_CALC_FOUND_ROWS = Cnf.at("DataBase", "emulateCalcFoundRows",
            "hsqldb".equalsIgnoreCase(Cnf.at("DataBase", "subProtocol", "")));

    /** The wrapper. */
    private final CloseableResultSetWrapper wrapper;

    /** The result set. */
    private final ResultSet resultSet;

    /** The sqlQuery. */
    private final String sqlQuery;

    /** The closed flag. */
    private final AtomicBoolean closed = new AtomicBoolean(false);

    /** The broker. */
    private Broker broker = null;

    /** The foundRows. */
    private int foundRows = -1;

    /** The hasCalcFoundRows. */
    private final boolean hasCalcFoundRows;

    /**
     * Instantiates a new DB result set.
     *
     * @param broker
     *            the broker
     * @param sql
     *            the sql request
     *
     * @throws BrokerException
     *             the broker exception
     * @throws SQLException
     *             the SQL exception
     */
    DBResultSet(final Broker broker, final String sql) throws BrokerException, SQLException {
        hasCalcFoundRows = Pattern.compile("(?i)^\\s*SELECT\\s+SQL_CALC_FOUND_ROWS\\s+").matcher(sql).find();
        sqlQuery = hasCalcFoundRows && EMULATE_CALC_FOUND_ROWS ? getInitialSQL(sql) : sql;
        wrapper = broker.executeQuery(true, sqlQuery);
        resultSet = wrapper.getResultSet();
        this.broker = broker;
        TRACKER.onOpen();
    }

    /**
     * Gets the initial SQL. Remove SQL_CALC_FOUND_ROWS from the SQL query! this feature is implemented in this class
     * rather than being delegated to the database.
     *
     * @param sql
     *            the sql
     *
     * @return the initial SQL
     */
    private static String getInitialSQL(final String sql) {
        return sql.replaceFirst("(?i)^\\s*SELECT\\s+SQL_CALC_FOUND_ROWS\\s+", "SELECT ");
    }

    /**
     * Gets the found rows SQL. Replace "SELECT ... FROM" by "SELECT COUNT(*) FROM", remove the "ORDER BY [ASC|DESC]"
     * statements and remove the "LIMIT a[,b]" at the end!
     *
     * @param sql
     *            the sql
     *
     * @return the found rows SQL
     */
    private static String getFoundRowsSQL(final String sql) {
        return sql.replaceAll("[\\s|\\t|\\r\\n]+", " ").trim().concat(" ")
                .replaceAll("(?i)\\s+ORDER\\s+BY\\s+(.*)\\s+[ASC|DESC]?", " ")
                .replaceFirst("(?i)^\\s*SELECT\\s+(.*)\\s+FROM\\s+", "SELECT COUNT(*) FROM ")
                .replaceFirst("(?i)\\s+LIMIT\\s+\\d+[,]?\\d+\\s*[;]?\\s*$", "");
    }

    /**
     * Gets the result set.
     *
     * @return the result set
     */
    public ResultSet getResultSet() {
        return resultSet;
    }

    /**
     * Gets the found rows.
     *
     * @return the found rows
     */
    public int getFoundRows() {
        return foundRows;
    }

    /**
     * Gets the found rows.
     *
     * @param cursor
     *            the cursor
     *
     * @return the found rows
     */
    public int getFoundRows(final DataBaseCursor cursor) {
        if (EMULATE_CALC_FOUND_ROWS) { // We do count(*) after the select so the result might be different!
            final var length = cursor.getLength();
            return length < foundRows ? length : foundRows;
        }
        // Normal SQL_CALC_FOUND_ROWS!
        return foundRows;
    }

    /**
     * Next.
     *
     * @return true, if successful
     *
     * @throws SQLException
     *             the SQL exception
     */
    public boolean next() throws SQLException {
        final var result = resultSet.next();
        if (!result && hasCalcFoundRows) {
            // Either We are required to process the second SQL query to find the total
            // number of entries without limits or we rely on the special MySQL feature!
            try (var setWrapper = broker.executeQuery(true,
                    EMULATE_CALC_FOUND_ROWS ? getFoundRowsSQL(sqlQuery) : "SELECT FOUND_ROWS()")) {
                final var set = setWrapper.getResultSet();
                if (set.next()) {
                    foundRows = set.getInt(1);
                }
            } catch (final Throwable t) {
                _log.warn("close", t);
            }
        }
        return result;
    }

    /**
     * Close.
     */
    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            boolean closedSuccessfully = true;
            try {
                try {
                    wrapper.close();
                } catch (Throwable t) {
                    closedSuccessfully = false;
                    _log.warn("Failed to close wrapper", t);
                }
                try {
                    broker.release();
                } catch (Throwable t) {
                    closedSuccessfully = false;
                    _log.warn("Failed to release broker", t);
                }
            } finally {
                TRACKER.onClose(closedSuccessfully);
            }
        }
    }

    /**
     * Was null.
     *
     * @return true, if successful
     *
     * @throws SQLException
     *             the SQL exception
     */
    public boolean wasNull() throws SQLException {
        return resultSet.wasNull();
    }

    /**
     * Gets the string.
     *
     * @param columnIndex
     *            the column index
     *
     * @return the string
     *
     * @throws SQLException
     *             the SQL exception
     */
    public String getString(final int columnIndex) throws SQLException {
        return resultSet.getString(columnIndex);
    }

    /**
     * Gets the boolean.
     *
     * @param columnIndex
     *            the column index
     *
     * @return the boolean
     *
     * @throws SQLException
     *             the SQL exception
     */
    public boolean getBoolean(final int columnIndex) throws SQLException {
        return resultSet.getBoolean(columnIndex);
    }

    /**
     * Gets the integer.
     *
     * @param columnIndex
     *            the column index
     *
     * @return the integer
     *
     * @throws SQLException
     *             the SQL exception
     */
    public Integer getInteger(final int columnIndex) throws SQLException {
        final var integer = resultSet.getString(columnIndex);
        Integer result = null;
        if (integer != null) {
            result = Integer.parseInt(integer);
        }
        return result;
    }

    /**
     * Gets the object.
     *
     * @param columnIndex
     *            the column index
     *
     * @return the object
     *
     * @throws SQLException
     *             the SQL exception
     */
    public Object getObject(final int columnIndex) throws SQLException {
        return resultSet.getObject(columnIndex);
    }

    /**
     * Gets the int.
     *
     * @param columnIndex
     *            the column index
     *
     * @return the int
     *
     * @throws SQLException
     *             the SQL exception
     */
    public int getInt(final int columnIndex) throws SQLException {
        return resultSet.getInt(columnIndex);
    }

    /**
     * Gets the long.
     *
     * @param columnIndex
     *            the column index
     *
     * @return the long
     *
     * @throws SQLException
     *             the SQL exception
     */
    public long getLong(final int columnIndex) throws SQLException {
        return resultSet.getLong(columnIndex);
    }

    /**
     * Gets the string.
     *
     * @param columnName
     *            the column name
     *
     * @return the string
     *
     * @throws SQLException
     *             the SQL exception
     */
    public String getString(final String columnName) throws SQLException {
        return resultSet.getString(columnName);
    }

    /**
     * Gets the boolean.
     *
     * @param columnName
     *            the column name
     *
     * @return the boolean
     *
     * @throws SQLException
     *             the SQL exception
     */
    public boolean getBoolean(final String columnName) throws SQLException {
        return resultSet.getBoolean(columnName);
    }

    /**
     * Gets the big decimal.
     *
     * @param columnName
     *            the column name
     *
     * @return the boolean
     *
     * @throws SQLException
     *             the SQL exception
     */
    public BigDecimal getBigDecimal(final String columnName) throws SQLException {
        return resultSet.getBigDecimal(columnName);
    }

    /**
     * Gets the integer.
     *
     * @param columnName
     *            the column name
     *
     * @return the integer
     *
     * @throws SQLException
     *             the SQL exception
     */
    public Integer getInteger(final String columnName) throws SQLException {
        final var integer = resultSet.getString(columnName);
        Integer result = null;
        if (integer != null) {
            result = Integer.parseInt(integer);
        }
        return result;
    }

    /**
     * Gets the object.
     *
     * @param columnName
     *            the column name
     *
     * @return the object
     *
     * @throws SQLException
     *             the SQL exception
     */
    public Object getObject(final String columnName) throws SQLException {
        return resultSet.getObject(columnName);
    }

    /**
     * Gets the int.
     *
     * @param columnName
     *            the column name
     *
     * @return the int
     *
     * @throws SQLException
     *             the SQL exception
     */
    public int getInt(final String columnName) throws SQLException {
        return resultSet.getInt(columnName);
    }

    /**
     * Gets the date.
     *
     * @param columnName
     *            the column name
     *
     * @return the date
     *
     * @throws SQLException
     *             the SQL exception
     */
    public Date getDate(final String columnName) throws SQLException {
        return resultSet.getDate(columnName);
    }

    /**
     * Gets the timestamp.
     *
     * @param columnName
     *            the column name
     *
     * @return the timestamp
     *
     * @throws SQLException
     *             the SQL exception
     */
    public Timestamp getTimestamp(final String columnName) throws SQLException {
        final var timestamp = getString(columnName);
        Timestamp result = null;
        if (timestamp != null) {
            result = new Timestamp(Long.parseLong(timestamp));
        }
        return result;
    }

    /**
     * Gets the time.
     *
     * @param columnName
     *            the column name
     *
     * @return the time
     *
     * @throws SQLException
     *             the SQL exception
     */
    public Time getTime(final String columnName) throws SQLException {
        return resultSet.getTime(columnName);
    }

    /**
     * Gets the long.
     *
     * @param columnName
     *            the column name
     *
     * @return the long
     *
     * @throws SQLException
     *             the SQL exception
     */
    public long getLong(final String columnName) throws SQLException {
        return resultSet.getLong(columnName);
    }

    /**
     * Gets the float.
     *
     * @param columnName
     *            the column name
     *
     * @return the float
     *
     * @throws SQLException
     *             the SQL exception
     */
    public float getFloat(final String columnName) throws SQLException {
        return resultSet.getFloat(columnName);
    }

    /**
     * Find column.
     *
     * @param columnName
     *            the column name
     *
     * @return the int
     *
     * @throws SQLException
     *             the SQL exception
     */
    public int findColumn(final String columnName) throws SQLException {
        return resultSet.findColumn(columnName);
    }

    /**
     * Gets the column type.
     *
     * @param columnIndex
     *            the column index
     *
     * @return the column type
     *
     * @throws SQLException
     *             the SQL exception
     */
    public int getColumnType(final int columnIndex) throws SQLException {
        return resultSet.getMetaData().getColumnType(columnIndex);
    }

    /**
     * Gets the column name.
     *
     * @param columnIndex
     *            the column index
     *
     * @return the column type
     *
     * @throws SQLException
     *             the SQL exception
     */
    public String getColumnName(final int columnIndex) throws SQLException {
        return resultSet.getMetaData().getColumnName(columnIndex);
    }
}
