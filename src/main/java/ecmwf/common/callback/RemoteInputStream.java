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
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The Interface RemoteInputStream.
 */
public interface RemoteInputStream extends Remote {
    /**
     * Available.
     *
     * @return the int
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    int available() throws IOException, RemoteException;

    /**
     * Close.
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    void close() throws IOException, RemoteException;

    /**
     * Mark.
     *
     * @param readlimit
     *            the readlimit
     *
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    void mark(int readlimit) throws RemoteException;

    /**
     * Mark supported.
     *
     * @return true, if successful
     *
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    boolean markSupported() throws RemoteException;

    /**
     * Read.
     *
     * @return the int
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    int read() throws IOException, RemoteException;

    /**
     * Read.
     *
     * @param len
     *            the len
     *
     * @return the byte stream
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    ByteStream read(int len) throws IOException, RemoteException;

    /**
     * Reset.
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    void reset() throws IOException, RemoteException;

    /**
     * Skip.
     *
     * @param n
     *            the n
     *
     * @return the long
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    long skip(long n) throws IOException, RemoteException;
}
