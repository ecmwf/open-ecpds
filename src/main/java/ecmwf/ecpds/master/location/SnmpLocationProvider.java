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

package ecmwf.ecpds.master.location;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import org.snmp4j.CommunityTarget;
import org.snmp4j.MessageDispatcherImpl;
import org.snmp4j.PDU;
import org.snmp4j.ScopedPDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.UserTarget;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.AuthHMAC192SHA256;
import org.snmp4j.security.AuthHMAC384SHA512;
import org.snmp4j.security.PrivAES128;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.Null;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import ecmwf.common.database.SystemPhysicalLocationStore.Status;
import ecmwf.ecpds.master.location.PhysicalLocationConfig.Machine;

/** One read-only GET per poll; sessions isolate even identical usernames with different credentials. */
public final class SnmpLocationProvider implements PhysicalLocationService.Provider {
    static final OID SYS_LOCATION = new OID("1.3.6.1.2.1.1.6.0");
    private final Set<Snmp> active = new HashSet<>();
    private boolean closed;

    @Override
    public PhysicalLocationService.Result query(Machine machine, long timeout, int retries) throws Exception {
        final var dispatcher = new MessageDispatcherImpl();
        final var profile = machine.profile();
        final Target<UdpAddress> target;
        final PDU request;
        if (profile.v3) {
            final var protocols = new SecurityProtocols(SecurityProtocols.SecurityProtocolSet.none);
            final var auth = "SHA512".equals(profile.authProtocol) ? new AuthHMAC384SHA512() : new AuthHMAC192SHA256();
            protocols.addAuthenticationProtocol(auth);
            protocols.addPrivacyProtocol(new PrivAES128());
            final var usm = new USM(protocols, new OctetString(MPv3.createLocalEngineID()), 0);
            usm.addUser(new UsmUser(new OctetString(profile.username), auth.getID(),
                    new OctetString(profile.authPassword), PrivAES128.ID, new OctetString(profile.privacyPassword)));
            dispatcher.addMessageProcessingModel(new MPv3(usm));
            final var userTarget = new UserTarget<UdpAddress>();
            userTarget.setVersion(SnmpConstants.version3);
            userTarget.setSecurityName(new OctetString(profile.username));
            userTarget.setSecurityLevel(SecurityLevel.AUTH_PRIV);
            target = userTarget;
            final var scoped = new ScopedPDU();
            scoped.setContextName(new OctetString(profile.context));
            request = scoped;
        } else {
            dispatcher.addMessageProcessingModel(new MPv2c());
            final var communityTarget = new CommunityTarget<UdpAddress>();
            communityTarget.setVersion(SnmpConstants.version2c);
            communityTarget.setCommunity(new OctetString(profile.communityPassword));
            target = communityTarget;
            request = new PDU();
        }
        target.setAddress(new UdpAddress(machine.address(), machine.port()));
        target.setTimeout(timeout);
        target.setRetries(retries);
        target.setMaxSizeRequestPDU(4096);
        request.setType(PDU.GET);
        request.add(new VariableBinding(SYS_LOCATION));
        final var bind = InetAddress.getByAddress(new byte[machine.address().getAddress().length]);
        final var transport = new DefaultUdpTransportMapping(new UdpAddress(bind, 0));
        transport.setMaxInboundMessageSize(4096);
        try (final var snmp = new Snmp(dispatcher, transport)) {
            synchronized (active) {
                if (closed) {
                    throw new IOException("Physical location provider closed");
                }
                active.add(snmp);
            }
            try {
                snmp.listen();
                final var event = snmp.get(request, target);
                if (event.getError() != null) {
                    return new PhysicalLocationService.Result(Status.ERROR, null);
                }
                return parse(event.getResponse());
            } finally {
                synchronized (active) {
                    active.remove(snmp);
                }
            }
        }
    }

    static PhysicalLocationService.Result parse(PDU response) throws Exception {
        if (response == null) {
            return new PhysicalLocationService.Result(Status.TIMEOUT, null);
        }
        if (response.getType() != PDU.RESPONSE || response.getErrorStatus() != PDU.noError || response.size() != 1) {
            return new PhysicalLocationService.Result(Status.ERROR, null);
        }
        final var binding = response.get(0);
        if (!SYS_LOCATION.equals(binding.getOid())) {
            return new PhysicalLocationService.Result(Status.ERROR, null);
        }
        if (binding.getVariable().equals(Null.noSuchObject) || binding.getVariable().equals(Null.noSuchInstance)) {
            return new PhysicalLocationService.Result(Status.UNSUPPORTED, null);
        }
        if (!(binding.getVariable() instanceof OctetString text) || text.length() > 4096) {
            return new PhysicalLocationService.Result(Status.ERROR, null);
        }
        var value = StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(text.getValue())).toString();
        value = value.replace('\r', ' ').replace('\n', ' ').replace('\t', ' ').strip();
        if (value.codePoints().anyMatch(Character::isISOControl) || value.codePointCount(0, value.length()) > 1024) {
            return new PhysicalLocationService.Result(Status.ERROR, null);
        }
        return new PhysicalLocationService.Result(value.isBlank() ? Status.NOT_CONFIGURED : Status.SUCCESS,
                value.isBlank() ? null : value);
    }

    @Override
    public void close() {
        synchronized (active) {
            closed = true;
            for (final var snmp : active) {
                try {
                    snmp.close();
                } catch (final IOException ignored) {
                }
            }
            active.clear();
        }
    }
}
