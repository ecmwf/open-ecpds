# DataMover Allocation & Load Balancing

When OpenECPDS needs to store or forward a file, it must select:

1. A **Transfer Group** — a logical cluster of DataMovers sharing the same storage pool.
2. A **storage volume** (filesystem index) within that group.
3. An ordered list of **DataMover servers** to try, from most to least preferred.

The following sections explain how these choices interact and which settings control
allocation.

---

## Transfer Group Resolution

The group is resolved through a fixed priority chain:

| Priority | Source | Detail |
|----------|--------|--------|
| 1 | **Explicit group name** | For example, the `scheduler.transfergroup` option in a Destination's properties. The name must identify an existing group. |
| 2a | **Destination's dissemination host** | If no explicit name is given but a destination name is provided, the Transfer Group is taken from the first active dissemination host associated with that destination. |
| 2b | **Destination's own Transfer Group** | If the destination has no dissemination hosts, the Transfer Group selected in its edit form is used instead. |
| 3 | **Global default** | Falls back to `Server.defaultTransferGroup` from the system configuration. Allocation fails if this setting is missing or refers to a nonexistent group. |

!!! note "UI Transfer Group field vs. `scheduler.transfergroup`"
    The Transfer Group dropdown in the Destination edit form updates the destination's own
    Transfer Group setting (priority 2b). If the destination has dissemination hosts, their
    own Transfer Group setting (priority 2a) takes precedence. The `scheduler.transfergroup`
    property in the Destination's options text (priority 1) overrides everything. Keep all
    three in sync after changes.

---

## Cluster-Level Weighted Round Robin (WRR)

Transfer Groups can be grouped into named **clusters**. When a group belongs to a cluster
and no storage volume was pre-allocated, allocation may switch to a different
group in the same cluster using **weighted round-robin (WRR)** scheduling.

### Eligibility

A group is a WRR candidate if:

- It belongs to the same cluster as the resolved group.
- Its configured cluster weight is strictly positive.
- It is currently **available**: marked active and has at least one active, reachable DataMover.

### Scheduling

Allocations rotate among eligible groups in proportion to their configured weights.
For example, weights of 2 and 1 give the first group twice as many allocations as the
second over a complete rotation. This balances allocation counts, not transferred bytes.
The rotation adapts when group availability or weights change.

### When Fallback is Triggered

Cluster fallback occurs when:

- **No storage volume was pre-allocated** — the scheduler
  deliberately allows load-balancing across cluster peers.
- The resolved group is **not currently available** — it is inactive or all its DataMovers
  are unreachable.

If the resolved group has no cluster name, or no eligible peer exists, the original group
remains selected; cluster fallback cannot provide an alternative.

---

## Graceful Group Fallback

If, after cluster WRR, the selected group is still unavailable **and** an explicit group
name was provided, allocation attempts one further fallback: the Transfer Group selected
in the destination's edit form, rather than the dissemination host's group.

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

Each Transfer Group has one or more **storage volumes** (indexed from 0). Once selected,
the volume remains consistent for the associated storage operations.

### Pre-allocated Index

If a volume has already been assigned (for example, when retrying an existing transfer),
that volume is reused rather than selecting another.

### Weighted Allocator

When no volume has been assigned, selection follows these rules:

| Mode | Trigger | Behaviour |
|------|---------|-----------|
| **Uniform random** | No usage statistics available yet | Volume index chosen uniformly at random. Used at startup before the background updater has run. |
| **Uniform random** | Volumes are roughly balanced (CV ≤ threshold) | Treats all volumes as equivalent; avoids artificially skewing allocation when differences are insignificant. |
| **Free-space weighted** | Normal operation | Volume selection probability is proportional to remaining free space. Volumes with more free space are more likely to be chosen. |
| **Size-aware weighted** | The file size is known and greater than zero | Same as free-space weighted, but each volume's effective free space is penalised by `α × fileSize` before computing probabilities, where `α = fileSizePenaltyFactor`. This discourages placing large files on nearly-full volumes. |

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

## Volume Usage Refresh

Volume usage statistics are refreshed periodically from available DataMovers.
Groups with no available DataMover cannot provide updated statistics.
Allocation therefore uses the latest collected measurements, rather than a new disk
measurement for every file.

The update frequency and other tuning parameters are described in the
[Configuration Parameters](#configuration-parameters) section below.

---

## DataMover Server Ordering

Within the selected group and volume, DataMover servers are normally ordered by a
**least-activity** policy:

### Pipeline

| Step | Description |
|------|-------------|
| 1 | Consider the servers configured for the selected group. |
| 2 | Retain only active, reachable DataMovers. |
| 3 | Prefer servers with fewer current downloads on the selected volume. |
| 4 | Rotate among equally loaded servers to spread successive allocations. |
| 5 | If a server is preferred (for example, to retry the same mover), try it first regardless of load, provided it is active and reachable. |

### Alternative: Most Free Space

Operations that prioritise free space instead order DataMovers by descending free space
on the selected volume, using the latest collected measurements. If no measurements
are available, the eligible servers are tried in random order.

When an operation supports retrying another DataMover after a failure, servers are
attempted in the selected order until one succeeds.

---

## Configuration Parameters

All parameters are in the `[TransferServerProvider]` configuration section unless
otherwise noted.

| Parameter | Default | Description |
|-----------|---------|-------------|
| `cvThreshold` | `0.03` | Coefficient-of-variation threshold. When the CV of volume weights falls below this value, uniform random selection is used instead of weighted selection. |
| `fileSizePenaltyFactor` | `1.0` | Penalty multiplier `α` for size-aware volume allocation. Set to `0` to disable size-awareness. |
| `usageUpdaterFreqInSec` | `2` | How often (in seconds) volume usage measurements are refreshed from the DataMovers. |
| `startUsageUpdater` | `true` | Whether to refresh volume usage measurements. Leave enabled for allocation to adapt to changing disk usage. |
| `debug` | `false` | When `true` (and log level is DEBUG), emits detailed allocation diagnostics. Produces significant output at scale; leave disabled in production. |
| `Server.defaultTransferGroup` | _(required)_ | Name of the Transfer Group to use when no explicit group or destination resolves one. Must reference a valid Transfer Group in the database. |

---

## Summary: Selection Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                     DataMover allocation                        │
├─────────────────────────────────────────────────────────────────┤
│ 1. Resolve TransferGroup                                        │
│      a. Explicit group (scheduler.transfergroup) ?              │
│      b. Destination's dissemination host's group ?              │
│      c. Destination's own Transfer Group ?                      │
│      d. Server.defaultTransferGroup                             │
│                                                                 │
│ 2. Cluster WRR fallback                                         │
│      (if no volume pre-allocated OR group unavailable)          │
│      → select peer group within cluster by weighted RR          │
│                                                                 │
│ 3. Graceful group fallback                                       │
│      (if still unavailable AND explicit group was given)       │
│      → try destination's own Transfer Group                    │
│                                                                 │
│ 4. Allocate volume                                              │
│      a. Pre-allocated volume → reuse it                         │
│      b. Known positive file size → size-aware selection         │
│      c. Otherwise → free-space weighted selection               │
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
