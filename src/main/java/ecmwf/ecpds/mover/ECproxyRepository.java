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

package ecmwf.ecpds.mover;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.management.timer.Timer;

import ecmwf.common.database.ECUser;
import ecmwf.common.database.ECtransModule;
import ecmwf.common.database.Host;
import ecmwf.common.database.TransferMethod;

/**
 * The Class ECproxyRepository.
 */
final class ECproxyRepository implements GenericRepository, ECpdsProxy {
    /** The _host. */
    private final Host _host;

    /** The _in. */
    private InputStream _in = null;

    /** The _out. */
    private OutputStream _out = null;

    /**
     * Instantiates a new ecproxy repository.
     *
     * @param host
     *            the host
     */
    ECproxyRepository(final Host host) {
        _host = host;
    }

    /**
     * Gets the host.
     *
     * @param name
     *            the name
     *
     * @return the host
     */
    @Override
    public Host getHost(final String name) {
        return _host;
    }

    /**
     * Gets the EC user.
     *
     * @param name
     *            the name
     *
     * @return the EC user
     */
    @Override
    public ECUser getECUser(final String name) {
        return _host.getECUser();
    }

    /**
     * Gets the ectrans module.
     *
     * @param name
     *            the name
     *
     * @return the ectrans module
     */
    @Override
    public ECtransModule getECtransModule(final String name) {
        return _host.getTransferMethod().getECtransModule();
    }

    /**
     * Gets the transfer method.
     *
     * @param name
     *            the name
     *
     * @return the transfer method
     */
    @Override
    public TransferMethod getTransferMethod(final String name) {
        return _host.getTransferMethod();
    }

    /**
     * Gets the data input stream.
     *
     * @param ticket
     *            the ticket
     *
     * @return the data input stream
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public synchronized InputStream getDataInputStream(final Object ticket) throws IOException {
        if (_in == null) {
            try {
                wait(1 * Timer.ONE_MINUTE);
            } catch (final InterruptedException e) {
                MoverServer._log.debug("Wakeup");
            }
        }
        if (_in == null) {
            throw new IOException("InputStream not set");
        }
        return _in;
    }

    /**
     * Gets the data output stream.
     *
     * @param ticket
     *            the ticket
     *
     * @return the data output stream
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public synchronized OutputStream getDataOutputStream(final Object ticket) throws IOException {
        if (_out == null) {
            try {
                wait(1 * Timer.ONE_MINUTE);
            } catch (final InterruptedException e) {
                MoverServer._log.debug("Wakeup");
            }
        }
        if (_out == null) {
            throw new IOException("OutputStream not set");
        }
        return _out;
    }

    /**
     * Gets the data input file.
     *
     * @param ticket
     *            the ticket
     *
     * @return the data input file
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public File getDataInputFile(final Object ticket) throws IOException {
        throw new IOException("Not supported with ECproxy");
    }

    /**
     * Gets the data output file.
     *
     * @param ticket
     *            the ticket
     *
     * @return the data output file
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public File getDataOutputFile(final Object ticket) throws IOException {
        throw new IOException("Not supported with ECproxy");
    }

    /**
     * Gets the original filename.
     *
     * @param ticket
     *            the ticket
     *
     * @return the original filename
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public String getOriginalFilename(final Object ticket) throws IOException {
        throw new IOException("Not supported with ECproxy");
    }

    /**
     * Sets the output stream.
     *
     * @param out
     *            the new output stream
     */
    @Override
    public synchronized void setOutputStream(final OutputStream out) {
        _out = out;
        notifyAll();
    }

    /**
     * Sets the input stream.
     *
     * @param in
     *            the new input stream
     */
    @Override
    public synchronized void setInputStream(final InputStream in) {
        _in = in;
        notifyAll();
    }
}
