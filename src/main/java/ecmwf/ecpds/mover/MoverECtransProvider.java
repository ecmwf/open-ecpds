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

import static ecmwf.common.ectrans.ECtransGroups.Module.HOST_ECTRANS;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_HOST_SELECTOR;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_USEDNSNAME;
import static ecmwf.common.ectrans.ECtransOptions.HOST_TEST_BYTES_PER_SEC;
import static ecmwf.common.text.Util.isEmpty;
import static ecmwf.common.text.Util.isNotEmpty;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.script.ScriptException;

import ecmwf.common.database.ECUser;
import ecmwf.common.database.ECtransDestination;
import ecmwf.common.database.ECtransModule;
import ecmwf.common.database.MSUser;
import ecmwf.common.ectrans.AllocateInterface;
import ecmwf.common.ectrans.ECtransException;
import ecmwf.common.ectrans.ECtransProvider;
import ecmwf.common.ectrans.NotificationInterface;
import ecmwf.common.technical.ByteSize;
import ecmwf.common.text.Format.DuplicatedChooseScore;
import ecmwf.common.text.Format;

/**
 * The Class MoverECtransProvider.
 */
final class MoverECtransProvider extends ECtransProvider {
    private final MoverServer _mover;

    /** The _repository. */
    private final GenericRepository _repository;

    /** The _set dir. */
    private final boolean _setDir;
    private final boolean pinnedPassword;

    /**
     * Instantiates a new mover provider.
     *
     * @param repository
     *            the repository
     */
    MoverECtransProvider(final MoverServer _mover, final GenericRepository repository) {
        this(_mover, repository, true);
    }

    /**
     * Instantiates a new mover provider.
     *
     * @param repository
     *            the repository
     * @param setDir
     *            the set dir
     */
    MoverECtransProvider(final MoverServer _mover, final GenericRepository repository, final boolean setDir) {
        this(_mover, repository, setDir, false);
    }

    MoverECtransProvider(final MoverServer _mover, final GenericRepository repository, final boolean setDir,
            final boolean pinnedPassword) {
        this._mover = _mover;
        _repository = repository;
        _setDir = setDir;
        this.pinnedPassword = pinnedPassword;
    }

    /**
     * Gets the root.
     *
     * @return the root
     */
    @Override
    public String getRoot() {
        return _mover.getRoot();
    }

    @Override
    public java.io.Closeable openTransferSession(final MSUser msuser) throws IOException {
        return pinnedPassword || _mover.legacySessions == null ? null : _mover.legacySessions.admit(msuser.getName());
    }

    /**
     * Gets the allocate interface.
     *
     * @param url
     *            the url
     * @param properties
     *            the properties
     *
     * @return the allocate interface
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws MoverException
     *             the mover exception
     */
    @Override
    public AllocateInterface getAllocateInterface(final String url, final Properties properties)
            throws IOException, MoverException {
        if (_mover.restProvider == null) {
            throw new MoverException("No RESTProvider available");
        }
        return _mover.restProvider.getRESTAllocate(url, properties);
    }

    /**
     * Gets the notification interface.
     *
     * @param url
     *            the url
     * @param name
     *            the name
     * @param password
     *            the password
     *
     * @return the notification interface
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws MoverException
     *             the mover exception
     */
    @Override
    public NotificationInterface getNotificationInterface(final String url, final String name, final String password)
            throws IOException, MoverException {
        if (_mover.restProvider == null) {
            throw new MoverException("No RESTProvider available");
        }
        return _mover.restProvider.getNotificationInterface(url, name, password);
    }

