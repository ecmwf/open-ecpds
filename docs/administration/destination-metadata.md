# Destination Metadata

**Destination Metadata** lets administrators attach structured, free-form information to a Destination — organisational details, contacts, escalation procedures, known issues, anything worth recording against a specific data flow but that doesn't belong in a transfer option or a Host property. Field *definitions* (what fields exist, their type, and their central default) are managed once, globally, on **Metadata Field Definitions** (`/do/admin/metafields`); each Destination then has its own **Metadata** tab (`/do/transfer/destination/metadata/{name}`) where those fields are filled in (or, for locked fields, simply displayed).

The most useful part of this feature for day-to-day operations is that it closes a real gap: OpenECPDS already knows a destination is failing (via monitoring/Opsview), but nothing in Opsview itself used to say *what to do about it* or *who to call*. Destination Metadata — combined with the **Include in Notes** export — lets that operational knowledge live right next to the alert, instead of in a separate wiki an operator has to go find under pressure.

---

## Why this exists

Every Destination tends to accumulate the same kind of "soft" information over time: who owns it, who to contact when it breaks, what's normal and what isn't, links to external documentation, a standard escalation path. Historically this lived wherever each team happened to put it — a wiki page, a shared spreadsheet, someone's memory — which means it's inconsistent, hard to discover from inside OpenECPDS, and easy to forget to update.

Destination Metadata gives this information a proper home:

- It's **structured** — a field has a name, a type, and (optionally) a category, so related fields group together sensibly on the page instead of being one giant free-text blob.
- It's **centrally defined, locally filled in** — an administrator decides once what fields exist across the whole platform (e.g. every Destination should have a "Primary Contact" and an "Escalation Procedure"), and each Destination's own page just shows those same fields ready to complete.
- It's **exportable to Opsview** — the fields that matter operationally can be pushed into Opsview as a host note, so the information an operator needs during an incident is visible exactly where the alert already is.

---

## Field types

