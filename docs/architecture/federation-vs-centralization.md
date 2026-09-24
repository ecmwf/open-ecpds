# Centralised Service vs. Federated Architecture

A question that comes up when organisations evaluate OpenECPDS — particularly other
meteorological or scientific data centres that already operate their own transfer
infrastructure — is how a **centrally operated ECPDS service** (such as the one ECMWF
runs) compares to a more **federated model**, where each organisation runs and governs
its own instance.

The short answer is that these are not mutually exclusive. OpenECPDS separates three
concerns that are often bundled together when people talk about "centralised vs
federated": the **protocol** used to move data, the **software** that implements that
movement, and the **service** that operates a given deployment day to day. Understanding
that separation is the key to seeing why a centrally operated ECPDS service and a
federated ecosystem of independently run instances are compatible, not competing, ideas.

## Three layers, three different questions

| Layer | Question it answers | OpenECPDS position |
|---|---|---|
| **Protocol** | What do systems actually speak to exchange data? | Open, standard protocols — FTP, SFTP, FTPS, HTTP/S, WebDAV, S3-compatible object storage, MQTT. See [Protocols & Connections](../concepts/protocols.md). |
| **Software** | What implementation moves the bytes and manages the workflow? | Open source, self-deployable, inspectable, modifiable. See [Why OpenECPDS](../why-openecpds.md) and [Deployment](../deployment/kubernetes.md). |
| **Service / operations** | Who runs a given instance, and for whom? | Can be centralised (one team operating a shared service, as ECMWF does) **or** federated (each organisation runs its own instance) — the software does not force either choice. |

Keeping these three layers distinct avoids a common misreading: that a centrally
*operated* ECPDS service implies a centralised, closed, or proprietary *architecture*.
It does not. ECMWF's operational deployment is one instance of an open-source platform
that speaks open protocols — not a gateway that other systems must be architecturally
tied to.

## Centralised governance does not require centralised infrastructure

Because OpenECPDS is built from standard, interoperable protocols and distributed under
an open-source licence, an organisation is never limited to consuming a single
ECMWF-operated instance:

- **No proprietary protocol lock-in.** Producers and consumers connect over the same
  standard protocols they would use with any other Managed File Transfer product. There
  is no ECPDS-specific wire protocol that a partner system must implement to
  interoperate. This means an organisation's applications and production workflows are
  not inherently tied to ECPDS as a piece of technology — if a team later chose a
  different transfer solution, migration is a question of protocol compatibility, not of
  unwinding a proprietary integration.
- **No software lock-in.** Because OpenECPDS is open source, an organisation can deploy,
  operate, inspect, and even modify its own instance instead of depending on a
  centrally-run one. Nothing about the architecture assumes a single global deployment;
  see [Components](components.md) and [Overview](overview.md) for how a full stack
  (Master Server, Data Movers, Monitor Server, Data Portal) is meant to be stood up
  per-deployment, including on a laptop via the
  [standalone container](../getting-started/standalone.md) or in production via
  [Kubernetes](../deployment/kubernetes.md).
- **Clean interoperability boundaries.** Because instances talk to each other (and to
  producers/consumers) through standard protocols rather than a private ECPDS-to-ECPDS
  handshake, two independently operated OpenECPDS deployments — or an OpenECPDS
  deployment and an entirely different MFT product — can already interoperate today
  without any special integration work.

In short, the centralised model ECMWF operates provides a convenient, well-run, common
**service** — not a dependency on a closed technology stack. Organisations that value
independence can run their own instance of the same open-source platform and still
interoperate through the protocols both sides already support.

## Where the trade-offs actually sit

Since the software and protocol layers are already open, the meaningful comparison
between "centralised" and "federated" is really about the **service/operations layer**
— i.e., who is responsible for running instances, and how many instances exist.

**Advantages of a centralised operational model** (one team running a shared service,
as ECMWF does for the current ECPDS deployment):

