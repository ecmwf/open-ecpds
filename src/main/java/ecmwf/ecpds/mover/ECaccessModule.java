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

package ecmwf.ecpds.mover;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import static ecmwf.common.ectrans.ECtransOptions.HOST_ECACCESS_DESTINATION;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECACCESS_GATEWAY;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECACCESS_LASTUSED;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECACCESS_LOADBALANCING;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_INITIAL_INPUT_FILTER;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_INITIAL_INPUT_MD5;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_INITIAL_INPUT_SIZE;
import static ecmwf.common.text.Util.isNotEmpty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.database.ECUser;
import ecmwf.common.database.ECtransDestination;
import ecmwf.common.database.MSUser;
import ecmwf.common.ecaccess.ConnectionException;
import ecmwf.common.ecaccess.StarterServer;
import ecmwf.common.ectrans.ECtransSetup;
import ecmwf.common.technical.ProxySocket;
import ecmwf.ecbatch.eis.rmi.client.DataAccess;
import ecmwf.ecbatch.eis.rmi.client.EccmdException;

/**
 * The Class ECaccessModule.
 */
public final class ECaccessModule extends ProxyModule {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(ECaccessModule.class);

    /** The Constant _mover. */
    private static final MoverServer _mover = StarterServer.getInstance(MoverServer.class);

    /** The _status. */
    private String currentStatus = "INIT";

    /** The _setup. */
    private ECtransSetup currentSetup = null;

    /** The _gateway. */
    private String targetGateway = null;

    /** The _access. */
    private DataAccess access = null;

    /** The _cookie. */
    private String cookie = null;

    /** The _ecuser. */
    private ECUser ecuser = null;

    /** The _msuser. */
    private MSUser msuser = null;

    /** The last filename used. */
    private String fileName = null;

    /** The _destination. */
    private ECtransDestination destination = null;

    /**
     * Connect.
     *
     * @param location
     *            the location
     * @param setup
     *            the setup
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws ConnectionException
     *             the connection exception
     * @throws EccmdException
     *             the eccmd exception
     */
    @Override
    public void connect(final String location, final ECtransSetup setup)
            throws IOException, ConnectionException, EccmdException {
        currentSetup = setup;
        _setStatus("CONNECT");
        final var lastused = setup.getString(HOST_ECACCESS_LASTUSED);
        final var loadbalancing = setup.getBoolean(HOST_ECACCESS_LOADBALANCING);
        final var gateways = setup.getStringList(HOST_ECACCESS_GATEWAY);
        final var size = gateways.size();
        if (size == 0) {
            throw new EccmdException("No ECaccess Gateway configured (please set the \"ecaccess.gateway\" parameter)");
        }
        final var toTry = new String[size];
        var i = 0;
        var n = 0;
        for (String gateway : gateways) {
            gateway = gateway.trim();
            toTry[i++] = gateway;
            if (loadbalancing && gateway.equals(lastused)) {
                n = i % toTry.length;
            }
        }
        String message = null;
        for (var j = 0; j < i; j++) {
            final var gateway = toTry[(n + j) % toTry.length];
            try {
                if ((access = _mover.getDataAccess(gateway)) != null && access.available(gateway)) {
                    setup.set(HOST_ECACCESS_LASTUSED, targetGateway = gateway);
                    _log.debug("Gateway selected: " + gateway + "/" + i + " (loadbalancing=" + loadbalancing + ")");
                    setAttribute("remote.hostName", gateway);
                    message = null;
                    break;
                }
                message = "No connection to Gateway(s) " + (isNotEmpty(gateways) ? gateways + " " : "");
            } catch (final EccmdException t) {
                _log.warn("Getting DataAccess", t);
                throw t;
            } catch (final Throwable t) {
                _log.warn("Getting DataAccess", t);
                message = "No connection to ECaccessServer";
            }
        }
        if (message != null) {
            throw new EccmdException(message);
        }
        cookie = getCookie() != null ? getCookie() + "@" + targetGateway : null;
        destination = new ECtransDestination(setup.getString(HOST_ECACCESS_DESTINATION));
        destination.setActive(false);
        msuser = getMSUser();
        msuser.setECtransDestination(null);
        msuser.setECtransDestinationName(null);
        ecuser = getECUser();
    }

    /**
     * Gets the.
     *
     * @param name
     *            the name
     * @param posn
     *            the posn
     * @param socket
     *            the socket
     *
     * @throws Exception
     *             the exception
     */
    @Override
    public void get(final String name, final long posn, final ProxySocket socket) throws Exception {
        _setStatus("GET");
        fileName = name;
        access.get(targetGateway, ecuser, msuser, destination, cookie, name, socket, posn);
    }