| Type | What it's for |
|---|---|
| `text` | Short free text (a name, a reference number, a single line of information). |
| `textarea` | Longer free text, multiple lines, no formatting. |
| `markdown` | Rich text — headings, bold/italic, lists, links, tables, code — authored with a live-preview editor and rendered to a restricted, safe HTML subset wherever it's exported. The natural choice for anything resembling a runbook or procedure (see [Worked example: an operator runbook](#worked-example-an-operator-runbook) below). |
| `url` | A single web link. |
| `email` | A single email address. |
| `phone` | A single phone number. |
| `password` | A secret value. Never rendered in plain text in the Opsview export, and redacted from REST API responses unless the caller has the `showSensitiveInfo` permission (see [REST API — Sensitive Fields](../rest-api.md#sensitive-fields)). |
| `contact` | A structured person: name, email, phone, fax. |
| `mail-group` | A structured mailing list: name, email. |
| `switchboard` | A structured phone contact: name, phone. |

Any field can be configured to accept more than one value (**Max Occurs**, e.g. several `contact` entries for "Computer Operations" — a primary and a backup), and fields are grouped into **categories** (e.g. `General`, `Contacts`, `Documentation`) purely for layout — related fields are shown together on the Destination's Metadata tab.

!!! tip "Markdown links always open in a new tab"
    Wherever Markdown content is rendered — the live preview or the final Opsview note — a link always opens in a new browser tab/page rather than navigating away from the current one, so following a reference link from an incident note never loses the operator's place.

---

## Default Value and the "Editable at destination level" flag

This is the mechanic that gives the feature most of its flexibility. Each field definition has two extra settings, alongside its type:

| Setting | Purpose |
|---|---|
| **Default Value** | The value used to seed this field the first time it's associated with a Destination. Optional — a field doesn't need one. |
| **Editable at destination level** | Whether a Destination is allowed to override that default with its own value. On by default. |

How the two combine determines what kind of field you get:

### Editable (the default) — a baseline you can customize per destination

With **Editable** left on, the Default Value is just a *starting point*: the first time a Destination's Metadata tab is opened, the field shows that default, ready to be edited. From then on, that Destination has its own independent value — editing it never affects any other Destination, and the central Default Value is only ever used again if a *new* Destination is associated with the field (or if an existing one clears its own value back to blank).

This is the right choice for information that's *mostly* the same everywhere but occasionally needs a local tweak — a standard procedure with one or two destination-specific details added, for example. See the worked example below for exactly this pattern, including using placeholders in the default text that each destination fills in with its own details.

### Not editable — a single, centrally-managed value

With **Editable** turned off, the field becomes **locked**: every Destination's Metadata tab shows it read-only (with a 🔒 icon next to its label), and it always reflects the field definition's *current* Default Value, live — not a snapshot taken when the Destination was created. If an administrator updates the central Default Value tomorrow, every Destination picks up the change immediately, with nothing to re-save per Destination.

This is the right choice for genuinely static, organisation-wide information that must stay identical and in sync everywhere — a legal/compliance disclaimer, a standard on-call paging procedure, a link to the incident-response wiki. Change it once, centrally, and it's correct everywhere instantly; no risk of 200 destinations silently drifting out of sync because 199 of them never got the memo.

!!! note "A locked field can still be exported per destination"
    Locking a field only affects its *content* — the per-destination **Include in Notes** flag (below) is still independent, so one Destination can choose to include that locked, centrally-managed text in its Opsview notes while another doesn't, even though neither can change *what* the text says.

---

## Including metadata in Opsview notes

On a Destination's Metadata tab, every field that isn't a `password` has an **Include in Notes** checkbox. Any field flagged this way is pushed to Opsview as a host note when an operator (or administrator) clicks **Export Notes** — one block per field, its label in bold, a separator, then its value(s), with another separator between fields so multiple blocks don't run together visually.

A `markdown` field's content is rendered to safe HTML for the export (so headings, lists, tables and links show up properly in Opsview, not as raw Markdown syntax); every other type is exported as plain text (structured types like `contact` are flattened to a readable "Name, email, phone" line).

Click **Preview Notes**, next to **Export Notes**, to see the saved note before
sending it. The preview uses exactly the same HTML rendering as export, including
field ordering, centrally managed defaults, and password exclusion, without sending
anything to Opsview. Opsview's own styling may differ. Both buttons require metadata
edit permission and activated monitoring, and are disabled until changes are saved.

---

## Worked example: an operator runbook

Say you want every Destination to carry a short "what to do if this breaks" note, visible directly in Opsview, without writing 200 almost-identical runbooks by hand.

**1. Define the field** on `/do/admin/metafields`:

- **Name**: `operatorRunbook`, **Label**: `Operator Runbook`, **Type**: `markdown`, **Category**: `Documentation`
- **Editable at destination level**: on (this one *should* vary a little per destination)
- **Default Value**:
  ```markdown
  # Operator Runbook

  **Primary contact:** _add contact here_
  **Backup contact:** _add contact here_

  ## If transfers are failing
  1. Check the [Monitoring page](https://example.org/monitoring) for this destination's current status.
  2. Confirm the destination Host is reachable (see Network Info / MTR).
  3. If down for more than 30 minutes, escalate per the standard [Escalation Procedure](#).

  ## Known quirks
  _destination-specific notes go here_
  ```

**2. Associate it with a Destination.** The first time `hourly_aq`'s Metadata tab is opened, it starts with exactly this text — the common structure and the standard first-response steps are already there. A local administrator then replaces the two placeholder lines (`_add contact here_`) with the actual contacts for that destination, and adds a line or two under "Known quirks" specific to it (e.g. *"This feed occasionally stalls for ~10 minutes around 00:00 UTC during the upstream provider's own rollover — this is expected, do not escalate before 00:15."*). Everything else — the numbered first-response steps, the link to Monitoring — stays as authored centrally, since there was no need to touch it.

**3. Flag it "Include in Notes" and click Export Notes.** The rendered runbook — contacts, steps, and this destination's own quirks — now appears directly on the Opsview host, right where an operator responding to an alert is already looking. No separate wiki lookup needed.

Meanwhile, a *second*, genuinely organisation-wide field — say `escalationPolicy`, **Editable at destination level** turned **off** — carries the one paging procedure that must never vary between destinations (e.g. *"No response after 30 minutes: page the on-call Data Engineer via the ECPDS-Ops rotation."*). It shows up read-only on every Destination's Metadata tab, and because it's also flagged **Include in Notes**, it appears in every Destination's exported Opsview note too — word-for-word identical, everywhere, and automatically updated everywhere the moment it's edited once, centrally.

Together, a Destination's exported notes end up combining both: a shared, centrally-enforced escalation policy, plus a destination-specific runbook built from a common template — exactly the mix of consistency and flexibility the two modes are designed to give you.

---

## Related

- [REST API — Destination Metadata](../rest-api.md#destination-metadata)
- [REST API — Sensitive Fields](../rest-api.md#sensitive-fields)
- [Destination Options](../concepts/destination-options.md)
- [Monitor UI — Administration](../monitor-ui/admin.md)
