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

import ecmwf.common.database.ECUser;
import ecmwf.common.database.ECtransModule;
import ecmwf.common.database.Host;
import ecmwf.common.database.TransferMethod;

/**
 * The Class StreamRepository. Allow bypassing the ECproxy when loading files from the current data-mover.
 */
final class StreamRepository implements GenericRepository {
    /** The _host. */
    private final Host _host;

    /** The _in. */
    private final InputStream _in;

    /** The _out. */
    private final OutputStream _out;

    /**
     * Instantiates a new stream repository.
     *
     * @param host
     *            the host
     * @param out
     *            the output stream
     */
    StreamRepository(final Host host, final OutputStream out) {
        _host = host;
        _out = out;
        _in = null;
    }

    /**
     * Instantiates a new stream repository.
     *
     * @param host
     *            the host
     * @param in
     *            the input stream
     */
    StreamRepository(final Host host, final InputStream in) {
        _host = host;
        _out = null;
        _in = in;
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
    public InputStream getDataInputStream(final Object ticket) throws IOException {
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
    public OutputStream getDataOutputStream(final Object ticket) throws IOException {
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
        throw new IOException("Not supported with StreamRepository");
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
        throw new IOException("Not supported with StreamRepository");
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
        throw new IOException("Not supported with StreamRepository");
    }
}
