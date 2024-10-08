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

package ecmwf.common.callback;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.IOException;
import java.io.OutputStream;

/**
 * The Class LocalOutputStream.
 */
public final class LocalOutputStream extends OutputStream {
    /** The _out. */
    private final RemoteOutputStream _out;

    /**
     * Instantiates a new local output stream.
     *
     * @param out
     *            the out
     */
    public LocalOutputStream(final RemoteOutputStream out) {
        _out = out;
    }

    /**
     * {@inheritDoc}
     *
     * Close.
     */
    @Override
    public synchronized void close() throws IOException {
        _out.close();
    }

    /**
     * {@inheritDoc}
     *
     * Flush.
     */
    @Override
    public void flush() throws IOException {
        _out.flush();
    }

    /**
     * {@inheritDoc}
     *
     * Write.
     */
    @Override
    public void write(final byte[] b) throws IOException {
        _out.write(b);
    }

    /**
     * {@inheritDoc}
     *
     * Write.
     */
    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        _out.write(b, off, len);
    }

    /**
     * {@inheritDoc}
     *
     * Write.
     */
    @Override
    public void write(final int b) throws IOException {
        _out.write(b);
    }
}
