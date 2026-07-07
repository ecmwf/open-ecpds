# DataMover Allocation & Load Balancing

When OpenECPDS needs to store or forward a file, it must select:

1. A **Transfer Group** — a logical cluster of DataMovers sharing the same storage pool.
2. A **storage volume** (filesystem index) within that group.
3. An ordered list of **DataMover servers** to try, from most to least preferred.

This process is handled by the `TransferServerProvider` class. Each provider instance
resolves these three choices once at construction time and is immutable thereafter.

---

## Transfer Group Resolution

The group is resolved through a fixed priority chain:

| Priority | Source | Detail |
|----------|--------|--------|
| 1 | **Explicit group name** | Passed directly by the caller — for example from the `scheduler.transfergroup` option in a Destination's properties. Looked up by name; throws if not found. |
| 2a | **Destination's dissemination host** | If no explicit name is given but a destination name is provided, the Transfer Group is taken from the first active dissemination host associated with that destination. |
| 2b | **Destination's own Transfer Group** | If the destination has no dissemination hosts, its own `transferGroupName` field (set in the UI) is used instead. |
| 3 | **Global default** | Falls back to `Server.defaultTransferGroup` from the system configuration. Throws if the entry is missing or refers to a nonexistent group. |

!!! note "UI Transfer Group field vs. `scheduler.transfergroup`"
    The Transfer Group dropdown in the Destination edit form updates the destination's own
    `transferGroupName` FK (priority 2b). If the destination has dissemination hosts, their
    own Transfer Group setting (priority 2a) takes precedence. The `scheduler.transfergroup`
    property in the Destination's options text (priority 1) overrides everything. Keep all
    three in sync after changes.

---

## Cluster-Level Weighted Round Robin (WRR)

Transfer Groups can be grouped into named **clusters**. When a group belongs to a cluster
and no explicit filesystem index was pre-allocated, the provider may switch to a different
group in the same cluster using **weighted round-robin (WRR)** scheduling.

### Eligibility

A group is a WRR candidate if:

- It belongs to the same cluster as the resolved group (`clusterName` field).
- Its `clusterWeight` is strictly positive.
- It is currently **available**: marked active and has at least one active, reachable DataMover.

### Scheduling

Candidates are sorted alphabetically to ensure a stable order independent of database
return ordering. An atomic counter is incremented per allocation; the counter slot maps into
the weighted sum of candidates to select the next group.

When the set of eligible candidates changes (groups are enabled/disabled or weights change),
the counter resets to avoid skew from stale rotation state.

### When Fallback is Triggered

Cluster fallback occurs when:

- **No filesystem was pre-allocated** (`allocatedFileSystem = null`) — the scheduler
  deliberately allows load-balancing across cluster peers.
- The resolved group is **not currently available** — it is inactive or all its DataMovers
  are unreachable.

If the resolved group has no cluster name, or no eligible peer exists, the original group
is returned unchanged.

---

## Graceful Group Fallback

If, after cluster WRR, the selected group is still unavailable **and** an explicit group
name was provided, the provider attempts one further fallback: it looks up the destination's
own `transferGroupName` FK directly (bypassing any dissemination-host lookup).

This covers the common operational scenario where an operator has updated the Transfer Group
in the Destination edit form, but the scheduler option or dissemination host has not yet
been updated:

```
Warning: TransferGroup group-old not available for destination DEST_NAME;
         falling back to destination's configured TransferGroup group-new
         — update scheduler.transfergroup in the Destination properties
         to remove this warning
```

The warning is a reminder to resolve the underlying inconsistency.

---

## Volume (Filesystem) Allocation

Each Transfer Group has one or more **storage volumes** (indexed from 0). A volume index
is selected once per provider instance and used consistently for all operations with that
instance.

### Pre-allocated Index

If the caller supplies an explicit `allocatedFileSystem` value (e.g., during a retry of
an existing transfer), that index is used directly and volume allocation is skipped.

### Weighted Allocator

When no explicit index is given, the `WeightedAllocator` selects a volume:

| Mode | Trigger | Behaviour |
|------|---------|-----------|
| **Uniform random** | No usage statistics available yet | Volume index chosen uniformly at random. Used at startup before the background updater has run. |
| **Uniform random** | Volumes are roughly balanced (CV ≤ threshold) | Treats all volumes as equivalent; avoids artificially skewing allocation when differences are insignificant. |
| **Free-space weighted** | Normal operation | Volume selection probability is proportional to remaining free space. Volumes with more free space are more likely to be chosen. |
| **Size-aware weighted** | Caller provides `fileSize > 0` | Same as free-space weighted, but each volume's effective free space is penalised by `α × fileSize` before computing probabilities, where `α = fileSizePenaltyFactor`. This discourages placing large files on nearly-full volumes. |

The coefficient-of-variation (CV) test applies in both the standard and size-aware modes:
if the standard deviation of volume weights divided by their mean falls below
`cvThreshold`, uniform selection is used even when statistics are available.

#### Size-aware penalty formula

