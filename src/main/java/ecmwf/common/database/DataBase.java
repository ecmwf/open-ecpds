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

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.sql.Date;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

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

import ecmwf.common.mbean.MBeanService;
import ecmwf.common.text.Format;

/**
 * The Class DataBase.
 */
public class DataBase extends DataGet implements MBeanService, Closeable {
    /** The Constant FQCN. */
    private static final String FQCN = DataBase.class.getName() + ".";

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(DataBase.class);

    /** The Constant NL. */
    private static final String NL = System.lineSeparator();

    /** The closed. */
    private final AtomicBoolean closed = new AtomicBoolean(false);

    /** The server. */
    private String server = null;

    /** The driver. */
    private Driver driver = null;

    /** The dataFile. */
    private ScriptFileImpl dataFile = null;

    /** The logEvents. */
    private boolean logEvents = false;

    /** The debugSql. */
    private boolean debugSql = false;

    /** The maxTime. */
    private long maxTime = 5 * Timer.ONE_SECOND;

    /** The maxCount. */
    private long maxCount = 5000;

    /** The brokerFactory. */
    private BrokerFactory brokerFactory = null;

    /**
     * Initialise.
     *
     * @param brokerProxy
     *            the broker proxy
     * @param driverClassName
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
     * @param serverUrl
     *            the server
     * @param repository
     *            the repository
     * @param validation
     *            the validation
     * @param logDatabaseEvents
     *            the log events
     * @param debugSqlRequests
     *            the debug sql
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IllegalAccessException
     *             the illegal access exception
     * @throws InstantiationException
     *             the instantiation exception
     * @throws ClassNotFoundException
     *             the class not found exception
     * @throws DataBaseException
     *             the data base exception
     * @throws IllegalArgumentException
     *             the illegal argument exception
     * @throws InvocationTargetException
     *             the invocation target exception
     * @throws NoSuchMethodException
     *             the no such method exception
     * @throws SecurityException
     *             the security exception
     */
    public void initialize(final String brokerProxy, final String driverClassName, final String level,
            final String protocol, final String subProtocol, final String alias, final String user,
            final String password, final String dbms, final String serverUrl, final String repository,
            final String validation, final boolean logDatabaseEvents, final boolean debugSqlRequests)
            throws SQLException, IllegalAccessException, InstantiationException, ClassNotFoundException,
            DataBaseException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
            SecurityException {
        logEvents = logDatabaseEvents;
        debugSql = debugSqlRequests;
        dataFile = getScriptFileImpl(repository);
        server = serverUrl != null && !serverUrl.isEmpty() ? serverUrl : null;
        _log.debug("Log events: {}", logDatabaseEvents ? "enabled" : "disabled");
        // Initialize the JDBC driver.
        if (System.getProperty(FQCN + "debug", "false").matches("^(?i)(yes|true)$")) {
            DriverManager.setLogWriter(new PrintWriter(new OutputStream() {
                @Override
                public void write(final int b) {
                    // Do nothing, we won't use this output stream
                }
            }) {

                @Override
                public void println(final String message) {
                    _log.debug(message);
                }
            });
        }
        driver = (Driver) Class.forName(driverClassName).getDeclaredConstructor().newInstance();
        DriverManager.registerDriver(driver);
        if (!(Class.forName(brokerProxy).getDeclaredConstructor()
                .newInstance() instanceof final BrokerFactory factory)) {
            throw new DataBaseException("Class " + brokerProxy + " not a BrokerFactory");
        }
        factory.init(debugSqlRequests, driverClassName, level, protocol, subProtocol, alias, user, password, dbms,
                validation);
        brokerFactory = factory;
    }

    /**
     * Format date.
     *
     * @param date
     *            the date
     *
     * @return the string
     */
    public static final String formatDate(final long date) {
        final var df = new java.sql.Date(date);
        final var dt = new java.util.GregorianCalendar();
        dt.setTime(df);
        return new java.text.SimpleDateFormat("yyyy-MM-dd").format(df);
    }

    /**
     * Format time.
     *
     * @param time
     *            the time
     *
     * @return the string
     */
    public static final String formatTime(final long time) {
        final var df = new java.sql.Time(time);
        final var dt = new java.util.GregorianCalendar();
        dt.setTime(df);
        return new java.text.SimpleDateFormat("HH:mm:ss").format(df);
    }

    /**
     * Reload repository.
     */
    public synchronized void reloadRepository() {
        dataFile = new ScriptFileImpl(dataFile.getRepository());
    }

