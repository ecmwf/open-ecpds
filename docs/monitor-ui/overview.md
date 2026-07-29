# Monitor UI Overview

The OpenECPDS Monitor is a web-based management interface for operators and administrators.
It provides real-time visibility into destinations, hosts, data files, transfer history,
and system health, as well as management of users and configuration.

!!! note
	This is only a limited overview of the Monitoring UI and its capabilities. The documentation will be expanded soon to cover all available pages, using real-world examples and up-to-date screenshots to provide a more comprehensive view of the application's features and the information it can present.

Two accounts are pre-configured in the standalone container:

| Account | Password | Role |
|---|---|---|
| `admin` | `admin2021` | Full administrator — all destinations, all management operations |
| `monitor` | `monitor2021` | Restricted user — limited destinations, read-only monitoring |

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