```
effective_free_space[i] = max(1, free_space[i] - min(free_space[i], α × fileSize))
```

Setting `fileSizePenaltyFactor = 0` disables the penalty entirely.
Setting `fileSizePenaltyFactor = 1` (the default) subtracts the full file size from each
volume's free space before selection.

---

## Background Volume Updater

Volume usage statistics are refreshed by a background daemon thread. On each tick:

1. All Transfer Groups in the database are iterated.
2. Groups with no available DataMover are skipped.
3. For each remaining group, all DataMovers are contacted once via RMI to retrieve
   per-volume used/total bytes and per-volume mover ordering.
4. The `WeightedAllocator` statistics and the `VOLUME_USAGE_CACHE` are updated atomically.

Group updates within a single tick run concurrently (thread pool capped at 16), but
per-volume calls within a single group remain sequential to avoid overwhelming the RMI
layer.

The update frequency and other tuning parameters are described in the
[Configuration Parameters](#configuration-parameters) section below.

---

## DataMover Server Ordering

Within the selected group and volume, active DataMover servers are ordered by a
**least-activity** policy computed once per provider construction:

### Pipeline

| Step | Description |
|------|-------------|
| 1 | Fetch declared servers for the group from the database. |
| 2 | Compute a **rotation offset**: a stable hash of `(caller, groupName, runtime-salt)` combined with a per-group sliding round-robin counter. This spreads successive allocations across servers while keeping the rotation stable within a JVM lifetime. |
| 3 | **Filter** servers: only servers that are both active and have a reachable `DataMover` interface are retained. |
| 4 | For each active server, read its current **download count** on the selected volume (`TransferScheduler.getNumberOfDownloadsFor`). |
| 5 | **Sort** ascending by download count (least loaded first). Ties are broken by rotation order from step 2. |
| 6 | If a **preferred server** was provided (e.g., to retry the same mover), it is reinserted at position 0 regardless of load, as long as it is active and reachable. |

### Access methods

| Method | Returns |
|--------|---------|
| `getTransferServersByLeastActivity()` | Servers ordered by the least-activity pipeline above (computed at construction). |
| `getTransferServersByMostFreeSpace()` | Servers re-ordered by descending free space on the selected volume, using the background-updated cache. Falls back to a shuffled copy of the least-activity list when no cache entry is available. |

Callers that want to retry on failure iterate the returned list from the beginning,
attempting each server in order until one succeeds.

---

## Configuration Parameters

All parameters are in the `[TransferServerProvider]` configuration section unless
otherwise noted.

| Parameter | Default | Description |
|-----------|---------|-------------|
| `cvThreshold` | `0.03` | Coefficient-of-variation threshold. When the CV of volume weights falls below this value, uniform random selection is used instead of weighted selection. |
| `fileSizePenaltyFactor` | `1.0` | Penalty multiplier `α` for size-aware volume allocation. Set to `0` to disable size-awareness. |
| `usageUpdaterFreqInSec` | `2` | How often (in seconds) the background daemon refreshes volume usage from the DataMovers. |
| `startUsageUpdater` | `true` | Whether to start the background usage updater at all. Set to `false` to disable dynamic allocation (useful in testing). |
| `debug` | `false` | When `true` (and log level is DEBUG), emits detailed per-allocation rotation and weight diagnostics. Produces significant output at scale; leave disabled in production. |
| `Server.defaultTransferGroup` | _(required)_ | Name of the Transfer Group to use when no explicit group or destination resolves one. Must reference a valid Transfer Group in the database. |

---

## Summary: Selection Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                TransferServerProvider construction              │
├─────────────────────────────────────────────────────────────────┤
│ 1. Resolve TransferGroup                                        │
│      a. Explicit groupName (scheduler.transfergroup) ?          │
│      b. Destination's dissemination host's group ?              │
│      c. Destination's own transferGroupName ?                   │
│      d. Server.defaultTransferGroup                             │
│                                                                 │
│ 2. Cluster WRR fallback                                         │
│      (if allocatedFileSystem=null OR group unavailable)         │
│      → select peer group within cluster by weighted RR          │
│                                                                 │
│ 3. Graceful group fallback                                       │
│      (if still unavailable AND explicit groupName was given)    │
│      → try destination's own transferGroupName directly         │
│                                                                 │
│ 4. Allocate volume                                              │
│      a. Explicit allocatedFileSystem → use as-is               │
│      b. fileSize > 0 → size-aware WeightedAllocator             │
│      c. otherwise   → free-space WeightedAllocator              │
│      (both b/c fall back to uniform random if CV ≤ threshold)  │
│                                                                 │
│ 5. Order DataMovers                                             │
│      Least-downloads on selected volume, rotation tie-breaker,  │
│      preferred server reinserted at position 0 if active        │
└─────────────────────────────────────────────────────────────────┘
```

---

## Related

- [Components](components.md)
- [Failover Mechanism](failover.md)
- [OpenECPDS Entities](../concepts/entities.md)
- [Destination Options](../concepts/destination-options.md)
