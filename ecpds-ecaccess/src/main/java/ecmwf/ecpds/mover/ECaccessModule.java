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

import static ecmwf.common.ectrans.ECtransOptions.HOST_ECACCESS_DESTINATION;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECACCESS_GATEWAY;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECACCESS_LASTUSED;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECACCESS_LOADBALANCING;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_INITIAL_INPUT_FILTER;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_INITIAL_INPUT_MD5;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_INITIAL_INPUT_SIZE;

import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.database.ECUser;
import ecmwf.common.database.ECtransDestination;
import ecmwf.common.database.MSUser;
import ecmwf.common.ecaccess.StarterServer;
import ecmwf.common.ectrans.ECtransSetup;
import ecmwf.common.technical.ProxySocket;
import ecmwf.ecpds.extension.ecaccess.EcaccessGatewayAccess;

/** ECaccess gateway transfers using Master RMI and the existing proxy-ticket data path. */
public final class ECaccessModule extends ProxyModule {
    private static final Logger LOG = LogManager.getLogger(ECaccessModule.class);
    private final Connector connector;
    private String status = "INIT";
    private ECtransSetup setup;
    private EcaccessGatewayAccess access;
    private String cookie;
    private ECUser user;
    private MSUser association;
    private ECtransDestination destination;
    private String fileName;

    public ECaccessModule() {
        this(target -> {
            final var mover = StarterServer.getInstance(MoverServer.class);
            if (mover == null) {
                throw new IOException("ECaccess transfers require a running Data Mover");
            }
            final var remote = mover.getMasterProxy().getExtensionService("ecaccess", target);
            if (remote == null || remote instanceof EcaccessGatewayAccess) {
                return (EcaccessGatewayAccess) remote;
            }
            throw new IOException("Incompatible ECaccess remote service");
        });
    }

    ECaccessModule(final Connector connector) {
        this.connector = connector;
    }

    @Override
    public void connect(final String location, final ECtransSetup setup) throws IOException {
        if (this.setup != null) {
            throw new IOException("ECaccess module is already connected");
        }
        final var gateways = setup.getStringList(HOST_ECACCESS_GATEWAY).stream().map(String::trim).toList();
        if (gateways.isEmpty() || gateways.stream().anyMatch(String::isBlank)) {
            throw new IOException("No ECaccess Gateway configured (set ecaccess.gateway)");
        }
        if (getECUser() == null || getMSUser() == null) {
            throw new IOException("Missing ECaccess transfer identity");
        }
        final var last = gateways.indexOf(setup.getString(HOST_ECACCESS_LASTUSED));
        final var start = setup.getBoolean(HOST_ECACCESS_LOADBALANCING) && last >= 0 ? (last + 1) % gateways.size() : 0;
        IOException failure = null;
        for (var i = 0; i < gateways.size(); i++) {
            final var gateway = gateways.get((start + i) % gateways.size());
            EcaccessGatewayAccess candidate = null;
            try {
                candidate = connector.open(gateway);
                if (candidate != null && candidate.available()) {
                    destination = new ECtransDestination(setup.getString(HOST_ECACCESS_DESTINATION));
                    destination.setActive(false);
                    association = (MSUser) getMSUser().clone();
                    association.setECtransDestination(null);
                    association.setECtransDestinationName(null);
                    user = getECUser();
                    cookie = getCookie() == null ? null : getCookie() + "@" + gateway;
                    setup.set(HOST_ECACCESS_LASTUSED, gateway);
                    setAttribute("remote.hostName", gateway);
                    this.setup = setup;
                    access = candidate;
                    candidate = null;
                    status = "CONNECT";
                    return;
                }
            } catch (Exception e) {
                failure = new IOException("Cannot connect to ECaccess gateway " + gateway, e);
            } finally {
                closeAccess(candidate);
            }
        }
        throw new IOException("No connection to configured ECaccess gateways", failure);
    }

    @Override
    public void get(final String name, final long offset, final ProxySocket socket) throws IOException {
        status("GET");
        fileName = name;
        access.get(user, association, destination, cookie, name, socket, offset);
    }

    @Override
    public void put(final String name, final long offset, final long size, final ProxySocket socket)
            throws IOException {
        status("PUT");
        fileName = name;
        access.put(user, association, destination, cookie, name, socket, offset, size);
    }

    @Override
    public void del(final String name) throws IOException {
        status("DEL");
        access.del(user, association, destination, cookie, name);
    }

    @Override
    public void mkdir(final String name) throws IOException {
        status("MKDIR");
        access.mkdir(user, association, destination, cookie, name);
    }

    @Override
    public void rmdir(final String name) throws IOException {
        status("RMDIR");
        access.rmdir(user, association, destination, cookie, name);
    }

    @Override
    public long size(final String name) throws IOException {
        status("SIZE");
        return access.size(user, association, destination, cookie, name);
    }

    @Override
    public void move(final String source, final String target) throws IOException {
        status("MOVE");
        access.move(user, association, destination, cookie, source, target);
    }

    @Override
    public String[] listAsStringArray(final String directory, final String pattern) throws IOException {
        status("LIST");
        try {
            final var regex = pattern == null ? null : Pattern.compile(pattern);
            return Arrays.stream(access.list(user, association, destination, cookie, directory))
                    .filter(line -> regex == null || regex.matcher(line).matches()).toArray(String[]::new);
        } catch (PatternSyntaxException e) {
            throw new IOException("Invalid ECaccess listing pattern", e);
        }
    }

    @Override
    public void remove(final boolean closedOnError) {
        if (setup == null) {
            return;
        }
        status = "CLOSE";
        setup = null;
        try {
            if (cookie != null) {
                access.remove(user, association, destination, cookie);
            }
        } catch (IOException | RuntimeException e) {
            LOG.warn("Could not remove ECaccess transfer cookie");
        } finally {
            closeAccess(access);
            access = null;
        }
    }

    private static void closeAccess(final EcaccessGatewayAccess access) {
        if (access != null) {
            try {
                access.close();
            } catch (IOException | RuntimeException e) {
                LOG.warn("Could not close ECaccess gateway access");
            }
        }
    }

    @Override
    public void delegateChecksum() {
        LOG.debug("Checksum will be done on the gateway");
    }

    @Override
    public void check(final long sent, final String checksum, final boolean error) throws IOException {
        super.check(sent, checksum, error);
        if (fileName != null) {
            setAttribute("remote.fileName", fileName);
        }
    }

    @Override
    public void setInputFilter(final String filter) throws IOException {
        connected();
        setup.set(HOST_ECTRANS_INITIAL_INPUT_FILTER, filter);
        association.setData(setup.getData());
    }

    @Override
    public void setInputSize(final long size) throws IOException {
        connected();
        setup.set(HOST_ECTRANS_INITIAL_INPUT_SIZE, size);
        association.setData(setup.getData());
    }

    @Override
    public void setInputMd5(final String md5) throws IOException {
        connected();
        setup.set(HOST_ECTRANS_INITIAL_INPUT_MD5, md5);
        association.setData(setup.getData());
    }

    @Override
    public String getStatus() {
        return status;
    }

    private void status(final String status) throws IOException {
        connected();
        this.status = status;
    }

    private void connected() throws IOException {
        if (setup == null) {
            throw new IOException("Module closed");
        }
    }

    @FunctionalInterface
    interface Connector {
        EcaccessGatewayAccess open(String target) throws Exception;
    }
}