    /**
     * Close.
     */
    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            if (driver != null) {
                try {
                    DriverManager.deregisterDriver(driver);
                } catch (final Exception e) {
                    _log.warn(e);
                } finally {
                    driver = null;
                }
            }
        } else {
            _log.debug("Already closed");
        }
    }

    /**
     * Gets the.
     *
     * @param <T>
     *            the generic type
     * @param object
     *            the object
     *
     * @return the t
     *
     * @throws DataBaseException
     *             the data base exception
     */
    @Override
    protected <T extends DataBaseObject> T get(final T object) throws DataBaseException {
        final var thread = Thread.currentThread();
        final var loader = thread.getContextClassLoader();
        thread.setContextClassLoader(object.getClass().getClassLoader());
        if (debugSql && _log.isDebugEnabled()) {
            _log.debug("DataBase request (1 element): get{}", Format.getClassName(object));
        }
        T result = null;
        try (var broker = brokerFactory.getBroker()) {
            result = broker.get(object);
        } catch (final Throwable e) {
            error("get", object, e);
            throw new BrokerException(e);
        } finally {
            thread.setContextClassLoader(loader);
        }
        if (result == null) {
            final var ident = new StringBuilder();
            for (final Object key : getPrimaryKeyValues(object)) {
                ident.append(ident.length() == 0 ? "" : ",").append(key);
            }
            // This String should not be changed as it is tested in some places
            // to know if an element is missing in the DataBase. Changing this
            // String might confuse the caller that might think that the element
            // exists!
            throw new DataBaseException(Format.getClassName(object) + " not found: {" + ident.toString() + "}");
        }
        return result;
    }

    /**
     * Gets the all.
     *
     * @param <T>
     *            the generic type
     * @param target
     *            the target
     *
     * @return the all
     */
    @Override
    protected <T extends DataBaseObject> DBIterator<T> getAll(final Class<T> target) {
        DBIterator<T> result;
        try (var broker = brokerFactory.getBroker()) {
            result = new DBIterator<>(broker, target);
        } catch (final Exception e) {
            error("getAll", target, e);
            result = new DBIterator<>();
        }
        return result;
    }

    /**
     * String array to hashtable. A value is expected to be in the form "key=value" or "key-=value". If the operator is
     * "=" then the value is escaped to avoid SQL injections. If the operator is "-=" then if means the value does not
     * need to be escaped (e.g. already escaped by some other means or generated SQL code).
     *
     * @param values
     *            the values
     *
     * @return the hashtable
     */
    private static Map<String, String> stringArrayToMapWithEscapedSQLValues(final String[] values) {
        final Map<String, String> hash = new HashMap<>();
        for (final String value : values) {
            final var index = value.indexOf("=");
            if (index > 0) {
                final var key = value.substring(0, index);
                final var sql = value.substring(index + 1);
                if (!key.endsWith("-")) {
                    hash.put(key, escapeSql(sql));
                } else {
                    hash.put(value.substring(0, index - 1), sql);
                }
            }
        }
        return hash;
    }

    /**
     * Escape SQL values to avoid SQL injections.
     *
     * @param value
     *            the value
     *
     * @return the escaped value
     */
    protected static String escapeSql(final String value) {
        final var builder = new StringBuilder(value.length() * 11 / 10);
        for (var i = 0; i < value.length(); ++i) {
            final var currentChar = value.charAt(i);
            switch (currentChar) {
            case 0: /* Must be escaped for Mysql */
                builder.append('\\').append('0');
                break;
            case '\n': /* Must be escaped for logs */
                builder.append('\\').append('n');
                break;
            case '\r':
                builder.append('\\').append('r');
                break;
            case '\\':
                builder.append('\\').append('\\');
                break;
            case '\'':
                builder.append('\\').append('\'');
                break;
            case '"': /* Better safe than sorry */
                builder.append('\\').append('"');
                break;
            case '\032': /* This gives problems on Win32 */
                builder.append('\\').append('Z');
                break;
            case '\u00a5', '\u20a9':
                // escape characters interpreted as backslash by Mysql fall through
            default:
                builder.append(currentChar);
            }
        }
        return builder.toString();
    }

    /**
     * Log sql request.
     *
     * @param sql
     *            the sql
     * @param start
     *            the start
     * @param stop
     *            the stop
     * @param numberOfRows
     *            the number of rows
     */
    protected void logSqlRequest(final String sql, final long start, final long stop, final int numberOfRows) {
        final var duration = stop - start;
        if (_log.isDebugEnabled() && duration > maxTime) {
            _log.debug("DataBase request completed ({}){}{}", Format.formatDuration(duration),
                    numberOfRows >= 0 ? " with " + numberOfRows + " row(s) affected" : "", debugSql ? "" : ": " + sql);
        }
    }

    /**
     * Log sql request.
     *
     * @param name
     *            the name
     * @param count
     *            the count
     */
    @Override
    protected void logSqlRequest(final String name, final long count) {
        if (debugSql || count > maxCount) {
            _log.debug("DataBase request completed ({} element(s)): {}", count, name);
        }
    }

    /**
     * Execute select.
     *
     * @param originalSql
     *            the original sql
     *
     * @return the DB result set
     *
     * @throws SQLException
     *             the SQL exception
     */
    protected DBResultSet executeSelect(final String sql) throws SQLException {
        DBResultSet result = null;
        if (debugSql) {
            _log.debug("executeSelect: {}", sql);
        }
        try (var broker = brokerFactory.getBroker()) {
            final var start = System.currentTimeMillis();
            result = new DBResultSet(broker, sql);
            logSqlRequest(sql, start, System.currentTimeMillis(), result.getFoundRows());
            return result;
        } catch (final SQLException e) {
            error("executeSelect", sql, e);
            throw e;
        } catch (final Exception e) {
            error("executeSelect", sql, e);
            throw new SQLException("Database not available");
        }
    }

    /**
     * Select.
     *
     * @param sql
     *            the sql
     *
     * @return the byte[]
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public byte[] select(final String sql) throws SQLException, IOException {
        final var current = System.currentTimeMillis();
        try (var dbResultSet = executeSelect(sql)) {
            return formatResultSet(dbResultSet.getResultSet(), System.currentTimeMillis() - current);
        }
    }

    /**
     * Update.
     *
     * @param sql
     *            the sql
     *
     * @return the string
     *
     * @throws SQLException
     *             the SQL exception
     */
    public String update(final String sql) throws SQLException {
        final var current = System.currentTimeMillis();
        final var numberOfRows = executeCustomUpdate(sql);
        return "Database updated in " + (System.currentTimeMillis() - current) + " ms: " + numberOfRows
                + " rows affected";
    }

    /**
     * Gets the result in text.
     *
     * @param head
     *            the head
     * @param data
     *            the data
     * @param duration
     *            the duration
     *
     * @return the byte[]
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static final byte[] getResultInText(final String[] head, final List<String[]> data, final long duration)
            throws IOException {
        final var width = head.length;
        final var size = new int[width];
        String[] row;
        final var height = data.size();
        for (var i = 0; i < width; i++) {
            size[i] = head[i].length();
        }
        for (var i = 0; i < height; i++) {
            row = data.get(i);
            for (var j = 0; j < width; j++) {
                final var l = row[j].length();
                if (l > size[j]) {
                    size[j] = l;
                }
            }
        }
        // Let's compress the data output to avoid consuming too much memory
        // with big requests!
        final var out = new ByteArrayOutputStream();
        final var gzip = new GZIPOutputStream(out, Deflater.BEST_COMPRESSION);
        for (var i = 0; i < width; i++) {
            gzip.write(head[i].getBytes());
            for (var l = head[i].length(); l <= size[i]; l++) {
                gzip.write(" ".getBytes());
            }
        }
        gzip.write(NL.getBytes());
        for (var i = 0; i < width; i++) {
            for (var l = 0; l < size[i]; l++) {
                gzip.write("-".getBytes());
            }
            gzip.write(" ".getBytes());
        }
        gzip.write(NL.getBytes());
        for (var i = 0; i < height; i++) {
            row = data.get(i);
            for (var j = 0; j < width; j++) {
                gzip.write(row[j].replace('\n', ' ').getBytes());
                for (var l = row[j].length(); l <= size[j]; l++) {
                    gzip.write(" ".getBytes());
                }
            }
            gzip.write(NL.getBytes());
        }
        gzip.write((NL + height + " row(s) in " + duration + " ms" + NL).getBytes());
        gzip.close();
        return out.toByteArray();
    }

    /**
     * _format result set.
     *
     * @param r
     *            the r
     * @param duration
     *            the duration
     *
     * @return the byte[]
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static final byte[] formatResultSet(final ResultSet r, final long duration)
            throws SQLException, IOException {
        if (r == null) {
            // The caller is expecting a gzip output but we won't compress as it
            // is a very short message!
            final var out = new ByteArrayOutputStream();
            final var gzip = new GZIPOutputStream(out, Deflater.NO_COMPRESSION);
            gzip.write("Result (empty)".getBytes());
            gzip.close();
            return out.toByteArray();
        }
        final var data = new ArrayList<String[]>();
        final var m = r.getMetaData();
        final var col = m.getColumnCount();
        final var head = new String[col];
        for (var i = 1; i <= col; i++) {
            head[i - 1] = m.getColumnLabel(i);
        }
        while (r.next()) {
            final var entry = new String[col];
            for (var i = 1; i <= col; i++) {
                entry[i - 1] = r.getString(i);
                if (r.wasNull()) {
                    entry[i - 1] = "(null)";
                }
            }
            data.add(entry);
        }
        return getResultInText(head, data, duration);
    }

    /**
     * Process action.
     *
     * @param columnName
     *            the column name
     * @param action
     *            the action
     * @param ids
     *            the ids
     *
     * @throws SQLException
     *             the SQL exception
     */
    private void processAction(final String columnName, final String action, final List<Object> ids)
            throws SQLException {
        if (!ids.isEmpty()) {
            final var st = new StringTokenizer(action, " ");
            if (st.countTokens() > 2) {
                final var operation = st.nextToken(); // UPDATE or DELETE
                if ("UPDATE".equalsIgnoreCase(operation)) {
                    final var tableName = Format.cleanTextContent(st.nextToken());
                    clearCache(tableName, columnName, ids);
                } else if ("DELETE".equalsIgnoreCase(operation)) {
                    st.nextToken(); // Remove the FROM
                    final var tableName = Format.cleanTextContent(st.nextToken());
                    clearCache(tableName, columnName, ids);
                } else {
                    throw new SQLException("Malformed request: " + action);
                }
            }
            final var sb = new StringBuilder();
            for (final Object id : ids) {
                sb.append(sb.length() > 0 ? "," : "").append("'").append(id).append("'");
            }
            ids.clear();
            executeCustomUpdate(sb.insert(0, action + " (").append(")").toString());
        }
    }

    /**
     * _execute update.
     *
     * @param originalSql
     *            the original sql
     *
     * @return the int
     *
     * @throws SQLException
     *             the SQL exception
     */
    private final int executeCustomUpdate(final String sql) throws SQLException {
        // Check if this is not a loop request to execute?
        if (sql.toUpperCase().startsWith("LOOP ")) {
            final var st = new StringTokenizer(sql, " ");
            if (st.countTokens() > 2) {
                st.nextToken(); // LOOP keyword
                final var limit = Integer.parseInt(st.nextToken());
                final var request = st.nextToken(";");
                var totalNumberOfRows = 0;
                var currentNumberOfRows = 0;
                do {
                    currentNumberOfRows = executeCustomUpdate(request);
                    totalNumberOfRows += currentNumberOfRows;
                } while (currentNumberOfRows >= limit);
                return totalNumberOfRows;
            }
            throw new SQLException("Malformed SQL LOOP request: {}", sql);
        }
        // Check if this is not a chunk request to execute?
        if (sql.toUpperCase().startsWith("CHUNK ")) {
            final var st = new StringTokenizer(sql, " ");
            if (st.countTokens() > 2) {
                st.nextToken(); // CHUNK keyword
                final var limit = Integer.parseInt(st.nextToken());
                final var request = st.nextToken(";");
                final var index = request.toUpperCase().indexOf(" IN ");
                if (index > 0) {
                    final var action = request.substring(0, index + 4).trim(); // UPDATE or DELETE FROM statement
                    final List<Object> ids = new ArrayList<>();
                    var totalNumberOfRows = 0;
                    String columnName = null;
                    try (var rs = executeSelect(request.substring(index + 4).trim())) {
                        while (rs.next()) {
                            if (columnName == null) {
                                columnName = rs.getColumnName(1);
                            }
                            ids.add(rs.getObject(1));
                            totalNumberOfRows++;
                            if (ids.size() > limit) {
                                processAction(columnName, action, ids);
                            }
                        }
                        if (columnName != null) {
                            processAction(columnName, action, ids);
                        }
                    }
                    return totalNumberOfRows;
                }
            }
            throw new SQLException("Malformed SQL CHUNK request: {}", sql);
        }
        if (debugSql) {
            _log.debug("executeUpdate: {}", sql);
        }
        try (var broker = brokerFactory.getBroker()) {
            final var start = System.currentTimeMillis();
            final int numberOfRows;
            if (sql.toUpperCase().startsWith("ANALYZE ")) {
                // This is treated as a query, not an update
                broker.executeQuery(false, sql).close(); // We don't need to read the result!
                numberOfRows = 0;
            } else {
                numberOfRows = broker.executeUpdate(sql);
            }
            logSqlRequest(sql, start, System.currentTimeMillis(), numberOfRows);
            return numberOfRows;
        } catch (final SQLException e) {
            error("executeUpdate", sql, e);
            throw e;
        } catch (final Exception e) {
            error("executeUpdate", sql, e);
            throw new SQLException("Database not available");
        }
    }

    /**
     * Execute select.
     *
     * @param menu
     *            the menu
     * @param name
     *            the name
     *
     * @return the DB result set
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    protected DBResultSet executeSelect(final String menu, final String name) throws SQLException, IOException {
        return dataFile.executeQuery(menu, "select", name);
    }

    /**
     * Execute select.
     *
     * @param menu
     *            the menu
     * @param name
     *            the name
     * @param values
     *            the values
     *
     * @return the DB result set
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    protected DBResultSet executeSelect(final String menu, final String name, final String[] values)
            throws SQLException, IOException {
        return dataFile.executeQuery(menu, "select", name, stringArrayToMapWithEscapedSQLValues(values));
    }

    /**
     * Execute query.
     *
     * @param <T>
     *            the generic type
     * @param menu
     *            the menu
     * @param name
     *            the name
     * @param resultClass
     *            the result class
     *
     * @return the DB iterator
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */

    protected <T extends DataBaseObject> DBIterator<T> executeQuery(final String menu, final String name,
            final Class<T> resultClass) throws SQLException, IOException {
        return dataFile.executeQuery(menu, "query", name, resultClass);
    }

    /**
     * Execute query.
     *
     * @param <T>
     *            the generic type
     * @param menu
     *            the menu
     * @param name
     *            the name
     * @param resultClass
     *            the result class
     * @param values
     *            the values
     *
     * @return the DB iterator
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */

    protected <T extends DataBaseObject> DBIterator<T> executeQuery(final String menu, final String name,
            final Class<T> resultClass, final String[] values) throws SQLException, IOException {
        return dataFile.executeQuery(menu, "query", name, resultClass, stringArrayToMapWithEscapedSQLValues(values));
    }

    /**
     * Execute count as int.
     *
     * @param menu
     *            the menu
     * @param name
     *            the name
     *
     * @return the int
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    protected int executeCountAsInt(final String menu, final String name) throws SQLException, IOException {
        try (var rs = dataFile.executeQuery(menu, "count", name)) {
            return rs.next() ? rs.getInt(1) : -1;
        }
    }

    /**
     * Execute count as int.
     *
     * @param menu
     *            the menu
     * @param name
     *            the name
     * @param values
     *            the values
     *
     * @return the int
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    protected int executeCountAsInt(final String menu, final String name, final String[] values)
            throws SQLException, IOException {
        try (var rs = dataFile.executeQuery(menu, "count", name, stringArrayToMapWithEscapedSQLValues(values))) {
            return rs.next() ? rs.getInt(1) : -1;
        }
    }

    /**
     * Execute count as long.
     *
     * @param menu
     *            the menu
     * @param name
     *            the name
     *
     * @return the long
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    protected long executeCountAsLong(final String menu, final String name) throws SQLException, IOException {
        try (var rs = dataFile.executeQuery(menu, "count", name)) {
            return rs.next() ? rs.getLong(1) : -1;
        }
    }

    /**
     * Execute count as long.
     *
     * @param menu
     *            the menu
     * @param name
     *            the name
     * @param values
     *            the values
     *
     * @return the long
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    protected long executeCountAsLong(final String menu, final String name, final String[] values)
            throws SQLException, IOException {
        try (var rs = dataFile.executeQuery(menu, "count", name, stringArrayToMapWithEscapedSQLValues(values))) {
            return rs.next() ? rs.getLong(1) : -1;
        }
    }

    /**
     * Execute update.
     *
     * @param menu
     *            the menu
     * @param name
     *            the name
     *
     * @return the int
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    protected int executeUpdate(final String menu, final String name) throws SQLException, IOException {
        return dataFile.executeUpdate(menu, "update", name);
    }

    /**
     * Execute update.
     *
     * @param menu
     *            the menu
     * @param name
     *            the name
     * @param values
     *            the values
     *
     * @return the int
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    protected int executeUpdate(final String menu, final String name, final String[] values)
            throws SQLException, IOException {
        return dataFile.executeUpdate(menu, "update", name, stringArrayToMapWithEscapedSQLValues(values));
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
            if ("LogEvents".equals(attributeName)) {
                return logEvents;
            }
            if ("DebugSql".equals(attributeName)) {
                return debugSql;
            }
            if ("MaxTime".equals(attributeName)) {
                return maxTime;
            }
            if ("MaxCount".equals(attributeName)) {
                return maxCount;
            }
            if ("Server".equals(attributeName)) {
                return server;
            }
            if ("Repository".equals(attributeName)) {
                return dataFile.getRepository();
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
        if ("LogEvents".equals(name)) {
            logEvents = ((Boolean) value);
            return true;
        }
        if ("DebugSql".equals(name)) {
            debugSql = ((Boolean) value);
            return true;
        }
        if ("MaxTime".equals(name)) {
            maxTime = (Long) value;
            return true;
        }
        if ("MaxCount".equals(name)) {
            maxCount = (Long) value;
            return true;
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
        return new MBeanInfo(this.getClass().getName(), """
                The OpenECPDS database, which can be local or remote, is used by the \
                ECaccess application to maintain information persistence. This \
                MBean provides informations concerning the database connection \
                and operations to manage the content of the database.""", new MBeanAttributeInfo[] {
                new MBeanAttributeInfo("Server", "java.lang.String", "Server: database location if SERVER mode.", true,
                        false, false),
                new MBeanAttributeInfo("LogEvents", "java.lang.Boolean", "LogEvents: log events in the database.", true,
                        true, false),
                new MBeanAttributeInfo("DebugSql", "java.lang.Boolean",
                        "DebugSql: display sql requests in the log file.", true, true, false),
                new MBeanAttributeInfo("MaxTime", "java.lang.Long",
                        "MaxTime: DebugSql automaticaly set for a request which take more than MaxTime to be executed.",
                        true, true, false),
                new MBeanAttributeInfo("MaxCount", "java.lang.Long",
                        "MaxCount: DebugSql automaticaly set for a request which return more than MaxCount elements.",
                        true, true, false),
                new MBeanAttributeInfo(
                        "Repository", "java.lang.String", "Repository: path for SQL scripts.", true, false, false) },
                new MBeanConstructorInfo[0],
                new MBeanOperationInfo[] {
                        new MBeanOperationInfo("clearCache", "clearCache(): clear the object cache", null, "void",
                                MBeanOperationInfo.ACTION),
                        new MBeanOperationInfo("reloadRepository",
                                "reloadRepository(): reload the scripts from the repository", null, "void",
                                MBeanOperationInfo.ACTION),
                        new MBeanOperationInfo("compactDataBase",
                                "compactDataBase(): shrinks HSQLDB files to the minimum size", null, "void",
                                MBeanOperationInfo.ACTION),
                        new MBeanOperationInfo("updateScriptContent",
                                "updateScriptContent(menu,group,name): refresh a cached SQL script",
                                new MBeanParameterInfo[] {
                                        new MBeanParameterInfo("menu", "java.lang.String", "the associated menu"),
                                        new MBeanParameterInfo("group", "java.lang.String", "the associated group"),
                                        new MBeanParameterInfo("name", "java.lang.String", "the associated name") },
                                "void", MBeanOperationInfo.ACTION),
                        new MBeanOperationInfo("execSelect", "execSelect(sql): execute the sql select request",
                                new MBeanParameterInfo[] {
                                        new MBeanParameterInfo("sql", "java.lang.String", "the sql select request") },
                                "java.lang.String", MBeanOperationInfo.ACTION),
                        new MBeanOperationInfo("execUpdate", "execUpdate(sql): execute the sql update request",
                                new MBeanParameterInfo[] {
                                        new MBeanParameterInfo("sql", "java.lang.String", "the sql update request") },
                                "java.lang.String", MBeanOperationInfo.ACTION) },
                new MBeanNotificationInfo[0]);
    }

    /**
     * Gets the time.
     *
     * @return the time
     *
     * @throws ParseException
     *             the parse exception
     */
    public static long getTime() throws ParseException {
        return getTime(new Date(System.currentTimeMillis()), new Time(System.currentTimeMillis()));
    }

    /**
     * Gets the time.
     *
     * @param date
     *            the date
     * @param time
     *            the time
     *
     * @return the time
     *
     * @throws ParseException
     *             the parse exception
     */
    public static long getTime(final Date date, final Time time) throws ParseException {
        return new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").parse(
                new SimpleDateFormat("yyyy.MM.dd").format(date) + " " + new SimpleDateFormat("HH:mm:ss").format(time))
                .getTime();
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
            if ("reloadRepository".equals(operationName) && signature.length == 0) {
                reloadRepository();
                return Boolean.TRUE;
            }
            if ("clearCache".equals(operationName) && signature.length == 0) {
                clearCache();
                return Boolean.TRUE;
            }
            if ("updateScriptContent".equals(operationName) && signature.length == 3
                    && "java.lang.String".equals(signature[0]) && "java.lang.String".equals(signature[1])
                    && "java.lang.String".equals(signature[2])) {
                if (dataFile != null) {
                    dataFile.updateScriptContent((String) params[0], (String) params[1], (String) params[2]);
                }
                return Boolean.TRUE;
            }
            if ("execSelect".equals(operationName) && signature.length == 1
                    && "java.lang.String".equals(signature[0])) {
                return Format.uncompress(select((String) params[0]));
            }
            if ("execUpdate".equals(operationName) && signature.length == 1
                    && "java.lang.String".equals(signature[0])) {
                return update((String) params[0]);
            }
        } catch (final Exception e) {
            _log.warn("Invoking the {} MBean method", operationName, e);
            throw new MBeanException(e);
        }
        throw new NoSuchMethodException(operationName);
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
        Activity activity = null;
        if (logEvents && ecuser != null) {
            try {
                // New activity entry.
                activity = new Activity();
                activity.setECUserName(ecuser.getName());
                activity.setECUser(ecuser);
                activity.setPlugin(plugin);
                activity.setHost(host);
                activity.setAgent(agent);
                insert(activity, true);
            } catch (final Throwable t) {
                _log.warn("Creating new Activity: {}", activity, t);
                activity = null;
            }
            if (activity != null) {
                newEvent(activity, action, comment, error);
            }
        } else {
            _log.debug("Don't record Activity for ECUser: {} (plugin={},comment={})", ecuser, plugin, comment);
        }
        return activity;
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
        Event event = null;
        if (logEvents && activity != null && activity.getId() != 0) {
            try {
                // New event entry.
                event = new Event();
                event.setActivityId(activity.getId());
                event.setActivity(activity);
                event.setDate(new Date(System.currentTimeMillis()));
                event.setTime(new Time(System.currentTimeMillis()));
                event.setAction(action);
                event.setComment(comment);
                event.setError(error);
                insert(event, true);
            } catch (final Throwable t) {
                _log.warn("Creating new Event: {}", event, t);
                event = null;
            }
        } else {
            _log.debug("Don't record Event for {} (comment={},error={}): {}", action, comment, error, activity);
        }
        return event;
    }

    /**
     * Checks if is null object.
     *
     * @param action
     *            the action
     * @param object
     *            the object
     *
     * @return true, if successful
     */
    private boolean isNullObject(final String action, final Object object) {
        if (debugSql) {
            _log.debug("{} {}: {}", action, Format.getClassName(object), object);
        }
        return object == null;
    }

    /**
     * _error.
     *
     * @param action
     *            the action
     * @param object
     *            the object
     * @param e
     *            the e
     */
    private static void error(final String action, final Object object, final Throwable e) {
        _log.error("{} {}: {}", action, Format.getClassName(object), object, e);
    }

    /**
     * Insert.
     *
     * @param object
     *            the object
     * @param createPk
     *            the create pk
     *
     * @throws DataBaseException
     *             the data base exception
     */
    public void insert(final DataBaseObject object, final boolean createPk) throws DataBaseException {
        insert(object, createPk, false);
    }

    /**
     * Insert.
     *
     * @param object
     *            the object
     * @param createPk
     *            the create pk
     * @param checkDuplicate
     *            the check duplicate
     *
     * @throws DataBaseException
     *             the data base exception
     */
    public void insert(final DataBaseObject object, final boolean createPk, final boolean checkDuplicate)
            throws DataBaseException {
        if (!isNullObject("Insert", object)) {
            try (var broker = brokerFactory.getBroker()) {
                if (checkDuplicate) {
                    Object result;
                    try {
                        result = get(object);
                    } catch (final Exception _) {
                        result = null;
                    }
                    if (result != null) {
                        final var ident = new StringBuilder();
                        for (final Object key : getPrimaryKeyValues(object)) {
                            ident.append(ident.length() == 0 ? "" : ",").append(key);
                        }
                        throw new DataBaseException(
                                Format.getClassName(object) + " already exists: {" + ident.toString() + "}");
                    }
                }
                broker.store(object, false);
            } catch (final Throwable e) {
                error("Insert", object, e);
                throw new DataBaseException(e.getMessage());
            }
        }
    }

    /**
     * Update a DataBase Object.
     *
     * @param object
     *            the object
     *
     * @throws DataBaseException
     *             the data base exception
     */
    public void update(final DataBaseObject object) throws DataBaseException {
        Throwable throwable = null;
        for (var i = 0; i < 5; i++) {
            // Was there an error already ?
            if (throwable != null) {
                final var message = throwable.getMessage();
                if (message == null || message.indexOf("try restarting transaction") == -1) {
                    // This is not an error that we should retry!
                    break;
                }
                // We should retry, so let's wait for 2 seconds before!
                try {
                    Thread.sleep(2000);
                } catch (final InterruptedException _) {
                }
            }
            if (!isNullObject("Update", object)) {
                try (var broker = brokerFactory.getBroker()) {
                    broker.store(object, true);
                    // The update was successful!
                    return;
                } catch (final Throwable t) {
                    error("Update", object, throwable = t);
                }
            }
        }
        // We couldn't update the database so we throw the last exception!
        throw new DataBaseException(Format.getMessage(throwable));
    }

    /**
     * Try to update a DataBase Object. In case of problem it will fail silently.
     *
     * @param object
     *            the object
     *
     * @return true, if successful
     */
    public boolean tryUpdate(final DataBaseObject object) {
        try {
            update(object);
            return true;
        } catch (final Throwable e) {
            _log.warn("{} NOT updated: {}", Format.getClassName(object), object, e);
            return false;
        }
    }

    /**
     * Try to insert a DataBase Object. In case of problem it will fail silently.
     *
     * @param object
     *            the object
     * @param createPk
     *            the create pk
     *
     * @return true, if successful
     */
    public boolean tryInsert(final DataBaseObject object, final boolean createPk) {
        try {
            insert(object, createPk);
            return true;
        } catch (final Throwable e) {
            _log.warn("{} NOT inserted: {}", Format.getClassName(object), object, e);
            return false;
        }
    }

    /**
     * Removes the.
     *
     * @param object
     *            the object
     *
     * @throws DataBaseException
     *             the data base exception
     */
    public void remove(final DataBaseObject object) throws DataBaseException {
        remove(new DataBaseObject[] { object });
    }

    /**
     * Removes the.
     *
     * @param objects
     *            the objects
     *
     * @throws DataBaseException
     *             the data base exception
     */
    public void remove(final DataBaseObject[] objects) throws DataBaseException {
        DataBaseObject object = null;
        try (var broker = brokerFactory.getBroker()) {
            for (final DataBaseObject dataBaseObject : objects) {
                if (isNullObject("Remove", object = dataBaseObject)) {
                    continue;
                }
                broker.delete(object);
            }
        } catch (final Throwable e) {
            error("Remove", object, e);
            throw new DataBaseException(e.getMessage());
        }
    }

    /**
     * Clear cache.
     */
    public void clearCache() {
        try (var broker = brokerFactory.getBroker()) {
            broker.clearCache();
        } catch (final BrokerException e) {
            _log.warn("Broker not available", e);
        }
    }

    /**
     * Clear cache for the specified entities and primary keys.
     *
     * @param <T>
     *            the generic type
     * @param clazz
     *            the clazz
     * @param primaryKeys
     *            the primary keys
     */
    public <T extends DataBaseObject> void clearCache(final Class<T> clazz, final List<Object> primaryKeys) {
        try (var broker = brokerFactory.getBroker()) {
            broker.clearCache(clazz, primaryKeys);
        } catch (final BrokerException e) {
            _log.warn("Broker not available", e);
        }
    }

    /**
     * Clear cache for the specified entities and primary keys.
     *
     * @param tableName
     *            the table name
     * @param columnName
     *            the column name
     * @param primaryKeys
     *            the primary keys
     */
    public void clearCache(final String tableName, final String columnName, final List<Object> primaryKeys) {
        try (var broker = brokerFactory.getBroker()) {
            broker.clearCache(tableName, columnName, primaryKeys);
        } catch (final BrokerException e) {
            _log.warn("Broker not available", e);
        }
    }

    /**
     * Gets the primary key values.
     *
     * @param object
     *            the object
     *
     * @return the primary key values
     *
     * @throws DataBaseException
     *             the data base exception
     */
    public Object[] getPrimaryKeyValues(final DataBaseObject object) throws DataBaseException {
        final var thread = Thread.currentThread();
        final var loader = thread.getContextClassLoader();
        thread.setContextClassLoader(object.getClass().getClassLoader());
        try (var broker = brokerFactory.getBroker()) {
            return broker.getPrimaryKeyValues(object);
        } catch (final Throwable e) {
            error("Remove", object, e);
            throw new DataBaseException(e.getMessage());
        } finally {
            thread.setContextClassLoader(loader);
        }
    }

    /**
     * Gets the script file impl.
     *
     * @param repository
     *            the repository
     *
     * @return the script file impl
     */
    public ScriptFileImpl getScriptFileImpl(final String repository) {
        return new ScriptFileImpl(repository);
    }

    /**
     * The Class ScriptFileImpl.
     */
    public final class ScriptFileImpl extends ScriptFile {
        /** The scriptsRepository. */
        private final String scriptsRepository;

        /**
         * Instantiates a new script file impl.
         *
         * @param repository
         *            the repository
         */
        ScriptFileImpl(final String repository) {
            loadScripts(new SQLFilter(), scriptsRepository = repository);
        }

        /**
         * Execute query.
         *
         * @param sql
         *            the sql
         *
         * @return the DB result set
         *
         * @throws SQLException
         *             the SQL exception
         */
        @Override
        public DBResultSet executeQuery(final String sql) throws SQLException {
            return executeSelect(sql);
        }

        /**
         * Execute query.
         *
         * @param <T>
         *            the generic type
         * @param resultClass
         *            the result class
         * @param originalSql
         *            the original sql
         *
         * @return the DB iterator
         *
         * @throws SQLException
         *             the SQL exception
         */
        @Override
        public <T extends DataBaseObject> DBIterator<T> executeQuery(final Class<T> resultClass, final String sql)
                throws SQLException {
            if (debugSql) {
                _log.debug("executeQuery: {}", sql);
            }
            try (var broker = brokerFactory.getBroker()) {
                final var start = System.currentTimeMillis();
                final var result = new DBIterator<>(broker, resultClass, sql);
                logSqlRequest(sql, start, System.currentTimeMillis(), -1);
                return result;
            } catch (final Exception e) {
                error("executeQuery", sql, e);
                throw new SQLException("Database not available");
            }
        }

        /**
         * Execute update.
         *
         * @param sql
         *            the sql
         *
         * @return the int
         *
         * @throws SQLException
         *             the SQL exception
         */
        @Override
        public int executeUpdate(final String sql) throws SQLException {
            final var tokenizer = new StringTokenizer(sql, ";");
            var rowsAffected = 0;
            while (tokenizer.hasMoreElements()) {
                final var subquery = tokenizer.nextToken().trim();
                if (!subquery.isEmpty()) {
                    rowsAffected += executeCustomUpdate(subquery);
                }
            }
            return rowsAffected;
        }

        /**
         * Gets the repository.
         *
         * @return the repository
         */
        public String getRepository() {
            return scriptsRepository;
        }
    }
}
