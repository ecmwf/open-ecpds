# Storage Lifecycle Policies

Storage Lifecycle Policies decide how long data files stay on the Data Movers, per
destination, destination type, transfer group or file name. They are managed at
`/do/admin/storagepolicies` (Administration → **Storage Lifecycle Policies**).

!!! note "Stage 1: definition only"
    This release only lets you define, order and test policies. **No file is removed or
    kept differently yet**: file expiry still follows the transfer lifetime. Later stages
    add dry-run reporting and then enforcement (dropping replicas).

## Principles

- A policy can only **shorten** retention. A file is never kept beyond the transfer
  lifetime: effective retention is `min(requeue window, expiry)`.
- The original copy is kept for the **requeue window** (default 7 days) so end users can
  ask for a re-queue after an issue.
- A file with a transfer that is still runnable is never removed.
- A built-in `default` policy, shown as **EXPIRY**, applies when nothing else matches: files are kept until the transfer expiry and nothing is removed early.

## Policy fields

| Field | Description |
|-------|-------------|
| Name | Unique name (max 32 characters). |
| Mode | `DRY_RUN` (evaluate and report only, default), `ENFORCE` or `DISABLED`. |
| Active | Inactive policies are ignored. |
| Destinations | Names, with `*` and `?` wildcards. |
| Destination types / Transfer groups | Multi-selects. |
| File name pattern | Wildcards `*` and `?` (e.g. `*.grib2`); prefix with `regex:` for a regular expression. |
| Requeue window | Minimum time the original is kept (default 7 days). |
| Restart on requeue | Restart the window when the transfer is re-queued (default off). |
| Drop replicas / after | Keep only the original copy, after the given delay. |

All entered criteria must match; empty criteria match anything.

## Selection

1. A destination option `scheduler.storagePolicy = "<name>"` always wins.
2. Otherwise the first active, non-disabled policy by **order** whose criteria match.
3. Otherwise the built-in `default`.

Use the up/down arrows to change the order, and **Which policy applies?** to test a
destination and file name.
