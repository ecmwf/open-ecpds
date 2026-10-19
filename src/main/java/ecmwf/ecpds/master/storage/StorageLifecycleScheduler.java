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

import java.sql.Timestamp;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.AttributeNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.database.DataTransfer;
import ecmwf.common.database.DataFile;
import ecmwf.common.database.StorageLifecycleStore;
import ecmwf.common.database.StorageLifecycleStore.Snapshot;
import ecmwf.common.mbean.MBeanManager;
import ecmwf.common.technical.Cnf;
import ecmwf.ecpds.master.MasterServer;
import ecmwf.ecpds.master.transfer.StatusFactory;
import ecmwf.common.ecaccess.MBeanScheduler;

/** Only removes redundant copies. Normal expiry remains the sole owner of original-file removal. */
public final class StorageLifecycleScheduler extends MBeanScheduler {
    private static final Logger LOG = LogManager.getLogger(StorageLifecycleScheduler.class);
    private final MasterServer master;
    private final StorageLifecycleStore store = new StorageLifecycleStore();
    private volatile List<StoragePolicy> policies = List.of();
    private volatile long policiesLoaded;
    private volatile boolean ready;
    private volatile String error = "";
    private volatile long lastScan;
    private volatile long cursor;
    private long scanCeiling;
    private boolean scanHadErrors;
    private final Map<String, long[]> scan = new HashMap<>();
    private volatile Map<String, long[]> completedScan = Map.of();
    private final Map<String, Boolean> pressure = new HashMap<>();
    private final AtomicLong errors = new AtomicLong();

    public StorageLifecycleScheduler(final MasterServer master) {
        super("StorageLifecycleScheduler");
        this.master = master;
        setDelay(Cnf.durationAt("StoragePolicy", "interval", 30_000));
        setJammedTimeout(Cnf.durationAt("StoragePolicy", "jammedTimeout", 600_000));
        setTimeRanges(Cnf.listOfTimeRangesAt("StoragePolicy", "timeRanges"));
    }

    public boolean enabled() {
        return Cnf.at("StoragePolicy", "enabled", false);
    }

    private synchronized List<StoragePolicy> policies() throws Exception {
        final long now = System.currentTimeMillis();
        if (!ready || now - policiesLoaded >= 30_000) {
            store.checkSchema();
            final var loaded = StoragePolicy.fromRows(master.getECpdsBase().getStoragePolicies());
            policies = loaded;
            policiesLoaded = now;
            ready = true;
        }
        return policies;
    }

    @Override
    public int nextStep() {
        if (!enabled()) {
            ready = false;
            cursor = 0;
            scan.clear();
            completedScan = Map.of();
            lastScan = 0;
            return NEXT_STEP_DELAY;
        }
        try {
            final var policies = policies();
            if (policies.stream()
                    .noneMatch(p -> p.isActive() && p.getMode() != StoragePolicy.Mode.DISABLED && p.isDropReplicas())) {
                cursor = 0;
                scan.clear();
                completedScan = Map.of();
                lastScan = 0;
                return NEXT_STEP_DELAY;
            }
            final int batch = Math.max(1, Math.min(5000, Cnf.at("StoragePolicy", "batchSize", 100)));
            if (cursor == 0) {
                scanCeiling = store.scanCeiling();
                scanHadErrors = false;
            }
            final var files = store.files(cursor, scanCeiling, batch);
            final long deadline = System.nanoTime()
                    + Math.max(1000, Cnf.durationAt("StoragePolicy", "stepBudget", 10_000)) * 1_000_000L;
            for (final long id : files) {
                if (!isRunning() || !enabled() || System.nanoTime() >= deadline) {
                    return NEXT_STEP_DELAY;
                }
                try (final var lease = StorageFileLocks.tryWrite(id)) {
                    if (lease != null) {
                        evaluate(id, policies);
                    }
                } catch (final Exception e) {
                    errors.incrementAndGet();
                    scanHadErrors = true;
                    error = "DataFile " + id + ": " + e.getMessage();
                    LOG.warn("Storage lifecycle evaluation failed for DataFile {}", id, e);
                    try {
                        store.error(id, e.getMessage(), System.currentTimeMillis());
                    } catch (final RuntimeException persistenceError) {
                        LOG.error("Cannot persist lifecycle error for DataFile {}", id, persistenceError);
                    }
                }
                cursor = id;
            }
            if (files.size() < batch) {
                completedScan = Map.copyOf(scan);
                scan.clear();
                lastScan = System.currentTimeMillis();
                cursor = 0;
                if (!scanHadErrors) {
                    error = "";
                }
            }
        } catch (final Exception e) {
            ready = false;
            errors.incrementAndGet();
            error = "Storage lifecycle suspended; check storage-lifecycle-runtime.sql: " + e.getMessage();
            LOG.error(error, e);
        }
        return NEXT_STEP_DELAY;
    }

