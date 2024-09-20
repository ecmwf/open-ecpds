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

package ecmwf.common.security;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * Decorator object for OutputStream that invokes the listener's #ioStarting and
 * #ioEnding methods when entering and exiting IO blocks. The #flush and #write
 * methods in this class are synchronized.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.IOException;
import java.io.OutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Class SslSocketOutputStream.
 */
class SslSocketOutputStream extends OutputStream {

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(SslSocketOutputStream.class);

    /** The start. */
    private boolean start = true;

    /** The header. */
    private byte[] header = null;

    /** The decoratee. */
    private final OutputStream decoratee;

    /** The listener. */
    private final SslSocket listener;

    /**
     * Create a new InterruptibleRMISocketOutputStream instance that decorates the supplied OutputStream and calls back
     * to the supplied listener.
     *
     * @param listener
     *            the listener to callback to
     * @param decoratee
     *            the OutputStream to decorate
     */
    SslSocketOutputStream(final SslSocket listener, final OutputStream decoratee) {
        this.decoratee = decoratee;
        this.listener = listener;
    }

    /**
     * Write.
     *
     * @param b
     *            the b
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     *
     * @see OutputStream#write(int)
     */
    @Override
    public synchronized void write(final int b) throws IOException {
        write(new byte[] { (byte) b });
    }

    /**
     * Close the underlying OutputStream. Unlike the #flush and #write methods, this method is not synchronized, and it
     * does not call back to the listener.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     *
     * @see OutputStream#close()
     */
    @Override
    public void close() throws IOException {
        this.decoratee.close();
    }

    /**
     * Return true if the supplied object is also an instance of InterruptibleRMISocketOutputStream, and if the delegate
     * and listener members are equal for both objects. Generally speaking IO objects do not override #equals, so this
     * method will tend to return false unless the decorated OutputStream class has overridden #equals.
     *
     * @param obj
     *            the obj
     *
     * @return true, if successful
     */
    @Override
    public boolean equals(final Object obj) {
        return obj instanceof SslSocketOutputStream && this.decoratee.equals(((SslSocketOutputStream) obj).decoratee)
                && this.listener.equals(((SslSocketOutputStream) obj).listener);
    }

    /**
     * Flush.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     *
     * @see OutputStream#flush()
     */
    @Override
    public synchronized void flush() throws IOException {
        // TODO
        this.decoratee.flush();
    }

    /**
     * Return decoratee#hashCode() ^ listener#hashCode().
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        return this.decoratee.hashCode() ^ this.listener.hashCode();
    }

    /**
     * Return this.getClass().getName() + [decoratee.toString()]
     *
     * @return the string
     */
    @Override
    public String toString() {
        return this.getClass().getName() + " [" + this.decoratee.toString() + "]";
    }

    /**
     * Write.
     *
     * @param b
     *            the b
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     *
     * @see OutputStream#write(byte[])
     */
    @Override
    public synchronized void write(final byte[] b) throws IOException {
        write(b, 0, 1);
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
     *
     * @see OutputStream#write(byte[], int, int)
     */
    @Override
    public synchronized void write(final byte[] b, final int off, final int len) throws IOException {
        if (start) {
            start = false;
            final var s = new String(b, off, len);
            if (s.indexOf("Agent-Name: ectools") != -1) {
                _log.debug("Header writing delayed for eccert");
                header = new byte[len];
                System.arraycopy(b, off, header, 0, len);
                return;
            }
        }
        if (header == null) {
            this.decoratee.write(b, off, len);
        } else {
            final var packet = new byte[header.length + len];
            System.arraycopy(header, 0, packet, 0, header.length);
            System.arraycopy(b, off, packet, header.length, len);
            header = null;
            this.decoratee.write(packet, 0, packet.length);
        }
    }
}
