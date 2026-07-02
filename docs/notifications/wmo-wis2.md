# Integration with WMO WIS2 Using MQTT

Thanks to its MQTT implementation, OpenECPDS has been successfully integrated with the
**WMO WIS2** infrastructure, which also relies on MQTT.

This allows OpenECPDS to function both as:

- A **WIS2 data provider**, publishing data to the network, and
- A **WIS2 client**, subscribing to relevant data streams.

This dual role ensures seamless interoperability with WIS2 and enhances the exchange of
meteorological data within the global community.

## Related

- [MQTT Overview](mqtt-overview.md)
- [Real-Time Data Dissemination with MQTT Broker](mqtt-dissemination.md)
- [Automated Data Acquisition with MQTT Client](mqtt-acquisition.md)
- [Implementation Details](implementation.md)
