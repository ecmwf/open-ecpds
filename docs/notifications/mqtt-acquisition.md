# Automated Data Acquisition with MQTT Client

OpenECPDS includes an embedded **MQTT client** within its
[HTTP transfer module](../transfer-modules/http.md), enabling seamless integration with
remote data providers. When enabled on an [acquisition host](../concepts/entities.md#dissemination-and-acquisition-hosts),
this client can be configured to subscribe to specific topics on a remote MQTT broker,
allowing OpenECPDS to receive real-time notifications when new data becomes available.

## Retrieval options

Upon receiving an MQTT message, OpenECPDS offers two configurable options for data
retrieval:

- **Link Extraction for Scheduled Download** — The system can extract a link from the
  received MQTT message and register it for later download, triggered by the
  [acquisition scheduler](../use-cases/acquisition.md), ensuring efficient and automated
  data retrieval.
- **Inline Data Extraction** — Alternatively, the system can extract the data content
  directly from the MQTT message itself, allowing immediate processing without requiring a
  separate download step.

## Configuration

To ensure flexibility, OpenECPDS provides dedicated MQTT configuration parameters,
allowing fine-tuning of various protocol options such as connection settings, message
handling behaviour, and retry mechanisms. This ensures optimal performance and
adaptability across different use cases and infrastructures. These parameters are part of
the [HTTP/HTTPS Transfer Module](../transfer-modules/http.md) options.

## Related

- [MQTT Overview](mqtt-overview.md)
- [Real-Time Data Dissemination with MQTT Broker](mqtt-dissemination.md)
- [WMO WIS2 Integration](wmo-wis2.md)
- [Acquisition](../use-cases/acquisition.md)
- [HTTP/HTTPS Transfer Module](../transfer-modules/http.md)
