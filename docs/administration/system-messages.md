# System Messages

**System Messages** (`/do/admin/systemmessages`) lets administrators define time-bounded warning/maintenance banners that are shown automatically to **every** user — no per-user configuration required — on every main Monitor UI menu page (landing page and the Data Files, Data Transfers, Users and Admin sections) and on the Data Portal.

This is the recommended way to announce planned outages, maintenance windows, or other service-wide notices (e.g. *"The service will be unavailable for maintenance from 20:00 to 22:00 UTC"*) without having to manually add and remove a banner before and after the event.

---

## How it works

Each message has:

| Field | Purpose |
|---|---|
| **Message** | The banner text, shown as-is (plain text, line breaks preserved). Focus this on the *reason* for the notice (e.g. "Service unavailable due to a software upgrade") — the Start/End window is shown automatically alongside it, so there is no need to also repeat the schedule in the message itself. |
| **Level** | `info` (blue), `warning` (orange, default) or `danger` (red) — controls the banner's colour and icon. |
| **Start** / **End** | The announced outage/maintenance window. The Start is when the service may become unavailable, so the message is shown **as soon as it is created** — warning users in advance — and remains visible until the End, after which it **disappears automatically**; there is nothing to clean up for it to stop being displayed. |

The **Create**/**Edit** form lets you pick Start and End directly, or use the **Quick duration** shortcuts (`+1h`, `+6h`, `+1d`, `+1 week`) to set the End relative to the Start.

A **Status** column in the list shows, for each message, whether it is currently:

- **Upcoming** — start time is in the future; already shown to users as an advance warning.
- **In progress** — within its start/end window, shown to users.
- **Expired** — end time is in the past; no longer shown, but the entry stays listed here (for history/audit) until explicitly deleted.

---

## Where messages are shown

- **Monitor UI** — just below the introduction card on every main menu page (`/do/start` and the Data Files, Data Transfers, Users and Admin sections), above any other warning banners (e.g. Critical Password / TLS certificate attention), visible regardless of the user's own permissions.
- **Data Portal** — on the portal's main page, using the same visual style as its existing notice/warning banners.

Both surfaces read the same underlying list, so a single message reaches users of either interface without any duplication.

!!! tip "Deleting vs. letting a message expire"
    Deleting a message removes it immediately, even if its End time has not yet been reached. If you simply want a message to stop being shown at a specific time, set its **End** time instead — no manual action will be needed when that time arrives.

---

## Related

- [Monitoring](../monitor-ui/monitoring.md)
- [Data Portal](../use-cases/data-portal.md)
- [Critical Action Password](critical-password.md)
