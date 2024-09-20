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

import java.io.IOException;

import ecmwf.common.database.ECtransHistory;

/**
 * The Class ECtransAction.
 */
abstract class ECtransAction {
    /** The _history. */
    private final ECtransHistory _history;

    /** The _provider. */
    private RemoteProvider _provider = null;

    /** The _container. */
    private ECtransContainer _container = null;

    /** The _cookie. */
    private String _cookie = null;

    /** The _callback. */
    private ECtransCallback _callback = null;

    /**
     * Instantiates a new ectrans action.
     */
    public ECtransAction() {
        _history = new ECtransHistory();
        _history.setAction(getName());
        _history.setError(false);
    }

    /**
     * Inits the.
     *
     * @param provider
     *            the provider
     * @param container
     *            the container
     * @param callback
     *            the callback
     * @param cookie
     *            the cookie
     */
    public void init(final RemoteProvider provider, final ECtransContainer container, final ECtransCallback callback,
            final String cookie) {
        _provider = provider;
        _container = container;
        _callback = callback;
        _cookie = cookie;
    }

    /**
     * Gets the EC trans container.
     *
     * @return the EC trans container
     */
    protected ECtransContainer getECTransContainer() {
        return _container;
    }

    /**
     * Gets the ectrans callback.
     *
     * @return the ectrans callback
     */
    protected ECtransCallback getECtransCallback() {
        return _callback;
    }

    /**
     * Gets the remote provider.
     *
     * @return the remote provider
     */
    protected RemoteProvider getRemoteProvider() {
        return _provider;
    }

    /**
     * Gets the ectrans history.
     *
     * @return the ectrans history
     */
    protected ECtransHistory getECtransHistory() {
        return _history;
    }

    /**
     * Gets the cookie.
     *
     * @return the cookie
     */
    protected String getCookie() {
        return _cookie;
    }

    /**
     * Gets the result.
     *
     * @return the result
     */
    public Object getResult() {
        return null;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    protected abstract String getName();

    /**
     * Exec.
     *
     * @param module
     *            the module
     * @param interruptible
     *            the interruptible
     *
     * @throws Exception
     *             the exception
     */
    protected abstract void exec(TransferModule module, boolean interruptible) throws Exception;

    /**
     * Close.
     *
     * @param module
     *            the module
     * @param timeout
     *            the timeout
     * @param asynchronous
     *            the asynchronous
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    protected void close(final TransferModule module, final long timeout, final boolean asynchronous)
            throws IOException {
        if (_container != null) {
            _container.close(_provider, getCookie() == null ? module : null, getECtransHistory(), timeout,
                    asynchronous);
        }
    }
}
