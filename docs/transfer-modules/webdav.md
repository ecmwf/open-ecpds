# WebDAV Transfer Module

The `webdav` module transfers files to and from remote WebDAV servers (RFC 4918). It supports HTTPS and HTTP, optional write locking, automatic parent-directory creation, and configurable TLS versions.

## Connection

```properties
webdav.scheme = "https"          # https (default) or http
webdav.port = "443"              # remote port
webdav.username = "myuser"       # login (overrides host credentials)
webdav.password = "secret"       # password (overrides host credentials)
webdav.path = "/webdav/data"     # base path prepended to every resource URL
webdav.sslValidation = "false"   # set true to enforce certificate validation
webdav.proxy = "proxy.example.com:3128"  # optional HTTP proxy
```

## Timeouts

```properties
webdav.connectTimeout = "PT30S"   # connection timeout (ISO-8601 duration, default 30 s)
webdav.socketTimeout = "PT300S"   # socket read timeout (default 300 s)
```

## TLS protocols

```properties
webdav.supportedProtocols = "TLSv1.2,TLSv1.3"   # restrict accepted TLS versions
```

## Write locking (RFC 4918)

When `useLock` is enabled, the module sends a `LOCK` request before each upload and `UNLOCK` after, preventing concurrent writes from other clients.

```properties
webdav.useLock = "true"          # enable exclusive write locking
webdav.lockTimeout = "300"       # lock duration in seconds (0 = infinite)
webdav.lockOwner = "ecpds"       # owner string recorded in the lock
```

## Directory creation

```properties
webdav.mkdirs = "true"           # auto-create missing parent directories (default: true)
```

## Full option reference

See the [ECtrans Options reference](../options.md#webdav) for all available `webdav.*` options with descriptions and defaults.