    private Snapshot snapshot(final long id) {
        return store.snapshot(id,
                Math.max(1, Math.min(Integer.MAX_VALUE - 1, Cnf.at("StoragePolicy", "maxRelatedTransfers", 10_000))));
    }

    private void evaluate(final long id, final List<StoragePolicy> policies) throws Exception {
        final long now = System.currentTimeMillis();
        final var snapshot = snapshot(id);
        final var file = snapshot.file();
        final var state = store.state(id);
        final var decision = StorageRetention.evaluate(policies, snapshot, now,
                policy -> underPressure(policy, file.getTransferGroupName(), file.getFileSystem()));
        if (Long.parseLong(state.getOrDefault("SLC_PIN_UNTIL", "0")) > now) {
            save(snapshot, decision, "PINNED", "Operator hold: " + state.get("SLC_PIN_REASON"), 0, now);
            return;
        }
        if (master.isStorageFileBusy(id) || snapshot.transfers().stream().anyMatch(transfer -> {
            final var cached = master.getDataTransferFromCache(transfer.id());
            return cached != null;
        })) {
            save(snapshot, decision, "BUSY", "Transfer, upload, backup, proxy, filter, replication or purge is active",
                    0, now);
            return;
        }
        if (!decision.eligible()) {
            save(snapshot, decision, "FULL", decision.reason(), 0, now);
            return;
        }
        final var servers = master.getECpdsBase().getTransferServers(file.getTransferGroupName());
        for (final var original : decision.originals()) {
            final var server = master.getECpdsBase().getTransferServer(original);
            final var mover = master.getDataMoverInterface(original);
            if (server == null || !server.getActive()
                    || !file.getTransferGroupName().equals(server.getTransferGroupName()) || mover == null
                    || mover.inspectStorageReplica(file) < file.getSize()) {
                save(snapshot, decision, "BLOCKED", "Original copy not verified on DataMover " + original, 0, now);
                return;
            }
        }
        long reclaimable = 0;
        boolean complete = true;
        final var targets = new ArrayList<ecmwf.common.database.TransferServer>();
        for (final var server : servers) {
            if (decision.originals().contains(server.getName())) {
                continue;
            }
            final var mover = master.getDataMoverInterface(server.getName());
            if (mover == null) {
                complete = false;
                continue;
            }
            final long size = mover.inspectStorageReplica(file);
            if (size >= 0) {
                reclaimable = Math.addExact(reclaimable, size);
                targets.add(server);
            } else if (size != -1) {
                complete = false;
            }
        }
        if (!decision.enforce()) {
            estimate(decision, targets.isEmpty() ? 0 : 1, reclaimable);
            save(snapshot, decision, "DRY_RUN", complete ? "Would remove verified replicas; original copies retained"
                    : "Partial estimate: some DataMovers are unavailable or busy", reclaimable, now);
            LOG.debug("Storage lifecycle DRY_RUN DataFile {} policies={} reclaimableBytes={}", id, decision.policies(),
                    reclaimable);
            return;
        }
        save(snapshot, decision, "PARTIAL", "Replica removal in progress; originals retained", reclaimable, now);
        for (final var server : targets) {
            if (!enabled()) {
                complete = false;
                break;
            }
            // Recheck the original immediately before each destructive RPC.
            for (final var original : decision.originals()) {
                final var originalServer = master.getECpdsBase().getTransferServer(original);
                final var mover = master.getDataMoverInterface(original);
                if (originalServer == null || !originalServer.getActive()
                        || !file.getTransferGroupName().equals(originalServer.getTransferGroupName()) || mover == null
                        || mover.inspectStorageReplica(file) < file.getSize()) {
                    estimate(decision, reclaimable > 0 ? 1 : 0, reclaimable);
                    save(snapshot, decision, "BLOCKED", "Original became unavailable; remaining replicas retained",
                            reclaimable, now);
                    return;
                }
            }
            final var mover = master.getDataMoverInterface(server.getName());
            final long removed = mover == null ? -2 : mover.dropStorageReplica(file);
            if (removed >= 0) {
                store.removed(file, decision.policies(), removed, now);
                reclaimable = Math.max(0, reclaimable - removed);
                LOG.info("Storage lifecycle removed replica DataFile {} instance={} mover={} policies={} bytes={}", id,
                        file.getFileInstance(), server.getName(), decision.policies(), removed);
                for (final var transfer : snapshot.transfers()) {
                    final var history = new DataTransfer(transfer.id());
                    history.setDestinationName(transfer.destination());
                    history.setStatusCode(transfer.status());
                    master.addTransferHistory(history, transfer.status(),
                            "Replica dropped by storage policies " + String.join(", ", decision.policies()) + " on "
                                    + server.getName() + "; originals retained");
                }
            } else if (removed != -1) {
                complete = false;
            }
        }
        save(snapshot, decision, complete ? "ORIGINAL_ONLY" : "PARTIAL",
                complete ? "Only original copies retained; requeue and downloads remain available"
                        : "Some replicas retained: DataMover unavailable or file in use; will retry",
                reclaimable, now);
        estimate(decision, reclaimable > 0 ? 1 : 0, reclaimable);
        if (complete) {
            store.resetReplication(id);
            final List<Object> ids = snapshot.transfers().stream().map(t -> (Object) t.id()).toList();
            master.getECpdsBase().clearCache(DataTransfer.class, ids);
        }
    }

