# TOTP Authentication

OpenECPDS delegates external credential checks to a Master-side
`AuthenticationProvider`. The bundled
`ecmwf.ecpds.extension.ecmwf.EcmwfTotpAuthenticationProvider` adapts ECMWF's
password/passcode verification endpoint. TOTP code generation is a standard
algorithm; the endpoint request format and client credentials are ECMWF-specific.

---

## How TOTP Works

When the ECMWF provider is selected, it forwards supplied credentials to the
configured endpoint. OpenECPDS accepts the login only when the response status
matches `expectedStatus` (default `200`).

OpenECPDS **auto-detects the credential type** before forwarding:

| Credential supplied | Detected as | Sent to TOTP endpoint as |
|---|---|---|
| Exactly 6 or 8 digits | **Passcode** (OTP) | `totp` parameter using `clientIdForPasscode` / `clientSecretForPasscode` |
| Anything else | **Password** | `password` parameter using `clientIdForPassword` / `clientSecretForPassword` |

This means users can log in with either their **regular password** or a **one-time passcode** from an authenticator app — no configuration change is needed to switch between them.

---

## Configuration

The ECMWF provider reads endpoint and client settings from `[TOTP]`:

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

If `[AuthenticationExtension] class` is blank, the ECMWF provider is selected
automatically when `[TOTP] active=true`. You may instead select it
explicitly, or configure another trusted provider:

```ini
[AuthenticationExtension]
class=ecmwf.ecpds.extension.ecmwf.EcmwfTotpAuthenticationProvider
```

Provider-specific options can be set in `[AuthenticationExtension]`; when
absent, the ECMWF provider uses the corresponding `[TOTP]` values. An explicitly
configured provider replaces the automatic ECMWF selection and does not require
`TOTP.active=true`. Restart the Master after changing provider configuration.

---

## TOTP for Web Users

Web Users are the operators and administrators who access the **Monitoring UI**.

| `TOTP.active` | Behaviour |
|---|---|
| `true`, no explicit provider | Web Users authenticate through the automatically selected ECMWF provider. |
| `false`, no provider configured | Web Users authenticate against the password stored in the database. |
| Any value, explicit provider configured | Web User credentials are validated by that provider; local passwords are not a fallback. |

!!! warning "Global effect"
    When the ECMWF provider is selected, every Web User must be accepted by
    its endpoint. Selecting another provider changes the credential verifier;
    it does not relax local active-WebUser and authorization checks.

!!! info "Password field in the UI"
    When an external provider is selected, the **Password** field and
    **Generate** button do not determine authentication; rejected credentials
    never fall back to the local password.

---

## TOTP for Data Users

Data Users are the external users who access data through the **Data Portal**.

External authentication for synchronized Data Users is controlled at the
individual user level via the **TOTP Authentication** toggle
(`isSynchronized`). The selected Master authentication provider validates their
credentials. Non-synchronized and self-service users retain their existing
local-password/subscriber-password handling.

| Authentication provider | Per-user synchronization toggle | Result |
|---|---|---|
| Configured | Enabled ✓ | User authenticates through the configured provider |
| Configured or not | Disabled ✗ | User authenticates against the database password |
| Not configured | Enabled ✓ | Login fails; database-password fallback is disabled |

!!! danger "Per-user TOTP with global TOTP inactive"
    If a Data User's **TOTP Authentication** toggle is enabled but no
    authentication provider is configured, login fails; the local password is
    not used as a fallback. The ECMWF provider is selected automatically when
    `TOTP.active=true`.

!!! info "Self-Service users are exempt"
    Data Users with the **Self-Service Registration** portal service mode bypass TOTP entirely and always authenticate against their database password, regardless of the per-user TOTP toggle.

---

## Credential Auto-Detection

The bundled ECMWF provider auto-detects passcodes for Web Users and synchronized
Data Users:

- A credential of **exactly 6 or 8 digits** is treated as a **TOTP passcode** (one-time code from an authenticator app).
- Any other credential is treated as a **password**.

This means users can log in with either credential type without any additional configuration.

---

## Summary

| User type | TOTP scope | Controlled by |
|---|---|---|
| Web User | Global | Configured `AuthenticationProvider`; ECMWF default selected by `TOTP.active` |
| Synchronized Data User | Per-user | Configured provider and per-user synchronization toggle |

See also: [Users & Access Control](../monitor-ui/users.md) for managing Web Users and Data Users in the UI.
