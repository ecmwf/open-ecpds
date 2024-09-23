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

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

/**
 * The Interface RemoteCnf.
 */
public interface RemoteCnf extends Remote {
    /**
     * Adds the.
     *
     * @param group
     *            the group
     *
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    void add(String group) throws RemoteException;

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
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    void add(String group, String key, String value) throws RemoteException;

    /**
     * Adds the.
     *
     * @param group
     *            the group
     * @param table
     *            the table
     *
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    void add(String group, Hashtable<String, String> table) throws RemoteException;

    /**
     * At.
     *
     * @param group
     *            the group
     *
     * @return the hashtable
     *
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    Hashtable<String, String> at(String group) throws RemoteException;

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
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    String at(String group, String key) throws RemoteException;

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
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    String at(String group, String key, String defaut) throws RemoteException;

    /**
     * Groups.
     *
     * @return the vector
     *
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    Vector<?> groups() throws RemoteException;

    /**
     * Values at.
     *
     * @param group
     *            the group
     *
     * @return the hashtable
     *
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    Hashtable<String, Vector<String>> valuesAt(String group) throws RemoteException;

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
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    Vector<?> valuesAt(String group, String key) throws RemoteException;

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
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    boolean booleanAt(String group, String key, boolean defaut) throws RemoteException;

    /**
     * Gets the added properties.
     *
     * @return the added properties
     *
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    Properties getAddedProperties() throws RemoteException;

    /**
     * Gets the export.
     *
     * @return the export
     *
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    Hashtable<String, Hashtable<String, String>> getExport() throws RemoteException;

    /**
     * Gets the system properties.
     *
     * @return the system properties
     *
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    Properties getSystemProperties() throws RemoteException;

    /**
     * Import from.
     *
     * @param export
     *            the export
     *
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    void importFrom(Hashtable<String, Hashtable<String, String>> export) throws RemoteException;

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
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    int intAt(String group, String key, int defaut) throws RemoteException;

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
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    String notEmptyStringAt(String group, String key) throws RemoteException;

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
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    String notEmptyStringAt(String group, String key, String defaut) throws RemoteException;
}
