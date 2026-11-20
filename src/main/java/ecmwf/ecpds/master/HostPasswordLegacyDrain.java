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
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

import ecmwf.common.database.ECpdsBase;
import ecmwf.common.database.HostPasswordRotationStore.Access;
import ecmwf.common.database.HostPasswordRotationStore.LegacyDrainProgress;
import ecmwf.common.database.HostPasswordRotationStore.LegacyRoute;
import ecmwf.common.rmi.RmiStubTransport;
import ecmwf.common.technical.Cnf;
import ecmwf.ecpds.mover.MoverInterface;
import ecmwf.ecpds.mover.MoverPasswordDrain;
import ecmwf.ecpds.mover.MoverPasswordDrain.Challenge;
import ecmwf.ecpds.mover.MoverPasswordEnvelope.Route;

/** Persistent first-cutover enrollment; a Mover restart never becomes an implicit zero-session acknowledgement. */
public final class HostPasswordLegacyDrain {
    private static final java.util.concurrent.Semaphore CAPACITY = new java.util.concurrent.Semaphore(4);
    private static final java.util.Set<Long> IN_FLIGHT = java.util.concurrent.ConcurrentHashMap.newKeySet();

    record Target(String id, String relay, long proxy, String mover) {
        Route route() {
            return proxy == 0 ? Route.DIRECT_RMI : Route.CONTINENTAL_HTTPS;
        }
    }

    private record Enrolled(Target target, MoverInterface relay, HostPasswordProbeDispatch.Keys keys,
            Challenge challenge, LegacyRoute enrollment) {
    }

    private HostPasswordLegacyDrain() {
    }

