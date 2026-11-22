# Local account passwords

Local Web User and Data User passwords use salted Argon2id hashes. External
authentication, SSH keys and self-service subscriber credentials use their
respective authentication mechanisms. OpenBao is not required.

Standalone generates unique initial account credentials on first startup; see
the [standalone guide](../getting-started/standalone.md#access). Other database
bootstraps may still include sample credentials, which must not be used in production.

The Monitor cannot display a stored password. Leave the password field blank
when editing other settings. Copy generated passwords before saving; the
account list API does not return passwords, even with `showSensitiveInfo`.

`[LocalPasswords] hashWritesEnabled` defaults to `yes`. The Master can also
authenticate stored plaintext credentials and hash them on a successful login
when hash writes are enabled. Once a hash exists, authentication does not fall
back to plaintext or a `user:password` digest. Disabling hash writes can store
unhashed passwords and is not recommended for normal operation; accounts with
an existing hash continue to use hashes.

## S3 signing secrets

Signed S3 needs a shared secret, not a password hash. Data Users with hashed
logins need a separate S3 secret unless a retained signing credential is present.
Changing the login password does not rotate a retained S3 secret. Unsigned S3
for Open Access is supported.

Edit the Data User to set a separate random S3 signing secret and update its
clients, or disable **signed** S3 access if it is not used. Both actions retire
any retained plaintext credential after ensuring the login password is hashed.
Don't reuse the portal password as the S3 secret.

Separate S3 secrets are encrypted in the database with AES-256-GCM using a
generic, externally provisioned PKCS#12 keystore. The versioned ciphertext is
bound to its purpose and account; other sensitive database values can use the
same facility with their own purpose and record identity. Passwords remain
one-way hashes. Each use must choose a stable purpose identifying the field and
a stable, unique identity for the record, and size its database column for the
encoded ciphertext. On a trusted host, for example:

```sh
umask 077
keytool -genseckey -alias database-encryption -keyalg AES -keysize 256 \
  -storetype PKCS12 -keystore /secure/path/database-encryption.p12
```

Set `[DatabaseEncryption] keyStore` to the keystore path and
`keyStorePassword` to its password. `keyAlias` defaults to
`database-encryption`; the key and keystore use the same password. Protect
the password in the Master configuration. Deploy the **same key** to the
active Master and any replacement machines, and back up the keystore separately
from the database.

When `[DatabaseEncryption] keyStore` is unset, the Master also accepts
`[LocalPasswords] s3EncryptionKeyStore`, `s3EncryptionKeyStorePassword` and
`s3EncryptionKeyAlias` (default `s3-signing`). The same key is required to read
stored S3 ciphertext.

The application only reads this keystore; it never creates, modifies or rotates
it. The key is cached in memory after first use, so restart the Master after
changing the keystore configuration. Replacing the key without re-encrypting the
database values makes those values unreadable; losing the key
requires resetting them. Key rotation is not automated. Installations without
separate signed S3 secrets do not need a keystore. OpenBao is not required.

Backups or logs may contain plaintext credentials. Protect them and review
access and retention.
