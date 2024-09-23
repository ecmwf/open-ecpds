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

import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import ecmwf.common.technical.Cnf;

/**
 * The Class RemoteCnfImp.
 */
public final class RemoteCnfImp extends CallBackObject implements RemoteCnf {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 5845723177983595096L;

    /**
     * Instantiates a new remote cnf imp.
     *
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    public RemoteCnfImp() throws RemoteException {
    }

    /**
     * {@inheritDoc}
     *
     * Adds the.
     */
    @Override
    public void add(final String group) throws RemoteException {
        throw new RemoteException("Not implemented");
    }

    /**
     * {@inheritDoc}
     *
     * Adds the.
     */
    @Override
    public void add(final String group, final String key, final String value) throws RemoteException {
        throw new RemoteException("Not implemented");
    }

    /**
     * {@inheritDoc}
     *
     * Adds the.
     */
    @Override
    public void add(final String group, final Hashtable<String, String> table) throws RemoteException {
        throw new RemoteException("Not implemented");
    }

    /**
     * {@inheritDoc}
     *
     * At.
     */
    @Override
    public Hashtable<String, String> at(final String group) throws RemoteException {
        return new Hashtable<>(Cnf.at(group));
    }

    /**
     * {@inheritDoc}
     *
     * At.
     */
    @Override
    public String at(final String group, final String key) throws RemoteException {
        return Cnf.at(group, key);
    }

    /**
     * {@inheritDoc}
     *
     * At.
     */
    @Override
    public String at(final String group, final String key, final String defaut) throws RemoteException {
        return Cnf.at(group, key, defaut);
    }

    /**
     * {@inheritDoc}
     *
     * Groups.
     */
    @Override
    public Vector<String> groups() throws RemoteException {
        throw new RemoteException("Not implemented");
    }

    /**
     * {@inheritDoc}
     *
     * Values at.
     */
    @Override
    public Hashtable<String, Vector<String>> valuesAt(final String group) throws RemoteException {
        throw new RemoteException("Not implemented");
    }

    /**
     * {@inheritDoc}
     *
     * Values at.
     */
    @Override
    public Vector<String> valuesAt(final String group, final String key) throws RemoteException {
        throw new RemoteException("Not implemented");
    }

    /**
     * {@inheritDoc}
     *
     * Boolean at.
     */
    @Override
    public boolean booleanAt(final String group, final String key, final boolean defaut) throws RemoteException {
        return Cnf.at(group, key, defaut);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the added properties.
     */
    @Override
    public Properties getAddedProperties() throws RemoteException {
        throw new RemoteException("Not implemented");
    }

    /**
     * {@inheritDoc}
     *
     * Gets the export.
     */
    @Override
    public Hashtable<String, Hashtable<String, String>> getExport() throws RemoteException {
        throw new RemoteException("Not implemented");
    }

    /**
     * {@inheritDoc}
     *
     * Gets the system properties.
     */
    @Override
    public Properties getSystemProperties() throws RemoteException {
        throw new RemoteException("Not implemented");
    }

    /**
     * {@inheritDoc}
     *
     * Import from.
     */
    @Override
    public void importFrom(final Hashtable<String, Hashtable<String, String>> export) throws RemoteException {
        throw new RemoteException("Not implemented");
    }

    /**
     * {@inheritDoc}
     *
     * Int at.
     */
    @Override
    public int intAt(final String group, final String key, final int defaut) throws RemoteException {
        return Cnf.at(group, key, defaut);
    }

    /**
     * {@inheritDoc}
     *
     * Not empty string at.
     */
    @Override
    public String notEmptyStringAt(final String group, final String key) throws RemoteException {
        throw new RemoteException("Not implemented");
    }

    /**
     * {@inheritDoc}
     *
     * Not empty string at.
     */
    @Override
    public String notEmptyStringAt(final String group, final String key, final String defaut) throws RemoteException {
        return Cnf.notEmptyStringAt(group, key, defaut);
    }
}