    private void estimate(final StorageRetention.Decision decision, final long candidates, final long bytes) {
        for (final var policy : decision.policies()) {
            final var stats = scan.computeIfAbsent(policy, ignored -> new long[2]);
            stats[0] += candidates;
            stats[1] = Math.addExact(stats[1], bytes);
        }
    }

    private void save(final Snapshot snapshot, final StorageRetention.Decision decision, final String state,
            final String message, final long bytes, final long now) {
        store.save(snapshot.file(), state, message, decision.policies(), decision.eligibleAt(),
                decision.guaranteedUntil(), bytes, now);
    }

    private synchronized boolean underPressure(final StoragePolicy policy, final String group, final Integer volume) {
        if (policy.getHighWatermark() == 0 || group == null || volume == null || volume < 0) {
            return false;
        }
        int highest = -1;
        for (final var server : master.getECpdsBase().getTransferServers(group)) {
            if (master.getDataMoverInterface(server.getName()) == null) {
                continue;
            }
            final var usage = master.getStorageVolumeUsage(server.getName());
            if (usage != null && usage.length == 2 && volume < usage[0].length && volume < usage[1].length
                    && usage[1][volume] > 0) {
                highest = Math.max(highest, (int) Math.min(100, 100.0 * usage[0][volume] / usage[1][volume]));
            }
        }
        final var key = policy.getName() + "/" + group + "/" + volume;
        final boolean active = highest >= 0 && (pressure.getOrDefault(key, false) ? highest > policy.getLowWatermark()
                : highest >= policy.getHighWatermark());
        pressure.put(key, active);
        return active;
    }

