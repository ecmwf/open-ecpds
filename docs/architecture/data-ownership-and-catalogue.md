# Data Ownership, Metadata and the Catalogue Question

A recurring question when organisations grow beyond a handful of "people who just
know the data" is: **who owns the knowledge about what a piece of data actually is,
where it came from, and who to contact about it?** This is a different question from
[Centralised vs. Federated](federation-vs-centralization.md) operations — it is not
about who *runs* the infrastructure, but about who *knows* what is flowing through it,
and how that knowledge is captured so it does not live only in a few people's heads.

This page describes how OpenECPDS approaches that problem: not by trying to be the
authoritative scientific/semantic catalogue for every dataset it moves, but by
maintaining a rich **operational catalogue** of products and their dissemination, with
strong **traceability**, that complements — rather than replaces — dedicated catalogue
and product-generation systems.

## The problem: knowledge concentrated in a few experts

As organisations scale, and especially as they restructure into more autonomous
domains/teams, a common failure mode appears: operational knowledge about *the data
itself* — not just the infrastructure moving it — becomes concentrated in a small
number of long-serving experts. Simple questions that should be answerable from
documentation end up depending on institutional memory instead:

- Who owns this file or product, and who do we contact about it?
- Where did it originally come from, and has it changed?
- Which team is responsible if a downstream user reports it as wrong or missing?

This is a harder problem than it looks, because it mixes two different kinds of
knowledge that are easy to conflate:

1. **Operational knowledge** — what is being disseminated, to where, on what schedule,
   with what status, and where a given file physically came from and went. This is
   knowledge *about the data flow*.
2. **Semantic/scientific knowledge** — what the data actually represents, how it was
   generated, and what it means to use it correctly. This is knowledge *about the data
   itself*.

A central infrastructure team is well placed to own the first kind. It should not need
to own the second — that responsibility belongs with the domain/team that produces and
understands the data. The risk is when neither kind of knowledge is captured anywhere
except in a person's head.

## What OpenECPDS captures

OpenECPDS does not attempt to be the authoritative catalogue for the scientific or
semantic description of every dataset it distributes. It is, however, deliberately
built to capture a fairly rich **operational** view of the data passing through it,
which is enough to answer most of the questions above without depending on a single
expert being available.

### Product and destination metadata

