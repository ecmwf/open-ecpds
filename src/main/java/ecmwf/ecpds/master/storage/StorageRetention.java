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

package ecmwf.ecpds.master.storage;

import static ecmwf.common.ectrans.ECtransGroups.Module.DESTINATION_SCHEDULER;
import static ecmwf.common.ectrans.ECtransOptions.DESTINATION_SCHEDULER_STORAGEPOLICY;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import ecmwf.common.database.StorageLifecycleStore.Snapshot;
import ecmwf.ecpds.master.transfer.StatusFactory;

/** Combines every live transfer's requirements; a single veto keeps every copy. */
public final class StorageRetention {
    public record Decision(boolean eligible, boolean enforce, long eligibleAt, long guaranteedUntil,
            Set<String> policies, Set<String> originals, String reason) {
    }

    private StorageRetention() {
    }

    public static Decision evaluate(final List<StoragePolicy> policies, final Snapshot snapshot, final long now,
            final Predicate<StoragePolicy> pressure) {
        final var names = new LinkedHashSet<String>();
        final var originals = new LinkedHashSet<String>();
        long eligibleAt = 0, guarantee = 0;
        boolean enforce = true, live = false;
        String veto = null;
        final var file = snapshot.file();
        if (file.getDeleted() || file.getRemoved() || !file.getDownloaded() || file.getFileSystem() == null
                || file.getArrivedTime() == null || file.getOriginal() == null || file.getSize() < 0) {
            return new Decision(false, false, 0, 0, Set.of(), Set.of(), "File unavailable or normal purge owns it");
        }
        for (final var transfer : snapshot.transfers()) {
            if (transfer.expiry() <= now) {
                continue;
            }
            live = true;
            final var setup = DESTINATION_SCHEDULER.getECtransSetup(transfer.configuration());
            final var policy = StoragePolicy
                    .select(policies, new StoragePolicy.Subject(transfer.destination(), transfer.type(),
                            transfer.group(), transfer.target(), setup.getString(DESTINATION_SCHEDULER_STORAGEPOLICY)))
                    .policy();
            names.add(policy.getName());
            final boolean active = policy.isActive() && policy.getMode() != StoragePolicy.Mode.DISABLED;
            enforce &= active && policy.getMode() == StoragePolicy.Mode.ENFORCE;
            if (!active || !policy.isDropReplicas()) {
                veto = "At least one live transfer requires all replicas";
            }
            if (!StatusFactory.DONE.equals(transfer.status())) {
                veto = "Pending, failed or stopped transfer: keep every copy";
            }
            final long start = policy.isRestartOnRequeue() ? transfer.finish() : transfer.firstFinish();
            if (start <= 0 || transfer.finish() <= 0) {
                veto = "Completion time unavailable: keep every copy";
            } else {
                final long window = policy.guaranteedUntil(start, transfer.expiry());
                guarantee = Math.max(guarantee, window);
                long ready = Math.max(StoragePolicy.addDuration(start, policy.getRequeueWindowMs()),
                        StoragePolicy.addDuration(transfer.finish(), policy.getDropReplicasAfterMs()));
                ready = Math.max(ready,
                        StoragePolicy.addDuration(file.getArrivedTime().getTime(), policy.getMinRetentionMs()));
                if (!pressure.test(policy)) {
                    ready = StoragePolicy.addDuration(ready, policy.getGraceMs());
                }
                eligibleAt = Math.max(eligibleAt, ready);
            }
            if (transfer.original() == null || transfer.original().isBlank()) {
                veto = "Original DataMover unknown: keep every copy";
            } else {
                originals.add(transfer.original());
            }
        }
        if (!live) {
            veto = "No live transfers: normal expiry purge owns the file";
        }
        return new Decision(veto == null && now >= eligibleAt, enforce, eligibleAt, guarantee, Set.copyOf(names),
                Set.copyOf(originals), veto != null ? veto
                        : now < eligibleAt ? "Waiting for retention windows and replica delay" : "Eligible");
    }
}
