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

package ecmwf.ecpds.master;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.Closeable;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.technical.StreamPlugThread;
import ecmwf.common.text.Format;

/**
 * The Class DownloadProgress.
 */
public final class DownloadProgress implements Serializable, Closeable {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The Constant _log. */
    private static final transient Logger _log = LogManager.getLogger(DownloadProgress.class);

    /** The _closed. */
    private final AtomicBoolean _closed = new AtomicBoolean(false);

    /** The _caller. */
    private final transient Closeable _caller;

    /** The _data file id. */
    private final String _root;

    /** The _data file id. */
    private final long _dataFileId;

    /** The _progress. */
    private final long _size;

    /** The _progress. */
    private long _duration;

    /** The _progress. */
    private long _byteSent;

    /**
     * Instantiates a new download progress.
     *
     * @param root
     *            the root
     * @param dataFileId
     *            the data file id
     * @param size
     *            the size
     * @param duration
     *            the duration
     * @param byteSent
     *            the byte sent
     * @param caller
     *            something to close
     */
    public DownloadProgress(final String root, final long dataFileId, final long size, final long duration,
            final long byteSent, final Closeable caller) {
        _root = root;
        _dataFileId = dataFileId;
        _size = size;
        _duration = duration;
        _byteSent = byteSent;
        _caller = caller;
    }

    /**
     * Gets the root.
     *
     * @return the root
     */
    public String getRoot() {
        return _root;
    }

    /**
     * Gets the data file.
     *
     * @return the data file
     */
    public long getDataFileId() {
        return _dataFileId;
    }

    /**
     * Gets the size.
     *
     * @return the size
     */
    public long getSize() {
        return _size;
    }

    /**
     * Sets the duration.
     *
     * @param duration
     *            the duration
     */
    public void setDuration(final long duration) {
        _duration = duration;
    }

    /**
     * Gets the duration.
     *
     * @return the duration
     */
    public long getDuration() {
        return _duration;
    }

    /**
     * Sets the byteSent.
     *
     * @param byteSent
     *            the byte sent
     */
    public void setByteSent(final long byteSent) {
        _byteSent = byteSent;
    }

    /**
     * Gets the byte sent.
     *
     * @return the byte sent
     */
    public long getByteSent() {
        return _byteSent;
    }

    /**
     * Check if it is closed.
     *
     * @return closed?
     */
    public boolean isClosed() {
        return _closed.get();
    }

    /**
     * {@inheritDoc}
     *
     * Close.
     */
    @Override
    public void close() {
        if (_closed.compareAndSet(false, true)) {
            StreamPlugThread.closeQuietly(_caller);
        } else {
            _log.debug("Already closed");
        }
    }

    /**
     * {@inheritDoc}
     *
     * To string.
     */
    @Override
    public String toString() {
        return "DownloadProgress [closed=" + isClosed() + ", root=" + _root + ", dataFileId=" + _dataFileId + ", size="
                + Format.formatSize(_size) + ", duration=" + Format.formatDuration(_duration) + ", sent="
                + Format.formatSize(_byteSent) + ", caller=" + Format.getClassName(_caller) + "]";
    }
}