Every product submitted to OpenECPDS carries **metadata** — information supplied by the
data provider describing what the product is, in addition to its physical
characteristics (size, checksum, storage location; see
[OpenECPDS Entities](../concepts/entities.md#data-files-and-data-transfers)).

Every **destination** — the mechanism OpenECPDS uses to organise dissemination of a
product to a particular place — similarly carries its own metadata describing the
destination and its relationship to the dissemination (contact details, country,
association to incoming users, and other custom fields; see
[Destination Metadata](../rest-api.md#destination-metadata) in the REST API reference).
This gives a rich, structured operational view of what is being distributed, where it
is going, and who/which organisation it is associated with — retrievable
programmatically, not just through the web interface.

To make that metadata useful to the people actually operating the service day to day,
rather than only to the people who configured it, [Product
Descriptions](../administration/product-descriptions.md) lets administrators attach a
plain-language **description** and operational **tips** to a product (optionally scoped
to a specific type/step). These are surfaced directly in the monitoring dashboard — the
tool operators already have open — as an expandable info card and as the text used in
delay/resume notifications, so an operator watching a product that is running late does
not need to already be an expert on it to understand what it is and what to do about it.
See [Monitoring](../monitor-ui/monitoring.md).

### A dedicated, complementary layer for the product catalogue itself

OpenECPDS deliberately does not try to duplicate a proper product reference/catalogue
system. At ECMWF, that responsibility sits with a separate, dedicated set of
applications — **PGEN/PREd** (Product GENeration / Product Requirement Editor) — built
around an internal product reference and catalogue, and owned by the teams that define
and generate products. OpenECPDS' product and destination metadata is the operational
complement to that catalogue: it is scoped to *what is being disseminated and how*,
while PGEN/PREd is scoped to *what the product is and why it exists*. Neither needs to
duplicate the other; each is authoritative for its own layer.

### End-to-end traceability

Alongside metadata, OpenECPDS maintains genuine **traceability** for every file it
handles:

- The **origin** of a product — including the original server/location it came from —
  is stored as metadata against the data file at ingestion time, not inferred after the
  fact.
- Every data file has a **complete history**, with timestamps, of the transfers derived
  from it: which destinations requested it, when, with what status, at what rate, and
  with what errors if any. See [Data Files & Infrastructure](../monitor-ui/data-files.md)
  and [Transfer History](../monitor-ui/transfer-history.md).

This means that when something goes wrong — a corrupted file, a delivery failure, or an
end-user complaint — the operational team does not need to already know who owns the
data to start investigating. They can trace a specific file back to its origin and, from
there, to the team responsible for it.

The same applies in the other direction. If an end user contacts the service about a
problem, the operational team can typically trace the issue back to the relevant
product using whatever fragments of information the user happens to provide — an email
address, a filename, a product/cycle/step combination — without that trace depending on
a specific person remembering the relevant context.

### Programmatic access to the same operational view

The [REST API](../rest-api.md) exposes this operational catalogue outside the web
interface, so other systems can query it directly instead of requiring a human to look
it up: [destination metadata](../rest-api.md#destination-metadata) and the
[monitoring summary endpoints](../rest-api.md#monitoring) — which return the current
status of every product/cycle, including where each one stands and what it means to be
delayed or missing — can both be consumed by external tools, dashboards, or an
organisation's own internal knowledge systems (see also
[Permission Configuration](../rest-api.md#permission-configuration) for scoping which
external system can read what).

## What this does and does not solve

Being explicit about the boundary matters:

- **It does solve**: "who to contact and where did this come from" for anything
  actually flowing through OpenECPDS, without requiring a specific expert to be
  available, and it does so consistently across every product and destination, because
  the metadata and traceability are structural, not optional documentation someone has
  to remember to write.
- **It does not solve**: being the authoritative source for the scientific/semantic
  description of a dataset, or for governance decisions about who *should* own a
  dataset in the first place. That knowledge legitimately belongs with the domain/team
  that produces the data — OpenECPDS' role is to expose enough metadata and links to
  that authoritative information (via product descriptions, destination metadata, and
  the REST API) that the people operating the transfer infrastructure are not forced to
  become domain experts themselves.

## Relationship to federated organisational models

This connects directly to the discussion in
[Centralised vs. Federated](federation-vs-centralization.md): as organisations move
towards more autonomous, domain-owned data (each domain increasingly exposing its own
data APIs), the semantic knowledge about the data can and should stay federated with the
domains that understand it best. What a shared dissemination infrastructure like
OpenECPDS needs to provide is a consistent **operational** view across all of those
domains — metadata, traceability, and monitoring that does not depend on which domain
produced a given product — so that the team operating the infrastructure has a common
way to answer "what is this, where did it come from, and who do I ask" regardless of
which domain owns the answer.

## Summary

- OpenECPDS is not the authoritative catalogue for the scientific/semantic description
  of every dataset it moves — that responsibility stays with the teams and systems
  (such as PGEN/PREd at ECMWF) that produce and understand the data.
- It does maintain a substantial **operational catalogue**: structured product and
  destination metadata, human-readable product descriptions surfaced directly in
  monitoring, and full origin-to-delivery traceability for every file.
- This lets an operational team resolve "who owns this / where did it come from / who
  do I contact" without depending on a small number of individual experts, and lets
  external systems query the same information programmatically through the REST API.
- The model is deliberately layered rather than centralising everything: semantic
  ownership can stay federated with the domains that understand the data, while the
  dissemination layer provides a consistent operational view across all of them.

## Related

- [Centralised vs. Federated](federation-vs-centralization.md)
- [OpenECPDS Entities](../concepts/entities.md)
- [Product Descriptions](../administration/product-descriptions.md)
- [Monitoring](../monitor-ui/monitoring.md)
- [Data Files & Infrastructure](../monitor-ui/data-files.md)
- [Transfer History](../monitor-ui/transfer-history.md)
- [REST API Reference](../rest-api.md)
