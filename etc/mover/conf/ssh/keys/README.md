# SSH Host Keys

This directory holds the SSH server host key pairs used by the **SshPlugin** SFTP service.

## Automatic generation

If this directory contains **no private key files** when the DataMover starts, three key pairs
are generated automatically and written here:

| File | Algorithm | Notes |
|---|---|---|
| `id_ecdsa` / `id_ecdsa.pub` | ECDSA P-521 | 256-bit security |
| `id_ed25519` / `id_ed25519.pub` | Ed25519 | Preferred modern algorithm |
| `id_rsa` / `id_rsa.pub` | RSA 4096-bit | Legacy client fallback |

Private key files are written in **OpenSSH format** with `600` permissions (owner read/write only).

## Manual key placement

You can place your own keys here before starting the service. Any file whose name starts with
`id_` (and does not end with `.pub`) is treated as a private key and loaded automatically.
The auto-generation step is skipped as long as at least one such file is present.

## Notes

- Key files are excluded from version control via `.gitignore`; they must be provisioned on
  each host independently.
- To force regeneration of all keys, remove the existing `id_*` files and restart the service.