    /**
     * Put.
     *
     * @param name
     *            the name
     * @param posn
     *            the posn
     * @param size
     *            the size
     * @param socket
     *            the socket
     *
     * @throws Exception
     *             the exception
     */
    @Override
    public void put(final String name, final long posn, final long size, final ProxySocket socket) throws Exception {
        _setStatus("PUT");
        fileName = name;
        access.put(targetGateway, ecuser, msuser, destination, cookie, name, socket, posn, size);
    }

    /**
     * Del.
     *
     * @param name
     *            the name
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void del(final String name) throws IOException {
        _setStatus("DEL");
        try {
            access.del(targetGateway, ecuser, msuser, destination, cookie, name);
        } catch (final Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    /**
     * Mkdir.
     *
     * @param dir
     *            the dir
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void mkdir(final String dir) throws IOException {
        _setStatus("MKDIR");
        try {
            access.mkdir(targetGateway, ecuser, msuser, destination, cookie, dir);
        } catch (final Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    /**
     * Rmdir.
     *
     * @param dir
     *            the dir
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void rmdir(final String dir) throws IOException {
        _setStatus("RMDIR");
        try {
            access.rmdir(targetGateway, ecuser, msuser, destination, cookie, dir);
        } catch (final Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    /**
     * Size.
     *
     * @param name
     *            the name
     *
     * @return the long
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public long size(final String name) throws IOException {
        _setStatus("SIZE");
        try {
            return access.size(targetGateway, ecuser, msuser, destination, cookie, name);
        } catch (final Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    /**
     * Move.
     *
     * @param source
     *            the source
     * @param target
     *            the target
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void move(final String source, final String target) throws IOException {
        _setStatus("MOVE");
        try {
            access.move(targetGateway, ecuser, msuser, destination, cookie, source, target);
        } catch (final Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    /**
     * List as string array.
     *
     * @param directory
     *            the directory
     * @param pattern
     *            the pattern
     *
     * @return the string[]
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public String[] listAsStringArray(final String directory, final String pattern) throws IOException {
        _setStatus("LIST");
        try {
            final List<String> result = new ArrayList<>();
            var i = 0;
            for (final String line : access.list(targetGateway, ecuser, msuser, destination, cookie, directory)) {
                if (pattern == null || line.matches(pattern)) {
                    if (getDebug()) {
                        _log.debug("List[" + i++ + "] " + line);
                    }
                    result.add(line);
                }
            }
            return result.toArray(new String[result.size()]);
        } catch (final Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    /**
     * Removes the.
     *
     * @param closedOnError
     *            the closed on error
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void remove(final boolean closedOnError) throws IOException {
        _setStatus("CLOSE");
        currentSetup = null;
        if (cookie != null) {
            _log.debug("Close remote cookie: " + cookie);
            try {
                access.remove(targetGateway, ecuser, msuser, destination, cookie);
            } catch (final Exception e) {
                _log.warn("Removing remote cookie", e);
            }
        }
    }

    /**
     * Delegate checksum.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void delegateChecksum() throws IOException {
        _log.debug("Checksum will be done on remote site");
    }

    /**
     * Check.
     *
     * @param sent
     *            the sent
     * @param checksum
     *            the checksum
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void check(final long sent, final String checksum) throws IOException {
        super.check(sent, checksum);
        if (checksum != null) {
            _log.debug("Checksum not performed here (" + checksum + ")");
        }
        if (fileName != null) {
            setAttribute("remote.fileName", fileName);
        }
    }

    /**
     * Sets the input filter.
     *
     * @param filter
     *            the new input filter
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void setInputFilter(final String filter) throws IOException {
        _log.debug("SetInputFilter: " + filter);
        currentSetup.set(HOST_ECTRANS_INITIAL_INPUT_FILTER, filter);
        msuser.setData(currentSetup.getData());
    }

    /**
     * Sets the input size.
     *
     * @param size
     *            the new input size
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void setInputSize(final long size) throws IOException {
        _log.debug("SetInputSize: " + size);
        currentSetup.set(HOST_ECTRANS_INITIAL_INPUT_SIZE, size);
        msuser.setData(currentSetup.getData());
    }

    /**
     * Sets the input md 5.
     *
     * @param md5
     *            the new input md 5
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void setInputMd5(final String md5) throws IOException {
        _log.debug("SetInputMd5: " + md5);
        currentSetup.set(HOST_ECTRANS_INITIAL_INPUT_MD5, md5);
        msuser.setData(currentSetup.getData());
    }

    /**
     * Gets the status.
     *
     * @return the status
     */
    @Override
    public String getStatus() {
        return currentStatus;
    }

    /**
     * Sets the status.
     *
     * @param status
     *            the status
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void _setStatus(final String status) throws IOException {
        _log.debug("Status set to: " + status);
        if (currentSetup == null) {
            throw new IOException("Module closed");
        }
        currentStatus = status;
    }
}
