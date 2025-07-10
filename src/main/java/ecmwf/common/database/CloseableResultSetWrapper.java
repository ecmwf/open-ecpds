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
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicBoolean;

import ecmwf.common.technical.ResourceTracker;

/**
 * The Class CloseableResultSetWrapper.
 */
public class CloseableResultSetWrapper implements AutoCloseable {

    /** The Constant TRACKER. */
    private static final ResourceTracker TRACKER = new ResourceTracker(CloseableResultSetWrapper.class);

    /** The result set. */
    private final ResultSet resultSet;

    /** The statement. */
    private final Statement statement;

    /** The closed flag. */
    private final AtomicBoolean closed = new AtomicBoolean(false);

    /**
     * Instantiates a new closeable result set wrapper.
     *
     * @param statement
     *            the statement
     * @param resultSet
     *            the result set
     */
    public CloseableResultSetWrapper(final Statement statement, final ResultSet resultSet) {
        this.resultSet = resultSet;
        this.statement = statement;
        TRACKER.onOpen();
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
     * Close.
     *
     * @throws SQLException
     *             the SQL exception
     */
    @Override
    public void close() throws SQLException {
        if (closed.compareAndSet(false, true)) {
            SQLException firstException = null;
            boolean closedSuccessfully = false;
            try {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    firstException = e;
                }
                try {
                    statement.close();
                } catch (SQLException e) {
                    if (firstException != null) {
                        firstException.addSuppressed(e);
                    } else {
                        firstException = e;
                    }
                }
                if (firstException == null) {
                    closedSuccessfully = true;
                }
            } finally {
                TRACKER.onClose(closedSuccessfully);
            }
            if (firstException != null) {
                throw firstException;
            }
        }
    }
}
