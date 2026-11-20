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

import java.io.IOException;
import java.util.UUID;

import ecmwf.common.database.HostPasswordRotationStore;
import ecmwf.common.database.HostPasswordRotationStore.Access;
import ecmwf.common.database.HostPasswordRotationStore.State;
import ecmwf.common.ectrans.CredentialProbe.Result;
import ecmwf.common.ectrans.PasswordRotation;
import ecmwf.common.ectrans.PasswordRotation.Approved;
import ecmwf.common.technical.Cnf;
import ecmwf.ecpds.mover.MoverInterface;

/** Internal staging only; never updates a Host or selects a password for delivery. */
final class HostPasswordStaging implements AutoCloseable {
    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager
            .getLogger(HostPasswordStaging.class);
    private final HostPasswordRotationStore journal;
    private final ecmwf.common.database.HostPasswordRetirementStore retirements;
    private final OpenBaoPasswordStore vault;
    private final java.util.concurrent.ScheduledExecutorService retirement;

    HostPasswordStaging(final ecmwf.common.database.ECpdsBase database, final OpenBaoPasswordStore vault) {
        journal = database.getHostPasswordRotationStore();
        retirements = database.getHostPasswordRetirementStore();
        this.vault = vault;
        retirement = Cnf.at("HostPasswordRotation", "retirementEnabled", false)
                ? java.util.concurrent.Executors.newSingleThreadScheduledExecutor(
                        Thread.ofPlatform().daemon(true).name("host-password-retirement").factory())
                : null;
        if (retirement != null) {
            retirement.scheduleWithFixedDelay(this::retireDue, 60, 60, java.util.concurrent.TimeUnit.SECONDS);
        }
    }

    OpenBaoPasswordStore vault() {
        return vault;
    }

    UUID stage(final Access access, final String password, final long retirementGraceSeconds) throws IOException {
        OpenBaoPasswordStore.validatePassword(password);
        final var reference = UUID.randomUUID();
        if (!journal.reserve(access, reference, retirementGraceSeconds)) {
            throw new IOException("Unable to reserve a pending password for this Host");
        }
        // On any ambiguous failure retain the reference; the vault may already have written it.
        vault.stage(reference, password);
        confirmStaged(access, reference);
        return reference;
    }

    void reconcile(final Access access, final UUID reference) throws IOException {
        final var candidate = journal.find(access).filter(pending -> reference.equals(pending.reference()))
                .orElseThrow(() -> new IOException("No matching pending Host password"));
        if (candidate.state() == State.DISCARDING) {
            discard(access, reference);
        } else if (candidate.state() == State.STAGING) {
            // Only a valid read of this immutable version proves that staging completed.
            vault.read(reference);
            confirmStaged(access, reference);
        }
    }

    Result probe(final Access access, final UUID reference, final Approved endpoint, final MoverInterface relay,
            final String recipient, final long proxyHost) throws IOException {
        final var snapshot = journal.reserveEligibleProbe(access, reference);
        final var result = HostPasswordProbeDispatch.probe(relay, recipient, proxyHost, access.host(), endpoint,
                vault.read(reference));
        if (result == Result.PASSED && !journal.recordPassedProbe(access.host(), reference, endpoint.fingerprint(),
                snapshot, endpoint.host(), endpoint.port(), endpoint.login())) {
            throw new IOException("Host configuration or pending password changed during probe");
        }
        return result;
    }

    void discard(final Access access, final UUID reference) throws IOException {
        // A STAGING write may still be in flight. Never discard it before confirming the immutable version exists.
        if (!journal.beginDiscard(access, reference)) {
            throw new IOException("Pending Host password is not available for discard");
        }
        vault.destroy(reference);
        journal.finishDiscard(access.host(), reference);
    }

    HostPasswordRotationStatus status(final Access access, final UUID expectedReference, final Result probe)
            throws IOException {
        final var pending = journal.find(access).orElse(null);
        if (expectedReference != null && (pending == null || !expectedReference.equals(pending.reference()))) {
            throw new IOException("Pending Host password changed");
        }
        final var endpoint = approvedEndpoint(access.host());
        final var active = journal.findActiveForDelivery(access.host(), access.destination(), endpoint.fingerprint(),
                access.address(), access.port(), access.login());
        final var draining = Cnf.at("HostPasswordRotation", "drainEnabled", false);
        return new HostPasswordRotationStatus(pending == null ? null : pending.reference(),
                pending == null ? null : pending.state(), probe, active.orElse(null),
                draining ? journal.legacyDrainProgress(access) : null,
                draining && Cnf.at("HostPasswordRotation", "activationEnabled", false)
                        && Cnf.at("HostPasswordRotation", "retirementEnabled", false)
                        && Cnf.at("HostPasswordRotation", "deliveryEnabled", false),
                pending == null ? null : pending.retirementGraceSeconds());
    }

    static Approved approvedEndpoint(final long host) {
        return PasswordRotation.approved(host);
    }

    private void confirmStaged(final Access access, final UUID reference) throws IOException {
        if (!journal.markStaged(access.host(), reference) && journal.find(access).filter(
                p -> p.reference().equals(reference) && (p.state() == State.STAGED || p.state() == State.TESTED))
                .isEmpty()) {
            throw new IOException("Pending Host password changed during staging");
        }
    }

    void retireDue() {
        final var store = retirements;
        try {
            for (final var candidate : store.dueRetirements()) {
                try {
                    if (store.beginRetirement(candidate)) {
                        vault.destroy(candidate.reference());
                        store.finishRetirement(candidate);
                    }
                } catch (final IOException | RuntimeException _) {
                    // The durable intent and previous pointer survive ambiguous destruction.
                    LOG.warn("Host password retirement remains unconfirmed: Host={}, reference={}", candidate.host(),
                            candidate.reference());
                }
            }
        } catch (final IOException | RuntimeException _) {
            // Database recovery is retried on the next bounded sweep.
            LOG.warn("Host password retirement sweep could not query its journal");
        }
    }

    @Override
    public void close() {
        if (retirement != null) {
            retirement.shutdownNow();
        }
        vault.close();
    }
}
