# Product Status Messages

**Product Status Messages** (`/do/admin/productmessages`) lets administrators customize the two pre-filled Outlook email bodies offered on a product's monitoring page (e.g. `/do/monitoring/summary/GENFO/06`) — **Products Delay** and **Products Resumed** — used to notify recipients that a product's dissemination is delayed, or that it has resumed.

---

## The two messages

| Message | Purpose |
|---|---|
| **Products Delay** | Pre-fills the email opened from the **Products Delay** button, used when a product's dissemination is running late. |
| **Products Resumed** | Pre-fills the email opened from the **Products Resumed** button, used once dissemination has caught up. |

Each is stored in the database (so it survives upgrades and can be customized per site without a code change) and, when left empty or unchanged from the built-in default, falls back to that default text automatically — there is nothing to "reset": just clear the field and save.

---

## Grouped vs. Ungrouped variants

Each of the two messages above actually has **two variants**, selected automatically depending on the page the notification is sent from:

- **Ungrouped** — used on a regular, single-cycle product page (e.g. `/do/monitoring/summary/GENFO/06`). Supports the `{{CYCLE}}` placeholder (a single cycle/time, e.g. `06`).
- **Grouped** — used instead on a product's merged, all-cycles page (`/do/monitoring/summary/{product}`, no cycle in the URL) — i.e. a product with [**"Group all cycles/times into one page"**](product-descriptions.md#grouped-monitoring-pages) enabled. Supports `{{CYCLES}}` (every cycle currently shown, e.g. `00-03,06-07,12`) instead of a single `{{CYCLE}}`.

The admin page shows an **Ungrouped**/**Grouped** toggle above each message's text area to switch between the two bodies being edited; both are saved together when you click **Save Messages**.

---

## Placeholders

| Placeholder | Replaced with |
|---|---|
| `{{PRODUCT}}` | The product currently being viewed (e.g. `GENFO`). |
| `{{CYCLE}}` | The single cycle/time currently being viewed (e.g. `06`). Empty on a grouped page, which has no single cycle. |
| `{{CYCLES}}` | Every cycle/time currently shown (e.g. `00-03,06-07,12` on a grouped page). On a regular, single-cycle page this is the same single value as `{{CYCLE}}`. |
| `{{DESCRIPTION}}` | The description(s) configured under [Product Descriptions](product-descriptions.md) for the current product — a plain text if only one type applies, a bullet list (one line per type) when several types shown on the page each resolve to a different description, or an empty string if none has been configured. |

The `<<...>>` markers in the built-in default text highlight wording that should be edited before sending — they have no special meaning to the system, they're just a visual convention.

---

## Cycle List Formatting

By default, the `{{CYCLES}}` placeholder compacts a long list of cycles into ranges for readability, e.g.:

```
00,01,02,03,06,07,12  →  00-03,06-07,12
```

Uncheck **"Automatically compact grouped cycle lists in the `{{CYCLES}}` placeholder"** (in the **Cycle List Formatting** card) to always show the complete, uncompacted list instead.

---

## Related

- [Product Descriptions](product-descriptions.md)
- [Monitoring](../monitor-ui/monitoring.md)
