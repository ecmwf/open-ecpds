# Purge All Data

!!! danger "Irreversible destructive operation"
    **Purge All Data** permanently deletes every transfer record, data file, and transfer history entry across all destinations and all Data Movers. There is no undo. Use only when performing a complete system reset.

The **Purge All Data** action is available under **Administration → Purge All Data** (`/do/admin/purge`) in the Monitoring UI.

---

## Confirmation Flow

The action is intentionally protected by a two-step confirmation to prevent accidental execution.

### Step 1 — Acknowledge the warning

Read the warning panel, then tick:

> *I understand that this will permanently delete all data from the database and from all data-mover disks, and that this action cannot be reversed.*

Click **Proceed to final confirmation** to advance to step 2. The button is disabled until the checkbox is ticked.

### Step 2 — Final confirmation

Three controls are presented:

| Control | Required | Description |
|---|---|---|
| **Confirmation phrase** | Yes | Type `PURGE ALL DATA` exactly (case-sensitive) to enable the submit button. |
| **Critical Password** | Only if set | If a [Critical Password](critical-password.md) has been configured, it must be supplied here. If no password has been set yet, this field is not shown — configure one first. |
| **Trigger cleanup immediately** | Optional | Wakes the purge and file-expiry schedulers immediately rather than waiting for their next scheduled cycle. Recommended in most cases. |
| **Also hard-delete all database records immediately** | Optional | See [Immediate database hard-delete](#immediate-database-hard-delete) below. |

Click **Confirm — Delete All Data** to execute.

---

## What Gets Deleted

| Data | Mechanism |
|---|---|
| All transfer requests and registered data files | Marked for deletion and picked up by the expiry schedulers |
| Physical files on Data Movers | Removed during the next (or immediate) mover purge scan |
| Transfer history, statistics, publications | Removed during the [immediate hard-delete](#immediate-database-hard-delete) if that option is selected |

Transfers are marked for deletion first; the **ExpiredDataFileScheduler** and **PurgeScheduler** then handle physical removal of files from each Data Mover's disk.

---

## Immediate Database Hard-Delete

When **Also hard-delete all database records immediately** is ticked, OpenECPDS
immediately removes transfer, history, statistics, publication and file metadata records.

After the database records are removed, OpenECPDS immediately triggers a **full disk scan** on every connected Data Mover via the `purgeAll()` RMI call. Because the mover scan works by comparing files on disk against entries in the database, any file no longer referenced in the database is deleted at this point — without waiting for the next scheduled scan cycle.

!!! note "This runs in the background"
    With large datasets (millions of transfers) the delete loop can take anywhere from a few minutes to several hours. The operation runs in a background thread so the browser does not time out. You can safely leave the page; progress is visible in the MasterServer log.

---

## After the Purge

Once the purge completes:

- All destination queues are empty.
- No files remain on any Data Mover's disk (subject to mover scan completion).
- The database contains no transfer, history, or data-file records.
- Destinations, hosts, and all other configuration remain intact.

The system is ready to accept new transfers immediately.

---

!!! info
    To reduce redundant Data Mover replicas before normal expiry, see
    [Storage Lifecycle Policies](storage-lifecycle-policies.md). Lifecycle holds never
    extend expiry or block this explicit purge operation.

## Prerequisites

- You must have **administrator** access to the Monitoring UI.
- A [Critical Password](critical-password.md) should be configured. If none is set, a warning banner is shown on the Administration page and the form will not ask for it — but it is strongly recommended to set one before using this feature in a production environment.
