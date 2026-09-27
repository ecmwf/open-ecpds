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

package ecmwf.common.monitor;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * This interface should be implemented by any class which implement a monitoring protocol (e.g. Nagios, Bigsister).
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 *
 * @version 6.7.7
 *
 * @since 2024-07-01
 */
public interface MonitorInterface {
    /**
     * Sends the message.
     *
     * @param name
     *            the name
     * @param service
     *            the service
     * @param status
     *            the status
     * @param message
     *            the message
     *
     * @throws java.lang.Exception
     *             the exception
     */
    void sendMessage(String name, String service, int status, String message) throws Exception;

    /**
     * Adds/replaces the notes attached to a destination/host. A {@code default} method (rather than a second abstract
     * one) so that {@link #sendMessage} remains this interface's only abstract method - existing providers set up as a
     * lambda (a single-method functional interface, e.g. a DataMover relaying {@code sendMessage} calls to the
     * MasterServer over its REST proxy) keep compiling unchanged, and simply don't support notes: the default throws
     * {@link UnsupportedOperationException}, which {@link MonitorManager#addNotes} turns into a
     * {@link MonitorException} for the caller.
     *
     * @param destination
     *            the destination
     * @param metadata
     *            the metadata
     *
     * @throws java.lang.Exception
     *             the exception
     */
    default void addNotes(String destination, String metadata) throws Exception {
        throw new UnsupportedOperationException("addNotes not supported by this MonitorInterface provider");
    }
}
