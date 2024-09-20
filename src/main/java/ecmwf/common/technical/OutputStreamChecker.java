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

package ecmwf.common.technical;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * The Class OutputStreamChecker.
 */
public abstract class OutputStreamChecker extends FilterOutputStream {

    /**
     * Catch exception.
     *
     * @param service
     *            the service
     * @param t
     *            the t
     */
    public abstract void catchException(String service, Throwable t);

    /**
     * Instantiates a new output stream checker.
     *
     * @param out
     *            the out
     */
    public OutputStreamChecker(final OutputStream out) {
        super(out);
    }

    /**
     * Write.
     *
     * @param b
     *            the b
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void write(final int b) throws IOException {
        try {
            out.write(b);
        } catch (final IOException e) {
            catchException("OutputStream.write", e);
            throw e;
        }
    }

    /**
     * Write.
     *
     * @param b
     *            the b
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void write(final byte b[]) throws IOException {
        try {
            out.write(b);
        } catch (final IOException e) {
            catchException("OutputStream.write", e);
            throw e;
        }
    }

    /**
     * Write.
     *
     * @param b
     *            the b
     * @param off
     *            the off
     * @param len
     *            the len
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void write(final byte b[], final int off, final int len) throws IOException {
        try {
            out.write(b, off, len);
        } catch (final IOException e) {
            catchException("OutputStream.write", e);
            throw e;
        }

    }

    /**
     * Flush.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void flush() throws IOException {
        try {
            out.flush();
        } catch (final IOException e) {
            catchException("OutputStream.flush", e);
            throw e;
        }
    }

    /**
     * Close.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void close() throws IOException {
        try {
            out.close();
        } catch (final IOException e) {
            catchException("OutputStream.close", e);
            throw e;
        }
    }
}
