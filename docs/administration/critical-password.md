# Critical Action Password

The **Critical Action Password** is a secondary administrator credential required to authorise irreversible operations in OpenECPDS (such as [Purge All Data](purge.md)). It is entirely separate from your normal Monitoring UI login credentials and is stored as a **SHA-256 hash** in the database — the plaintext is never persisted anywhere.

!!! warning "Set this password before performing any destructive operations"
    Certain admin actions (Purge All Data) will prompt for this password when it is configured. If it has not been set yet, a yellow banner is shown on the start page and Administration landing page as a reminder.

---

## Setting or Renewing the Password

Navigate to **Administration → Critical Action Password** (`/do/admin/criticalpassword`).

### Initial setup (no password exists)

The form shows two fields:

| Field | Description |
|---|---|
| **New Password** | The password to set. Minimum 12 characters. |
| **Confirm New Password** | Must match the new password exactly. |

Click **Set Password** to hash and store the credential. The banner disappears immediately.

### Renewal (password already set)

The form shows three fields:

| Field | Description |
|---|---|
| **Current Password** | The existing Critical Action Password, to verify your identity. |
| **New Password** | The new password to set. Minimum 12 characters. |
| **Confirm New Password** | Must match the new password exactly. |

Click **Renew Password**. The new hash is stored immediately and takes effect for all logged-in administrators on all Monitor instances.

---

## How It Works

- The password is hashed with **SHA-256** (UTF-8 encoded, hex output) before being stored.
- The hash is stored in the `SYS_CONFIG` database table under group `Master`, parameter name `criticalActionPasswordHash`.
- The plaintext password is never written to disk, logs, or configuration files.
- All Monitor instances share the same value because they all query the same MasterServer database.
- Renewing the password takes effect immediately — there is no need to restart any component.

---

## Security Recommendations

- Choose a long, random password (at minimum 16 characters) that you do not reuse elsewhere.
- Store it in a team password manager or secrets vault, not in a plain text file.
- Rotate it periodically or immediately after a member of the admin team leaves.
- The password is intentionally **different** from the Monitoring UI login so that compromising a user account does not automatically grant access to destructive operations.
