# Standalone SFTP host keys

SSH host keys identify the SFTP server; they are separate from HTTPS
certificates. Checking a fingerprint is meaningful only if the server's
private host key is unique to that installation and kept secret.

When no host keys exist, the standalone Mover generates them on first startup and
stores them in `/data/lib/mover/ssh/keys`. They are reused on restart and when the container is
recreated with the same data directory and image build. Back up `/data` securely:
the private keys identify the server and must remain secret.

!!! warning "Existing host keys"
    When reusing existing host keys, verify that they are unique to the installation
    and have not been exposed. Rotate them if necessary.

## Verify and trust the key

After the container is healthy, its administrator obtains the fingerprint from
the generated **public** key:

```bash
docker exec standalone cat /data/lib/mover/ssh/keys/id_ed25519.pub |
  ssh-keygen -lf - -E sha256
```

Obtain that fingerprint from the administrator over a trusted, independent
channel. On the SFTP client, collect the key offered by the server and display
its fingerprint:

```bash
candidate=$(mktemp)
ssh-keyscan -T 10 -t ed25519 -p 7022 localhost > "$candidate"
ssh-keygen -lf "$candidate" -E sha256
```

For a remote host or different published port, replace `localhost` and `7022`
in the scan and connection commands. `ssh-keyscan` does **not** authenticate
the server: compare every displayed SHA256 fingerprint with the administrator's
fingerprint. If any differs, or the scan fails, do not connect or trust the key.

Only after they match, add the scanned key to the client's `known_hosts`:

```bash
mkdir -p ~/.ssh
chmod 700 ~/.ssh
touch ~/.ssh/known_hosts
chmod 600 ~/.ssh/known_hosts
cat "$candidate" >> ~/.ssh/known_hosts
rm "$candidate"
sftp -P 7022 test@localhost
```

If SSH reports a changed host key, stop and investigate with the administrator;
do not blindly delete or accept the old entry. A legitimate key rotation or
container recreation may require a separately verified update to `known_hosts`.