    /**
     * Update MS user.
     *
     * @param msuser
     *            the msuser
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void updateMSUser(final MSUser msuser) throws IOException {
        final var host = _repository.getHost(msuser.getName());
        if (host == null) {
            // Not found in the cache?
            MoverServer._log.warn("Host {} not found in cache (update canceled)", msuser.getName());
        } else {
            final var hostId = host.getName();
            if (hostId == null) {
                // This Host was specifically tagged for not being updated!
                MoverServer._log.debug("Source Host for retrieval (no update): {}", msuser.getName());
            } else if (!hostId.equals(msuser.getName())) {
                // The Host name does not match the MSUser name?
                MoverServer._log.warn("Host {} does not match (update canceled)", msuser.getName());
            } else {
                // The Host should be updated!
                MoverServer._log.debug("Updating Host: {} ({})", hostId, host.getNickname());
                host.setData(msuser.getData());
                try {
                    _mover.getMasterProxy().updateData(host);
                } catch (final Exception e) {
                    MoverServer._log.warn("updateData", e);
                }
            }
        }
    }

    /**
     * Gets the object.
     *
     * @param key
     *            the key
     *
     * @return the object
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public Object getObject(final Object key) throws IOException {
        final var hostName = key.toString();
        final var host = _repository.getHost(hostName);
        if (host == null) {
            throw new IOException("Host " + hostName + " not found");
        }
        return host.clone();
    }

    /**
     * Gets the MS user.
     *
     * @param ecuser
     *            the ecuser
     * @param name
     *            the name
     *
     * @return the MS user
     *
     * @throws ECtransException
     *             the ectrans exception
     */
    @Override
    public MSUser getMSUser(final String ecuser, final String name) throws ECtransException {
        final var host = _repository.getHost(name);
        if (host == null) {
            MoverServer._log.debug("MSUser {} not found in the repository", name);
            return null;
        }
        final var method = host.getTransferMethod();
        final var destination = new ECtransDestination(method.getName());
        destination.setActive(method.getActive());
        destination.setComment(method.getComment());
        destination.setECtransModuleName(method.getECtransModuleName());
        destination.setECtransModule(method.getECtransModule());
        destination.setResolve(method.getResolve());
        destination.setRestrict(method.getRestrict());
        destination.setValue(method.getValue());
        var hostDir = host.getDir();
        String realDir = null;
        if (_setDir && isNotEmpty(hostDir)) {
            final var token = new StringTokenizer(hostDir, "\n");
            if (token.hasMoreTokens() && !(hostDir = token.nextToken()).trim().startsWith("(")) {
                final var index = hostDir.indexOf("$");
                if (index != -1) {
                    realDir = hostDir.substring(0, index);
                } else {
                    realDir = hostDir;
                }
            }
        }
        final var setup = HOST_ECTRANS.getECtransSetup(host.getData());
        var hostName = host.getHost();
        // Do we have a host selector to find an alternative host-name/IP?
        final var hostSelector = setup.getString(HOST_ECTRANS_HOST_SELECTOR);
        if (!pinnedPassword && isNotEmpty(hostSelector)) {
            try {
                final var sb = new StringBuilder(hostSelector);
                Format.replaceAll(sb, "$mover", getRoot());
                Format.replaceAll(sb, "$host", host.getHost());
                Format.replaceAll(sb, "$network", host.getNetworkName());
                Format.replaceAll(sb, "$group", host.getTransferGroupName());
                hostName = Format.choose(sb.toString());
                MoverServer._log.debug("Using alternative hostname: {}", hostName);
            } catch (final DuplicatedChooseScore e) {
                throw new ECtransException("Could not resolve host field (multiple choices selected)");
            } catch (final ScriptException e) {
                throw new ECtransException("Could not resolve host field (" + e.getMessage() + ")");
            }
        }
        if (!pinnedPassword && !setup.getBoolean(HOST_ECTRANS_USEDNSNAME) && hostName != null
                && hostName.indexOf("$") == -1) {
            // The host name is static (not using parameters) so we can
            // process its IP address
            final var hostLocation = host.getHostLocation();
            final var hostIp = hostLocation.getIp();
            try {
                hostName = InetAddress.getByName(hostName).getHostAddress();
                if (isEmpty(hostIp) || !hostIp.equals(hostName)) {
                    final var hostId = host.getName();
                    if (isNotEmpty(hostId)) {
                        // This Host should be updated on the Master!
                        MoverServer._log.debug("Host-{} IP change (update): {} != {}", hostId, hostIp, hostName);
                        hostLocation.setIp(hostName);
                        try {
                            _mover.getMasterProxy().updateLocation(host);
                        } catch (final Throwable t) {
                            MoverServer._log.warn("updateLocation", t);
                        }
                    }
                }
            } catch (final UnknownHostException _) {
                if (!isNotEmpty(hostIp)) {
                    throw new ECtransException("unknown host " + hostName + " (consider setting the "
                            + HOST_ECTRANS_USEDNSNAME.getFullName() + " option to true)");
                }
                MoverServer._log.warn("Using cached IP address: {}", hostIp);
                hostName = hostIp;
            }
        }
        // For the non operational system include the transfer rate for the
        // host in the ectrans data (to allow the test module to transmit at
        // the host usual rate)! For this to work, the hibernate.hbm.xml file must
        // include the hostStats in the HOST.
        if (!MoverServer.OPERATIONAL) {
            final var hostStats = host.getHostStats();
            if (hostStats != null && hostStats.getDuration() > 0) {
                final var bytesPerSecond = (long) (hostStats.getSent() / ((double) hostStats.getDuration() / 1000));
                MoverServer._log.info("Setting bytesPerSecond for Host-{} to: {}", host.getName(), bytesPerSecond);
                setup.set(HOST_TEST_BYTES_PER_SEC, ByteSize.of(bytesPerSecond));
            } else {
                MoverServer._log.debug("No transfer rate found for Host-{}", host.getName());
            }
        }
        final var msuser = new MSUser(name);
        msuser.setActive(host.getActive());
        msuser.setComment(host.getComment());
        msuser.setData(setup.getData());
        msuser.setDir(realDir);
        msuser.setECtransDestinationName(destination.getName());
        msuser.setECtransDestination(destination);
        msuser.setECUserName(host.getECUserName());
        msuser.setECUser(host.getECUser());
        msuser.setHost(hostName);
        msuser.setLogin(host.getLogin());
        if (!pinnedPassword) {
            msuser.setPasswd(host.getPasswd());
        }
        return msuser;
    }

