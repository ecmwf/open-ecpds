# Google Cloud Storage Transfer Module

!!! info
    All options use the `gcs.` prefix. Authentication uses a GCS **Service Account**. The host *Login* maps to `clientId` and *Password* maps to `privateKeyId`; both can be set explicitly via options instead.

## Connection

### Endpoint & URL

By default the module connects to `storage.googleapis.com`. Use `gcs.url` to point to a GCS-compatible emulator or private endpoint.

| Option | Default | Description |
| --- | --- | --- |
| `gcs.url` | *scheme://host:port* | Full base URL override (e.g. `http://localhost:4443` for the emulator) |
| `gcs.scheme` | `http` | URL scheme when constructing the endpoint URL from host + port |
| `gcs.port` | `443` | Port when constructing the endpoint URL |

!!! warning
    For standard GCS usage, leave `gcs.url` unset. The SDK routes to `storage.googleapis.com` automatically using the project credentials.

### TLS / SSL

| Option | Default | Description |
| --- | --- | --- |
| `gcs.protocol` | `TLS` | SSL/TLS protocol version (e.g. `TLSv1.2`, `TLSv1.3`) |
| `gcs.sslValidation` | `false` | Validate the server TLS certificate. Set to `true` for production; disable only for local emulators with self-signed certificates. |

### Quick-start examples

```properties
gcs.sslValidation = "yes"
gcs.protocol = "TLSv1.3"
```

```properties
gcs.url = "http://localhost:4443"
gcs.scheme = "http"
gcs.port = "4443"
gcs.sslValidation = "no"
```

## Service Account

### Service Account credentials

GCS authentication requires a Service Account JSON key. Instead of uploading the JSON file, extract the individual fields and set them as options:

| Option | JSON field | Description |
| --- | --- | --- |
| `gcs.projectId` | `project_id` | GCP project ID |
| `gcs.clientId` | `client_id` | Service account client ID (or set host *Login*) |
| `gcs.clientEmail` | `client_email` | Service account email address |
| `gcs.privateKeyId` | `private_key_id` | Private key ID (or set host *Password*) |
| `gcs.privateKey` | `private_key` | RSA private key in PKCS8 PEM format (paste the full key block) |

!!! warning
    The `gcs.privateKey` value contains the full PEM block including newlines. Paste it exactly as it appears in the service account JSON file, replacing literal `\n` sequences with actual newlines.

### Quick-start example

```properties
gcs.projectId = "my-gcp-project"
gcs.clientEmail = "ecpds@my-gcp-project.iam.gserviceaccount.com"
gcs.clientId = "123456789012345678901"
gcs.privateKeyId = "abcdef1234567890"
gcs.privateKey = "-----BEGIN RSA PRIVATE KEY-----
MIIEo...
-----END RSA PRIVATE KEY-----"
```

## Bucket

### Bucket selection

The bucket name can be embedded in the URL path (`user:pass@host/bucketName`) or set explicitly here. Explicit options override the URL path value.

| Option | Default | Description |
| --- | --- | --- |
| `gcs.bucketName` | *from URL path* | Fixed bucket name for all transfers. When set, all objects are placed in this bucket. |
| `gcs.prefix` | *empty* | Path prefix prepended to every object key inside the bucket (e.g. `data/incoming/`) |
| `gcs.allowEmptyBucketName` | `false` | Allow operations without a bucket name; the first path segment is used as the object name |

### Bucket creation

| Option | Default | Description |
| --- | --- | --- |
| `gcs.mkBucket` | `false` | Automatically create the bucket if it does not exist on first connection |
| `gcs.bucketLocation` | *empty* | GCS region / location for auto-created buckets (e.g. `EU`, `us-central1`) |

### Listing ownership

| Option | Default | Description |
| --- | --- | --- |
| `gcs.ftpuser` | *from service account email* | Owner user name shown in generated directory listing entries |
| `gcs.ftpgroup` | *from service account email* | Owner group name shown in generated directory listing entries |

### Quick-start examples

```properties
gcs.bucketName = "my-data-bucket"
gcs.prefix = "ecpds/incoming/"
```

```properties
gcs.bucketName = "auto-created-bucket"
gcs.mkBucket = "yes"
gcs.bucketLocation = "EU"
```

## Transfer

### Upload chunk size

GCS uploads use a resumable upload protocol. The `gcs.chunkSize` controls how much data is buffered and sent per HTTP request. It **must** be a multiple of 256 KiB. Larger chunks reduce request overhead but use more memory.

| Option | Default | Description |
| --- | --- | --- |
| `gcs.chunkSize` | *SDK default (~15 MB)* | Upload chunk size as a byte size value (e.g. `8m`, `32m`). Must be a multiple of `256k`. Values below `256k` are rejected. |

!!! warning
    If the provided `gcs.chunkSize` is not a multiple of 256 KiB, it is automatically rounded down. A value below 256 KiB is rejected with an error.

### Quick-start examples

```properties
gcs.chunkSize = "32m"
```

```properties
gcs.bucketName = "streaming-bucket"
gcs.prefix = "live/"
```

### Object naming

GCS object names are derived from the remote file path. If `gcs.bucketName` is set, the object name is `{prefix}{filename}`. Otherwise, the first path component is used as the bucket name and the remainder as the object key.

## Related

- [Transfer Modules overview](index.md)
- [Object Storage](../concepts/object-storage.md)
