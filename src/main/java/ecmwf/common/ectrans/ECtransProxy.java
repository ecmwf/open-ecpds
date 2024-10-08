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
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.File;
import java.io.IOException;

/**
 * The Class ECtransProxy.
 */
final class ECtransProxy extends ECtransInterface {
    /** The _container. */
    private final ECtransContainer _container;

    /**
     * Instantiates a new ectrans proxy.
     *
     * @param container
     *            the container
     * @param persistent
     *            the persistent
     * @param ecuser
     *            the ecuser
     * @param remote
     *            the remote
     * @param location
     *            the location
     */
    public ECtransProxy(final ECtransContainer container, final boolean persistent, final String ecuser,
            final String remote, final String location) {
        super(persistent, ecuser, remote, location);
        _container = container;
    }

    /**
     * {@inheritDoc}
     *
     * Del.
     */
    @Override
    public void del(final String name) throws ECtransException, IOException {
        _getECtransContainer().syncExec(new ECtransDel(_getTarget(name)), _cookie, _ecuser, _remote, _location, true);
    }

    /**
     * {@inheritDoc}
     *
     * Put.
     */
    @Override
    public void put(final String name, final long ticket, final long posn, final long size)
            throws ECtransException, IOException {
        _getECtransContainer().syncExec(new ECtransPut(_getTarget(name), ticket, posn, size), _cookie, _ecuser, _remote,
                _location, true);
    }

    /**
     * {@inheritDoc}
     *
     * Size.
     */
    @Override
    public long size(final String name) throws ECtransException, IOException {
        final var size = new ECtransSize(_getTarget(name));
        _getECtransContainer().syncExec(size, _cookie, _ecuser, _remote, _location, true);
        return size.getSize();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the.
     */
    @Override
    public void get(final String name, final long ticket, final long posn) throws ECtransException, IOException {
        _getECtransContainer().syncExec(new ECtransGet(_getTarget(name), ticket, posn), _cookie, _ecuser, _remote,
                _location, true);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the attribute.
     */
    @Override
    public void setAttribute(final Object key, final Object value) throws ECtransException, IOException {
        _getECtransContainer().syncExec(new ECtransSetAttribute(key, value), _cookie, _ecuser, _remote, _location,
                true);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the attribute.
     */
    @Override
    public Object getAttribute(final Object key) throws ECtransException, IOException {
        final var getAttribute = new ECtransGetAttribute(key);
        _getECtransContainer().syncExec(getAttribute, _cookie, _ecuser, _remote, _location, true);
        return getAttribute.getValue();
    }

    /**
     * {@inheritDoc}
     *
     * Invoke.
     */
    @Override
    public Object invoke(final String method, final Class<?>[] argTypes, final Object[] args)
            throws ECtransException, IOException {
        final var invoke = new ECtransInvoke(method, argTypes, args);
        _getECtransContainer().syncExec(invoke, _cookie, _ecuser, _remote, _location, true);
        return invoke.getObject();
    }

    /**
     * Gets the ectrans container.
     *
     * @return the ectrans container
     *
     * @throws ECtransException
     *             the ectrans exception
     */
    private synchronized ECtransContainer _getECtransContainer() throws ECtransException {
        if (_container != null) {
            return _container;
        }
        throw new ECtransException("ECtransProxy closed/not initialized");
    }

    /**
     * Gets the target.
     *
     * @param name
     *            the name
     *
     * @return the string
     */
    private String _getTarget(final String name) {
        return new File(name).getName();
    }

    /**
     * {@inheritDoc}
     *
     * Close.
     */
    @Override
    public void close() throws IOException {
        try {
            _container.close(_cookie, _ecuser, _remote, _location);
        } finally {
            super.close();
        }
    }
}
