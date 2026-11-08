# NTF (Notification Outcome) Fields

The **NTF** event records the outcome of an MQTT publication for each configured
Data Mover. The Master emits one record after each Mover's result is known,
including disconnected Movers. If no Movers are configured, it emits one `FAILED`
record with an empty `MoverName`.

The Splunk file and native OpenTelemetry outputs are independently configured,
as for other operational events. OpenTelemetry uses event name `ecpds.NTF`,
body `NTF` and severity `INFO`; filter `Status` for failures.

## Example

=== "Splunk (splunk.log)"

    ```text
    NTF;TimeStamp=2026-11-05 16:00:00.0;PublicationId=42;DataTransferId=123;DestinationName=mqtt_demo;TransferProtocol=mqtt;MoverName=ecpds-mover;Topic=openecpds/demo/file.dat;FieldEncoding=percent-record-separators;QoS=1;Retain=false;PayloadBytes=160;Status=ACCEPTED;SubscriberDeliveryConfirmed=false
    ```

=== "OpenTelemetry"

    Simplified Collector/backend representation, not the raw OTLP payload.
    See [Reading the examples](overview.md#reading-the-examples).

    ```json
    {
      "resource": {
        "service.name": "open-ecpds-master",
        "service.instance.id": "master.example.org"
      },
      "scope": {
        "name": "ecmwf.ecpds.operational-events"
      },
      "timestamp": "2026-11-05T16:00:00Z",
      "severityText": "INFO",
      "eventName": "ecpds.NTF",
      "body": "NTF",
      "attributes": {
        "ecpds.event.id": "c589192c-9247-48bf-aeb3-08ae2032e108",
        "ecpds.event.category": "NTF",
        "TimeStamp": "2026-11-05 16:00:00.0",
        "PublicationId": 42,
        "DataTransferId": 123,
        "DestinationName": "mqtt_demo",
        "TransferProtocol": "mqtt",
        "MoverName": "ecpds-mover",
        "Topic": "openecpds/demo/file.dat",
        "FieldEncoding": "percent-record-separators",
        "QoS": 1,
        "Retain": false,
        "PayloadBytes": 160,
        "Status": "ACCEPTED",
        "SubscriberDeliveryConfirmed": false
      }
    }
    ```

## Fields

| Field | Description |
|-------|-------------|
| **TimeStamp** | Time the Master records the result, formatted in its JVM timezone. |
| **PublicationId** | Identifier used to correlate with MQTT Activity. |
| **DataTransferId** | Identifier used to correlate with transfer history. |
| **DestinationName** | Destination whose notification is being published. |
| **TransferProtocol** | `mqtt`. |
| **MoverName** | Data Mover hosting the broker, or empty when no Movers are configured. |
| **Topic** | Publication topic. |
| **FieldEncoding** | `percent-record-separators`: in destination, Mover and topic strings, `%`, `;`, CR and LF are escaped as `%25`, `%3B`, `%0D` and `%0A` in both outputs. Decode once to recover the original value. |
| **QoS** | Effective MQTT QoS, 0, 1 or 2. |
| **Retain** | Effective MQTT retain flag. |
| **PayloadBytes** | UTF-8 encoded notification length in bytes, not its content. |
| **Status** | `ACCEPTED`, `FAILED` or `UNCONFIRMED` for this Mover's publication result. Timeouts and uncertain outcomes are `UNCONFIRMED`. |
| **SubscriberDeliveryConfirmed** | Always `false`: broker acceptance does not prove subscriber receipt. |

The record contains no message body, inline file content, client ID or credentials.
It does not create a new trace/span or infer a dissemination attempt from the
transfer ID. Publication script failures or skips happen before broker submission
and appear in MQTT Activity rather than as broker-outcome `NTF` records.

## Related

- [Event Logging overview](overview.md)
- [Real-Time Data Dissemination with MQTT](../notifications/mqtt-dissemination.md)
