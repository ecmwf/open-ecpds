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

package ecmwf.common.database;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * A factory for creating Broker objects. Implementations of this Class should have an empty public constructor!
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 *
 * @version 6.7.7
 *
 * @since 2024-07-01
 */

interface BrokerFactory {

    /**
     * Inits the broker.
     *
     * @param debug
     *            the debug
     * @param driver
     *            the driver
     * @param level
     *            the level
     * @param protocol
     *            the protocol
     * @param subProtocol
     *            the sub protocol
     * @param alias
     *            the alias
     * @param user
     *            the user
     * @param password
     *            the password
     * @param dbms
     *            the dbms
     * @param validation
     *            the validation
     */
    void init(boolean debug, String driver, String level, String protocol, String subProtocol, String alias,
            String user, String password, String dbms, String validation);

    /**
     * Gets the broker.
     *
     * @return the broker
     *
     * @throws BrokerException
     *             Signals that a broker exception has occurred.
     */
    Broker getBroker() throws BrokerException;
}
