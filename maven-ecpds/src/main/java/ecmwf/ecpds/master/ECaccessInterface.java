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
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.rmi.RemoteException;

import ecmwf.common.ecaccess.ProviderInterface;

/**
 * The Interface ECaccessInterface.
 */
interface ECaccessInterface extends ProviderInterface {
    /**
     * Gets the management interface.
     *
     * @return the management interface
     *
     * @throws RemoteException
     *             the remote exception
     */
    ManagementInterface getManagementInterface() throws RemoteException;

    /**
     * Gets the data base interface.
     *
     * @return the data base interface
     *
     * @throws RemoteException
     *             the remote exception
     */
    DataBaseInterface getDataBaseInterface() throws RemoteException;

    /**
     * Gets the attachment access interface.
     *
     * @return the attachment access interface
     *
     * @throws RemoteException
     *             the remote exception
     */
    DataAccessInterface getAttachmentAccessInterface() throws RemoteException;

    /**
     * Gets the data file access interface.
     *
     * @return the data file access interface
     *
     * @throws RemoteException
     *             the remote exception
     */
    DataAccessInterface getDataFileAccessInterface() throws RemoteException;
}
