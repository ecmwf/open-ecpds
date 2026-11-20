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
 */

package ecmwf.ecpds.master;

import java.io.IOException;
import java.security.GeneralSecurityException;

import ecmwf.common.database.DataBaseException;
import ecmwf.common.database.DataTransfer;
import ecmwf.common.database.ECpdsBase;
import ecmwf.common.database.Host;
import ecmwf.common.database.HostPasswordRetirementStore;
import ecmwf.common.database.HostPasswordRotationStore.ProbeHost;
import ecmwf.common.ectrans.PasswordRotation;
import ecmwf.common.ectrans.PasswordRotation.Approved;
import ecmwf.common.rmi.RmiStubTransport;
import ecmwf.common.technical.Cnf;
import ecmwf.ecpds.master.transfer.DestinationOption;
import ecmwf.ecpds.master.transfer.HostOption;
import ecmwf.ecpds.master.transfer.TransferManagement;
import ecmwf.ecpds.mover.MoverInterface;
import ecmwf.ecpds.mover.MoverPasswordEnvelope;
import ecmwf.ecpds.mover.MoverPasswordEnvelope.Assignment;
import ecmwf.ecpds.mover.MoverPasswordEnvelope.Delivery;
import ecmwf.ecpds.mover.MoverPasswordEnvelope.Operation;
import ecmwf.ecpds.mover.MoverPasswordEnvelope.Purpose;
import ecmwf.ecpds.mover.MoverPasswordEnvelope.Route;

final class HostPasswordDeliveryDispatch {
    private HostPasswordDeliveryDispatch() {
    }

    static DataTransfer putIfActive(final ECpdsBase database, final String selectedMover, final MoverInterface mover,
            final Host[] sources, final DataTransfer transfer, final OpenBaoPasswordStore vault)
            throws IOException, DataBaseException {
        if (!Cnf.at("HostPasswordRotation", "deliveryEnabled", false)) {
            return null;
        }
        final var host = Long.parseLong(transfer.getHostName());
        if (Cnf.at("PasswordProbeHosts", Long.toString(host), "").isBlank()) {
            // Legacy admission still checks both markers before calling the Mover.
            return null;
        }
        final var rotations = database.getHostPasswordRotationStore();
        final var retirements = database.getHostPasswordRetirementStore();
        final var selected = transfer.getHost();
        final var snapshot = ProbeHost.of(transfer.getDestinationName(), selected);
        final var endpoint = approvedEndpoint(host);
        final var active = rotations.findActiveForDispatch(host, snapshot,
                endpoint == null ? null : endpoint.fingerprint(), endpoint == null ? null : endpoint.host(),
                endpoint == null ? 0 : endpoint.port(), endpoint == null ? null : endpoint.login());
        if (active.isEmpty()) {
            return null;
        }
        if (vault == null || endpoint == null || !Cnf.at("CallBack", "requireMutualTls", false)
                || RmiStubTransport.inspect(mover) != RmiStubTransport.Kind.MUTUAL_TLS_FACTORY) {
            throw new IOException("Vault-backed password delivery is unavailable");
        }
        if (!selected.getActive() || !HostOption.DISSEMINATION.equals(selected.getType())
                || selected.getAutomaticLocation() || !endpoint.matches(snapshot.moduleClass(), snapshot.archive())
                || !endpoint.capability().supports(snapshot.methodValue(), snapshot.hostData(), endpoint.port())
                || !endpoint.host().equals(selected.getHost()) || !endpoint.login().equals(selected.getLogin())) {
            throw new IOException("Vault-backed password Host snapshot changed");
        }
        final var proxyHost = transfer.getProxyHost();
        final long proxy;
        final String recipient;
        if (proxyHost == null) {
            proxy = 0;
            recipient = selectedMover;
        } else {
            if (!proxyHost.getActive() || !HostOption.PROXY.equals(proxyHost.getType())) {
                throw new IOException("Vault-backed password requires an active Continental route");
            }
            proxy = Long.parseLong(proxyHost.getName());
            recipient = Cnf.at("PasswordDeliveryRelayMovers", proxyHost.getName(), "");
        }
        if (recipient == null || recipient.isBlank() || proxy < 0) {
            throw new IOException("Vault-backed password requires an approved final Mover");
        }
        if (proxy == 0) {
            TransferManagement.requireLegacySourcesAllowed(sources);
        }
        final var route = proxy == 0 ? Route.DIRECT_RMI : Route.CONTINENTAL_HTTPS;
        final HostPasswordProbeDispatch.Keys keys;
        final String keyFingerprint;
        try {
            keys = HostPasswordProbeDispatch.keys(recipient);
            keyFingerprint = fingerprint(keys.recipient());
        } catch (final GeneralSecurityException | IOException | RuntimeException _) {
            throw new IOException("Vault-backed password delivery keys are unavailable");
        }
        final var target = TransferManagement.preparePinnedPut(recipient, transfer);
        final var file = transfer.getDataFile();
        final var configuration = snapshot.fingerprint();
        final var password = vault.read(active.orElseThrow());
        if (new DestinationOption(transfer.getDestination()).ifTargetExistResume()) {
            final var stat = new Delivery(transfer.getId(), file.getId(), file.getSize(), snapshot.destination(),
                    target, 0, 0, endpoint.fingerprint(), configuration, Operation.SIZE, active.orElseThrow());
            final var nonce = challenge(mover, proxy, host, stat);
            final var sealed = seal(route, recipient, host, nonce, stat, password, keys);
            final long size = proxy == 0 ? mover.sizePinnedPassword(host, nonce, stat, sealed, transfer)
                    : mover.sizeContinentalPinnedPassword(proxy, host, nonce, stat, sealed, transfer);
            if (size > 0) {
                throw new IOException("Vault-backed password will not overwrite a non-empty resume target");
            }
            if (size < -1) {
                throw new IOException("Vault-backed password remote size is unknown");
            }
        }
        final var put = new Delivery(transfer.getId(), file.getId(), file.getSize(), snapshot.destination(), target, 0,
                0, endpoint.fingerprint(), configuration, Operation.PUT, active.orElseThrow());
        final var nonce = challenge(mover, proxy, host, put);
        final var sealed = seal(route, recipient, host, nonce, put, password, keys);
        retirements.reserveDelivery(
                new HostPasswordRetirementStore.Attempt(nonce, host, active.orElseThrow(), transfer.getId(), recipient,
                        keyFingerprint, route.name(), put.fingerprint()),
                snapshot, endpoint.fingerprint(), endpoint.host(), endpoint.port(), endpoint.login());
        try {
            final var result = proxy == 0 ? mover.putPinnedPassword(host, nonce, put, sealed, sources, transfer)
                    : mover.putContinentalPinnedPassword(proxy, host, nonce, put, sealed, transfer);
            if (result != null && recipient.equals(result.getMoverName())) {
                retirements.acknowledgeDelivery(nonce);
                return result;
            }
        } catch (final IOException | RuntimeException _) {
            // A response can be lost after the final Mover submits the upload.
        }
        throw new MasterServer.PinnedPasswordDispatchUnconfirmed();
    }

