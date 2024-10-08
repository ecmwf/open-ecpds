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

package ecmwf.common.ectrans.module;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * The Class PortalModule. Do nothing, only there to allow triggering the
 * notification from the ECtransPut class on schedule time.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.ectrans.ECtransSetup;
import ecmwf.common.ectrans.TransferModule;
import ecmwf.common.technical.StreamPlugThread;

/**
 * The Class PortalModule.
 */
public final class PortalModule extends TransferModule {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(PortalModule.class);

    /** The size. */
    private long size = -1;

    /** The currentStatus. */
    private String currentStatus = "INIT";

    /** The closed. */
    private final AtomicBoolean closed = new AtomicBoolean(false);

    /**
     * {@inheritDoc}
     *
     * Gets the status.
     */
    @Override
    public String getStatus() {
        return currentStatus;
    }

    /**
     * {@inheritDoc}
     *
     * Connect.
     */
    @Override
    public void connect(final String location, final ECtransSetup setup) throws IOException {
        // The file is not sent anywhere, just made available!
        setAvailable(true);
        setStatus("CONNECT");
    }

    /**
     * {@inheritDoc}
     *
     * Del.
     */
    @Override
    public void del(final String name) throws IOException {
        throw new IOException("DEL method not implemented");
    }

    /**
     * {@inheritDoc}
     *
     * Put.
     */
    @Override
    public OutputStream put(final String name, final long posn, final long size) throws IOException {
        throw new IOException("PUT method not implemented");
    }

    /**
     * {@inheritDoc}
     *
     * Put.
     */
    @Override
    public boolean put(final InputStream in, final String name, final long posn, final long size) throws IOException {
        _log.debug("Put file {} ({})", name, posn);
        setStatus("PUT");
        if (posn > 0) {
            throw new IOException("Resume not supported (posn>0)");
        }
        // Let's consume the data to make sure the file is available for the data
        // portal!
        this.size = StreamPlugThread.consume(in, StreamPlugThread.DEFAULT_BUFF_SIZE);
        _log.debug("Consummed {} bytes", size);
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the.
     */
    @Override
    public InputStream get(final String name, final long posn) throws IOException {
        throw new IOException("GET method not implemented");
    }

    /**
     * {@inheritDoc}
     *
     * Size.
     */
    @Override
    public long size(final String name) throws IOException {
        setStatus("SIZE");
        if (size == -1) {
            throw new FileNotFoundException(name);
        }
        // This will make the ECtransPut class happy!
        return size;
    }

    /**
     * Sets the status.
     *
     * @param status
     *            the status
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void setStatus(final String status) throws IOException {
        _log.debug("Status set to: {}", status);
        if (closed.get()) {
            throw new IOException("Module closed");
        }
        currentStatus = status;
    }

    /**
     * {@inheritDoc}
     *
     * Close.
     */
    @Override
    public void close() throws IOException {
        if (closed.compareAndSet(false, true)) {
            _log.debug("Close connection");
            currentStatus = "CLOSE";
            _log.debug("Close completed");
        } else {
            _log.debug("Already closed");
        }
    }
}
