# Data Users

A **Data User** (internally `IncomingUser`) is an account that grants external clients
access to the [Data Portal](data-portal.md). Data Users are independent of Web Users
(monitoring-interface accounts) and are managed at
`/do/user/incoming` in the monitoring interface.

---

## Core concepts

### What a Data User controls

| Attribute | Purpose |
|---|---|
| **Data Login** | Unique identifier used as the username when connecting (e.g. `myorg_data`) |
| **Portal Service** | How visitors authenticate — see [Portal Service modes](#portal-service-modes) below |
| **Enabled** | Whether the account is active. Disabled users cannot connect |
| **Password / TOTP** | Credentials (Standard Login mode only) |
| **Comment** | Free-text description shown in the monitoring interface |
| **Country** | Country flag shown in the user list |
| **Properties** | Fine-grained `portal.*` ECtrans options — see [Data User Options](data-portal-user-options.md) |
| **Authorised SSH Keys** | Public keys for SFTP key-based authentication |
| **Data Policies** | Groups of permissions that define which Destinations are accessible |
| **Destinations** | Direct destination associations (alternative to using a Data Policy) |

### Data Policies

A **Data Policy** groups one or more Destinations and a set of permitted operations (e.g.
`get`, `put`, `delete`, `dir`). Assigning a Data Policy to a Data User is the recommended
way to grant access to multiple destinations with a consistent permission set.

A Data User can also be associated directly with individual Destinations, which is useful
for one-off or special-case configurations. If a user has neither direct Destination
associations nor any Data Policy covering reachable Destinations, the monitoring interface
flags them as **Unassigned** in the user list.

---

## Portal Service modes

The **Portal Service** field controls how visitors authenticate when they access the data
portal for a given Data User. It replaces the deprecated `portal.anonymous` ECtrans
option and is stored as a first-class column on the `INCOMING_USER` table.

### Standard Login *(default)*

!!! info ""
    :material-lock: **Pre-configured credentials required**

Visitors must authenticate with a username and password (or TOTP passcode) that an
administrator has configured in advance. This is the classic mode suitable for
organisations or individuals who need controlled, audited access.

- Supports **TOTP** (Time-based One-Time Password) two-factor authentication
- Password can be generated automatically via the *Generate* button in the editor
- SSH public-key authentication is supported for SFTP connections via *Authorised SSH Keys*

**Use when:** the set of users is known in advance and must be explicitly provisioned.

---

### Open Access

!!! warning ""
    :material-lock-open: **No credentials required**

Anyone can access the data portal for this user without providing any credentials. The
portal serves the data publicly, with no login prompt.

- TOTP and password fields are not shown or used
- Suitable for fully public, open datasets
- Can be combined with `portal.geoblocking` to restrict by geography if needed
- Replaces the old `portal.anonymous = "yes"` ECtrans option

**Use when:** the data is intended for unrestricted public access (e.g. open-data portals,
WMO WIS2 datasets).

---

### Self-Service *(coming soon)*

!!! note ""
    :material-account-plus: **Visitors register via email**

Visitors register themselves by providing their email address. OpenECPDS sends them
generated credentials by email, which they then use to log in. The administrator can
view, enable, disable, or delete registered users in the monitoring interface.

- No manual provisioning required — users self-enroll
- Credentials are unique per registrant
- Provides an audit trail of who has accessed the data
- Administrators can revoke access per individual

**Use when:** the audience is too large or dynamic to provision manually, but you still
want to know who is accessing the data (unlike Open Access).

!!! note
    The full Self-Service registration flow (email sending, registration page, admin UI)
    is under active development. The `self-service` mode value can already be set on a
    Data User in preparation.

---

## Managing Data Users

### Listing users (`/do/user/incoming`)

The user list shows all Data Users with their Portal Service badge, Enabled status, TOTP
status (shown as N/A for non-Standard-Login users), country flag, and active session count.

The **Filter** button opens a query builder with the following criteria:

| Filter | Description |
|---|---|
| Destination | Users reachable via this destination (direct or via a policy) |
| Policy | Users that have this Data Policy assigned |
| Comment | Glob pattern match on the comment field (`*` and `?` wildcards) |
| Country | Filter by country |
| Enabled | Yes / No |
| Portal Service | Standard Login / Open Access / Self-Service |
| TOTP | Yes / No (only meaningful for Standard Login users) |
| Properties errors | Show only users whose ECtrans properties contain validation errors |

Use the **Unassigned only** button to quickly identify users with no reachable
Destinations — these users would never be able to access any data.

### Creating a user

Navigate to `/do/user/incoming` and click **Create**. The minimum required fields are:

1. **Data Login** — unique, alphanumeric + `_` and `.` only
2. **Portal Service** — choose the appropriate mode
3. **Password** (Standard Login only) — use *Generate* for a secure random password
4. At least one **Destination** or **Data Policy** association

### Editing a user

Navigate to `/do/user/incoming/edit/update_form/<login>`. Changes are saved immediately
on clicking **Save**. The TOTP and password fields are automatically hidden when the
Portal Service is set to Open Access or Self-Service.

### Viewing a user

The read-only view at `/do/user/incoming/<login>` shows the user's current configuration,
active sessions, associated Destinations and Policies, and recent connection history.

---

## Authentication details

### Password authentication

When Portal Service is **Standard Login** and TOTP is disabled, the standard
username + password combination is used across all supported protocols (FTP, SFTP, SCP,
HTTPS, S3).

### TOTP (Two-Factor Authentication)

When **TOTP authentication** is enabled, the password field is ignored and the user must
provide a valid TOTP passcode (6 or 8 digits, RFC 6238). The shared secret is
provisioned by the administrator. Authenticator apps such as Google Authenticator, Authy,
or any RFC 6238-compatible app can be used.

### SSH public-key authentication (SFTP)

Public keys listed in the **Authorised SSH Keys** field are accepted for SFTP
connections in addition to (or instead of) password/TOTP. One key per line, in standard
OpenSSH format.

### S3 authentication

For S3 connections, the **Data Login** is used as the `AccessKeyId` and the password (or
TOTP passcode) as the `SecretAccessKey`. Open Access users can connect to the S3 endpoint
without any credentials.

---

## Related

- [Data Portal](data-portal.md) — overview of portal workflows (push / pull)
- [Data User Options](data-portal-user-options.md) — full `portal.*` ECtrans property reference
- [Portal Transfer Module](../transfer-modules/portal.md) — technical module details
- [REST API — Destination Metadata](../rest-api.md#destination-metadata) — programmatic metadata access
- [INH event fields](../event-logging/inh-fields.md) — incoming connection history records
- [DEA event fields](../event-logging/dea-fields.md) — denied access records