    static void recordSuccess(final ECpdsBase database, final DataTransfer transfer) {
        final var receipt = transfer.getPasswordDeliveryReceipt();
        if (receipt == null) {
            return;
        }
        try {
            final var retirements = database.getHostPasswordRetirementStore();
            final var id = MoverPasswordEnvelope.successfulDeliveryAttempt(receipt);
            final var attempt = retirements.findAttempt(id).orElseThrow(() -> new IOException("Unknown attempt"));
            final var keys = HostPasswordProbeDispatch.keys(attempt.mover());
            final var success = MoverPasswordEnvelope
                    .verifySuccessfulDelivery(
                            receipt, keys.recipient(), new Assignment(Purpose.PASSWORD_TRANSFER,
                                    Route.valueOf(attempt.route()), attempt.mover(), attempt.host(), attempt.id()),
                            attempt.reference(), attempt.context());
            if (attempt.transfer() != transfer.getId() || !attempt.key().equals(fingerprint(keys.recipient()))) {
                throw new IOException("Delivery evidence binding changed");
            }
            retirements.recordDeliveryOutcome(attempt, receipt, success.successful());
        } catch (final IOException | GeneralSecurityException | RuntimeException _) {
            // Unverifiable callbacks never start retirement; ordinary delivery processing continues.
        }
    }

    private static String fingerprint(final java.security.interfaces.RSAPublicKey key) throws GeneralSecurityException {
        return java.util.HexFormat.of()
                .formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(key.getEncoded()));
    }

    private static java.util.UUID challenge(final MoverInterface mover, final long proxy, final long host,
            final Delivery delivery) throws IOException {
        final var nonce = proxy == 0 ? mover.beginPinnedPassword(host, delivery)
                : mover.beginContinentalPinnedPassword(proxy, host, delivery);
        if (nonce == null) {
            throw new IOException("Vault-backed password delivery challenge unavailable");
        }
        return nonce;
    }

    private static String seal(final Route route, final String recipient, final long host, final java.util.UUID nonce,
            final Delivery delivery, final String password, final HostPasswordProbeDispatch.Keys keys)
            throws IOException {
        return MoverPasswordEnvelope.sealTransfer(
                new Assignment(Purpose.PASSWORD_TRANSFER, route, recipient, host, nonce), delivery, password,
                keys.recipient(), keys.signing());
    }

    private static Approved approvedEndpoint(final long host) {
        try {
            return PasswordRotation.approved(host);
        } catch (final IllegalArgumentException _) {
            return null;
        }
    }
}
