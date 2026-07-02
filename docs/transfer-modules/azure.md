# Azure Blob Storage Transfer Module

!!! info
    All options use the `azure.` prefix. Three authentication modes are supported: **Shared Key** (host Login/Password = account name/key), **SAS Token** (`azure.sasUrl` + `azure.sasSubscriptionKey`), and **Managed Identity** (`azure.userAssignedClientId`).

## Connection

### Endpoint URL

By default the module constructs the endpoint as `{scheme}://{host}:{port}` using the host record. Use `azure.url` to override with a full URL (e.g. Azurite emulator or sovereign cloud endpoint).

| Option | Default | Description |
| --- | --- | --- |
| `azure.url` | *scheme://host:port* | Full endpoint URL override (e.g. `http://127.0.0.1:10000/devstoreaccount1` for Azurite) |
| `azure.scheme` | `https` | URL scheme (`https` for production, `http` for local emulator) |
| `azure.port` | `443` | Port when constructing the endpoint URL from host + scheme |

### Quick-start examples

```properties
azure.scheme = "https"
azure.port = "443"
```

```properties
azure.url = "http://127.0.0.1:10000/devstoreaccount1"
azure.scheme = "http"
```

## Authentication

### Mode 1 — Shared Key (Storage Account Key)

The simplest method. Set the host *Login* to the storage account name and *Password* to the account key (base64-encoded). This is the default when no SAS URL or Managed Identity is configured.

| Field | Value |
| --- | --- |
| Host *Login* | Storage account name (e.g. `myaccount`) |
| Host *Password* | Storage account key (base64 string from Azure portal) |

### Mode 2 — SAS Token (via API gateway)

Obtain a Shared Access Signature token from a REST endpoint on each connection. Useful when tokens are managed centrally or expire frequently.

| Option | Default | Description |
| --- | --- | --- |
| `azure.sasUrl` | *empty* | URL of the SAS token endpoint (GET request returns the token) |
| `azure.sasSubscriptionKey` | *empty* | Value sent as the `Ocp-Apim-Subscription-Key` header on the SAS token request |

!!! warning
    Both `azure.sasUrl` and `azure.sasSubscriptionKey` must be set together. If either is empty, the module falls through to Shared Key or Managed Identity.

### Mode 3 — Managed Identity

Use an Azure user-assigned managed identity. No passwords stored in the host record. Requires the ECpds host to run in Azure (VM, AKS, App Service, etc.) and have the identity assigned.

| Option | Default | Description |
| --- | --- | --- |
| `azure.userAssignedClientId` | *empty* | Client ID of the user-assigned managed identity. When set, Managed Identity authentication is used (Shared Key and SAS are ignored). |

### Auth priority

1. If **Login + Password** (account name + key) are both set → **Shared Key**
2. Else if **sasUrl + sasSubscriptionKey** are both set → **SAS Token**
3. Else if **userAssignedClientId** is set → **Managed Identity**

### Quick-start examples

```properties
azure.sasUrl = "https://apim.example.com/storage/sas"
azure.sasSubscriptionKey = "abc123key"
```

```properties
azure.userAssignedClientId = "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
```

## Container

### Container selection

The container name can be embedded in the URL path (`user:pass@host/containerName`) or set explicitly. If both are present, the option value takes precedence.

| Option | Default | Description |
| --- | --- | --- |
| `azure.containerName` | *from URL path* | Fixed container name for all transfers. When set, all blobs are placed in this container. |
| `azure.mkContainer` | `false` | Automatically create the container if it does not exist |

### Listing ownership

| Option | Default | Description |
| --- | --- | --- |
| `azure.ftpuser` | *storage account name* | Owner user name shown in generated directory listing entries |
| `azure.ftpgroup` | *storage account name* | Owner group name shown in generated directory listing entries |

### Quick-start example

```properties
azure.containerName = "ecpds-data"
azure.mkContainer = "yes"
```

## Transfer

### Upload strategy

Small files (below `azure.multipartSize`) are uploaded in a single PUT. Larger files use block-blob parallel upload with configurable block size and concurrency.

| Option | Default | Description |
| --- | --- | --- |
| `azure.multipartSize` | `256m` | Files at or above this size use parallel block upload; smaller files use a single PUT |
| `azure.blockSize` | `10k` | Size of each block in a parallel block-blob upload (e.g. `4m`, `100m`) |
| `azure.numBuffers` | `5` | Maximum number of concurrent block upload buffers (parallelism) |
| `azure.chunkSize` | `0` (disabled) | Flux buffer chunk size when reading the input stream (0 = unbuffered). Rarely needs changing. |

### Integrity & overwrite

| Option | Default | Description |
| --- | --- | --- |
| `azure.overwrite` | `true` | Overwrite existing blobs. Set to `false` to fail if the blob already exists. |
| `azure.ignoreDelete` | `true` | Ignore errors when deleting a blob before overwrite. Set to `false` to fail if the pre-delete step errors. |
| `azure.ignoreCheck` | `true` | Skip post-transfer size/ETag verification. Set to `false` to verify blob properties after upload. |
| `azure.useMD5` | `false` | Compute and send a Content-MD5 header on uploads for server-side integrity checking |

### Quick-start examples

```properties
azure.multipartSize = "64m"
azure.blockSize = "4m"
azure.numBuffers = "10"
azure.overwrite = "yes"
```

```properties
azure.ignoreDelete = "no"
azure.ignoreCheck = "no"
azure.useMD5 = "yes"
```

## Related

- [Transfer Modules overview](index.md)
- [Object Storage](../concepts/object-storage.md)
