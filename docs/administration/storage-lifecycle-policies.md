# Storage Lifecycle Policies

Storage Lifecycle Policies reduce redundant Data Mover copies while preserving
downloads and requeue, per destination, destination type, transfer group or file name. They are managed at
`/do/admin/storagepolicies` (Administration → **Storage Lifecycle Policies**).

!!! warning "Opt-in enforcement"
    The feature defaults to **disabled**. With the required database tables and matching
    Master, Monitor and Mover components in place, set `[StoragePolicy] enabled=yes` on the Master.
    **Existing `ENFORCE` policies take effect immediately when enabled.**
    Use `DRY_RUN` policies first if you want estimates without deletion.
    The original is **never released early**. Normal transfer expiry and explicit purge remain unchanged.

## Principles

- A policy can only **shorten** retention. The requeue guarantee is capped by
  transfer expiry; normal expiry wins even when a policy window has not elapsed.
- Replicas are kept until **both** the requeue window (default 7 days) and the replica
  delay have elapsed. The original remains until normal expiry, including after the window.
- All live transfers sharing a file must allow reduction. A `Keep all`, inactive,
  disabled or expiry-only policy vetoes it; any `DRY_RUN` match prevents enforcement.
- Pending, failed and stopped transfers keep all existing copies. Active uploads,
  downloads, filtering, replication, backup, proxy and purge work are protected.
- Every known original must be on an active, connected Mover and verified locally
  before each removal. If verification fails, replicas are retained; no automatic
  promotion or deletion of the last copy is attempted.
- A built-in `default` policy, shown as **EXPIRY**, applies when nothing else matches: files are kept until the transfer expiry and nothing is removed early.

## Policy fields

| Field | Description |
|-------|-------------|
| Name | Unique name (max 32 characters). |
| Mode | `DRY_RUN` (evaluate and report only, default), `ENFORCE` or `DISABLED`. |
| Active | Inactive policies are ignored by automatic matching; an explicit inactive policy keeps all copies. |
| Destinations | Names, with `*` and `?` wildcards. |
| Destination types / Transfer groups | Multi-selects. |
| File name pattern | Wildcards `*` and `?` (e.g. `*.grib2`); prefix with `regex:` for a regular expression. |
| Requeue window | Minimum window before replicas become eligible for removal (default 7 days). Originals remain until expiry. |
| Restart on requeue | Off: window starts at first successful completion. On: use the latest successful completion following requeue. No replicas are removed while a transfer is pending. |
| Drop replicas / after | Remove non-original copies only after both the window and the delay from latest completion. |
| Minimum replica retention | Additional floor measured from file arrival, in hours. |
| Extra grace | Additional delay after all retention conditions have been met, in hours. |
| High / low watermarks | Both zero disables pressure handling. Otherwise `0 < low < high <= 100`. High starts pressure mode; low ends it. |
| Lazy replication delay | `ENFORCE` only, measured from arrival. Zero retains eager replication. An eager policy, hold, or unhealthy original bypasses deferral. |

All entered criteria must match; empty criteria match anything.

## Selection

1. A destination option `scheduler.storagePolicy = "<name>"` always wins.
2. Otherwise the first active, non-disabled policy by **order** whose criteria match.
3. Otherwise the built-in `default`.

Use the up/down arrows to change the order, and **Which policy applies?** to test a
destination and file name.

An explicit reference to a missing policy is an error and blocks lifecycle evaluation,
not permission to fall through to a less protective policy. Invalid policy rows also
suspend evaluation. Reassign destinations before deleting a referenced policy.

## Holds and file visibility

The **Storage lifecycle** card on file and transfer details shows the most recent
assessment, selected policies, requeue-window date and replica-removal eligibility.
The **Manage storage hold** link opens the administrator's file-status tool.
Enter an optional reason and UTC end date; leaving the date blank means the last
transfer expiry. Remove the hold explicitly or let it expire.

A hold protects all **existing** copies against policy reduction. It does not restore
previously removed replicas, extend transfer lifetime, or block explicit deletion.
Pins are persisted across service restarts and replacement instances of the same file.

`ORIGINAL_ONLY` means the assessment found no remaining non-original copies on the
configured Movers; it is not a continuously refreshed inventory. Downloads prefer
the original after reduction. Requeue resets replication bookkeeping so normal
replication can run again, subject to any opted-in lazy delay.
On-demand access may recreate a replica, which a later assessment can remove again.

## Pressure and lazy replication

Pressure uses fresh per-volume usage for connected Movers in the file's transfer
group. It skips **only extra grace**: it never shortens the requeue window, replica
delay or minimum retention, and never overrides a hold or active reader.
Unknown/stale usage does not activate pressure mode.

Lazy replication is an explicit redundancy trade-off: fast transfers can finish
before an additional copy is made. The shortest delay across all related policies
wins, and any eager/expiry-only policy retains eager replication. An unavailable or
unverifiable original bypasses deferral; a connected surviving replica, or a Mover
able to retrieve from a configured source, is selected for recovery. No policy can
recover a file when its only copy and all re-fetch sources are unavailable.

## Reports and metrics

The administration page shows eligible-file/reclaimable-byte estimates from the
last complete scan, persistent reclaimed-byte and removed-replica counters, and last
application times. Shared files appear under each applicable policy, so per-policy
rows **must not be summed** for a global total. Unavailable or busy Movers make
estimates partial. Deletion followed by a crash before accounting is committed can
undercount savings; counters are operational measurements, not a billing ledger.

The `ECaccess:service=StorageLifecycleScheduler` JMX MBean exposes `Enabled`,
`Errors`, `LastCompleteScan` and tabular `PolicyMetrics`. The bundled Prometheus JMX
exporter can expose these numeric metrics; see [JMX export](../monitoring/jmx-export.md).
Per-file outcomes and holds are persisted, while scan estimates and pressure
hysteresis restart with the Master. History records identify the policy and Mover
for successful replica removals.

## Deployment and sizing

The database must contain `STORAGE_POLICY`, `STORAGE_LIFECYCLE` and
`STORAGE_POLICY_METRIC`. The supplied installation schema includes these tables.
No schema changes are performed automatically by the running service.

```ini
[StoragePolicy]
enabled=no
interval=30s
batchSize=100
stepBudget=10s
maxRelatedTransfers=10000
maxUsageAge=2m
```

The scheduler scans by file ID in bounded batches without loading the entire store.
`stepBudget` limits work between files, not an RPC already in progress. Tune the
batch size and interval for the installation and watch `LastCompleteScan`; estimates
remain explicitly unavailable until a complete scan finishes. A file exceeding
`maxRelatedTransfers` fails closed with an explicit error instead of truncating its
related transfers. Replication uses a bounded cursor while lifecycle is enabled so
deferred files do not hide later eligible files behind a query limit.

Policy edits are refreshed within 30 seconds; an already admitted removal can finish.
The supplied configuration reloads the `StoragePolicy` group automatically. Preserve
`#reload "StoragePolicy"` when adding the group to an existing configuration, or restart
the Master after configuration changes. Enablement, batch/budget limits and usage age
are read dynamically; interval, time ranges and jammed timeout require a restart.

Lifecycle deletion requires Movers with guarded inspection/removal RPCs.
Movers without these capabilities are excluded from deletion. With lifecycle disabled, ordinary
replication, downloads and expiry continue without reading runtime tables.
Set `enabled=no` as the kill switch; any destructive RPC already admitted may finish.
Full release of an unexpired original, automatic original promotion, checksum
deduplication and size/metadata matching extensions are **not included**.