    /** Any eager/expiry-only policy, operator hold or unhealthy original defeats deferral. */
    public boolean shouldReplicate(final DataTransfer transfer) {
        try {
            return shouldReplicateChecked(transfer);
        } catch (final Exception e) {
            LOG.warn("Storage policy deferral unavailable for DataTransfer {}; retaining normal replication",
                    transfer.getId(), e);
            return true;
        }
    }

    private boolean shouldReplicateChecked(final DataTransfer transfer) throws Exception {
        if (!enabled()) {
            return true;
        }
        final var policies = policies();
        if (policies.stream().noneMatch(policy -> policy.isActive() && policy.getMode() == StoragePolicy.Mode.ENFORCE
                && policy.getLazyReplicationMs() > 0)) {
            return true;
        }
        final var snapshot = snapshot(transfer.getDataFileId());
        final long now = System.currentTimeMillis();
        final var state = store.state(transfer.getDataFileId());
        if (Long.parseLong(state.getOrDefault("SLC_PIN_UNTIL", "0")) > now) {
            return true;
        }
        long delay = Long.MAX_VALUE;
        for (final var related : snapshot.transfers()) {
            if (related.expiry() <= now) {
                continue;
            }
            final var setup = DESTINATION_SCHEDULER.getECtransSetup(related.configuration());
            final var policy = StoragePolicy
                    .select(policies, new StoragePolicy.Subject(related.destination(), related.type(), related.group(),
                            related.target(), setup.getString(DESTINATION_SCHEDULER_STORAGEPOLICY)))
                    .policy();
            if (!policy.isActive() || policy.getMode() != StoragePolicy.Mode.ENFORCE
                    || policy.getLazyReplicationMs() == 0 || related.original() == null
                    || master.getECpdsBase().getTransferServer(related.original()) == null
                    || !master.getECpdsBase().getTransferServer(related.original()).getActive()
                    || !hasReplica(related.original(), snapshot.file())) {
                return true;
            }
            delay = Math.min(delay, policy.getLazyReplicationMs());
        }
        if (delay == Long.MAX_VALUE || snapshot.file().getArrivedTime() == null) {
            return true;
        }
        return now >= StoragePolicy.addDuration(snapshot.file().getArrivedTime().getTime(), delay);
    }

    public List<ecmwf.common.database.TransferServer> retrievalOrder(final DataTransfer transfer,
            final List<ecmwf.common.database.TransferServer> servers) {
        if (!enabled()) {
            return servers;
        }
        try {
            policies();
            final var state = store.state(transfer.getDataFileId());
            if ("ORIGINAL_ONLY".equals(state.get("SLC_STATE"))
                    && Integer.parseInt(state.getOrDefault("SLC_INSTANCE", "-1")) == StorageLifecycleStore
                            .instance(transfer.getDataFile())) {
                final var ordered = new ArrayList<>(servers);
                final var original = transfer.getOriginalTransferServerName();
                ordered.sort(java.util.Comparator.comparingInt(server -> server.getName().equals(original) ? 0 : 1));
                return ordered;
            }
        } catch (final Exception e) {
            LOG.warn("Lifecycle retrieval preference unavailable for DataTransfer {}; using normal source selection",
                    transfer.getId(), e);
        }
        return servers;
    }

