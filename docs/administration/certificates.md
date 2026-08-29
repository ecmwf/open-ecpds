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

The status is computed by the MasterServer, cached for up to one hour, and **immediately invalidated** after any certificate is deployed or reloaded through the UI — so all Monitor instances always see the same up-to-date value without requiring a restart or manual refresh.

---

## First Startup

On first startup, if no keystore file is found at the path configured in `ecmwf.properties`, OpenECPDS automatically generates a unique **self-signed RSA-2048 certificate** and stores it as a PKCS#12 keystore. This happens for both the Monitor and each Data Mover independently, so each component gets its own certificate with the server's hostname as the Common Name (CN) and Subject Alternative Name (SAN).

This means OpenECPDS is ready to use over HTTPS out of the box with no manual certificate configuration.

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

### Import Certificate

Imports a certificate from an uploaded file. Supported formats:

| Format | File extension | Notes |
|---|---|---|
| PEM | `.pem`, `.crt` | May include both certificate and private key |
| PKCS#12 / PFX | `.pfx`, `.p12` | Standard format from most CAs and tools |
| Java KeyStore | `.jks` | Legacy format, still widely used |

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

## Security Recommendations

- Use certificates with a **validity period of one year or less** and rotate them regularly.
- Use **RSA 2048-bit or ECDSA P-256** keys or stronger.
- Enable only **TLS 1.2 and TLS 1.3** (the defaults). Earlier protocol versions are disabled by default.
- Ensure the certificate's **Subject Alternative Names** cover all hostnames under which the Monitor and Data Movers are accessed to avoid TLS errors on modern clients.
- After deploying a production certificate, verify the SHA-256 fingerprint shown in the UI matches the fingerprint of the file provided by your CA.
