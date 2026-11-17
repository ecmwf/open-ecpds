# TLS Certificate Management

OpenECPDS exposes all its HTTPS endpoints — the Monitoring UI and the Data Portal on each Data Mover — over TLS. Certificates are managed centrally from the **Administration → TLS Certificates** page in the Monitoring UI, so administrators never need to touch individual servers or restart daemons.

---

## Certificate Status Indicator

The **TLS Certificates** entry in the Admin Tasks card, the left-hand navigation menu, and the start-page quick-links shows a live status based on the health of all certificates across every connected Monitor and Data Mover:

| Colour | Meaning |
|---|---|
| 🔴 **Red** — *Expired* badge | At least one certificate has already passed its expiry date. |
| 🟡 **Yellow** — *Attention* badge | At least one certificate is self-signed or expires within 30 days. |
| Default grey | All certificates are CA-signed and not close to expiry. |

Certificate status is shared across Monitor instances and refreshed after a
certificate is deployed or reloaded through the UI.

---

## First Startup

On first startup, if no keystore file is found at the path configured in `ecmwf.properties`, OpenECPDS automatically generates a unique **self-signed RSA-2048 certificate** and stores it as a PKCS#12 keystore. This happens for both the Monitor and each Data Mover independently, so each component gets its own certificate with the server's hostname as the Common Name (CN) and Subject Alternative Name (SAN).

This means OpenECPDS is ready to use over HTTPS out of the box with no manual certificate configuration.

MQTT TLS startup also creates its configured keystore if missing, independently of
HTTP plugin startup order. Existing non-empty keystores are never regenerated
automatically. Generated `.pfx`/`.p12` files are ignored by Git and must not be committed.

!!! warning "Self-signed certificates are for evaluation only"
    A self-signed certificate is not trusted by browsers or operating systems by default. Visitors will see a security warning. Replace it with a certificate issued by a trusted Certificate Authority (CA) before deploying the system in a production environment.

When you log in as an administrator on a server using a self-signed certificate, a **TLS Notice** banner is displayed at the top of every page as a reminder:

> **TLS Notice:** This server is currently using a self-signed certificate intended for evaluation purposes. Consider installing a certificate issued by a trusted Certificate Authority (CA) before using this system in production. **[Manage Certificates]**

---

## Certificate Management Page

Navigate to **Administration → TLS Certificates** (`/do/admin/certificates`) to manage certificates.

The page is divided into three sections:

---

### Monitor Certificate (top card)

A detailed view of the TLS certificate currently active on **this Monitor**. It shows:

| Field | Description |
|---|---|
| Subject | The DN of the entity the certificate was issued to |
| Issuer | The DN of the entity that issued (signed) the certificate |
| Valid From (UTC) | The date from which the certificate is valid |
| Valid Until (UTC) | The certificate expiry date (highlighted in red if expired or expiring within 30 days) |
| Type | **Self-Signed** or **CA-Signed** |
| Key Algorithm | Algorithm and key size (e.g. RSA 2048 bit) |
| Serial Number | Unique serial number in hexadecimal |
| SHA-256 Fingerprint | Cryptographic fingerprint for identity verification |
| Keystore Path | Path to the PKCS#12 keystore file on disk |
| Subject Alt Names | All DNS names and IP addresses covered by the certificate (SANs) |

!!! tip "Why Subject Alt Names matter"
    The certificate on the Monitor is also deployed to Data Movers (see *Deploy to All Movers* below). Because all services may be reached under multiple hostnames, the certificate's **Subject Alternative Names (SANs)** should include every DNS name and optionally every IP address under which the Monitor and any Data Mover is accessed. Modern TLS clients require the CN or a SAN to match the hostname; a missing entry will cause certificate errors.

---

### Monitor Certificates table

Shows the TLS certificate currently active on **each connected Monitor daemon** (useful in multi-Monitor deployments). Each row shows:

| Column | Description |
|---|---|
| Monitor | Hostname of the Monitor daemon |
| Subject | The DN of the certificate's subject |
| Subject Alt Names | DNS names and IP addresses covered by the certificate |
| Valid Until | Certificate expiry date |
| Type | **Self-Signed** or **CA-Signed** |
| SHA-256 Fingerprint | Cryptographic fingerprint for verification |

The **Deploy to All Monitors** button pushes the current Monitor certificate to every connected Monitor daemon over RMI, reloading each one without dropping active connections.

---

### Data Mover Certificates table

Shows the TLS certificate currently active on **each connected Data Mover**. Each row shows:

