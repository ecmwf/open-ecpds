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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.technical.ResourceTracker;
import ecmwf.common.text.Format;

/**
 * The Class CloseableResultSetWrapper.
 */
public class CloseableResultSetWrapper implements AutoCloseable {

	/** The Constant _log. */
	private static final Logger _log = LogManager.getLogger(CloseableResultSetWrapper.class);

	/** The Constant TRACKER. */
	private static final ResourceTracker TRACKER = new ResourceTracker(CloseableResultSetWrapper.class);

	/** The result set. */
	private final ResultSet resultSet;

	/** The statement. */
	private final Statement statement;

	/** The start time. */
	private final long startTime = System.currentTimeMillis();

	/** The closed flag. */
	private final AtomicBoolean closed = new AtomicBoolean(false);

	/**
	 * Instantiates a new closeable result set wrapper.
	 *
	 * @param resultSet the result set
	 * @param statement the statement
	 * @throws SQLException
	 */
	public CloseableResultSetWrapper(final Statement statement, final String sql) throws SQLException {
		this.resultSet = statement.executeQuery(sql);
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
	 * @throws SQLException the SQL exception
	 */
	@Override
	public void close() throws SQLException {
		if (closed.compareAndSet(false, true)) {
			// We can now release the resources!
			try {
				resultSet.close();
			} finally {
				try {
					statement.close(); // ensure both are closed
				} finally {
					TRACKER.onClose();
					final var elapsed = System.currentTimeMillis() - startTime;
					if (_log.isDebugEnabled() && TRACKER.getClosedCount() % 100 == 0) {
						_log.debug("Close after {}: {}", Format.formatDuration(elapsed), TRACKER);
					}
				}
			}
		}
	}
}
