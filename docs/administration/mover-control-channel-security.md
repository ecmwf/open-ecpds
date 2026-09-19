# Securing the Mover Control Channel

Proxy and Continental Data Movers do not have a direct RMI connection to the
MasterServer. Instead, they relay their control-plane calls (health checks,
transfer/host updates, ecAuth token requests, live-transfer statistics, ...)
through a regular OpenECPDS Data Mover, using a REST/JSON interface exposed
under `/ecpds/mover/*` and `/ecpds/master/*`.

This control channel is served by the **same HTTPS port and servlet context**
as the public-facing Data Portal (the `home/`, `file/`, `data/...` and
`register` end-user paths), because both are handled by the same `HttpPlugin`
on each Data Mover. Without any further protection, anyone able to reach a
Data Mover's Data Portal port could call these control-channel endpoints
directly — for example requesting an ecAuth token for an arbitrary user,
deleting data files, or injecting fake transfer/host updates into the Master
Server.

To prevent this, OpenECPDS signs every `mover/*` and `master/*` request with
an **HMAC-SHA256 signature**, computed from the HTTP method, path, query
string, a timestamp and the request body. The signature is verified before
the request reaches any business logic; end-user facing paths on the same
port (`home/`, `file/`, `data/...`, `register`, ...) are untouched by this
mechanism and keep using their own, separate, authentication.

## Configuration

The signature is enabled by configuring a shared secret — the same
`[Security] sharedSecret` option already used elsewhere in OpenECPDS (e.g. by
the plain-socket control channel) — identically on every Data Mover and every
Proxy/Continental Data Mover that need to talk to each other. The Master
Server itself does not need it, since it is not a party to this REST channel.

Just like the existing keystore password, the value is **not** hardcoded
directly in `ecmwf.properties`. It is read from a JVM system property that
the startup script populates from a `SHARED_SECRET` environment variable,
itself sourced from the Mover's `mover.cnf` (or the Proxy's `proxy.cnf`) —
the same file already used for `KEYSTORE_PASSWORD` and other per-host secrets:

```ini
# mover.cnf / proxy.cnf
export SHARED_SECRET=<a-long-random-value>
```

`ecmwf.properties` then simply references it:

```ini
[Security]
SSLKeyStore=${mover.etc}/ecpds-mover.pfx
SSLKeyStorePassword=${keystore.password}
sharedSecret=${sharedsecret.value}

# Optional: how much clock drift/replay window to tolerate (default 5m)
controlChannelMaxSkew=5m
```

This keeps the actual secret value out of any file that might be checked
into version control or bundled into a shared configuration template, and
lets it be provisioned the same way as other host-specific credentials (a
secrets manager, a `mover.cnf`/`proxy.cnf` populated by configuration
management, a Docker/Kubernetes secret injected as the `SHARED_SECRET`
environment variable, ...).

Generate a strong random value, for example:

```bash
openssl rand -base64 32
```

!!! warning "Unset by default, for backward compatibility"
    If `SHARED_SECRET`/`sharedSecret` is left unset (the default in the
    shipped `mover.cnf`/`proxy.cnf` templates), the control channel remains
    unauthenticated exactly as before this feature was introduced — a warning
    is logged at startup to make the operator aware. Setting a shared secret
    is strongly recommended on any Data Mover that is reachable from outside
    a fully trusted network.

## Rollout order

To enable this without downtime across a fleet of Movers:

1. Set `sharedSecret` on the **Proxy/Continental Data Movers first** (the
   REST clients) and restart them. They start signing their requests; Data
   Movers that don't have the secret configured yet simply ignore the extra
   signature headers, so nothing breaks.
2. Then set `sharedSecret` on the **regular Data Movers** (the REST servers)
   and restart them. From that point on, they require and verify a valid
   signature on every `mover/*`/`master/*` request — which every
   Proxy/Continental Data Mover is by then already sending.

## Relationship with `checkControlChannelIsSecure`

This mechanism is independent of, and complementary to, the existing
`[HttpPlugin] checkControlChannelIsSecure=true` option, which only rejects
control-channel requests made over plain HTTP (transport-level protection).
The shared secret adds actual request **authentication**; both should be
enabled together for defence in depth.

## Notes on clock synchronisation

The signature includes a timestamp, and requests are rejected if it falls
outside `controlChannelMaxSkew` (default 5 minutes) of the receiving server's
clock — this also bounds how long a captured request could be replayed.
Since Data Movers and Proxy/Continental Data Movers are servers (not
end-user machines), keeping them on NTP/chrony is standard practice and
easily keeps clocks within this window, even across continents.

## Related

- [Continental Data Movers](../architecture/continental-data-movers.md)
- [TLS Certificate Management](certificates.md)
