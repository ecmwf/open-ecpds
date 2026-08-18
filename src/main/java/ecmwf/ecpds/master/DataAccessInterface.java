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

import java.io.IOException;
import java.rmi.Remote;

import ecmwf.common.ecaccess.FileListElement;
import ecmwf.common.technical.ProxySocket;

/**
 * The Interface DataAccessInterface.
 */
public interface DataAccessInterface extends Remote {
    /**
     * Gets the file last modified.
     *
     * @param destinationNameOrType
     *            the destination name or type
     * @param source
     *            the source
     *
     * @return the file last modified
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    long getFileLastModified(String destinationNameOrType, String source) throws MasterException, IOException;

    /**
     * Size.
     *
     * @param destinationNameOrType
     *            the destination name or type
     * @param source
     *            the source
     *
     * @return the long
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    long size(String destinationNameOrType, String source) throws MasterException, IOException;

    /**
     * Gets the.
     *
     * @param destinationNameOrType
     *            the destination name or type
     * @param source
     *            the source
     *
     * @return the file list element
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    FileListElement get(String destinationNameOrType, String source) throws MasterException, IOException;

    /**
     * Gets the proxy socket input.
     *
     * @param destinationNameOrType
     *            the destination name or type
     * @param source
     *            the source
     * @param offset
     *            the offset
     *
     * @return the proxy socket input
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    ProxySocket getProxySocketInput(String destinationNameOrType, String source, long offset)
            throws MasterException, IOException;

    /**
     * Gets the proxy socket input.
     *
     * @param destinationNameOrType
     *            the destination name or type
     * @param source
     *            the source
     * @param offset
     *            the offset
     * @param length
     *            the length
     *
     * @return the proxy socket input
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    ProxySocket getProxySocketInput(String destinationNameOrType, String source, long offset, long length)
            throws MasterException, IOException;

    /**
     * Gets the proxy socket output.
     *
     * @param caller
     *            the caller (e.g. master, monitor)
     * @param destinationNameOrType
     *            the destination name or type
     * @param target
     *            the target
     * @param offset
     *            the offset
     * @param umask
     *            the umask
     *
     * @return the proxy socket output
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    ProxySocket getProxySocketOutput(String caller, String destinationNameOrType, String target, long offset, int umask)
            throws MasterException, IOException;

    /**
     * Delete.
     *
     * @param destinationNameOrType
     *            the destination name or type
     * @param source
     *            the source
     * @param force
     *            the force
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    void delete(String destinationNameOrType, String source, boolean force) throws MasterException, IOException;

    /**
     * Delete with attribution. Default implementation delegates to the 3-arg form; override to record who/how.
     */
    default void delete(String destinationNameOrType, String source, boolean force, String byAndFrom)
            throws MasterException, IOException {
        delete(destinationNameOrType, source, force);
    }

    /**
     * Mkdir.
     *
     * @param destinationNameOrType
     *            the destination name or type
     * @param path
     *            the path
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    void mkdir(String destinationNameOrType, String path) throws MasterException, IOException;

    /**
     * Rmdir.
     *
     * @param destinationNameOrType
     *            the destination name or type
     * @param path
     *            the path
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    void rmdir(String destinationNameOrType, String path) throws MasterException, IOException;

    /**
     * List.
     *
     * @param destinationNameOrType
     *            the destination name or type
     * @param path
     *            the path
     * @param sort
     *            the sort
     * @param order
     *            the order
     *
     * @return the file list element[]
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    FileListElement[] list(String destinationNameOrType, String path, int sort, int order)
            throws MasterException, IOException;

    /**
     * List.
     *
     * @param destinationNameOrType
     *            the destination name or type
     * @param path
     *            the path
     *
     * @return the file list element[]
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    FileListElement[] list(String destinationNameOrType, String path) throws MasterException, IOException;

    /**
     * Move.
     *
     * @param destinationNameOrType
     *            the destination name or type
     * @param source
     *            the source
     * @param target
     *            the target
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    void move(String destinationNameOrType, String source, String target) throws MasterException, IOException;

    /**
     * Move with attribution. Default implementation delegates to the 3-arg form; override to record who/how.
     */
    default void move(String destinationNameOrType, String source, String target, String byAndFrom)
            throws MasterException, IOException {
        move(destinationNameOrType, source, target);
    }

    /**
     * Check.
     *
     * @param proxy
     *            the proxy
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    void check(ProxySocket proxy) throws MasterException, IOException;
}
