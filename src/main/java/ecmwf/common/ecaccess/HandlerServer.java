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

package ecmwf.common.ecaccess;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.IOException;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.starter.Starter;
import ecmwf.common.technical.Cnf;
import ecmwf.common.version.Version;

/**
 * The Class HandlerServer.
 */
public final class HandlerServer extends StarterServer implements HandlerInterface {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 204629532308204304L;

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(HandlerServer.class);

    /** The Constant _VERSION. */
    private static final String _VERSION = Version.getFullVersion();

    /**
     * Instantiates a new handler server.
     *
     * @param starter
     *            the starter
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     * @throws javax.management.InstanceAlreadyExistsException
     *             the instance already exists exception
     * @throws javax.management.MBeanRegistrationException
     *             the MBean registration exception
     * @throws javax.management.NotCompliantMBeanException
     *             the not compliant m bean exception
     * @throws javax.management.MalformedObjectNameException
     *             the malformed object name exception
     * @throws javax.management.InstanceNotFoundException
     *             the instance not found exception
     */
    public HandlerServer(final Starter starter)
            throws IOException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException,
            MalformedObjectNameException, InstanceNotFoundException {
        super(starter);
        _log.info("HandlerServer-version: " + _VERSION);
        final var container = getPluginContainer();
        container.loadPlugins();
        container.startPlugins();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the root.
     */
    @Override
    public String getRoot() {
        return Cnf.at("Login", "root", Cnf.at("Login", "hostName"));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the password.
     */
    @Override
    public String getPassword() {
        return Cnf.at("Login", "password");
    }

    /**
     * {@inheritDoc}
     *
     * Gets the version.
     */
    @Override
    public String getVersion() {
        return _VERSION;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the service.
     */
    @Override
    public String getService() {
        return Cnf.at("Login", "service", "HandlerServer");
    }
}
