# Product Descriptions

**Product Descriptions** (`/do/admin/productdescriptions`) lets administrators attach metadata to a monitored product (e.g. `GOPER`, `ERA5T-WEAF`), optionally scoped to one of its **types** (the *Type* column shown on a product's monitoring page, e.g. `AN`/`FC`/`EM`/`ES`).

---

## Generic vs. Type-Specific Entries

Each entry is keyed by **Product** + **Type**. The **Type** field can be:

- **Blank (generic entry)** — applies to *every* type of that product that doesn't have its own, more specific entry. There can only be one generic entry per product.
- **A specific type** (e.g. `AN`) — applies only to that type, and **overrides** the generic entry for it.

In other words, the generic entry is the **default/fallback**: it is only used for a type when no dedicated entry exists for that exact type. You are free to mix the two — e.g. define a generic entry with a sensible default Tips text, then add a single type-specific entry only for the one type that needs different wording; every other type will keep using the generic default.

!!! tip "Only need one message for all types?"
    You only ever need to create the **generic** (blank Type) entry. There is no need to duplicate the same Description/Tips text once per type — a single generic entry already covers all of them.

---

## Fields

| Field | Purpose |
|---|---|
| **Product** / **Type** | Identify the entry. Type blank = generic/default entry for the product. |
| **Description** | Used to build the `{{DESCRIPTION}}` placeholder in [Product Status Messages](product-status-messages.md). When the types shown resolve to different descriptions, it renders one `- TYPE: description` bullet per type; if they all resolve to the same text (e.g. only a generic entry is configured), it appears once as plain text. |
| **Tips** | Same default/override and bullet-list-or-plain-text behaviour as Description, but shown as an expandable **ⓘ** info card on the product's monitoring page (e.g. `/do/monitoring/summary/GOPER/06/0/AN`) instead of in an email message. |
| **Group all cycles/times into one page** | Only meaningful — and only editable — on the **generic** (blank Type) entry of a product. See [Grouped Monitoring Pages](#grouped-monitoring-pages) below. |

---

## Grouped Monitoring Pages

By default, each cycle/time of a product (e.g. `00`, `06`, `12`, `18`) gets its own pill on `/do/monitoring` and its own page at `/do/monitoring/summary/{product}/{time}`. For products with many cycles per day but few entries per cycle, this can clutter the header with a large number of near-identical pills.

Enabling **Group all cycles/times into one page** on a product's generic entry instead:

- Merges every cycle of that product into a **single pill** on `/do/monitoring`, shown with a <i class="bi bi-layers-fill"></i> layers icon instead of a `T-Product` time prefix.
- The merged pill's colour reflects the **worst status** across all of the product's cycles (ignoring cycles with no data at all, unless every cycle has none), and it is positioned in the header using the **earliest scheduled** cycle among them.
- Links to a single page, `/do/monitoring/summary/{product}` (no time in the URL), listing every cycle/time for that product together in one table instead of one page per cycle.
- The **Email Notifications** card on that page (Products Delay/Resumed, Copy BCC Emails) aggregates the BCC contacts registered against every individual cycle of the product, and uses the **Grouped** variant of the [Product Status Messages](product-status-messages.md#grouped-vs-ungrouped-variants) — with `{{CYCLES}}` (e.g. `00-03,06-07,12`) in place of a single `{{CYCLE}}`.

---

## Housekeeping

- <i class="bi bi-exclamation-triangle-fill text-warning"></i> is shown next to a product/type that does not currently exist in the [monitoring interface](../monitor-ui/monitoring.md) — for example a typo, or a product that is no longer active. Use the **Unknown only** filter to isolate these, and **Delete All Unknown** to clean them up in bulk.
- **Import from Monitoring** scans every product/type pair currently visible in the monitoring interface and adds a blank entry (empty Description/Tips) for any pair not yet configured here, ready to be filled in via Edit.

---

## Related

- [Product Status Messages](product-status-messages.md)
- [Monitoring](../monitor-ui/monitoring.md)
- [Data Ownership & Catalogue](../architecture/data-ownership-and-catalogue.md)
- [REST API — Monitoring](../rest-api.md#monitoring)

## Product Models

The **Product Models** card groups products per model (for example `IFS` or `AIFS`). A model is identified only by its
name, which must be unique (case-insensitive), and a product belongs to at most one model. Assigning a product to a
model moves it from its previous one; deleting a model leaves its products unassigned. Deleting the last description of a product also removes it from its model. Models are used to filter the
products on the [Delay Notification](../monitoring/delay-notification.md) page.

### How to

1. Open **Admin → Product Descriptions** (`/do/admin/productdescriptions`) and scroll to the **Product Models** card.
2. In the last row, enter a **model name** (for example `IFS`) and tick the **products** in the drop-down list. Only products that have a product description can be selected.
3. Click the save button on the row. To change a model, edit its name or ticked products and save again.
4. To remove a model, click the delete button on its row and confirm. Its products are kept, but left without a model.
