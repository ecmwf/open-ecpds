---
title: REST API
---

# REST API Reference (v1)

OpenECPDS exposes a versioned JSON REST API that allows external systems to manage incoming users, destinations, metadata, monitoring data, and data files programmatically.

## Base URL

```
https://<host>/ecpds/v1/
```

All endpoints are mounted under the `/ecpds/` context of the Monitor/Master server. The API version prefix `v1` is part of every path.

!!! warning "HTTPS only"
    Every API call **must** be made over HTTPS. Requests over plain HTTP are rejected with `403 Forbidden`.

---

## Authentication

All endpoints (except `GET /v1/version`) require HTTP **Basic Authentication**.

Supply credentials in the `Authorization` header:

```
Authorization: Basic <base64(username:password)>
```

=== "curl"

    ```bash
    curl -u apiuser:secret https://<host>/ecpds/v1/version
    ```

    !!! tip "Pretty-print the JSON response"
        Responses are returned as a single line of compact JSON. Pipe `curl`'s output through
        [`jq`](https://jqlang.github.io/jq/) (or `python3 -m json.tool` if `jq` is not installed) to format it:

        ```bash
        curl -sk -u apiuser:secret https://<host>/ecpds/v1/monitoring/summary | jq .
        # or, without jq:
        curl -sk -u apiuser:secret https://<host>/ecpds/v1/monitoring/summary | python3 -m json.tool
        ```

        `jq` can also filter/reshape the output directly, e.g. to list only the product and status of each entry:

        ```bash
        curl -sk -u apiuser:secret https://<host>/ecpds/v1/monitoring/summary \
          | jq '.stepStatusList[] | {product, time, step, type, status}'
        ```

=== "Python"

    ```python
    import requests
    resp = requests.get(
        "https://<host>/ecpds/v1/version",
        auth=("apiuser", "secret"),
        verify=True,
    )
    print(resp.json())
    ```

If the `Authorization` header is missing or malformed, the API returns `401 Unauthorized`.

---

## Permission Configuration

!!! tip "Prefer the API Client web interface"
    The recommended way to create and manage API clients is the **API Client Server Permissions** page
    (`/do/user/api`) in the web interface, rather than editing the `[API]` section of the properties file
    directly. When a client is created there, a random secret is generated and shown to you **once**; only
    its hash is stored server-side, and it can be reset at any time without editing configuration files or
    restarting the server. Permissions can also be toggled per-service from the same page. Setting plaintext
    passwords directly in the properties file still works (for backward compatibility, and for values already
    provisioned at startup), but is **not recommended** since the password is stored and distributed in clear
    text wherever that configuration file is deployed.

API users and their allowed operations can also be defined in the OpenECPDS properties file under the `[API]`
section — this is mainly useful for bootstrapping the first client(s) before the web interface is available, or
for automated deployments that provision configuration files. Entries found here are migrated into the
database-backed API client store on startup (and then manageable from `/do/user/api` like any other client).
The format is:

```ini
[API]
<username>=<password>:<service-regex>
```

| Field | Description |
|---|---|
| `<username>` | The API username (matches the Basic auth username) |
| `<password>` | The plaintext API password (matches the Basic auth password) |
| `<service-regex>` | A Java regular expression matched against the **service name** of the operation being called |

**Service names** are the internal method names listed in each endpoint's description below.

### Examples

Allow a user to call all services:

```ini
[API]
myuser=mysecret:.*
```

Allow a user only to read destination metadata (GET operations):

```ini
[API]
readonly=readpass:getDestinationMetaFields|getDestinationMetaValuesByDestination
```

Allow a user to manage incoming users and associations, but nothing else:

```ini
[API]
incomingmgr=pass123:incomingUser.*|incomingCategory.*|incomingAssociation.*
```

Allow a user to submit data files and manage destination backups:

```ini
[API]
datapipeline=pipepass:datafilePut|datafileDel|datafileSize|destinationBackup.*
```

Allow a user to only read the product monitoring summary (e.g. an operator-facing tool or dashboard):

```ini
[API]
ops=opspass:monitoringSummaryList
```

!!! note "No permissions = no access"
    If a username is not listed in the `[API]` section, or the operation does not match the regex, the request is rejected with a `DataBaseException: User not authorized` error.

---

## Response Format

All responses are JSON objects. Successful responses include `"status": "ok"`:

```json
{ "status": "ok", "fieldName": ... }
```

Error responses include `"status": "error"` and a `"message"` field:

```json
{ "status": "error", "message": "User not authorized" }
```

HTTP-level errors (auth failures, missing parameters) return standard HTTP status codes (`400`, `401`, `403`, `412`) with a plain-text body.

---

## Endpoints

### System

#### `GET /v1/version`

Returns the current OpenECPDS version. No authentication required.

**Parameters:** none

**Response:**
```json
{ "status": "ok", "version": "6.7.7-20240701" }
```

**Service name:** *(none — open to all)*

---

### Incoming Users

These endpoints manage incoming user accounts (data consumers/publishers) and their category and destination associations.

#### `POST /v1/incoming/user/add`

Creates a new incoming user with a password.

**Query parameters:**

| Parameter | Required | Description |
|---|---|---|
| `id` | ✅ | Username for the new incoming user |
| `pass` | ✅ | Password |
| `email` | ✅ | Email address |
| `iso` | ✅ | ISO country code (2-letter) |
| `portalService` | ❌ | Data Portal access mode: `standard-login`, `open-access`, or `self-service`. Defaults to `standard-login` on creation; on update, an omitted/empty value leaves the existing mode unchanged. See [Data Users — Portal Service modes](use-cases/data-users.md#portal-service-modes) |
| `active` | ❌ | `true`/`false`. Whether the user is active (allowed to authenticate/be associated with destinations). Defaults to `false` on creation; on update, an omitted/empty value leaves the existing flag unchanged. Equivalent to (and overridden by) the `incoming/category/add` "ECPDS" marker convention described below |

**Service name:** `incomingUserAdd`

**Response:**
```json
{ "status": "ok" }
```

---

#### `POST /v1/incoming/user/add2`

Creates or updates an incoming user without a password (password set separately or via other means).

**Query parameters:**

| Parameter | Required | Description |
|---|---|---|
| `id` | ✅ | Username |
| `email` | ✅ | Email address |
| `iso` | ✅ | ISO country code |
| `portalService` | ❌ | Data Portal access mode: `standard-login`, `open-access`, or `self-service`. Defaults to `standard-login` if omitted |
| `active` | ❌ | `true`/`false`. Whether the user is active. Defaults to `true` if omitted |

**Service name:** `incomingUserAdd2`

---

#### `GET /v1/incoming/user/list`

Lists all incoming users, optionally filtered by destination.

**Query parameters:**

| Parameter | Required | Description |
|---|---|---|
| `destination` | ❌ | Filter users associated with this destination name |

**Service name:** `incomingUserList`

**Response:**
```json
{
  "status": "ok",
  "incomingUserList": ["user1", "user2"]
}
```

---

#### `DELETE /v1/incoming/user/del/{id}`

Deactivates (soft-deletes) an incoming user.

**Path parameters:**

| Parameter | Description |
|---|---|
| `id` | Username of the incoming user to deactivate |

**Service name:** `incomingUserDel`

**Response:**
```json
{ "status": "ok" }
```

---

#### `POST /v1/incoming/category/add`

Assigns one or more categories to an incoming user. The request body is a JSON array of category names.

**Query parameters:**

| Parameter | Required | Description |
|---|---|---|
| `id` | ✅ | Username of the incoming user |

**Request body:** `Content-Type: application/json`

```json
["category1", "category2"]
```

**Service name:** `incomingCategoryAdd`

**Response:**
```json
{ "status": "ok" }
```

!!! warning "Activation required before associating a destination"
    A newly created incoming user (via `incoming/user/add` or `incoming/user/add2`) is **inactive** by
    default unless `active=true` is passed at creation time. Calling `incoming/association/add` on an
    inactive user fails with `User <id> not found/active`.

    The simplest way to activate a user is the `active` parameter on `incoming/user/add`/`add2` (see
    above), either at creation time or in a follow-up call to `incoming/user/add`.

    Alternatively, `incoming/category/add` can also activate/deactivate a user as a side effect: passing
    a category name that contains `ECPDS` (case-insensitive), e.g. `["ECPDS"]`, activates the user — this
    is a special marker, not an actual category that needs to exist in the `CATEGORY` table (the
    category list itself is not stored). Omitting an `ECPDS` marker deactivates the user again. This
    predates the `active` parameter and is kept for backward compatibility, but `active` is the more
    direct and self-explanatory option for new integrations.

---

#### `POST /v1/incoming/association/add`

Associates an incoming user with a destination.

**Query parameters:**

| Parameter | Required | Description |
|---|---|---|
| `id` | ✅ | Incoming username |
| `destination` | ✅ | Destination name |

**Service name:** `incomingAssociationAdd`

**Response:**
```json
{ "status": "ok" }
```

---

#### `DELETE /v1/incoming/association/del`

Removes the association between an incoming user and a destination.

**Query parameters:**

| Parameter | Required | Description |
|---|---|---|
| `id` | ✅ | Incoming username |
| `destination` | ✅ | Destination name |

**Service name:** `incomingAssociationDel`

**Response:**
```json
{ "status": "ok" }
```

---

#### `GET /v1/incoming/association/list`

Lists all destination associations for an incoming user.

**Query parameters:**

| Parameter | Required | Description |
|---|---|---|
| `id` | ✅ | Incoming username |

**Service name:** `incomingAssociationList`

**Response:**
```json
{
  "status": "ok",
  "associationList": ["destination_a", "destination_b"]
}
```

---

### Destinations

#### `GET /v1/destination/list`

Returns a list of destinations, optionally filtered.

**Query parameters:**

| Parameter | Required | Description |
|---|---|---|
| `iso` | ❌ | ISO country code filter |
| `id` | ❌ | Incoming username — returns only destinations associated with this user |
| `type` | ❌ | Destination type filter (integer) |

**Service name:** `destinationList`

**Response:**
```json
{
  "status": "ok",
  "destinationList": [
    { "name": "dest_a", "type": 0, "country": "DE", ... },
    ...
  ]
}
```

---

#### `GET /v1/destination/{name}`

Returns details for a single destination.

**Path parameters:**

| Parameter | Description |
|---|---|
| `name` | Destination name |

**Service name:** *(basic auth only — no per-service check)*

**Response:**
```json
{
  "status": "ok",
  "destination": { "name": "dest_a", "type": 0, ... }
}
```

---

#### `GET /v1/destination/type/list`

Returns all available destination types.

**Parameters:** none

**Service name:** *(basic auth only)*

**Response:**
```json
{
  "status": "ok",
  "typeList": ["dissemination", "acquisition", ...]
}
```

---

#### `GET /v1/destination/country/list`

Returns all countries that have at least one destination associated.

**Parameters:** none

**Service name:** `destinationCountryList`

**Response:**
```json
{
  "status": "ok",
  "countryList": ["DE", "FR", "GB", ...]
}
```

---

### Destination Metadata

The metadata endpoints allow reading and writing structured metadata fields attached to destinations. Fields are defined globally via the Metadata Field Definitions admin page (`/do/admin/metafields`). All metadata is grouped by **category** (e.g. `General`, `Contacts`, `Data`, `Storage`, `Documentation`, `Procedures`, `Alerts`) in both GET responses and PUT request bodies.

#### `GET /v1/destination/metadata/fields`

Returns all **active** metadata field definitions (name, label, type, category, etc.). Use this as a reference catalogue of all available field names and their types.

**Parameters:** none

**Service name:** `getDestinationMetaFields`

**Response:**
```json
{
  "status": "ok",
  "fields": [
    {
      "id": 1,
      "name": "organisationWebPage",
      "label": "Organisation Web Page",
      "type": "url",
      "category": "General",
      "active": true
    },
    {
      "id": 8,
      "name": "computerOperations",
      "label": "Computer Operations",
      "type": "contact",
      "category": "Contacts",
      "active": true
    },
    ...
  ]
}
```

**Field types:**

| Type | Description |
|---|---|
| `text` | Plain text |
| `textarea` | Multi-line text |
| `url` | URL string |
| `email` | Email address |
| `phone` | Phone number |
| `password` | Stored encrypted |
| `contact` | JSON object `{"name","email","phone","fax"}` |
| `mail-group` | JSON object `{"name","email"}` |
| `switchboard` | JSON object `{"name","phone"}` |

---

#### `GET /v1/destination/{name}/metadata`

Returns all metadata values for a destination grouped by category. Fields with no value are included as `null`. Multi-value fields (e.g. multiple contacts) are returned as arrays.

**Path parameters:**

| Parameter | Description |
|---|---|
| `name` | Destination name |

**Service name:** `getDestinationMetaValuesByDestination`

**Response:**
```json
{
  "status": "ok",
  "destination": "hourly_aq",
  "exportedAt": "2026-07-13T09:00:00Z",
  "metadata": {
    "General": {
      "organisationWebPage": "https://www.example.org/",
      "SADNumber": null,
      "contractId": null,
      "generalComments": null
    },
    "Contacts": {
      "computerOperations": [
        { "name": "Alice Smith", "email": "alice@example.org", "phone": "+44 1234 567890", "fax": null },
        { "name": "Bob Jones",  "email": "bob@example.org",   "phone": null,               "fax": null }
      ],
      "meteorologists": null,
      "switchboard": { "name": "Switchboard", "phone": "+44 1234 000000" },
      "mailGroup": { "name": "ops-list", "email": "ops@example.org" }
    },
    "Documentation": {
      "documentationUrl": "https://docs.example.org/"
    }
  }
}
```

!!! note
    Structured field types (`contact`, `mail-group`, `switchboard`) are returned as nested JSON objects rather than raw strings. The exact keys available depend on the field type (see the table under `GET /v1/destination/metadata/fields`).

---

#### `PUT /v1/destination/{name}/metadata`

Replaces **all** metadata values for a destination. Existing values are removed and replaced atomically. The request body must use the same grouped-by-category structure as the GET response — you can GET, modify values, and PUT back without any format transformation.

Fields set to `null` or omitted are skipped (no value stored). Multi-value fields accept either a single value or an array.

**Path parameters:**

| Parameter | Description |
|---|---|
| `name` | Destination name |

**Request body:** `Content-Type: application/json`

```json
{
  "metadata": {
    "General": {
      "organisationWebPage": "https://www.example.org/",
      "generalComments": "Updated via REST API"
    },
    "Contacts": {
      "computerOperations": [
        { "name": "Alice Smith", "email": "alice@example.org", "phone": "+44 1234 567890" },
        { "name": "Bob Jones",   "email": "bob@example.org" }
      ],
      "mailGroup": { "name": "ops-list", "email": "ops@example.org" }
    }
  }
}
```

| Body field | Type | Description |
|---|---|---|
| `metadata` | object | Required. Top-level object keyed by category name |
| `metadata.<category>` | object | Field name → value mapping for that category |
| `metadata.<category>.<fieldName>` | string / object / array / null | Value(s) for the field. Use field names from `/v1/destination/metadata/fields`. Structured types (`contact` etc.) are JSON objects. Multi-value fields accept an array. `null` means no value. |

**Service name:** `setDestinationMetaValues`

**Response:**
```json
{
  "status": "ok",
  "destination": "hourly_aq",
  "count": 3
}
```

!!! tip "Round-trip workflow"
    The simplest way to update metadata is: `GET` the current values, edit the returned `metadata` object, then `PUT` it back. The format is identical in both directions.

---

### Destination Backups

Destination backup operations allow exporting and restoring destination configurations.

#### `GET /v1/destination/backup`

Returns backup data for a set of destinations, optionally filtered.

**Query parameters:**

| Parameter | Required | Description |
|---|---|---|
| `iso` | ❌ | ISO country code filter |
| `id` | ❌ | Incoming username filter |
| `type` | ❌ | Destination type filter (integer) |

**Service name:** `destinationBackupList`

---

#### `GET /v1/destination/backup/{name}`

Returns backup data for a single destination.

**Path parameters:**

| Parameter | Description |
|---|---|
| `name` | Destination name |

**Service name:** `destinationBackupList`

---

#### `PUT /v1/destination/backup`

Restores one or more destinations from a backup JSON structure.

**Query parameters:**

| Parameter | Required | Description |
|---|---|---|
| `copySharedHost` | ❌ | If `true`, also copies shared hosts referenced by the destinations |

**Request body:** `Content-Type: application/json`

```json
{
  "backup": { ... }
}
```

**Service name:** `putDestinationBackup`

**Response:**
```json
{
  "status": "ok",
  "message": "Number of Destination(s) created: 3"
}
```

---

### Hosts

#### `POST /v1/host/option`

Sets a configuration option on a host. The `name` parameter uses dot-notation `<module>.<key>`.

**Query parameters:**

| Parameter | Required | Description |
|---|---|---|
| `hostid` | ✅ | The host ID |
| `name` | ✅ | Option name in `<module>.<key>` format (e.g. `FTP.passive`) |

**Form body:** `Content-Type: application/x-www-form-urlencoded`

| Field | Required | Description |
|---|---|---|
| `value` | ✅ | The new option value |

**Service name:** `updateHostOption`

**Response:**
```json
{ "status": "ok" }
```

---

### Monitoring

#### `GET /v1/monitoring/summary`

Returns the merged step-status list for **every product and cycle** currently known (the same data shown in the
web interface's "All Cycles and Products" view at `/do/monitoring/summary/`), optionally narrowed down with the
query filters below.

**Query parameters:**

| Parameter | Required | Description |
|---|---|---|
| `product` | ❌ | Product name filter (exact match, case-insensitive) |
| `time` | ❌ | Cycle/reference time filter (exact match, case-insensitive, e.g. `00`, `12`) |
| `step` | ❌ | Step filter (integer, as string) |
| `type` | ❌ | Step type filter (e.g. `FC`), case-insensitive |
| `status` | ❌ | Raw generation status code filter (e.g. `DONE`, `INIT`), case-insensitive |

**Service name:** `monitoringSummaryList`

**Response:**
```json
{
  "status": "ok",
  "stepStatusList": [
    {
      "product": "DisseminationProducts", "time": "00", "buffer": 0, "step": 0, "type": "FC",
      "status": "Done", "statusCode": "DONE", "statusLevel": 1,
      "arrivalTime": 1732012345, "scheduledTime": 1732012000, "lastUpdate": 1732012345,
      "productTime": 1731984000, "minutesBeforeSchedule": 5
    },
    ...
  ]
}
```

---

#### `GET /v1/monitoring/summary/{product}`

Returns the merged step-status list for **every cycle** currently known for one product (the same data shown in
the web interface's "All Cycles" view at `/do/monitoring/summary/{product}`), optionally narrowed down with the
query filters below.

**Path parameters:**

| Parameter | Description |
|---|---|
| `product` | Product name (e.g. `DisseminationProducts`) |

**Query parameters:**

| Parameter | Required | Description |
|---|---|---|
| `time` | ❌ | Cycle/reference time filter (exact match, case-insensitive) |
| `step` | ❌ | Step filter (integer, as string) |
| `type` | ❌ | Step type filter, case-insensitive |
| `status` | ❌ | Raw generation status code filter, case-insensitive |

**Service name:** `monitoringSummaryList`

**Response:** same shape as `GET /v1/monitoring/summary` above.

---

#### `GET /v1/monitoring/summary/{product}/{time}`

Returns the current monitoring step-status summary for a given product and reference time, optionally narrowed
down with the query filters below.

**Path parameters:**

| Parameter | Description |
|---|---|
| `product` | Product name (e.g. `DisseminationProducts`) |
| `time` | Reference time string (e.g. `00`, `12`) |

**Query parameters:**

| Parameter | Required | Description |
|---|---|---|
| `step` | ❌ | Step filter (integer, as string) |
| `type` | ❌ | Step type filter, case-insensitive |
| `status` | ❌ | Raw generation status code filter, case-insensitive |

**Service name:** `monitoringSummaryList`

**Response:**
```json
{
  "status": "ok",
  "stepStatusList": [
    { "step": 0, "type": "FC", "status": "done", ... },
    ...
  ]
}
```

---

#### `GET /v1/monitoring/summary/{product}/{time}/{step}/{type}`

Returns the historical status list for a specific product step.

**Path parameters:**

| Parameter | Description |
|---|---|
| `product` | Product name |
| `time` | Reference time string |
| `step` | Step number (integer, as string) |
| `type` | Step type (e.g. `FC`) |

**Service name:** `monitoringSummaryList`

**Response:**
```json
{
  "status": "ok",
  "stepStatusHistoryList": [ ... ]
}
```

---

### Data Files

#### `GET /v1/datafile/put`

Registers a new data file for dissemination to a destination. The file must already be accessible at the given `source` URL or path.

**Query parameters:**

| Parameter | Required | Description |
|---|---|---|
| `destination` | ✅ | Destination name to disseminate to |
| `source` | ✅ | Source URL or path of the file |
| `uniquename` | ❌ | Unique identifier for the transfer (prevents duplicates) |
| `target` | ❌ | Target filename at the destination (defaults to source filename) |
| `metadata` | ❌ | Optional metadata string attached to the transfer |
| `priority` | ❌ | Transfer priority (integer, lower = higher priority) |
| `lifetime` | ❌ | Expiry duration string (e.g. `7d`, `24h`) |
| `at` | ❌ | Scheduled start time (ISO-8601 or relative string) |
| `standby` | ❌ | If `true`, transfer is queued in standby mode |
| `force` | ❌ | If `true`, re-queues even if the file was already transferred |

**Service name:** `datafilePut`

**Response:**
```json
{
  "status": "ok",
  "id": 123456
}
```

The returned `id` is the internal data file identifier, usable with `datafileSize` and `datafileDel`.

---

#### `GET /v1/datafile/size`

Returns the size (in bytes) of a data file by its internal ID.

**Query parameters:**

| Parameter | Required | Description |
|---|---|---|
| `id` | ✅ | Internal data file ID (long) |

**Service name:** `datafileSize`

**Response:**
```json
{
  "status": "ok",
  "size": 1048576
}
```

---

#### `DELETE /v1/datafile/del`

Deletes a data file from the system by its internal ID.

**Query parameters:**

| Parameter | Required | Description |
|---|---|---|
| `id` | ✅ | Internal data file ID (long) |

**Service name:** `datafileDel`

**Response:**
```json
{ "status": "ok" }
```

---

## Error Reference

| HTTP Status | Meaning |
|---|---|
| `200 OK` | Request succeeded |
| `400 Bad Request` | Malformed request |
| `401 Unauthorized` | Missing or invalid `Authorization` header |
| `403 Forbidden` | Request made over plain HTTP (HTTPS required) |
| `412 Precondition Failed` | A required parameter is missing or null |
| `500` (JSON `"status": "error"`) | Server-side error; see `"message"` for details including `User not authorized` |

---

## Complete Configuration Example

The following shows a typical `[API]` section for a production configuration with multiple API users:

```ini
[API]
# Full administrative access
admin_api=Admin$ecret99:.*

# Read-only metadata access
metadata_reader=MdRead42:getDestinationMetaFields|getDestinationMetaValuesByDestination

# Pipeline user: submit and delete files, read destination info
pipeline=Pipe#pass88:datafilePut|datafileDel|datafileSize|destinationList|destination

# Incoming user management (e.g. called from a provisioning system)
provisioning=Prov&key77:incomingUser.*|incomingCategory.*|incomingAssociation.*
```

!!! tip "Regex tips"
    - Use `.*` to allow all services (full admin).
    - Use `|` to separate multiple allowed service names.
    - Java regex is used — `.*` and `\b` are valid; test patterns carefully.
    - Service names are **case-sensitive** and must match exactly as documented above.

## Related

- [OpenECPDS Entities](concepts/entities.md)
- [Product Descriptions](administration/product-descriptions.md)
- [Data Ownership & Catalogue](architecture/data-ownership-and-catalogue.md)
- [Monitoring](monitor-ui/monitoring.md)
