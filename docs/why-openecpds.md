# Why OpenECPDS

OpenECPDS is an open-source,
ECMWF-backed,
distributed Managed File Transfer and data distribution platform built for organisations that
cannot treat data exchange as an afterthought.
It combines acquisition,
dissemination,
portal access,
monitoring,
and automation in one operational system designed for sustained,
mission-critical use.

Many platforms can transfer files securely.
OpenECPDS is designed for teams that also need to **scale distribution**,
**orchestrate multiple data movers**,
**integrate with scientific and operational workflows**,
and **operate the whole system as infrastructure** rather than as a single gateway.

If you are evaluating OpenECPDS against traditional MFT products,
this page explains where it overlaps,
where it differentiates,
and why those differences matter.

!!! tip "Try it now"
    Want to evaluate the platform before planning a full deployment?
    Start with the [standalone container](getting-started/standalone.md),
    then follow [Installation](getting-started/installation.md) and [First Run](getting-started/first-run.md).

## Introduction

OpenECPDS originated from operational needs at [ECMWF](https://www.ecmwf.int/),
where large-scale,
time-sensitive,
international data exchange is part of daily production.
It is now available as open source,
giving organisations access to the same architectural ideas that support demanding real-world
workflows in meteorology,
science,
and operational services.

The platform is centred on a distributed **Data Store** and three complementary services:
**Data Acquisition**,
**Data Dissemination**,
and the **Data Portal**.
Together,
these cover provider-side retrieval,
consumer-side delivery,
and self-service push/pull access without forcing you to assemble separate products for each
function.

Because OpenECPDS is open source,
teams can inspect how it works,
adapt it to their own operating model,
and integrate it into existing tooling without being locked into a proprietary workflow.
At the same time,
it is documented,
container-ready,
and structured enough to be evaluated like an enterprise platform rather than like an internal
prototype.

## Common Ground with MFT Platforms

If you come from a commercial MFT evaluation,
you will recognise many core capabilities.
OpenECPDS covers the fundamentals you would expect from a serious transfer platform,
including secure protocols,
automation,
scheduling,
monitoring,
and API access.

| Feature | OpenECPDS | Typical MFT |
|---|---|---|
| Secure file transfer | Yes — outgoing via FTP, FTPS, SFTP, HTTPS, S3, and cloud storage modules; incoming portal via FTP, SFTP, SCP, HTTPS and S3 | Yes |
| Scheduling | Yes — per-destination scheduler with time windows, delay, lifetime, standby, and duplicate handling | Yes |
| Automation | Yes — retries, hooks, handlers, event triggers, metadata injection | Yes |
| Monitoring | Yes — web monitoring UI, history, event logs, transfer statistics | Yes |
| REST API | Yes — versioned JSON API for users, destinations, metadata, monitoring, and backups | Often yes |
| High availability patterns | Yes — distributed Data Movers, failover-oriented architecture, proxy/continental mover support | Often yes |
| Cloud storage support | Yes — Amazon S3, Azure Blob Storage, Google Cloud Storage | Increasingly yes |
| User-facing portal | Yes — incoming and outgoing access via HTTPS, SFTP, SCP, FTP, and S3-style interfaces | Sometimes |
| Fine-grained transfer tuning | Yes — protocol-specific optimisation plus low-level TCP controls | Varies |
| Event-driven workflows | Yes — embedded MQTT broker/client, per-destination MQTT publishing, MQTT acquisition | Less common |

That overlap is important.
OpenECPDS is not asking organisations to trade away mainstream MFT requirements in order to
get specialised scientific-data capabilities.
Instead,
it starts from the same operational baseline and extends it into areas that matter when data
volumes,
distribution fan-out,
and workflow integration become harder.

## Where OpenECPDS Differentiates

The strongest reasons to choose OpenECPDS usually appear **after** the basic feature checklist.
This is where architecture,
scale model,
workflow fit,
and openness become more important than simple protocol support.
### Multi-node Data Mover infrastructure

OpenECPDS is built around a clear separation between the **Master Server**,
which coordinates transfers,
and the **Data Movers**,
which move the bytes.
That design matters because it lets you scale transfer capacity,
place movers near networks or storage zones,
and route work according to transfer group,
role,
or geography instead of forcing everything through a single node.

In portal workflows,
OpenECPDS even distinguishes between the **User Data Mover** that accepts a user connection
and the **Target Data Mover** that stores the content.
This gives the platform flexibility in how it handles ingress,
storage placement,
and load balancing.
For organisations with multiple data centres,
segmented networks,
or regional distribution points,
that is a practical architectural advantage rather than just an implementation detail.
See [Components](architecture/components.md)
and [Continental Data Movers](architecture/continental-data-movers.md).
### Sustained high-volume dissemination

Some transfer products are excellent at orchestrating business-file exchange but are not
optimised for sustained,
large-scale dissemination to many remote destinations.
OpenECPDS is designed for exactly that kind of workload:
repeated operational delivery,
custom scheduling,
per-host tuning,
Data Mover allocation,
and queue behaviour that can be controlled per destination.

The platform also treats data lifecycle,
compression,
checksums,
and expiry as built-in operational concerns.
That makes it a better fit for organisations that need to move large datasets repeatedly and
predictably,
not just complete isolated one-off transfers.
See [Additional Features](concepts/additional-features.md)
and [Data Transfer Lifecycle](architecture/data-transfer-lifecycle.md).
### MQTT and WIS2 integration

This is one of the clearest areas where OpenECPDS differs from a typical general-purpose MFT
platform.
OpenECPDS includes an embedded **MQTT broker** for publication,
an **MQTT client** for acquisition,
and documented integration patterns for **WMO WIS2**.
That means the same platform can publish notifications to downstream consumers,
subscribe to upstream notifications,
and turn those events into transfer activity without bolting on a separate messaging system.

For organisations in meteorology,
environmental monitoring,
research dissemination,
or other event-driven data ecosystems,
this is a major advantage.
It aligns the transfer platform with modern publish/subscribe workflows instead of forcing
polling-only integration patterns.
See [MQTT Overview](notifications/mqtt-overview.md)
and [WMO WIS2 Integration](notifications/wmo-wis2.md).
### Operational monitoring designed for distributed transfer systems

OpenECPDS includes a dedicated monitoring interface for destinations,
hosts,
transfers,
users,
and infrastructure activity.
This is not only an audit log or dashboard layer:
operators can inspect transfer state,
manage queues,
review event histories,
and work with concepts that map directly to how a distributed transfer estate is run.

The documentation also reflects this operational focus,
with dedicated sections for transfer statistics,
structured event categories,
global reach,
and physical infrastructure examples.
That matters when the evaluation question is not only “can it move files?” but also “can we
operate this reliably every day?”
See [Monitoring](monitoring/transfer-statistics.md),
[Event Logging](event-logging/overview.md),
and [Global Reach](global-reach.md).
### Data Portal: incoming and outgoing in one platform

Many organisations end up combining separate products for scheduled outbound delivery and
user-driven inbound/outbound access.
OpenECPDS keeps those worlds together.
Its **Data Portal** supports user-initiated download and upload workflows,
while the same platform also runs background dissemination and acquisition services.

This matters operationally because governance,
identity,
logging,
quota management,
and storage policy can remain consistent.
You do not have to maintain one product for scheduled delivery and another for partner-facing
portal exchange if your use case spans both.
See [Data Portal](use-cases/data-portal.md)
and [Data User Options](use-cases/data-portal-user-options.md).
### Container-native deployment and fast evaluation path

OpenECPDS is designed to run in containers,
with documented Docker-based workflows,
a Kubernetes deployment path,
and a **standalone image** that packages the full stack for quick evaluation.
This makes it easier to test on a laptop,
run in a development environment,
and then move towards more realistic deployment topologies without switching technologies.

For evaluators,
that shortens time-to-first-impression.
For operators,
it supports reproducible builds and infrastructure-as-code style deployment practices.
The same platform can start small and still align with container-centric operating models.
See [System Requirements](getting-started/requirements.md),
[Installation](getting-started/installation.md),
and [Deploying on Kubernetes](deployment/kubernetes.md).
### Open REST API

OpenECPDS exposes a versioned JSON REST API for programmatic control over incoming users,
destinations,
metadata,
monitoring data,
and backup/restore workflows.
That makes it easier to integrate with provisioning systems,
internal portals,
CMDBs,
workflow engines,
and automation pipelines.

Because the API is documented openly,
it can also be evaluated alongside the UI and transfer workflows rather than hidden behind a
separate commercial agreement or private SDK.
For organisations that care about platform integration,
that transparency lowers adoption friction.
See [REST API](rest-api.md).
### Extensible transfer module architecture

OpenECPDS uses a modular transfer architecture.
That gives the platform a stable operational core while still allowing protocol-specific
behaviour and future extension.
It already includes modules for FTP,
FTPS,
SFTP,
HTTP/HTTPS,
Amazon S3,
Google Cloud Storage,
Azure Blob Storage,
ECauth,
and portal-oriented workflows.

This matters for two reasons.
First,
it lets operators tune each protocol appropriately rather than flattening all endpoints into a
lowest-common-denominator abstraction.
Second,
it gives technical teams a cleaner path for adding or adapting integrations when their
environment has specialised requirements.
See [Transfer Modules](transfer-modules/index.md)
and [Protocols & Connections](concepts/protocols.md).

## Who is it for?

OpenECPDS is especially relevant for organisations whose transfer requirements are shaped by
**scale**,
**operational continuity**,
**multi-protocol distribution**,
or **scientific and public-service workflows**.
That includes environments where datasets are large,
distribution is repeated,
and the same platform must serve both automated systems and external users.

Typical candidates include:

- **National meteorological services** that disseminate operational products to many partners
  and also ingest data from upstream providers
- **Scientific data centres** that need a blend of scheduled delivery,
  archive-facing storage,
  acquisition,
  and user portal access
- **Operational data hubs** that coordinate data exchange across regions,
  networks,
  and consuming systems
- **Research infrastructures** distributing large machine-readable datasets via web,
  MQTT,
  and cloud-compatible interfaces
- **Government or intergovernmental services** that need policy controls,
  auditability,
  and geographically distributed delivery
- **Organisations consolidating multiple transfer tools** into a single operational platform
- **Individual researchers or small teams** who want to store local datasets and share them
  with collaborators or the public — the Data Portal provides a self-hosted, multi-protocol
  file system interface (FTP, SFTP, SCP, HTTPS, S3) without the need for a third-party
  storage service
- **Data publishers** who need to distribute files on a schedule, notify subscribers via
  MQTT when new data arrives, and give end users a simple portal to download at their own
  pace

OpenECPDS scales down as well as up — the standalone container runs comfortably on a
laptop, and a minimal single-node deployment works well as a centralised file gateway or
personal data store. Whether the challenge is a small internal hub, a self-hosted file
sharing service, or large-scale multi-node dissemination to thousands of destinations,
the same platform and the same operational model apply.

## Proven at Scale

OpenECPDS is not positioned as a theoretical architecture.
The documentation describes an **ECMWF operational deployment** and supporting infrastructure
that illustrate the scale the platform is intended to handle.
That is one of the strongest signals for evaluators who want evidence of operational maturity.

Examples documented in this site include:

- **more than 1,000 destinations** across **80+ countries**
- deployments that scale to **hundreds of systems managing petabytes of data**
- an illustrative ECMWF infrastructure of **80 bare-metal systems** with **2 petabytes** of
  storage
- a geographically distributed model including a **Continental Data Mover** in the United
  States to support transatlantic distribution patterns

These figures matter because they show that OpenECPDS is aimed at sustained,
large operational estates rather than only lab-scale demonstrations.
Even if your deployment is smaller,
that design headroom can reduce the risk of outgrowing the platform later.
See [Global Reach](global-reach.md)
and [Illustrative Physical Infrastructure](deployment/infrastructure.md).

## Why organisations choose it

When teams choose OpenECPDS,
it is often because they want more than “secure transfer software.”
They want a platform that can sit at the centre of data exchange,
connect to varied protocols,
handle scheduled and user-driven access,
scale out through multiple movers,
and participate in event-driven ecosystems.

Common reasons include:

- avoiding fragmentation across separate acquisition,
  dissemination,
  portal,
  and notification tools
- keeping operational control over tuning,
  scheduling,
  monitoring,
  and lifecycle policy
- supporting scientific or mission-critical workflows that do not map neatly to generic
  business-file patterns
- preferring an open,
  inspectable,
  ECMWF-backed codebase over a closed appliance model
- wanting a realistic path from local evaluation to containerised production deployment

## Getting Started

The fastest way to understand whether OpenECPDS fits your environment is to run it and inspect
how the pieces work together.
Start with the requirements,
bring up the platform,
and then explore the monitoring UI,
portal,
MQTT flows,
and protocol modules that matter to your use case.

Recommended path:

1. Review [System Requirements](getting-started/requirements.md)
2. Follow [Installation](getting-started/installation.md)
3. Launch the stack with [First Run](getting-started/first-run.md)
4. Explore [Architecture Overview](architecture/overview.md)
5. Compare [Additional Features](concepts/additional-features.md) and the
   [Transfer Modules](transfer-modules/index.md) with your current MFT requirements

!!! note "Evaluation mindset"
    When comparing OpenECPDS with another MFT product,
    do not stop at the protocol checklist.
    OpenECPDS ships with modules for FTP, FTPS, SFTP, SCP, HTTP/S, Amazon S3, Azure Blob,
    Google Cloud Storage, and MQTT — and if a protocol you need is missing, both extension
    points are open:

    - **Outgoing (acquisition & dissemination):** the [transfer module framework](transfer-modules/index.md)
      lets you add new protocols without touching the core platform.
    - **Incoming (Data Portal):** the [server plugin framework](concepts/plugin-infrastructure.md)
      (`SshPlugin`, `FtpPlugin`, `HttpPlugin`, `MqttPlugin`…) provides FTP, SFTP, SCP, HTTPS, S3
      and MQTT today, and supports the same kind of extension for new incoming protocols.

    If you need a protocol that is not yet supported, do not hesitate to
    [suggest it or raise a request](contributing.md) — ECMWF is always keen to extend
    OpenECPDS further, and is happy to help guide or collaborate on integration and
    development.
    Evaluate the architecture,
    distribution model,
    portal capabilities,
    event-driven integration,
    and operational tooling as a whole.
## Related

- [System Requirements](getting-started/requirements.md)
- [Installation](getting-started/installation.md)
- [First Run](getting-started/first-run.md)
- [Architecture Overview](architecture/overview.md)
- [Additional Features](concepts/additional-features.md)
- [Transfer Modules](transfer-modules/index.md)
- [REST API](rest-api.md)
- [Global Reach](global-reach.md)