- A single, well-resourced team accumulates deep operational expertise, rather than that
  expertise being duplicated (and unevenly maintained) across many organisations.
- Consistent monitoring, alerting, and incident response — see
  [Monitoring](../monitoring/transfer-statistics.md) and
  [Live Earth](../monitor-ui/monitoring.md#live-earth) — across the whole estate instead
  of per-instance tooling maturity.
- Centralised governance: consistent security posture, TLS/certificate management (see
  [TLS Certificates](../administration/certificates.md)), access control conventions,
  and lifecycle policy.
- No duplicated effort standing up, patching, and scaling infrastructure that multiple
  organisations would otherwise each build independently. See
  [Global Reach](../global-reach.md) and
  [Physical Infrastructure](../deployment/infrastructure.md) for the scale ECMWF
  currently operates at.

**Advantages of a more federated operational model** (each organisation running its own
OpenECPDS instance, all built on the same open-source platform and standard protocols):

- Local autonomy: an organisation controls its own upgrade cadence, configuration,
  scaling, and data residency.
- Isolation: operational issues, load spikes, or outages in one organisation's instance
  do not directly affect another's.
- Proximity: an instance can be deployed close to a particular network, data source, or
  regulatory boundary — conceptually similar to how OpenECPDS already lets a single
  deployment place [Continental Data Movers](continental-data-movers.md) near regional
  networks, just taken a step further to a fully independent deployment.

**The trade-off, in practice**, is mostly about *duplication*: a federated model
distributes operational effort (and risk) across more teams, each of which needs to
develop and maintain the lifecycle-management, monitoring, and security expertise that a
centralised team would otherwise provide once for everyone. Neither position is
"correct" in the abstract — it depends on how much an organisation values operational
independence versus how much it benefits from a shared, centrally maintained service.

## A hybrid model is the natural fit

Because the protocol and software layers are already open, OpenECPDS does not force a
choice between "fully centralised" and "fully federated." A practical middle ground —
and arguably the model the platform is best suited to — looks like this:

- ECMWF (or another operator) runs a **central service** for organisations that want the
  convenience of a shared, professionally operated platform, with the operational
  benefits described above.
- Organisations that need more autonomy, isolation, or in-country deployment run their
  **own OpenECPDS instance(s)**, built from the same open-source codebase and
  configuration conventions, so they benefit from the same engineering investment
  without inheriting a dependency on the centrally-run service.
- All instances — centrally operated or independently operated — interoperate through
  the same standard protocols described in [Protocols & Connections](../concepts/protocols.md),
  so producers and consumers are never locked into a single deployment to exchange data.

In this model, centralisation is a **deployment and governance choice made per
instance**, not a property baked into the architecture. An organisation can start by
consuming the centrally operated service, and move to operating its own instance later
(or the reverse) without a protocol-level migration, because both sides of that move
speak the same open standards.

## Summary

- OpenECPDS is open-source software built on open, standard protocols — not a
  proprietary central service that other systems must connect to.
- The main advantage of a centralised operational model is **operational**
  (shared expertise, consistent monitoring and governance, no duplicated
  infrastructure effort) rather than **architectural** (there is no technical
  requirement to use a single, centrally-run instance).
- Because organisations can deploy their own instance of the same open-source platform
  and still interoperate over standard protocols, OpenECPDS is well suited to a
  federated model of independently operated instances, while still allowing a
  centralised, ECMWF-operated service where that is the more convenient choice.

## Related

- [Why OpenECPDS](../why-openecpds.md)
- [Architecture Overview](overview.md)
- [Components](components.md)
- [Continental Data Movers](continental-data-movers.md)
- [Protocols & Connections](../concepts/protocols.md)
- [Global Reach](../global-reach.md)
- [Kubernetes Deployment](../deployment/kubernetes.md)
- [Standalone Container](../getting-started/standalone.md)
- [Data Ownership & Catalogue](data-ownership-and-catalogue.md)
