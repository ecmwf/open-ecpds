# Standalone vault modes

The default `embedded` mode starts and unseals OpenBao automatically for
evaluation. Its root token and unseal material are stored on `/data`, so
anyone with a copy of that volume can unlock the vault. Do not use this mode
to protect production credentials.

Choose a mode when creating a **new** standalone data volume. The integration
does not support changing the seal mode of an initialized vault; do not delete
the seal marker or switch modes by changing an environment variable.
The standalone container is not yet a production-ready deployment.

## Automatic unsealing with an external key

Set `ECPDS_OPENBAO_ENABLED=auto` to use OpenBao's native static-key seal.
It requires a 32-byte cryptographically random key supplied by an independently
protected secret-management system. The key must be available again after every
host reboot; do not store it in `/data`, the image, an environment variable or an
ordinary backup alongside the vault. OpenBao recommends this mechanism only
when the operating environment already has a trusted source for injecting the
key. Protect and back up that source independently.

For a new, empty data volume, add these options to the
[standalone start command](../getting-started/standalone.md#start):

```bash
-e ECPDS_OPENBAO_ENABLED=auto \
--mount type=bind,src=/secure/openbao-seal.key,dst=/run/secrets/ecpds-openbao-seal.key,readonly \
```

Replace the source with your managed key file. It must be owned by container UID
12005 and have mode `0400`. Account for host user-namespace mapping. Container
and host administrators remain trusted. The key is not generated or copied
into `/data`; a SHA-256 identifier there rejects a different key. Static seal
key rotation is not supported by this integration.

Initialize once from an interactive terminal:

```bash
docker exec -it standalone /usr/local/ecpds/standalone/openbao-operator.sh init
docker exec -it standalone /usr/local/ecpds/standalone/openbao-operator.sh auto-login
docker exec -it standalone /usr/local/ecpds/standalone/openbao-operator.sh configure
```

`init` displays the initial root token and five recovery shares. Record them
securely outside `/data`; three shares authorize recovery operations but cannot
decrypt the vault if the seal key is lost. Enter the initial root token into
`auto-login` and `configure`. Do not run `unseal` during initialization.

`auto-login` displays a JSON AppRole credential once. Store it in an independently
protected secret manager, not in `/data` or a log. The credential can issue
Master-policy tokens and must be protected like the Master token. `configure`
creates the Master token, revokes the initial root token and displays a separate
recovery password once. Record that password separately from the other secrets.
Then recreate the container with its original options and an additional mount:

```bash
--mount type=bind,src=/secure/openbao-approle.json,dst=/run/secrets/ecpds-openbao-approle.json,readonly \
```

The JSON file must be owned by container UID 12001 and have mode `0400`. Ensure
both secret files are supplied before every restart. The Master token is
renewed when possible; if it has expired, the AppRole issues a new periodic
token without operator intervention. If the credential is missing, invalid or
inaccessible, the vault can unseal but the Master remains unavailable until its
token is recovered. Monitor this condition and test recovery and secret-manager
availability. Bootstrap refuses to overwrite an existing `standalone-master`
auth mount; if initial AppRole provisioning is interrupted, repair it through
OpenBao administration rather than rerunning the command blindly.

If the seal key is lost, recovery shares cannot decrypt the vault, even from a
backup. Back up the seal key separately and test a stopped-volume restore.

## Manual unsealing

For a new data volume, add `-e ECPDS_OPENBAO_ENABLED=manual` to the standalone
start command. Initialize and configure the vault once:

```bash
docker exec -it standalone /usr/local/ecpds/standalone/openbao-operator.sh init
docker exec -it standalone /usr/local/ecpds/standalone/openbao-operator.sh unseal
docker exec -it standalone /usr/local/ecpds/standalone/openbao-operator.sh configure
```

`init` displays five unseal shares and an initial root token. Record them
securely outside `/data`; three distinct shares are required to unseal. Record
the initial root token for `configure`, which creates the Master token and
revokes it. If initialization is interrupted before the shares are saved, the
vault cannot be recovered. `configure` also displays a randomly generated
recovery password once and asks you to confirm it. Store it securely outside
`/data`, separately from the shares. Services start once provisioning completes.

After each container restart, run `unseal` and enter three shares. The Master
token is renewed while running. If it expires while the container is stopped,
unseal the vault, then follow the recovery procedure below.

## Recovering the Master token

For `manual` or `auto` mode, if the Master token cannot be renewed or replaced,
use the recovery password and three distinct shares:

```bash
docker exec -it standalone /usr/local/ecpds/standalone/openbao-operator.sh recover
docker exec -it standalone /usr/local/ecpds/standalone/openbao-operator.sh configure
```

`recover` displays a temporary root token once. Enter it into `configure`,
which issues a new Master token and revokes that root token. The recovery login
can start root-token generation but cannot read stored Host credentials or create
tokens directly. Its short-lived session expires after 15 minutes; retry if
necessary. Losing the recovery password or a quorum of shares prevents this
recovery. Back up the vault data and test recovery before relying on it.

If configuring the recovery login is interrupted after its authentication mount
is created, `configure` fails closed rather than replacing an existing password.
Keep the initial root token until configuration completes; an administrator can
repair the incomplete login with that token.

The vault does not itself enable Host password rotation. See the
[credential rotation guide](kubernetes.md#credential-rotation-enablement).
To disable the vault entirely, set `ECPDS_OPENBAO_ENABLED=false`. An
[external OpenBao setup](standalone-openbao.md) is also available.
