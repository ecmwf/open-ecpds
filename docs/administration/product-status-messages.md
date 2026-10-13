# Product Status Messages

**Product Status Messages** (`/do/admin/productmessages`) lets administrators customize the two pre-filled Outlook email bodies offered on a product's monitoring page (e.g. `/do/monitoring/summary/GENFO/06`) — **Products Delay** and **Products Resumed** — used to notify recipients that a product's dissemination is delayed, or that it has resumed.

---

## The two messages

| Message | Purpose |
|---|---|
| **Products Delay** | Pre-fills the email opened from the **Products Delay** button, used when a product's dissemination is running late. |
| **Products Resumed** | Pre-fills the email opened from the **Products Resumed** button, used once dissemination has caught up. |

Each is stored in the database (so it survives upgrades and can be customized per site without a code change) and, when left empty or unchanged from the built-in default, falls back to that default text automatically. Use **Reset current variant** to restore the currently displayed Ungrouped or Grouped body to its built-in default, then click **Save Messages** to persist the reset.

---

## Grouped vs. Ungrouped variants

Each of the two messages above actually has **two variants**, selected automatically depending on the page the notification is sent from:

- **Ungrouped** — used on a regular, single-cycle product page (e.g. `/do/monitoring/summary/GENFO/06`). Supports the `{{CYCLE}}` placeholder (a single cycle/time, e.g. `06`).
- **Grouped** — used instead on a product's merged, all-cycles page (`/do/monitoring/summary/{product}`, no cycle in the URL) — i.e. a product with [**"Group all cycles/times into one page"**](product-descriptions.md#grouped-monitoring-pages) enabled. Supports `{{CYCLES}}` (every cycle currently shown, e.g. `00-03,06-07,12`) instead of a single `{{CYCLE}}`.

The admin page shows an **Ungrouped**/**Grouped** toggle above each message's text area to switch between the two bodies being edited; both are saved together when you click **Save Messages**. **Reset current variant** restores the displayed body to its built-in default; click **Save Messages** to persist it. Placeholder buttons turn green when their placeholder is present in the currently displayed template, and update as you edit the text. An inline warning lists any unrecognized `{{...}}` placeholders; these remain unchanged in the generated message.

---

## Placeholders

| Placeholder | Replaced with |
|---|---|
| `{{PRODUCT}}` | The product currently being viewed (e.g. `GENFO`). |
| `{{PRODUCTS}}` | For the Grouped messages: the product of the page on the monitoring pages, or the list of all the selected products on the [Delay Notification](../monitoring/delay-notification.md) page. `{{PRODUCT}}` still works in grouped messages and gives the same value. |
| `{{CYCLE}}` | The single cycle/time currently being viewed (e.g. `06`). Empty on a grouped page, which has no single cycle. |
| `{{CYCLES}}` | Every cycle/time currently shown (e.g. `00-03,06-07,12` on a grouped page). On a regular, single-cycle page this is the same single value as `{{CYCLE}}`. |
| `{{DESCRIPTION}}` | The description(s) configured under [Product Descriptions](product-descriptions.md) for the current product — a plain text if only one type applies, a bullet list (one line per type) when several types shown on the page each resolve to a different description, or an empty string if none has been configured. |
| `{{AFFECTED}}` | Used by the [Delay Notification](../monitoring/delay-notification.md) page (Grouped delay message): replaced with the products and cycles selected as *Definite* or *Possible*, then the reason, expected delay, ETA and note. When absent, that block is inserted after the first paragraph. Removed from the messages sent from the monitoring pages. |
| `{{REASON}}` | The reason entered in the Delay Notification form, or the reason saved with a notice when composing its resolution. |
| `{{ETA}}` | The notice's ETA, followed by `UTC` when set. |
| `{{EXPECTED_DELAY}}` | The expected delay selected for the notice. |
| `{{NOTE}}` | The note entered for the notice. |

The `{{DESCRIPTION}}` placeholder is included in all four built-in message bodies. The last four placeholders are available in the Grouped Products Delay and Products Resumed messages. They are replaced with the current notice's values (or the saved values when composing a resolution) and are empty in regular monitoring-page emails. In the Grouped Products Delay template, a value placed with its own placeholder is omitted from the automatically inserted affected block, while the other details remain there. If none is used, `{{AFFECTED}}` continues to include all of those details as before.

---

## Cycle List Formatting

By default, the `{{CYCLES}}` placeholder compacts a long list of cycles into ranges for readability, e.g.:

```
00,01,02,03,06,07,12  →  00-03,06-07,12
```

Uncheck **"Automatically compact grouped cycle lists in the `{{CYCLES}}` placeholder"** (in the **Cycle List Formatting** card) to always show the complete, uncompacted list instead.

---

## Products Delay Message

Used in two places:

- **Ungrouped** variant: the *Products Delay* button of a single product/cycle page.
- **Grouped** variant: the same button on an all-cycles page, and the **body of the notice** on the
  [Delay Notification](../monitoring/delay-notification.md) page, which always starts from this variant because a notice
  covers several products and cycles.

Use the **Insert at cursor** buttons above the editor to add a placeholder where the cursor is (or over the selected text).
Only the placeholders valid for the displayed variant are offered, and each placeholder can be used several times.

Example of a Grouped message:

```
Dear colleagues,

The dissemination of ECMWF {{PRODUCTS}} products for the {{CYCLES}}Z cycles will be delayed.

{{AFFECTED}}

{{DESCRIPTION}}

Kind regards,
ECMWF Duty Manager
```

On the Delay Notification page, `{{AFFECTED}}` is replaced with the *Definite* and *Possible* lists, followed by the
reason, expected delay, ETA and note, except for details placed with their own individual placeholders above. Use it
**once**: a second use repeats the list. If it is missing, the list is inserted after the first paragraph. When the
message is used from a monitoring page, `{{AFFECTED}}` is simply removed.

### Products with different cycles

If the selected products do not share the same cycles, `{{PRODUCTS}}` names the cycles of each product, for example
`GENFO (00,12Z) and GWAVE (06,18Z)`, and the wording "for the {{CYCLES}}Z cycles" becomes "for their respective cycles".
Only that exact wording is rewritten: if you phrase the sentence differently, `{{CYCLES}}` shows the combined list of
cycles instead. Cycle lists follow the *Cycle List Formatting* option below.

---

## Products Resumed Message

- **Ungrouped** variant: the *Products Resumed* button of a single product/cycle page.
- **Grouped** variant: the same button on an all-cycles page, and the pre-filled text of **Inform resolution** (in *Sent
  notices* of the Delay Notification page), for the cells being resolved.

`{{PRODUCTS}}`, `{{CYCLES}}` and `{{DESCRIPTION}}` work as in the delay message. `{{AFFECTED}}` is not available here.

## Notification Addresses

The **Notification Addresses** card sets the **From / To address** (default `operators@ecmwf.int`) and the distribution list
that is **always in Bcc** (default `op_delay_ecmwf@lists.ecmwf.int`). They are used by the *Products Delay* / *Products
Resumed* buttons of the monitoring pages and by the [Delay Notification](../monitoring/delay-notification.md) page. Leave
a field empty to restore its default.

---

## Related

- [Product Descriptions](product-descriptions.md)
- [Monitoring](../monitor-ui/monitoring.md)
