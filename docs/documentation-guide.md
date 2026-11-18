# Documentation guide

Use this guide to find the right part of the OpenECPDS documentation. Start with
a common task below, or browse the section summaries to see what each area covers.

| I want to… | Start here |
|---|---|
| Try the full stack without building it | [Run the standalone container](getting-started/standalone.md) |
| Bring data in, send data out, or let partners exchange files | [Acquisition](use-cases/acquisition.md), [Dissemination](use-cases/dissemination.md), or the [Data Portal](use-cases/data-portal.md) |
| Find out why a transfer failed or took too long | [Transfer history](monitor-ui/transfer-history.md), then [transfer network statistics](monitoring/transfer-statistics.md) |
| Notify consumers, replay events, or replicate a destination | [MQTT dissemination](notifications/mqtt-dissemination.md), [notification history and replay](notifications/history.md), or [one-way replication with Aviso](notifications/aviso-acquisition.md) |

## Learn and deploy

- **[Home](index.md) and [Why OpenECPDS](why-openecpds.md):** Understand what the platform does, who it's for, and how it compares with managed file transfer platforms.
- **[Getting Started](getting-started/requirements.md):** Check system requirements, try the standalone image, or follow [installation](getting-started/installation.md) and [first run](getting-started/first-run.md). For development, see [IDE setup](getting-started/ide-setup.md).
- **[Architecture](architecture/overview.md):** See how the Master, Monitor, Data Movers, and database work together. Use the architecture guides to understand the [transfer lifecycle](architecture/data-transfer-lifecycle.md), [failover](architecture/failover.md), and [centralised vs. federated deployments](architecture/federation-vs-centralization.md).
- **[Deployment](deployment/kubernetes.md):** Find deployment guidance for [Kubernetes](deployment/kubernetes.md) or [physical infrastructure](deployment/infrastructure.md), as well as [container registry and release instructions](deployment/release.md).
- **[Concepts](concepts/entities.md):** Learn what Hosts, Destinations, and other entities represent. Look here for [protocols](concepts/protocols.md), [object storage](concepts/object-storage.md), and how [Destination](concepts/destination-options.md) and [Host](concepts/host-options.md) options shape transfers.
- **[Global Reach](global-reach.md):** See where the platform is used and the scale of its delivery network.

## Configure data exchange

- **Use Cases:** Follow [acquisition](use-cases/acquisition.md) to pull files from providers, [dissemination](use-cases/dissemination.md) to push products to destinations, or the [Data Portal](use-cases/data-portal.md) to let remote users upload and download. The user guides distinguish [data users](use-cases/data-users.md) from [web users](use-cases/web-users.md).
- **[Transfer Modules](transfer-modules/index.md):** Choose a protocol or storage backend and look up its options. Start with [ECtrans common options](transfer-modules/ectrans.md) for settings shared across modules.
- **[Host Directory](host-directory/index.md):** Find out what the Directory field means for a given Host type and where to use acquisition, dissemination, or replication scripts.
- **[Notifications](notifications/mqtt-overview.md):** Follow the MQTT guides for [real-time dissemination](notifications/mqtt-dissemination.md), [event-driven acquisition](notifications/mqtt-acquisition.md), or optional [WMO WIS2 integration](notifications/wmo-wis2.md). Use [notification history](notifications/history.md) for persisted events and Aviso watch and replay, or the [Aviso replication guide](notifications/aviso-acquisition.md) to configure additive, one-way destination replication.

## Operate and troubleshoot

- **[Monitor UI](monitor-ui/overview.md):** Find the screens for destinations, transfer hosts, queues, transfer history, data files, and users. Start here for day-to-day work in the web interface.
- **Administration:** Configure services using the `ecmwf.properties` references for the [Master](configuration/master.md), [Monitor](configuration/monitor.md), and [Data Mover](configuration/mover.md). Use the administration guides for [TLS certificates](administration/certificates.md), [login configuration](administration/openid-connect.md), and [storage lifecycle policies](administration/storage-lifecycle-policies.md).
- **Monitoring:** Diagnose transfer performance with [network statistics](monitoring/transfer-statistics.md), monitor services with [JMX, Hawtio, and Prometheus](monitoring/jmx-export.md), or set up [delay notifications](monitoring/delay-notification.md). This is separate from the Monitor UI's guide to screens and controls.
- **[Event Logging](event-logging/overview.md):** Look up structured event categories and the meaning of individual fields, including upload history, errors, and notification outcomes.

## Integrate and find reference material

- **[ECPDS CLI](getting-started/ecpds-cli.md):** Install the command-line client and submit files from scripts or a terminal.
- **[REST API](rest-api.md) and [Java API Reference](api-reference.md):** Use the REST reference for HTTP integrations and the Javadocs for Java classes and methods.
- **[Support](support.md) and [Contributing](contributing.md):** Find support resources and guidance for working on the project.
- **[Changelog](changelog.md) and [Glossary](glossary.md):** See what changed in a release or look up a term used in the documentation.