    public String replicationSource(final DataTransfer transfer, final String preferred) {
        if (!enabled()) {
            return preferred;
        }
        final var file = transfer.getDataFile();
        try {
            final var original = master.getECpdsBase().getTransferServer(preferred);
            if (original != null && original.getActive() && hasReplica(preferred, file)) {
                return preferred;
            }
            for (final var server : master.getECpdsBase().getTransferServers(file.getTransferGroupName())) {
                if (server.getActive() && hasReplica(server.getName(), file)) {
                    return server.getName();
                }
            }
            // A connected mover may still retrieve the file from a configured acquisition/backup source.
            for (final var server : master.getECpdsBase().getTransferServers(file.getTransferGroupName())) {
                if (server.getActive() && master.getDataMoverInterface(server.getName()) != null) {
                    return server.getName();
                }
            }
        } catch (final Exception e) {
            LOG.warn("Cannot choose an alternative replication source for DataTransfer {}", transfer.getId(), e);
        }
        return preferred;
    }

    private boolean hasReplica(final String moverName, final DataFile file) {
        final var mover = master.getDataMoverInterface(moverName);
        if (mover == null) {
            return false;
        }
        try {
            return mover.inspectStorageReplica(file) >= file.getSize();
        } catch (final RemoteException e) {
            LOG.warn("Cannot verify replication source {} for DataFile {}; trying other sources", moverName,
                    file.getId(), e);
            return false;
        }
    }

    public Map<String, String> describe(final long id) throws Exception {
        final var result = new LinkedHashMap<String, String>();
        result.put("enabled", String.valueOf(enabled()));
        if (!enabled()) {
            result.put("SLC_STATE", "DISABLED");
            result.put("SLC_MESSAGE", "Storage lifecycle is disabled; existing expiry behaviour applies.");
            return result;
        }
        policies();
        result.putAll(store.state(id));
        final var snapshot = snapshot(id);
        final long now = System.currentTimeMillis();
        final var decision = StorageRetention.evaluate(policies, snapshot, now, policy -> underPressure(policy,
                snapshot.file().getTransferGroupName(), snapshot.file().getFileSystem()));
        if (Integer.parseInt(result.getOrDefault("SLC_INSTANCE", "-1")) != StorageLifecycleStore
                .instance(snapshot.file())) {
            result.put("SLC_STATE", "NOT_ASSESSED");
            result.put("SLC_MESSAGE", "This file instance has not been assessed yet.");
        }
        result.putIfAbsent("SLC_STATE", "NOT_ASSESSED");
        if (Long.parseLong(result.getOrDefault("SLC_PIN_UNTIL", "0")) > now) {
            result.put("SLC_STATE", "PINNED");
            result.put("SLC_MESSAGE", "Operator hold: " + result.get("SLC_PIN_REASON"));
        } else if ("PINNED".equals(result.get("SLC_STATE"))) {
            result.put("SLC_STATE", "NOT_ASSESSED");
            result.put("SLC_MESSAGE", "Hold ended; awaiting next assessment.");
        }
        result.put("SLC_POLICIES", String.join(", ", decision.policies()));
        result.put("SLC_GUARANTEED_UNTIL", String.valueOf(decision.guaranteedUntil()));
        result.put("SLC_ELIGIBLE_AT", String.valueOf(decision.eligibleAt()));
        return result;
    }

    public void pin(final long id, final long until, final String reason, final String user) throws Exception {
        if (!enabled()) {
            throw new IllegalStateException("Enable StoragePolicy before changing holds");
        }
        policies();
        try (final var lease = StorageFileLocks.read(id)) {
            final var snapshot = snapshot(id);
            final long now = System.currentTimeMillis();
            final long expiry = snapshot.transfers().stream().mapToLong(t -> t.expiry()).max().orElse(0);
            final long effectiveUntil = until == -1 ? expiry : until;
            if (until != 0 && (effectiveUntil <= now || effectiveUntil > expiry)) {
                throw new IllegalArgumentException(
                        "Hold date must be in the future and no later than the last transfer expiry");
            }
            if (reason == null || reason.length() > 512 || user == null || user.length() > 64) {
                throw new IllegalArgumentException("Invalid hold reason or operator identity");
            }
            store.pin(snapshot.file(), effectiveUntil, reason, user);
            for (final var transfer : snapshot.transfers()) {
                final var history = new DataTransfer(transfer.id());
                history.setDestinationName(transfer.destination());
                history.setStatusCode(transfer.status());
                master.addTransferHistory(history, transfer.status(), effectiveUntil == 0
                        ? "Storage hold removed by " + user
                        : "Storage hold until " + new Timestamp(effectiveUntil) + " by " + user + ": " + reason);
            }
            LOG.info("Storage hold DataFile {} until={} by={} reason={}", id, effectiveUntil, user, reason);
        }
    }

