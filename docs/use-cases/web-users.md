# Web Users

A **Web User** (internally `ECUser`) is an account that grants access to the **Monitoring Interface** — the browser-based management console at the root URL of the Monitor. Web Users are independent of Data Users (Data Portal accounts) and are managed at `/do/user/user` in the monitoring interface.

---

## Core concepts

### What a Web User controls

| Attribute | Purpose |
|---|---|
| **Web Login** | Unique identifier used as the username when logging in (e.g. `jsmith`) |
| **Password** | Local password used for sign-in when no external provider is active, and for operations requiring local reauthentication |
| **Enabled** | Whether the account is active. Disabled users cannot log in |
| **Comment** | Free-text description to identify the account (e.g. full name or team) |
| **Categories** | Access-control groups that determine which Destinations and UI pages the user can access |
| **Properties** | Fine-grained `monitor.*` options — see [Web User Options](../concepts/web-user-options.md) |

### Categories

**Categories** are the primary access-control mechanism for Web Users. Each category defines:

- A set of **Destinations** the user can see and manage
- A set of **Resources** (URL path patterns) the user is allowed to access

A Web User can belong to multiple categories; permissions are **additive** — the user has access to the union of all Destinations and Resources across all their categories. A user with no categories assigned has no access to any managed resources.

### Resources

**Resources** map URL path patterns to specific monitoring pages or management actions. They are assigned to Categories. For example, a Resource might permit access to `/do/admin/*` (all admin pages) or only `/do/monitoring/*` (read-only monitoring).

---

## Managing Web Users

### Listing users (`/do/user/user`)

The user list shows all Web Users with their Enabled status, category memberships, and comment. Click any user to view their detail page.

### Creating a user

Navigate to `/do/user/user` and click **Create**. The required fields are:

1. **Web Login** — unique identifier
2. **Password** — use the *Generate* button for a secure random local password
3. At least one **Category** assignment

### Editing a user

Navigate to `/do/user/user/edit/update_form/<login>`. Changes are saved on clicking **Save**. The page shows:

- Current external-authentication state below the password field
- **Category** assignment with a picker for adding/removing categories

### Viewing a user

The read-only view at `/do/user/user/<login>` shows the user's current configuration and category memberships.

---

## Authentication

### Password authentication

When no external authentication provider is configured, Web Users authenticate
with their **Web Login** and local password. New and changed passwords are stored
as salted hashes; see [Local account passwords](../administration/local-passwords.md).

The password can be set manually or generated using the *Generate* button. An
existing password cannot be displayed; leave the field blank to keep it unchanged.

### TOTP (Two-Factor Authentication)

When a Master authentication provider is configured, it validates credentials for
**all** Web Users and local passwords are not a login fallback. With no explicit
provider, `TOTP.active=true` selects the bundled ECMWF provider; an explicitly
configured provider can be active regardless of that flag.

The bundled ECMWF provider **auto-detects the credential type**:

- A credential of **exactly 6 or 8 digits** is treated as a **TOTP passcode** (one-time code from an authenticator app)
- Any other credential is treated as a **password**

| Configuration | Behaviour |
|---|---|
| No provider, `TOTP.active=false` | Web Users use local passwords |
| No explicit provider, `TOTP.active=true` | Web Users authenticate via the ECMWF provider |
| Explicit provider configured | Web Users authenticate via that provider, regardless of `TOTP.active` |

!!! warning "Global effect"
    A configured provider applies to **all** Web Users. Each user must be accepted
    by the provider as well as have an active local Web User record and permissions.

!!! info "Local passwords with external authentication"
    The **Password** field remains available and can be used for operations
    requiring local reauthentication. It does not provide a login fallback while
    the external provider is active.

For configuration details, see the [TOTP Authentication](../administration/totp.md) administration page.

---

## Web User Options

Web Users support a small set of `monitor.*` properties that customise their experience in the Monitoring UI. These are set in the user's **Properties** field.

| Option | Default | Description |
|---|---|---|
| `monitor.shareFeedback` | `true` | Show or hide the **Share Feedback** button in the portal header for this user |

See [Web User Options](../concepts/web-user-options.md) for the full reference.

---

## Access Control Reference

### How permissions are resolved

1. The logged-in user's **Categories** are looked up.
2. Each category's **Destinations** list is unioned — the user can see and manage all those Destinations.
3. Each category's **Resources** list is unioned — the user can access all URL paths covered by those Resources.
4. If a request URL does not match any Resource in any of the user's categories, access is **denied**.

### Typical category structure

A common pattern is to have:

| Category | Destinations | Resources |
|---|---|---|
| `read-only-ops` | All production destinations | `/do/monitoring/*`, `/do/transfer/*` |
| `operators` | A subset of destinations | `/do/monitoring/*`, `/do/transfer/*`, `/do/management/*` |
| `admins` | All destinations | `/do/*` (all pages) |

---

## Related

- [Users & Access Control](../monitor-ui/users.md) — UI overview of users, categories, policies, and resources
- [Web User Options](../concepts/web-user-options.md) — `monitor.*` property reference
- [TOTP Authentication](../administration/totp.md) — configuring TOTP for Web Users and Data Users
- [Data Users](data-users.md) — the Data Portal counterpart to Web Users
