# DEA (Denied Access) Fields

The **DEA** event records authentication and authorisation failures within OpenECPDS.
These events are generated whenever a data user attempts to access OpenECPDS but is denied
due to insufficient permissions, invalid credentials, or failed authentication mechanisms
such as TOTP (Time-Based One-Time Password).

## Example

```text
DEA;TimeStamp=2025-02-12 11:00:00.0;UserId=uid;Message=Maximum number of connections exceeded (20);Context=Using https on DataMover=my.mover.name.it from uid@my.host.it
```

## Fields

| Field | Description |
|-------|-------------|
| **UserId** | Holds the identifier of the underlying data user. |
| **Message** | Error message providing details of the failure (see possible errors below). |
| **Context** | Captures both the method (e.g. ftp, https, sftp, s3, mqtt) and the source (e.g. Data Mover and hostname). |

## Possible errors

| Error | Description |
|-------|-------------|
| **Not found** | The requested data user does not exist or is no longer available. May be due to an incorrect user identifier or a deleted data user. |
| **Geolocation restriction** | Access to the requested resource is blocked based on the user's geographical location. May be enforced due to compliance, licensing agreements, or security policies that limit access to specific regions or countries. |
| **Disabled** | The data user account has been deactivated, due to administrative action or security reasons. |
| **TOTP authentication failed** | The provided Time-Based One-Time Password (TOTP) code is incorrect or has expired. Typically happens when a user enters an invalid 2FA code or if there is a time synchronisation issue. |
| **Password authentication failed** | The provided password is incorrect — due to a typo or an attempted unauthorised access. |
| **Password not set** | The administrator has not set a password for the data user, preventing password-based login. May occur when an account is newly created but not yet configured. |
| **Maximum number of connections exceeded** | The number of simultaneous connections has reached the system-defined limit. Additional connection attempts are denied until existing connections are closed or the limit is increased. Often used to prevent server overload. |
| **No associated Destinations** | The data user does not have any configured destinations. May indicate a misconfiguration or an incomplete setup. |
| **No associated Permissions** | The data user lacks the required permissions to perform any action once logged in. May indicate a misconfiguration or an incomplete setup. |

These logs can be used for security monitoring, auditing, and troubleshooting
access-related issues within OpenECPDS.

## Related

- [Event Logging overview](overview.md)
- [Data Portal](../use-cases/data-portal.md)
- [INH fields](inh-fields.md)