    public Map<String, Map<String, String>> report() throws Exception {
        if (!enabled()) {
            return Map.of();
        }
        policies();
        final var report = new TreeMap<String, Map<String, String>>();
        final var persisted = store.metrics();
        for (final var policy : policies) {
            final var stats = completedScan.getOrDefault(policy.getName(), new long[2]);
            final var row = new LinkedHashMap<>(persisted.getOrDefault(policy.getName(), Map.of()));
            row.putIfAbsent("reclaimedBytes", "0");
            row.putIfAbsent("replicasDropped", "0");
            row.putIfAbsent("lastApplied", "0");
            row.put("candidates", String.valueOf(stats[0]));
            row.put("reclaimableBytes", String.valueOf(stats[1]));
            row.put("lastScan", String.valueOf(lastScan));
            report.put(policy.getName(), row);
        }
        return report;
    }

    @Override
    public String getActivity() {
        return "enabled=" + enabled() + ",ready=" + ready + ",cursor=" + cursor + ",lastCompleteScan=" + lastScan
                + ",errors=" + errors.get() + (error.isEmpty() ? "" : ",error=" + error);
    }

    @Override
    public MBeanInfo getMBeanInfo() {
        return MBeanManager.addMBeanInfo(super.getMBeanInfo(), "Storage lifecycle: original copies are never released.",
                new MBeanAttributeInfo[] {
                        new MBeanAttributeInfo("Enabled", "java.lang.Boolean", "Lifecycle kill switch", true, false,
                                false),
                        new MBeanAttributeInfo("Errors", "java.lang.Long", "Failed evaluations", true, false, false),
                        new MBeanAttributeInfo("LastCompleteScan", "java.lang.Long", "Last completed scan (epoch ms)",
                                true, false, false),
                        new MBeanAttributeInfo("PolicyMetrics", TabularData.class.getName(),
                                "Per-policy metrics; shared files appear under each policy", true, false, false) },
                new MBeanOperationInfo[0]);
    }

    @Override
    public Object getAttribute(final String name) throws AttributeNotFoundException, MBeanException {
        return switch (name) {
        case "Enabled" -> enabled();
        case "Errors" -> errors.get();
        case "LastCompleteScan" -> lastScan;
        case "PolicyMetrics" -> metricsTable();
        default -> super.getAttribute(name);
        };
    }

    private TabularData metricsTable() throws MBeanException {
        try {
            final String[] columns = { "policy", "candidates", "reclaimableBytes", "reclaimedBytes", "replicasDropped",
                    "lastApplied" };
            final OpenType<?>[] types = { SimpleType.STRING, SimpleType.LONG, SimpleType.LONG, SimpleType.LONG,
                    SimpleType.LONG, SimpleType.LONG };
            final var rowType = new CompositeType("StoragePolicyMetrics", "Storage policy metrics", columns, columns,
                    types);
            final var table = new TabularDataSupport(
                    new TabularType("StoragePolicies", "Storage policy metrics", rowType, new String[] { "policy" }));
            for (final var entry : report().entrySet()) {
                final Map<String, Object> row = new HashMap<>();
                row.put("policy", entry.getKey());
                for (int i = 1; i < columns.length; i++) {
                    row.put(columns[i], Long.parseLong(entry.getValue().get(columns[i])));
                }
                table.put(new CompositeDataSupport(rowType, row));
            }
            return table;
        } catch (final Exception e) {
            throw new MBeanException(e);
        }
    }
}
