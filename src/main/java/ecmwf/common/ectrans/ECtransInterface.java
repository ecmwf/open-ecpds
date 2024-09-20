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

package ecmwf.common.ectrans;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.Closeable;
import java.io.IOException;

/**
 * The Class ECtransInterface.
 */
public abstract class ECtransInterface implements Closeable {
    /** The _cookie. */
    protected String _cookie = null;

    /** The _ecuser. */
    protected String _ecuser = null;

    /** The _remote. */
    protected String _remote = null;

    /** The _location. */
    protected String _location = null;

    /**
     * Instantiates a new ectrans interface.
     *
     * @param persistent
     *            the persistent
     * @param ecuser
     *            the ecuser
     * @param remote
     *            the remote
     * @param location
     *            the location
     */
    public ECtransInterface(final boolean persistent, final String ecuser, final String remote, final String location) {
        _cookie = persistent ? "ECtransProxy." + hashCode() : null;
        _ecuser = ecuser;
        _remote = remote;
        _location = location;
    }

    /**
     * Del.
     *
     * @param name
     *            the name
     *
     * @throws ECtransException
     *             the ectrans exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public abstract void del(String name) throws ECtransException, IOException;

    /**
     * Puts the.
     *
     * @param name
     *            the name
     * @param ticket
     *            the ticket
     * @param posn
     *            the posn
     * @param size
     *            the size
     *
     * @throws ECtransException
     *             the ectrans exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public abstract void put(String name, long ticket, long posn, long size) throws ECtransException, IOException;

    /**
     * Size.
     *
     * @param name
     *            the name
     *
     * @return the long
     *
     * @throws ECtransException
     *             the ectrans exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public abstract long size(String name) throws ECtransException, IOException;

    /**
     * Gets the.
     *
     * @param name
     *            the name
     * @param ticket
     *            the ticket
     * @param posn
     *            the posn
     *
     * @throws ECtransException
     *             the ectrans exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public abstract void get(String name, long ticket, long posn) throws ECtransException, IOException;

    /**
     * Sets the attribute.
     *
     * @param key
     *            the key
     * @param value
     *            the value
     *
     * @throws ECtransException
     *             the ectrans exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public abstract void setAttribute(Object key, Object value) throws ECtransException, IOException;

    /**
     * Gets the attribute.
     *
     * @param key
     *            the key
     *
     * @return the attribute
     *
     * @throws ECtransException
     *             the ectrans exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public abstract Object getAttribute(Object key) throws ECtransException, IOException;

    /**
     * Invoke.
     *
     * @param method
     *            the method
     * @param argTypes
     *            the arg types
     * @param args
     *            the args
     *
     * @return the object
     *
     * @throws ECtransException
     *             the ectrans exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public abstract Object invoke(String method, Class<?>[] argTypes, Object[] args)
            throws ECtransException, IOException;

    /**
     * Close.
     *
     * @return true, if successful
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void close() throws IOException {
        _cookie = null;
        _ecuser = null;
        _remote = null;
        _location = null;
    }
}
