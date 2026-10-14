# Monitor UI Overview

The OpenECPDS Monitor is a web-based management interface for operators and administrators.
It provides real-time visibility into destinations, hosts, data files, transfer history,
and system health, as well as management of users and configuration.

!!! note
	This is only a limited overview of the Monitoring UI and its capabilities. The documentation will be expanded soon to cover all available pages, using real-world examples and up-to-date screenshots to provide a more comprehensive view of the application's features and the information it can present.

Three accounts are pre-configured in the standalone container:

| Account | Password | Role |
|---|---|---|
| `admin` | `admin2021` | Full administrator — all destinations, all management operations |
| `monitor` | `monitor2021` | Restricted user — limited destinations, read-only monitoring |
| `operator` | `operator2021` | Operator — all destinations, operational views, no administration |

These accounts demonstrate the three typical access profiles. Access is controlled by
*categories* assigned to each web user (see **Access Control**), so any real deployment can
define its own variants.

| Profile | Account | Categories | What the user sees |
|---|---|---|---|
| **End user** | `monitor` | `mstate`, `monitoring`, `transfers`, `hourly_aq operations` | Only the destination(s) granted to the user (here `hourly_aq`), with their own transfers and status. No **Operations** section, no administration. |
| **Operator** | `operator` | `operator`, `administrator`, `monitoring`, `transfer info`, `transfers`, `operations` | **All destinations**, the **Operations** section (Dissemination, Acquisition, Live Earth, System Topology, Delay Notification) and transfer information, but none of the data-file, transfer-admin or system administration pages. |
| **Administrator** | `admin` | all of the above plus `admin`, `datafile`, `transfer admin`, `mstate` | Everything, including managing destinations, hosts, users, categories, resources and the global configuration of the application. |

Destination access is granted by per-destination categories (for example `hourly_aq operations`)
or by the global `operations` category, which gives access to all destinations. The
**Operations** section requires both `monitoring` and `transfer info`.

The monitor is accessible at `https://<host>:8443`.

!!! note
    All screenshots below were captured from the standalone container using the `admin` account
    unless otherwise noted.



## Navigation structure

The main navigation bar divides the interface into five sections:

| Section | Purpose |
|---|---|
| **Monitoring** | Real-time dashboard and transfer status overview |
| **Transfers** | Destinations, hosts, transfer history, methods, and modules |
| **Data Files** | Data file inventory, transfer groups, movers, and traffic stats |
| **Users** | Data portal users, web users, categories, policies, and resources |
| **Admin** | System-wide tools: metadata, requeue, upload, and audit |

## Session expiry

Across the Monitor UI, an asynchronous request that discovers an expired session
reloads the current page through the normal login flow, returning to that page
after authentication. This covers both Fetch and XMLHttpRequest (including jQuery
AJAX). Permission denials and genuine network/server errors do not trigger this
reload. Unsaved edits are not preserved.