    /**
     * Gets the EC user.
     *
     * @param name
     *            the name
     *
     * @return the EC user
     */
    @Override
    public ECUser getECUser(final String name) {
        return _repository.getECUser(name);
    }

    /**
     * Gets the ectrans module.
     *
     * @param name
     *            the name
     *
     * @return the ectrans module
     */
    @Override
    public ECtransModule getECtransModule(final String name) {
        return _repository.getECtransModule(name);
    }

    /**
     * Gets the ectrans destination.
     *
     * @param name
     *            the name
     *
     * @return the ectrans destination
     */
    @Override
    public ECtransDestination getECtransDestination(final String name) {
        final var method = _repository.getTransferMethod(name);
        if (method == null) {
            return null;
        }
        final var destination = new ECtransDestination(method.getName());
        destination.setActive(method.getActive());
        destination.setComment(method.getComment());
        destination.setECtransModuleName(method.getECtransModuleName());
        destination.setECtransModule(method.getECtransModule());
        destination.setResolve(method.getResolve());
        destination.setRestrict(method.getRestrict());
        destination.setValue(method.getValue());
        return destination;
    }

    /**
     * Gets the data input stream.
     *
     * @param ticket
     *            the ticket
     *
     * @return the data input stream
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public InputStream getDataInputStream(final Object ticket) throws IOException {
        return _repository.getDataInputStream(ticket);
    }

    /**
     * Gets the data output stream.
     *
     * @param ticket
     *            the ticket
     *
     * @return the data output stream
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public OutputStream getDataOutputStream(final Object ticket) throws IOException {
        return _repository.getDataOutputStream(ticket);
    }

    /**
     * Gets the data input file.
     *
     * @param ticket
     *            the ticket
     *
     * @return the data input file
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public File getDataInputFile(final Object ticket) throws IOException {
        return _repository.getDataInputFile(ticket);
    }

    /**
     * Gets the data output file.
     *
     * @param ticket
     *            the ticket
     *
     * @return the data output file
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public File getDataOutputFile(final Object ticket) throws IOException {
        return _repository.getDataOutputFile(ticket);
    }

    /**
     * Gets the original filename.
     *
     * @param ticket
     *            the ticket
     *
     * @return the original filename
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public String getOriginalFilename(final Object ticket) throws IOException {
        return _repository.getOriginalFilename(ticket);
    }
}