    static LegacyDrainProgress drain(final MasterServer master, final Access access, final UUID reference)
            throws IOException {
        if (!Cnf.at("HostPasswordRotation", "drainEnabled", false) || !Cnf.at("CallBack", "requireMutualTls", false)) {
            throw new IOException("Online credential drain is disabled");
        }
        if (!IN_FLIGHT.add(access.host())) {
            throw new IOException("A credential drain is already running for this Host");
        }
        if (!CAPACITY.tryAcquire()) {
            IN_FLIGHT.remove(access.host());
            throw new IOException("Credential drain capacity is unavailable");
        }
        final var task = new java.util.concurrent.FutureTask<>(() -> collect(master, access, reference));
        Thread.ofVirtual().name("password-drain-" + access.host()).start(() -> {
            try {
                task.run();
            } finally {
                IN_FLIGHT.remove(access.host());
                CAPACITY.release();
            }
        });
        try {
            return task.get(20, java.util.concurrent.TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            task.cancel(true);
            Thread.currentThread().interrupt();
            throw new IOException("Online credential drain was interrupted");
        } catch (final java.util.concurrent.TimeoutException e) {
            task.cancel(true);
            throw new IOException("Online credential drain could not be confirmed within its time budget");
        } catch (final java.util.concurrent.ExecutionException e) {
            throw new IOException("Online credential drain could not be verified");
        }
    }

    private static LegacyDrainProgress collect(final MasterServer master, final Access access, final UUID reference)
            throws IOException {
        final var journal = master.getDataBase(ECpdsBase.class).getHostPasswordRotationStore();
        try {
            final var enrolled = new ArrayList<Enrolled>();
            final var owners = new HashMap<String, String>();
            for (final var target : targets()) {
                requireBudget();
                final var relay = master.getDataMoverInterface(target.relay());
                if (relay == null || RmiStubTransport.inspect(relay) != RmiStubTransport.Kind.MUTUAL_TLS_FACTORY
                        || !target.relay().equals(relay.getRoot())) {
                    throw new IOException("An enrolled legacy drain route is unavailable");
                }
                final var keys = HostPasswordProbeDispatch.keys(target.mover());
                final var fingerprint = digest(keys.recipient().getEncoded());
                final var owner = owners.putIfAbsent(fingerprint, target.mover());
                if (owner != null && !owner.equals(target.mover())) {
                    throw new IOException("Final Movers must have distinct delivery identities");
                }
                final var challenge = target.proxy() == 0 ? relay.beginLegacyPasswordFence(access.host())
                        : relay.beginContinentalLegacyPasswordFence(target.proxy(), access.host());
                if (challenge == null) {
                    throw new IOException("An enrolled legacy drain challenge is unavailable");
                }
                enrolled.add(new Enrolled(target, relay, keys, challenge, new LegacyRoute(target.id(), target.relay(),
                        target.proxy(), target.mover(), fingerprint, challenge.incarnation())));
            }
            final var endpoint = HostPasswordStaging.approvedEndpoint(access.host());
            final var cutover = journal.prepareLegacyDrain(access, reference, endpoint.fingerprint(),
                    enrolled.stream().map(Enrolled::enrollment).toList());
            for (final var entry : enrolled) {
                requireBudget();
                final var target = entry.target();
                final var command = MoverPasswordDrain.command(target.mover(), target.route(), access.host(),
                        cutover.id(), entry.challenge(), entry.keys().signing());
                final var receipt = target.proxy() == 0
                        ? entry.relay().fenceLegacyPassword(access.host(), cutover.id(), entry.challenge(), command)
                        : entry.relay().fenceContinentalLegacyPassword(target.proxy(), access.host(), cutover.id(),
                                entry.challenge(), command);
                final var evidence = MoverPasswordDrain.verifyEvidence(receipt, target.mover(), target.route(),
                        access.host(), cutover.id(), entry.challenge(), entry.keys().recipient());
                journal.recordLegacyDrainEvidence(access, cutover, entry.enrollment(), entry.challenge().nonce(),
                        receipt, evidence.sessions());
            }

            final var progress = journal.legacyDrainProgress(access);
            if (progress.routes() > 0 && progress.routes() == progress.freshReceipts() && progress.sessions() == 0) {
                return journal.reconcileLegacyAdmissions(access, cutover);
            }
            return progress;
        } catch (final GeneralSecurityException | RuntimeException _) {
            throw new IOException("Online credential drain could not be verified");
        }
    }

    private static void requireBudget() throws IOException {
        if (Thread.currentThread().isInterrupted()) {
            throw new IOException("Online credential drain time budget expired");
        }
    }

    public static void requireLegacyRoute(final long host, final String relay, final long proxy) throws IOException {
        if (Cnf.at("HostPasswordRotation", "drainEnabled", false)
                && !Cnf.at("PasswordProbeHosts", Long.toString(host), "").isBlank()
                && targets().stream().noneMatch(target -> target.relay().equals(relay) && target.proxy() == proxy)) {
            throw new IOException("Legacy credential dispatch requires an enrolled route");
        }
    }

    static List<Target> targets() throws IOException {
        final var result = new ArrayList<Target>();
        final var ids = new java.util.HashSet<String>();
        for (final var value : Cnf.at("PasswordDrainFleet").values()) {
            final var fields = value.trim().split("\\s+");
            try {
                if (fields.length != 3 || fields[0].isBlank() || fields[0].length() > 255 || fields[2].isBlank()
                        || fields[2].length() > 255) {
                    throw new IllegalArgumentException();
                }
                final var proxy = Long.parseLong(fields[1]);
                if (proxy < 0 || proxy == 0 && !fields[0].equals(fields[2])
                        || proxy > 0 && !fields[2].equals(Cnf.at("PasswordDeliveryRelayMovers", fields[1], ""))) {
                    throw new IllegalArgumentException();
                }
                final var id = digest((fields[0] + "\n" + proxy + "\n" + fields[2]).getBytes(StandardCharsets.UTF_8));
                if (!ids.add(id) || ids.size() > 64) {
                    throw new IllegalArgumentException();
                }
                result.add(new Target(id, fields[0], proxy, fields[2]));
            } catch (final IllegalArgumentException | GeneralSecurityException _) {
                throw new IOException("Invalid legacy drain fleet configuration");
            }
        }
        if (result.isEmpty()) {
            throw new IOException("Legacy drain fleet is not configured");
        }
        return List.copyOf(result);
    }

    private static String digest(final byte[] value) throws GeneralSecurityException {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value));
    }
}
