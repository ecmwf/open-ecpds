# TOTP Authentication

OpenECPDS supports **Time-based One-time Password (TOTP)** authentication as an alternative to local password validation. When enabled, login credentials are verified against an external TOTP-compatible server (e.g. Keycloak) using an OAuth2 resource-owner password flow, rather than comparing against the password stored in the OpenECPDS database.

---

## How TOTP Works

When a user logs in, OpenECPDS forwards the supplied credentials to the configured TOTP endpoint with an HTTP POST. The endpoint validates the credentials and returns an HTTP status code. OpenECPDS accepts the login only if the response matches the configured `expectedStatus` (default `200`).

OpenECPDS **auto-detects the credential type** before forwarding:

| Credential supplied | Detected as | Sent to TOTP endpoint as |
|---|---|---|
| Exactly 6 or 8 digits | **Passcode** (OTP) | `totp` parameter using `clientIdForPasscode` / `clientSecretForPasscode` |
| Anything else | **Password** | `password` parameter using `clientIdForPassword` / `clientSecretForPassword` |

This means users can log in with either their **regular password** or a **one-time passcode** from an authenticator app — no configuration change is needed to switch between them.

---

## Configuration

TOTP is configured in the MasterServer properties file under the `[TOTP]` section:

```ini
[TOTP]
# Enable TOTP authentication (default: false)
active=true

# URL of the TOTP/OAuth2 token endpoint
url=https://auth.example.org/realms/myrealm/protocol/openid-connect/token

# OAuth2 client credentials for password-based login
clientIdForPassword=ecpds-password-client
clientSecretForPassword=your-client-secret

# OAuth2 client credentials for passcode (OTP) login
clientIdForPasscode=ecpds-totp-client
clientSecretForPasscode=your-totp-client-secret

# OAuth2 grant type (default: password)
grantType=password

# HTTP status code that means "authenticated" (default: 200)
expectedStatus=200
```

Changes to `active` take effect only after restarting the MasterServer.

---

## TOTP for Web Users

Web Users are the operators and administrators who access the **Monitoring UI**.

| `TOTP.active` | Behaviour |
|---|---|
| `true` | **All** Web Users authenticate through the TOTP endpoint. The password stored in the database is ignored entirely. |
| `false` | **All** Web Users authenticate against the password stored in the database. |

!!! warning "Global effect"
    `TOTP.active` is a global switch. When set to `true`, every Web User must have a valid account on the TOTP server. Any Web User without an account on the TOTP server will be unable to log in.

!!! info "Password field in the UI"
    When `TOTP.active = true`, the **Password** field and **Generate** button on the Web User edit page are still visible but have no effect at runtime — the stored password is never checked. The field only becomes relevant again if the MasterServer is restarted with `TOTP.active = false`.

---

## TOTP for Data Users

Data Users are the external users who access data through the **Data Portal**.

TOTP for Data Users is controlled at the **individual user level** via the **TOTP Authentication** toggle (`isSynchronized` field) on the Data User edit page, combined with the global `TOTP.active` setting.

| `TOTP.active` (global) | Per-user TOTP toggle | Result |
|---|---|---|
| `true` | Enabled ✓ | User authenticates through the TOTP endpoint |
| `true` | Disabled ✗ | User authenticates against the database password |
| `false` | Enabled ✓ | ⚠️ Login **fails** — TOTP endpoint is unavailable but database password is not used |
| `false` | Disabled ✗ | User authenticates against the database password |

!!! danger "Per-user TOTP with global TOTP inactive"
    If a Data User's **TOTP Authentication** toggle is enabled but `TOTP.active = false` globally, the user will be **locked out** — they cannot log in at all. TOTP authentication is attempted but throws an error, and no fallback to the database password occurs. Always ensure the global TOTP service is active before enabling per-user TOTP.

!!! info "Self-Service users are exempt"
    Data Users with the **Self-Service Registration** portal service mode bypass TOTP entirely and always authenticate against their database password, regardless of the per-user TOTP toggle.

---

## Credential Auto-Detection

Both Web Users and Data Users benefit from the same passcode auto-detection logic:

- A credential of **exactly 6 or 8 digits** is treated as a **TOTP passcode** (one-time code from an authenticator app).
- Any other credential is treated as a **password**.

This means users can log in with either credential type without any additional configuration.

---

## Summary

| User type | TOTP scope | Controlled by |
|---|---|---|
| Web User | Global | `TOTP.active` in MasterServer config |
| Data User | Per-user | `TOTP.active` **and** per-user TOTP toggle |

See also: [Users & Access Control](../monitor-ui/users.md) for managing Web Users and Data Users in the UI.