| Column | Description |
|---|---|
| Mover | Hostname of the Data Mover |
| Subject | The DN of the certificate's subject |
| Subject Alt Names | DNS names and IP addresses covered by the certificate |
| Valid Until | Certificate expiry date |
| Type | **Self-Signed** or **CA-Signed** |
| SHA-256 Fingerprint | Cryptographic fingerprint for verification |

If a Mover is offline or has not yet reported its certificate, the row shows *"Offline or no certificate data available"*.

---

## Available Actions

### Generate Self-Signed Certificate

Replaces the current Monitor certificate with a new self-signed RSA-2048 / SHA-256 certificate. You can optionally specify a hostname; if left blank, the current server hostname is used.

!!! caution
    This action replaces the existing certificate immediately. Any browser or client that has pinned the old certificate will need to re-accept the new one.

### Generate CSR

Generates a PKCS#10 Certificate Signing Request (CSR) using the private key currently stored in the Monitor keystore, and downloads it as a PEM file (`ecpds-monitor.csr`). Submit this CSR to your Certificate Authority to obtain a CA-signed certificate.

### Generate CSR with a New Key

Use **New Key + CSR** when rotating the private key. Enter the Common Name and any additional DNS/IP Subject Alternative Names, then download the CSR. The Common Name is automatically included as a SAN. OpenECPDS stores the new private key in a separate password-protected pending keystore beside the active keystore; it does not change the active certificate. Only one pending request can exist at a time.

After the CA returns the signed PEM certificate and any intermediate certificates, select **Import signed certificate** and upload a PEM file with the leaf certificate first, followed by its intermediate chain. OpenECPDS checks certificate validity, chain ordering/signatures where supplied, requested SAN coverage, and that the leaf public key matches the pending private key before replacing the active keystore and hot-reloading this Monitor. The pending key is retained if validation or reload fails. You can discard an unused request without affecting the active certificate.

The generated pending keystore uses the configured Monitor keystore password and file permissions. Keep the pending request on the same Monitor until activation or discard; the private key is not included in the downloaded CSR. Once activated, use **Deploy to All Movers** and/or **Deploy to All Monitors** if appropriate and if the requested SANs cover those hosts.

### Import Certificate

Imports a certificate from an uploaded file. Supported formats:

| Format | File extension | Notes |
|---|---|---|
| PEM | `.pem`, `.crt` | May include both certificate and private key |
| PKCS#12 / PFX | `.pfx`, `.p12` | Standard format from most CAs and tools |
| Java KeyStore | `.jks` | Java certificate and private-key container |

