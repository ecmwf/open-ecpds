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
 * The Interface RemoteOutputStream.
 */
public interface RemoteOutputStream extends Remote {
    /**
     * Close.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws RemoteException
     *             the remote exception
     */
    void close() throws IOException, RemoteException;

    /**
     * Flush.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws RemoteException
     *             the remote exception
     */
    void flush() throws IOException, RemoteException;

    /**
     * Write.
     *
     * @param b
     *            the b
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws RemoteException
     *             the remote exception
     */
    void write(byte[] b) throws IOException, RemoteException;

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
     * @throws RemoteException
     *             the remote exception
     */
    void write(byte[] b, int off, int len) throws IOException, RemoteException;

    /**
     * Write.
     *
     * @param b
     *            the b
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws RemoteException
     *             the remote exception
     */
    void write(int b) throws IOException, RemoteException;
}
