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

import java.sql.SQLException;

import org.hibernate.Session;
import org.hibernate.internal.SessionImpl;

import com.zaxxer.hikari.pool.ProxyConnection;

/**
 * Wrapper utility to abstract and handle underlying JDBC connections from Hibernate sessions, supporting both MariaDB
 * and MySQL drivers.
 */
class GenericJdbcConnectionWrapper {

    /**
     * Generic interface representing JDBC connections with basic common operations needed (readOnly, autoCommit, host
     * info).
     */
    public interface GenericJdbcConnection {
        /**
         * Sets the connection's read-only mode.
         *
         * @param readOnly
         *            true to set read-only mode, false otherwise
         *
         * @throws SQLException
         *             if a database access error occurs
         */
        void setReadOnly(boolean readOnly) throws SQLException;

        /**
         * Sets the connection's auto-commit mode.
         *
         * @param autoCommit
         *            true to enable auto-commit, false otherwise
         *
         * @throws SQLException
         *             if a database access error occurs
         */
        void setAutoCommit(boolean autoCommit) throws SQLException;

        /**
         * Returns a string representing host information of the connection.
         *
         * @return host info string for logging or tracking
         */
        String getHostInfoString();
    }

    /**
     * Adapter wrapping a MariaDB JDBC connection implementing the generic interface.
     */
    public static class MariaDbConnectionAdapter implements GenericJdbcConnection {

        /** The conn. */
        private final org.mariadb.jdbc.Connection conn;

        /**
         * Constructs a new MariaDbConnectionAdapter wrapping the given MariaDB connection.
         *
         * @param conn
         *            underlying MariaDB connection to wrap
         */
        public MariaDbConnectionAdapter(final org.mariadb.jdbc.Connection conn) {
            this.conn = conn;
        }

        /**
         * Sets the read only.
         *
         * @param readOnly
         *            the new read only
         *
         * @throws SQLException
         *             the SQL exception
         */
        @Override
        public void setReadOnly(final boolean readOnly) throws SQLException {
            conn.setReadOnly(readOnly);
        }

        /**
         * Sets the auto commit.
         *
         * @param autoCommit
         *            the new auto commit
         *
         * @throws SQLException
         *             the SQL exception
         */
        @Override
        public void setAutoCommit(final boolean autoCommit) throws SQLException {
            conn.setAutoCommit(autoCommit);
        }

        /**
         * Gets the host info string.
         *
         * @return the host info string
         */
        @Override
        public String getHostInfoString() {
            return conn.__test_host(); // MariaDB-specific method to get host info
        }
    }

    /**
     * Adapter wrapping a MySQL JDBC connection implementing the generic interface.
     */
    public static class MysqlConnectionAdapter implements GenericJdbcConnection {

        /** The conn. */
        private final com.mysql.cj.jdbc.ConnectionImpl conn;

        /**
         * Constructs a new MysqlConnectionAdapter wrapping the given MySQL connection.
         *
         * @param conn
         *            underlying MySQL connection to wrap
         */
        public MysqlConnectionAdapter(final com.mysql.cj.jdbc.ConnectionImpl conn) {
            this.conn = conn;
        }

        /**
         * Sets the read only.
         *
         * @param readOnly
         *            the new read only
         *
         * @throws SQLException
         *             the SQL exception
         */
        @Override
        public void setReadOnly(final boolean readOnly) throws SQLException {
            conn.setReadOnly(readOnly);
        }

        /**
         * Sets the auto commit.
         *
         * @param autoCommit
         *            the new auto commit
         *
         * @throws SQLException
         *             the SQL exception
         */
        @Override
        public void setAutoCommit(final boolean autoCommit) throws SQLException {
            conn.setAutoCommit(autoCommit);
        }

        /**
         * Gets the host info string.
         *
         * @return the host info string
         */
        @Override
        public String getHostInfoString() {
            // MySQL driver way to get host info
            return conn.getSession().getHostInfo().toString();
        }
    }

    /**
     * Generic method to retrieve the underlying physical connection of the given type from the provided Hibernate
     * session.
     *
     * @param <T>
     *            the type of the connection class expected
     * @param session
     *            Hibernate session
     * @param connectionClass
     *            the Class object of the connection type to unwrap
     *
     * @return unwrapped connection instance if found, otherwise null
     */
    public static <T> T getUnderlyingConnection(final Session session, final Class<T> connectionClass) {
        try {
            if (session instanceof final SessionImpl hibernateSession) {
                final var physicalConn = hibernateSession.getJdbcCoordinator().getLogicalConnection()
                        .getPhysicalConnection();

                if (physicalConn instanceof final ProxyConnection proxy && proxy.isWrapperFor(connectionClass)) {
                    return proxy.unwrap(connectionClass);
                }
            }
        } catch (final SQLException _) {
            // Ignored
        }
        return null;
    }

    /**
     * Detects the underlying connection type for the given Hibernate session, returning a GenericJdbcConnection wrapper
     * for MariaDB or MySQL connections.
     *
     * @param session
     *            Hibernate session
     *
     * @return a GenericJdbcConnection wrapper if connection detected; null otherwise
     */
    public static GenericJdbcConnection wrap(final Session session) {
        final var mariadbConn = getUnderlyingConnection(session, org.mariadb.jdbc.Connection.class);
        if (mariadbConn != null) {
            return new MariaDbConnectionAdapter(mariadbConn);
        }
        final var mysqlConn = getUnderlyingConnection(session, com.mysql.cj.jdbc.ConnectionImpl.class);
        if (mysqlConn != null) {
            return new MysqlConnectionAdapter(mysqlConn);
        }
        return null;
    }
}
