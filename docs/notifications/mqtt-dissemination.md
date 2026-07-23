# Real-Time Data Dissemination with MQTT Broker

The MQTT Broker enables users of the dissemination service to subscribe to notifications,
ensuring they are alerted as soon as new products become available in the Data Store.

## Functional overview of the Notification System

The Notification System involves several key components:

- The **Product Data Store**.
- The **Dissemination and Acquisition Systems**.
- The **MQTT and Message Brokers**.

Below the Data Store, **OpenECPDS Destinations** serve to organise and manage products for
each customer. These [destinations](../concepts/entities.md#destinations-and-aliases)
include various configuration parameters, including an **MQTT topic** used for message
identification and routing to the appropriate MQTT subscribers.

A **notification** is triggered each time a product is added to the Data Store via the
Acquisition System. The product may either be made available through the Portal or pushed
to a remote site by the Dissemination System. In both cases, the user receiving the
notification will extract the product's location from the message:

- If the product is on the **Portal**, the user must retrieve it before processing.
- If it has been **pushed to a remote site**, it is immediately available for processing.

## Number of notifications

The number of notifications triggered when a new product is added depends on the
**destination's configuration**:

- **No notification** is sent if the destination is not configured to report updates.
- **One or two notifications** are sent if the destination is configured to notify one or
  both Brokers.

If the **MQTT Broker** is notified, it checks whether any MQTT clients have subscribed to
the destination's topic. If a matching subscription exists, the client receives the
notification along with the associated payload.

For the **Message Broker**, the process is simpler: upon receiving a notification, it
directly forwards it to the configured clients.

## Typical interaction in the OpenECPDS Notification System

The MQTT-based notification system follows a structured interaction between three key
components: the **MQTT Client**, the **MQTT Broker**, and the **OpenECPDS Data Store**.

- **Connection Establishment** — The process begins with the Client initiating a
  connection to the Broker by sending a **CONNECT** message. The Broker acknowledges the
  connection by responding with a **CONNACK** message.
- **Subscription to a Topic** — Once connected, the Client registers its interest in a
  specific topic by sending a **SUBSCRIBE** message to the Broker. The Broker then listens
  for relevant **PUBLISH** messages from the Data Store that match the subscribed topic.
- **Message Delivery and Product Retrieval** — When the Data Store generates a **PUBLISH**
  message for a subscribed topic, the Broker forwards it to all Clients that have
  subscribed to that topic. The Client then extracts the file location from the message
  and proceeds to fetch the product using one of the available protocols on the OpenECPDS
  Portal: **HTTPS, S3, SFTP, or FTP**.

## Retained messages and late client connections

When the **retain flag** is enabled in the MQTT message, even if the Data Store sends a
**PUBLISH** message before the Client is connected, the message remains available for
delivery. As a result, when the Client connects later, it still receives the retained
message, ensuring no critical notifications are missed. See
[Retained Messages in MQTT](mqtt-overview.md#retained-messages-in-mqtt).

## Configuration and access control

The MQTT configuration can be fine-tuned at the **destination level**, allowing for
greater flexibility in how notifications are handled. Additionally, **access control**
mechanisms can be configured at the **data user** level, ensuring secure and controlled
distribution of messages.

## Configuring a destination to publish MQTT notifications

### Step 1 — Enable event triggering and MQTT publishing

Add the following to the destination's **Properties** field. The `incoming.event` (for
portal uploads/downloads) or `acquisition.event` (for acquired files) flag is what causes
a publication to be queued; `mqtt.publish` then routes that publication to the MQTT broker.

```properties
incoming.event   = "yes"   # or acquisition.event = "yes" for acquired data
mqtt.publish     = "yes"
mqtt.contentType = "application/json"
mqtt.expiryInterval = "PT48H"
mqtt.retain      = "yes"
```

By default the topic is `destinationName/targetFileName`. Override it with `mqtt.topic`:

```properties
mqtt.topic = "my/custom/topic/"   # trailing slash -> target name is appended
```

### Step 2 — Build a rich JSON payload with JavaScript

For static payloads use `mqtt.payload` directly in Properties. For dynamic payloads that
embed file metadata and access URLs, place a JavaScript snippet in the destination's
**JavaScript** field. The script is executed for every completed transfer and can override
any `mqtt.*` option by returning a plain object.

See the [full reference and example](../concepts/destination-options.md#building-the-payload-with-javascript)
in the Destination Options guide, including a complete WIS2-style GeoJSON notification
with HTTPS, SFTP and S3 download links.

### Step 3 — Grant subscribe access to a data user

Set `portal.mqttPermission` on the data user's account (in their **Properties** field):

```properties
portal.mqttPermission = "#"           # all topics
# or restrict to a subtree:
portal.mqttPermission = "hourly_aq/#"
```

## Testing MQTT notifications

Once a destination is configured, test the subscription with
[mqttx](https://mqttx.app/cli) (recommended — supports `--insecure` without a CA file):

```bash
mqttx sub \
  -h localhost -p 8883 \
  -l mqtts \
  -u test -P test2021 \
  --insecure \
  -t '#'
```

Or with [Mosquitto](https://mosquitto.org/download/) (requires extracting the cert first):

```bash
# Fetch the broker's self-signed certificate directly
openssl s_client -connect localhost:8883 </dev/null 2>/dev/null \
  | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > /tmp/ecpds.pem

mosquitto_sub \
  --host localhost --port 8883 \
  --username test --pw test2021 \
  --cafile /tmp/ecpds.pem \
  --insecure \
  --topic '#' -v
```

!!! note
    Port `8883` is MQTTS (MQTT over TLS). The `--insecure` flag skips hostname
    verification for self-signed certificates. In production, use a properly signed
    certificate and remove `--insecure`.

## Related

- [MQTT Overview](mqtt-overview.md)
- [Automated Data Acquisition with MQTT Client](mqtt-acquisition.md)
- [Implementation Details](implementation.md)
- [Dissemination](../use-cases/dissemination.md)
