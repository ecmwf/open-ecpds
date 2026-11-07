# Integration with WMO WIS2 Using MQTT

Thanks to its MQTT implementation, OpenECPDS has been successfully integrated with the
**WMO WIS2** infrastructure, which also relies on MQTT.

This allows OpenECPDS to function both as:

- A **WIS2 data provider**, publishing data to the network, and
- A **WIS2 client**, subscribing to relevant data streams.

This dual role ensures seamless interoperability with WIS2 and enhances the exchange of
meteorological data within the global community.

## Optional WIS2 acquisition profile

MQTT remains a generic transport. Existing acquisition hosts keep their script-based
extraction unless an administrator explicitly selects the bundled WIS2 profile:

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
The profile replaces the message-extraction script on that host; it does not change
destination publication scripts or start another broker. Set `http.mqttProfile = "script"`
to restore the existing extraction path.

The initial profile:

- Validates the fields used for acquisition: data topic, message ID, full `data_id`,
  publication timestamp, selected retrieval link, advertised length and integrity.
  It prefers an `update` link over `canonical`, and uses the first URL when that
  link advertises an array of alternatives.
- Registers versions using the full data identity, normalized publication timestamp
  and full advertised digest, rather than a mirror URL or broker message ID.
  Identical relays within the same destination/target therefore share a scheduled
  file; later publication timestamps or different digests identify another version.
- Checks advertised Base64 SHA-256, SHA-384 or SHA-512 digests against the received
  bytes before a scheduled retrieval is marked complete. Unsupported algorithms
  and malformed digests reject the notification, rather than disabling verification.
  A mismatch fails the retrieval and removes the failed local file through the
  existing retrieval cleanup. Internal OpenECPDS checksums remain independent.
- Accepts Base64 inline content up to 16 KiB, checking its declared length and
  advertised digest before submission. Missing integrity is allowed and is shown
  as **Not advertised**; this profile does not invent a producer digest.
- Preserves notification attribution in the existing file metadata store, including
  identities longer than one metadata value column. **Data Files → file details**
  shows an **Acquisition Notification** card alongside normal acquisition state
  and transfer history. Rejected messages are reported in acquisition listing
  progress and Mover logs, without recording their complete payload.

Notification metadata is bounded to 16 KiB and each identity/topic field to 4,096
characters. Reserved `notify.chunks` and `notify.<number>` attributes encode that
metadata in existing bounded value columns; do not edit these internal attributes.
Invalid or incomplete stored attribution blocks retrieval rather than silently
dropping its expected digest. No database schema migration is required.

Upgrade the Master, Monitor and participating Movers together before enabling the
profile. Previously scheduled files do not acquire integrity metadata retroactively.
Changing a host back to script mode does not remove integrity checks from files
already registered with notification metadata.

This is a data acquisition profile, not full WNM JSON Schema validation or WMO Global
Cache certification. It does not enforce cross-publication lineage rules, provide
historical replay, choose weighted cache candidates, or provide a separate incoming
activity dashboard with deadline outcomes. Those are separate increments. Durable
MQTT sessions still depend on broker retention and stable client identity.

## Related

- [MQTT Overview](mqtt-overview.md)
- [Real-Time Data Dissemination with MQTT Broker](mqtt-dissemination.md)
- [Automated Data Acquisition with MQTT Client](mqtt-acquisition.md)
- [Implementation Details](implementation.md)
