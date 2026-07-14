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
portal for a given Data User. It is stored as a first-class column on the `INCOMING_USER` table.

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

**Use when:** the data is intended for unrestricted public access (e.g. open-data portals,
WMO WIS2 datasets).

---

### Self-Service

!!! note ""
    :material-account-plus: **Visitors register themselves via email verification**

Anyone can request access by filling in a registration form. OpenECPDS sends them a
verification email; once they click the link their credentials are activated and emailed
to them. They then log in using the **Data User's login name** as the username and their
own personal, auto-generated password.

All configuration of the Data User (destinations, permissions, portal options) applies
equally to every registered subscriber — you configure it once and all subscribers
inherit it automatically.

#### How it works end-to-end

1. **Administrator** sets the Portal Service to **Self-Service** on the Data User and
   optionally sets per-user properties in the Data User's **Properties** field:
   ```properties
   portal.registrationAutoApprove = "true"       # activate on email click (recommended for testing)
   portal.registrationAdminEmail = "you@org.com" # optional: notify an admin on each new registration
   ```
   The global `registrationEnabled=true` flag in `ecmwf.properties` (`[DataPortal]` section)
   must also be set to show the *Share registration link* item in the portal menu.
2. **Administrator** shares the registration link. When logged into the data portal as
   the Self-Service Data User, the username menu (top-right) shows a
   **Share registration link** item which opens:
   ```
   https://<portal-host>/ecpds/register?user=<data-login>
   ```
3. **Visitor** fills in the form (name, email address, country) and clicks
   **Send Verification Email**.
4. OpenECPDS creates an inactive **subscriber record** in the `PORTAL_SUBSCRIBER` table,
   generates a unique verification token and a random 12-character password, and sends
   the visitor a verification email.
5. **Visitor** clicks the link in the email (`/ecpds/verify?token=…`).
   - If `portal.registrationAutoApprove = "true"` → subscriber is immediately activated and receives
     a second email containing their login credentials (username + personal password).
   - If `portal.registrationAutoApprove = "false"` (default) → the administrator is notified and must manually
     activate the subscriber in the admin interface before credentials are sent.
6. **Visitor** logs into the data portal using:
   - **Username:** the Data User's login (e.g. `myorg_data`)
   - **Password:** their personal auto-generated password

#### Subscriber table (`PORTAL_SUBSCRIBER`)

Each registrant is stored as an independent row:

| Column | Description |
|---|---|
| `PSB_INU_ID` | The Data User this subscriber belongs to |
| `PSB_EMAIL` | Subscriber's email (also their identifier; unique per Data User) |
| `PSB_NAME` | Full name provided at registration |
| `PSB_ISO` | Two-letter country code |
| `PSB_PASSWORD` | Auto-generated personal password (plain text, stored server-side) |
| `PSB_ACTIVE` | `1` = active (can log in), `0` = pending verification or disabled |
| `PSB_VERIFY_TOKEN` | Token sent in the verification email; cleared on activation |
| `PSB_CREATED_TIME` | Epoch-millisecond registration timestamp |

#### Configuration reference

**Global settings** (`ecmwf.properties` `[DataPortal]` section):

| Property | Default | Description |
|---|---|---|
| `registrationEnabled` | `false` | Show the *Share registration link* item in the portal menu |

**Per-user ECtrans properties** (set in the Data User's **Properties** field):

| Property | Default | Description |
|---|---|---|
| `portal.registrationAutoApprove` | `"false"` | Activate subscriber immediately when they verify their email |
| `portal.registrationAdminEmail` | *(empty)* | Email address to notify on each new registration or activation |

!!! tip "Per-user vs global"
    `portal.registrationAutoApprove` and `portal.registrationAdminEmail` are **per Data User** — each
    Self-Service user can have a different admin email and approval policy.
    The global `registrationEnabled` flag only controls whether the portal menu shows the
    *Share registration link* item; the registration endpoint itself is always active for Self-Service users.

#### Testing without email

To test the flow without a live mail server, fetch the token directly from the database
and call the verify endpoint manually:

```sql
SELECT PSB_EMAIL, PSB_PASSWORD, PSB_VERIFY_TOKEN, PSB_ACTIVE
FROM PORTAL_SUBSCRIBER
WHERE PSB_INU_ID = 'myorg_data';
```

Then open `https://<portal-host>/ecpds/verify?token=<PSB_VERIFY_TOKEN>` in a browser.

**Use when:** the audience is too large or dynamic to provision manually, but you still
want to know who is accessing the data and be able to revoke individual access — unlike
Open Access which is fully anonymous.

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
