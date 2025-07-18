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

import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import ecmwf.common.rmi.SocketConfig;
import ecmwf.common.technical.Cnf;
import ecmwf.common.text.Format;

/**
 * The Class RemoteCnfImp.
 */
public final class RemoteCnfImp extends CallBackObject implements RemoteCnf {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 5845723177983595096L;

    /**
     * Instantiates a new remote cnf imp.
     *
     * @throws RemoteException
     *             the remote exception
     */
    public RemoteCnfImp() throws RemoteException {
    }

    /**
     * Adds the.
     *
     * @param group
     *            the group
     *
     * @throws RemoteException
     *             the remote exception
     */
    @Override
    public void add(final String group) throws RemoteException {
        throw Format.getRemoteException(SocketConfig.getLocalAddress(), "Not implemented");
    }

    /**
     * Adds the.
     *
     * @param group
     *            the group
     * @param key
     *            the key
     * @param value
     *            the value
     *
     * @throws RemoteException
     *             the remote exception
     */
    @Override
    public void add(final String group, final String key, final String value) throws RemoteException {
        throw Format.getRemoteException(SocketConfig.getLocalAddress(), "Not implemented");
    }

    /**
     * Adds the.
     *
     * @param group
     *            the group
     * @param table
     *            the table
     *
     * @throws RemoteException
     *             the remote exception
     */
    @Override
    public void add(final String group, final Hashtable<String, String> table) throws RemoteException {
        throw Format.getRemoteException(SocketConfig.getLocalAddress(), "Not implemented");
    }

    /**
     * At.
     *
     * @param group
     *            the group
     *
     * @return the hashtable
     *
     * @throws RemoteException
     *             the remote exception
     */
    @Override
    public Hashtable<String, String> at(final String group) throws RemoteException {
        return new Hashtable<>(Cnf.at(group));
    }

    /**
     * At.
     *
     * @param group
     *            the group
     * @param key
     *            the key
     *
     * @return the string
     *
     * @throws RemoteException
     *             the remote exception
     */
    @Override
    public String at(final String group, final String key) throws RemoteException {
        return Cnf.at(group, key);
    }

    /**
     * At.
     *
     * @param group
     *            the group
     * @param key
     *            the key
     * @param defaut
     *            the defaut
     *
     * @return the string
     *
     * @throws RemoteException
     *             the remote exception
     */
    @Override
    public String at(final String group, final String key, final String defaut) throws RemoteException {
        return Cnf.at(group, key, defaut);
    }

    /**
     * Groups.
     *
     * @return the vector
     *
     * @throws RemoteException
     *             the remote exception
     */
    @Override
    public Vector<String> groups() throws RemoteException {
        throw Format.getRemoteException(SocketConfig.getLocalAddress(), "Not implemented");
    }

    /**
     * Values at.
     *
     * @param group
     *            the group
     *
     * @return the hashtable
     *
     * @throws RemoteException
     *             the remote exception
     */
    @Override
    public Hashtable<String, Vector<String>> valuesAt(final String group) throws RemoteException {
        throw Format.getRemoteException(SocketConfig.getLocalAddress(), "Not implemented");
    }

    /**
     * Values at.
     *
     * @param group
     *            the group
     * @param key
     *            the key
     *
     * @return the vector
     *
     * @throws RemoteException
     *             the remote exception
     */
    @Override
    public Vector<String> valuesAt(final String group, final String key) throws RemoteException {
        throw Format.getRemoteException(SocketConfig.getLocalAddress(), "Not implemented");
    }

    /**
     * Boolean at.
     *
     * @param group
     *            the group
     * @param key
     *            the key
     * @param defaut
     *            the defaut
     *
     * @return true, if successful
     *
     * @throws RemoteException
     *             the remote exception
     */
    @Override
    public boolean booleanAt(final String group, final String key, final boolean defaut) throws RemoteException {
        return Cnf.at(group, key, defaut);
    }

    /**
     * Gets the added properties.
     *
     * @return the added properties
     *
     * @throws RemoteException
     *             the remote exception
     */
    @Override
    public Properties getAddedProperties() throws RemoteException {
        throw Format.getRemoteException(SocketConfig.getLocalAddress(), "Not implemented");
    }

    /**
     * Gets the export.
     *
     * @return the export
     *
     * @throws RemoteException
     *             the remote exception
     */
    @Override
    public Hashtable<String, Hashtable<String, String>> getExport() throws RemoteException {
        throw Format.getRemoteException(SocketConfig.getLocalAddress(), "Not implemented");
    }

    /**
     * Gets the system properties.
     *
     * @return the system properties
     *
     * @throws RemoteException
     *             the remote exception
     */
    @Override
    public Properties getSystemProperties() throws RemoteException {
        throw Format.getRemoteException(SocketConfig.getLocalAddress(), "Not implemented");
    }

    /**
     * Import from.
     *
     * @param export
     *            the export
     *
     * @throws RemoteException
     *             the remote exception
     */
    @Override
    public void importFrom(final Hashtable<String, Hashtable<String, String>> export) throws RemoteException {
        throw Format.getRemoteException(SocketConfig.getLocalAddress(), "Not implemented");
    }

    /**
     * Int at.
     *
     * @param group
     *            the group
     * @param key
     *            the key
     * @param defaut
     *            the defaut
     *
     * @return the int
     *
     * @throws RemoteException
     *             the remote exception
     */
    @Override
    public int intAt(final String group, final String key, final int defaut) throws RemoteException {
        return Cnf.at(group, key, defaut);
    }

    /**
     * Not empty string at.
     *
     * @param group
     *            the group
     * @param key
     *            the key
     *
     * @return the string
     *
     * @throws RemoteException
     *             the remote exception
     */
    @Override
    public String notEmptyStringAt(final String group, final String key) throws RemoteException {
        throw Format.getRemoteException(SocketConfig.getLocalAddress(), "Not implemented");
    }

    /**
     * Not empty string at.
     *
     * @param group
     *            the group
     * @param key
     *            the key
     * @param defaut
     *            the defaut
     *
     * @return the string
     *
     * @throws RemoteException
     *             the remote exception
     */
    @Override
    public String notEmptyStringAt(final String group, final String key, final String defaut) throws RemoteException {
        return Cnf.notEmptyStringAt(group, key, defaut);
    }
}
