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
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import static ecmwf.common.ectrans.ECtransOptions.HOST_TEST_BYTES_PER_SEC;
import static ecmwf.common.ectrans.ECtransOptions.HOST_TEST_DELAY;
import static ecmwf.common.ectrans.ECtransOptions.HOST_TEST_ERRORS_FREQUENCY;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.ectrans.ECtransSetup;
import ecmwf.common.ectrans.TransferModule;
import ecmwf.common.technical.MonitoredOutputStream;
import ecmwf.common.technical.NullOutputStream;
import ecmwf.common.text.Format;

/**
 * The Class TestModule.
 */
public final class TestModule extends TransferModule {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(TestModule.class);

    /** The Constant errorsFrequency. */
    private static final Object errorsSync = new Object();

    /** The errors. */
    private static int errors = 0;

    /** The _setup. */
    private ECtransSetup currentSetup = null;

    /** The out. */
    private MonitoredOutputStream testOutput = null;

    /** The status. */
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
        this.currentSetup = setup;
        delay("connect");
        _log.warn("Fake connect");
        setStatus("CONNECT");
        setAttribute("remote.hostName", InetAddress.getLocalHost().getHostName());
    }

    /**
     * {@inheritDoc}
     *
     * Del.
     */
    @Override
    public void del(final String name) throws IOException {
        delay("del");
        _log.warn("Fake del of: {}", name);
        setStatus("DEL");
    }

    /**
     * {@inheritDoc}
     *
     * Put.
     */
    @Override
    public OutputStream put(final String name, final long posn, final long size) throws IOException {
        delay("put");
        _log.warn("Fake put of: {} (posn={})", name, posn);
        setStatus("PUT");
        setAttribute("remote.fileName", name);
        final var bytesPerSecond = getBytesPerSeconds();
        _log.warn("Faking write (file not sent), size: {}, rate: {}/s", () -> Format.formatSize(size),
                () -> Format.formatSize(bytesPerSecond));
        testOutput = new MonitoredOutputStream(new NullOutputStream(bytesPerSecond));
        return testOutput;
    }

    /**
     * {@inheritDoc}
     *
     * Copy.
     */
    @Override
    public void copy(final String source, final String target, final long posn, final long size) throws IOException {
        delay("copy");
        _log.warn("Fake copy of: {} -> {} (posn={})", source, target, posn);
        setStatus("COPY");
        setAttribute("remote.fileName", target);
        final var copyDurationInMillis = size / getBytesPerSeconds() * 1000L;
        _log.warn("Faking copy (file not copied), size: {}, wait: {}/s", () -> Format.formatSize(size),
                () -> Format.formatDuration(copyDurationInMillis));
        try {
            Thread.sleep(copyDurationInMillis);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
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
        delay("size");
        _log.warn("Fake size of: {}", name);
        setStatus("SIZE");
        if (testOutput == null) {
            throw new FileNotFoundException(name);
        }
        return testOutput.getByteSent();
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
        final int errorsFrequency = getSetup().getInteger(HOST_TEST_ERRORS_FREQUENCY);
        this.currentStatus = status;
        if (errorsFrequency > 0) {
            synchronized (errorsSync) {
                if (errors++ > errorsFrequency) {
                    errors = 0;
                    throw new IOException(
                            "Simulated error on " + status + " (1 error every " + errorsFrequency + " request(s))");
                }
            }
        }
    }

    /**
     * Gets the setup. Utility call to get the ECtransSetup and check if the module is not closed!
     *
     * @return the setup
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private ECtransSetup getSetup() throws IOException {
        if (closed.get()) {
            throw new IOException("Module closed");
        }
        return currentSetup;
    }

    /**
     * {@inheritDoc}
     *
     * Close.
     */
    @Override
    public void close() throws IOException {
        delay("close");
        if (closed.compareAndSet(false, true)) {
            _log.debug("Close connection");
            currentStatus = "CLOSE";
            if (testOutput != null) {
                testOutput.close();
            }
            _log.debug("Close completed");
        } else {
            _log.debug("Already closed");
        }
    }

    /**
     * Gets the bytes per seconds.
     *
     * @return the bytes per seconds
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private long getBytesPerSeconds() throws IOException {
        return getSetup().getOptionalByteSize(HOST_TEST_BYTES_PER_SEC)
                .map(byteSize -> byteSize.isZero() ? HOST_TEST_BYTES_PER_SEC.getDefaultByteSize() : byteSize)
                .orElse(HOST_TEST_BYTES_PER_SEC.getDefaultByteSize()).size();

    }

    /**
     * Delay.
     *
     * @param operation
     *            the operation
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void delay(final String operation) throws IOException {
        final var delayInMillis = getSetup().getDuration(HOST_TEST_DELAY).toMillis();
        _log.info("Waiting for {} to {}", () -> Format.formatDuration(delayInMillis), () -> operation);
        try {
            Thread.sleep(delayInMillis);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
