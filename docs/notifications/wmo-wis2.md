# Integration with WMO WIS2 Using MQTT

Thanks to its MQTT implementation, OpenECPDS has been successfully integrated with the
**WMO WIS2** infrastructure, which also relies on MQTT.

This allows OpenECPDS to function both as:

- A **WIS2 data provider**, publishing data to the network, and
- A **WIS2 client**, subscribing to relevant data streams.

This dual role ensures seamless interoperability with WIS2 and enhances the exchange of
meteorological data within the global community.

## Optional WIS2 acquisition profile

MQTT is a generic transport. Acquisition hosts use script-based extraction by
default; an administrator can explicitly select the bundled WIS2 profile:

```properties
http.mqttMode = "yes"
http.mqttProfile = "wis2"
http.ftpLike = "yes"
acquisition.noRetrieval = "no"
acquisition.useSymlink = "yes"
acquisition.requeueOnFailure = "yes"
```

Configure the broker address, credentials, TLS and session settings as for any
[MQTT acquisition host](mqtt-acquisition.md). The host Directory is the topic filter,
for example `origin/a/wis2/+/+/data/core/#` or `cache/a/wis2/+/+/data/core/#`.
The profile interprets acquisition messages on that host. Set
`http.mqttProfile = "script"` to select script-based extraction.

This profile uses the shared MQTT acquisition engine, including inline file
ingestion and OpenECPDS integrity controls. It provides built-in interpretation
of WIS2 notifications, structured attribution and verification against a
producer-advertised digest.

The WIS2 profile:

- Validates the fields used for acquisition: data topic, message ID, full `data_id`,
  publication timestamp, selected retrieval link, advertised length and integrity.
  It prefers an `update` link over `canonical`, and uses the first URL when that
  link advertises an array of alternatives.
- Registers versions using the full data identity, normalized publication timestamp
  and full advertised digest.
  Identical relays within the same destination/target therefore share a scheduled
  file; later publication timestamps or different digests identify another version.
- Checks advertised Base64 SHA-256, SHA-384 or SHA-512 digests against the received
  bytes before a scheduled retrieval is marked complete. Algorithm and digest
  validation rejects unsupported algorithms and malformed digests.
  A mismatch fails the retrieval and removes the failed local file through the
  existing retrieval cleanup. Internal OpenECPDS checksums remain independent.
- Uses shared MQTT inline-content ingestion to interpret WIS2
  `properties.content` directly through its built-in decoder. The profile
  accepts Base64 content up to 16 KiB **decoded**, including empty files, and
  checks its declared length and advertised digest before submission. If the
  length is absent, it uses the decoded byte count. Notifications include a valid
  `canonical` or `update` link; inline content is submitted directly as file bytes.
  The acquisition card displays the availability and value of the advertised
  digest. Inline-content validation applies the WIS2 profile's limits.
- Preserves notification attribution in the existing file metadata store, including
  identities longer than one metadata value column. **Data Files → file details**
  shows an **Acquisition Notification** card alongside normal acquisition state
  and transfer history. Rejected messages are reported in acquisition listing
  progress and Mover logs.

The WIS2 notification payload is bounded to 1 MiB. Stored notification metadata is
bounded to 16 KiB and each identity/topic field to 4,096 characters.
Reserved `notify.chunks` and `notify.<number>` attributes encode that
metadata in existing bounded value columns.
Invalid or incomplete stored attribution blocks retrieval.

Advertised-digest checks follow the notification metadata attached to each file,
independently of the host's current profile selection.

MQTT subscriptions run within acquisition listing cycles.
Broker-side session recovery depends on the configured QoS, session expiry,
stable client identity and clean-start settings.

## Related

- [MQTT Overview](mqtt-overview.md)
- [Real-Time Data Dissemination with MQTT Broker](mqtt-dissemination.md)
- [Automated Data Acquisition with MQTT Client](mqtt-acquisition.md)
- [Implementation Details](implementation.md)
