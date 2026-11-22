# Standalone SFTP host keys

SSH host keys identify the SFTP server; they are separate from HTTPS
certificates. Checking a fingerprint is meaningful only if the server's
private host key is unique to that installation and kept secret.

!!! warning "Provision a unique host key"
    A standalone image can contain host keys from its build environment. Such
    keys can be shared with other containers or exposed to anyone with the
    image. Do not rely on their fingerprints to authenticate your server.
    Provision a unique private host key outside the image before trusting it.
    The default key directory is also outside `/data`, so preserve your
    independently provisioned keys when recreating the container.

## Provision a key

On the container host, an administrator can generate an Ed25519 host key in a
new, empty directory. For example, with an existing `/secure` parent directory:

```bash
sudo mkdir -m 0700 /secure/openecpds-sftp
sudo ssh-keygen -q -t ed25519 -N '' -f /secure/openecpds-sftp/id_ed25519
sudo chown 12002:12002 /secure/openecpds-sftp \
  /secure/openecpds-sftp/id_ed25519 /secure/openecpds-sftp/id_ed25519.pub
```

UID and GID `12002` are the standalone Mover identity. Restrict access to the
directory and private key, and back them up securely. When creating the
container, add this argument to the [standalone `docker run`](standalone.md#start):

```bash
--mount type=bind,source=/secure/openecpds-sftp,target=/usr/local/ecpds/mover/conf/ssh/keys,readonly
```

The bind mount must preserve ownership and allow UID `12002` to read the key.
Recreate an existing container to add the mount, retaining the same data volume
and image build. Reuse the protected key directory across recreation; never
copy its private key into the image.

## Verify and trust the key

The administrator obtains the fingerprint from the provisioned **public** key:

```bash
sudo ssh-keygen -lf /secure/openecpds-sftp/id_ed25519.pub -E sha256
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
