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

import javax.management.timer.Timer;
import ecmwf.common.ecaccess.StorageRepository;
import ecmwf.common.technical.Cnf;
import ecmwf.common.text.Format;

/**
 * The Class ProxyHostRepository. Check if ProxyHost notifications are on time. If a ProxyHost is expired then all the
 * current transfers occurring on this Host are cancelled.
 */
final class ProxyHostRepository extends StorageRepository<ProxyHostRepository.ProxyHost> {
    private final MasterServer _master;

    /**
     * Instantiates a new proxy host repository.
     *
     * @param name
     *            the name
     */
    ProxyHostRepository(final MasterServer _master, final String name) {
        // Bind the Master before superclass construction and field initialization.
        this._master = _master;
        super(name, Cnf.at("StorageRepository", "proxyHostSize", 2),
                Cnf.at("StorageRepository", "proxyHostDelay", 15 * Timer.ONE_SECOND));
    }

    /**
     * Gets the key.
     *
     * @param proxyHost
     *            the proxy host
     *
     * @return the key
     */
    /*
     * The key is the name of the ProxyHost.
     *
     * @see ecmwf.common.ecaccess.MBeanRepository#getKey(java.lang.Object)
     */
    @Override
    public String getKey(final ProxyHostRepository.ProxyHost proxyHost) {
        return proxyHost._name;
    }

    /**
     * Gets the status.
     *
     * @param proxyHost
     *            the proxy host
     *
     * @return the status
     */
    /*
     * For the display on the JMX interface.
     *
     * @see ecmwf.common.ecaccess.MBeanRepository#getStatus(java.lang.Object)
     */
    @Override
    public String getStatus(final ProxyHostRepository.ProxyHost proxyHost) {
        return proxyHost._name + " (" + Format.formatDuration(proxyHost._lastUpdate, System.currentTimeMillis()) + ")";
    }

    /**
     * Expired.
     *
     * @param proxyHost
     *            the proxy host
     *
     * @return true, if successful
     */
    /*
     * If we had no news from the ProxyHost for the past 2 minutes then we should consider it as expired. The ProxyHost
     * will do the same if it can not send any alive message to the MasterServer.
     *
     * @see ecmwf.common.ecaccess.StorageRepository#expired(java.lang.Object)
     */
    @Override
    public boolean expired(final ProxyHostRepository.ProxyHost proxyHost) {
        return System.currentTimeMillis() - proxyHost._lastUpdate > 2 * Timer.ONE_MINUTE;
    }

    /**
     * Update.
     *
     * @param proxyHost
     *            the proxy host
     *
     * @throws Exception
     *             the exception
     */
    /*
     * The ProxyHost is expired and it is now removed from the list.
     *
     * @see ecmwf.common.ecaccess.StorageRepository#update(java.lang.Object)
     */
    @Override
    public void update(final ProxyHostRepository.ProxyHost proxyHost) throws Exception {
        _master.resetTransferServer(proxyHost._name, "ProxyHost " + proxyHost._name + " interrupted");
    }

    /**
     * The Class ProxyHost used to keep information about remote ProxyHost in the ProxyHost repository.
     */
    static final class ProxyHost {

        /** The _name. */
        final String _name;

        /** The _last update. */
        final long _lastUpdate;

        /**
         * Instantiates a new proxy host.
         *
         * @param name
         *            the name
         */
        ProxyHost(final String name) {
            _name = name;
            _lastUpdate = System.currentTimeMillis();
        }
    }
}