If the uploaded file is password-protected (PKCS#12, JKS, or encrypted PEM), enter the password in the **Password** field. Leave it blank if the file uses the same password as the current keystore.

The certificate is imported into the Monitor keystore, then **hot-reloaded** — active HTTPS connections are not dropped.

### Reload from Disk

Hot-reloads the certificate on **this Monitor only** directly from the keystore file currently on disk (the path shown in the **Keystore Path** field), without requiring a file upload or daemon restart. Use this after replacing the keystore file manually — for example via automation, a secrets manager, or `certbot` — to activate the new certificate immediately.

> **Scope:** This button applies only to the Monitor you are currently connected to. It does not affect other Monitors or any Data Movers.

The same reload capability is available for remote components via the buttons in the **Monitor Certificates** and **Data Mover Certificates** tables:

- **Reload from Disk (All Monitors / All Movers)** — instructs every connected Monitor or Data Mover to reload its own keystore file from disk. No certificate is pushed from the Master; each component re-reads its local file.
- **Per-row reload icon** (`↺`) — same operation, targeted at a single Monitor or Mover.

### Download Public Certificate

Downloads the Monitor's public certificate as a PEM file (`ecpds-monitor.pem`). Use this to install the certificate in a browser, OS trust store, or MQTT client.

### Deploy to All Movers

Pushes the current Monitor certificate to every connected Data Mover over RMI. Each Mover writes the new keystore to disk and hot-reloads its HTTPS server without interrupting active connections.

This is the recommended way to synchronise all components after importing a new CA-signed certificate.

Each Mover retains its configured keystore password, file permissions and ownership.
The certificate, private key and complete chain are preserved, and the SHA-256
fingerprint does not change. PKCS#12 files use AES-256/PBES2 protection with
PBKDF2-HMAC-SHA256 and a SHA-256 MAC and can be read directly by OpenSSL.

Master, Monitor and Mover HTTP/MQTT services check private-key PKCS#12 protection
at startup and write this format when needed. The configured password, certificate,
private key, complete chain, SHA-256 fingerprint, file permissions and ownership
are preserved. Writes are validated before atomic replacement. JKS and
certificate-only truststores are not rewritten. Unreadable keystores and unsupported
entries are left untouched, and the failure is logged.

Hawtio can share the service's PKCS#12 file. Verify the file locally with:

```bash
openssl pkcs12 -in /path/to/ecpds-mover.pfx -info -noout
```

Enter the Mover's keystore password interactively. If Hawtio uses this file, configure
the same password there and restart Hawtio after redeployment. Neither the certificate
nor its private key needs to be regenerated.

### Deploy to All Monitors

Pushes the current Monitor certificate to every other connected Monitor over RMI. Useful in multi-Monitor deployments to keep all Monitor UIs on the same certificate.

---

## Replacing with a CA-Signed Certificate

To replace the auto-generated self-signed certificate with one from a trusted CA:

1. Navigate to **Administration → TLS Certificates**.
2. Click **Generate CSR** to download a CSR for the current private key.
3. Submit the CSR to your CA and obtain a signed certificate (PEM or PKCS#12).
4. Click **Import Certificate**, upload the signed certificate, and click **Import & Activate**.
5. Click **Deploy to All Movers** to push the new certificate to all Data Movers.
6. If running multiple Monitors, also click **Deploy to All Monitors**.

The HTTPS servers on the Monitor and all Data Movers will pick up the new certificate without any service interruption.

---

## Manual Configuration

If you prefer to manage certificates outside the UI (for example, using automation or a secrets manager), configure the keystore path and password directly in `ecmwf.properties`:

```ini
[Security]
SSLKeyStore=${monitor.etc}/ecpds-monitor.pfx
SSLKeyStorePassword=<your-password>
```

The `SSLKeyStore` path must point to a PKCS#12 (default) or JKS keystore. The UI will continue to read from and write to this location.

You can also use the `[HttpPluginSSL]` section for an override that takes precedence over `[Security]`:

```ini
[HttpPluginSSL]
keyStorePath=/etc/certs/ecpds.pfx
keyStorePassword=<password>
keyStoreType=PKCS12
```

After replacing the file on disk, use one of the **Reload from Disk** actions in the UI to activate the new certificate without restarting the daemon: the button in the **Monitor Certificate** card reloads this Monitor only; the buttons in the **Monitor Certificates** and **Data Mover Certificates** tables reload individual or all remote components.

---

## Changing Keystore Passwords

The standalone `tools/RotateKeystorePassword.java` script requires a JDK 17+ on a
POSIX filesystem. It supports ordinary file-backed JKS, JCEKS and PKCS#12 stores,
not hardware tokens or custom providers. Run it from the source checkout:

```bash
java tools/RotateKeystorePassword.java --keystore /path/to/service.pfx
```

The existing password, new password and confirmation are prompted without echoing.
For automation, supply file paths instead of passwords:

```bash
java tools/RotateKeystorePassword.java \
  --keystore /path/to/service.pfx \
  --old-password-file /secure/old-password \
  --new-password-file /secure/new-password
```

Password files must be owned by the invoking user, have no group/other permissions
(for example, mode `0600`), and contain UTF-8 text. One final LF or CRLF is removed;
spaces are not trimmed. Do not put passwords in command arguments or shell history.

The script changes the store password and all private/secret key-entry passwords.
If any key entry cannot be recovered with the existing store password, it fails
without replacing the original. Certificates, complete chains and key material are
verified before atomic replacement. A uniquely named `.backup-...` file retains
the original bytes and password. POSIX permissions, owner and group are preserved
on both files; ACLs, extended attributes and hard-link relationships are not.
Protect and retain the backup until recovery is no longer needed, then dispose
of it according to your credential-retention policy.

Stop all services and other writers using the file first. After rotation, update
every matching password configuration, including overrides and Hawtio if it shares
the file, before restarting. The script does not edit configuration or restart
services. It preserves the keystore format and does not select different
cryptographic protection algorithms. Use a strong, unique new password.

---

## Security Recommendations

- Use certificates with a **validity period of one year or less** and rotate them regularly.
- Use **RSA 2048-bit or ECDSA P-256** keys or stronger.
- Enable only **TLS 1.2 and TLS 1.3** (the defaults). Other TLS versions are disabled by default.
- Ensure the certificate's **Subject Alternative Names** cover all hostnames under which the Monitor and Data Movers are accessed to avoid TLS errors on modern clients.
- After deploying a production certificate, verify the SHA-256 fingerprint shown in the UI matches the fingerprint of the file provided by your CA.
