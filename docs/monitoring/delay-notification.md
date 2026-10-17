# Delay Notification

**Operations → Delay Notification** (`/do/monitoring/delaynotification`) lets operators send a single, consistent
notice for all the products and cycles affected by a delay, then tell the same users when it is resolved. It is
available to users who can access the monitoring pages. The page is also available from existing `/do/monitoring*`
links and bookmarks.

## Send a notice

1. Filter products by name (same syntax as the monitoring page: comma-separated, `*`/`?` wildcards, `!` or `-` to
   exclude), by [model](../administration/product-descriptions.md#product-models), and/or by generation timing. The
   timing filter is multi-select: *No data / pending*, *Received >30m ahead*, *Received 15-30m ahead*,
   *Received 0-15m ahead*, *On schedule / slightly late*, *Not received / overdue*, and *Transitional / other*.
   A product remains visible when any of its cycles matches the selected timing categories; **All timings** disables
   this filter. Where a schedule offset is available, the timing category is based on that offset (including for
   completed products), so it agrees with the cell tint and hover text. Otherwise the generation status is used.
2. The **Before** column shows a colored dot for the nearest cycle status; hover it to see the exact time before
   schedule or how long it is overdue. Cycle cells use a soft tint matching the **Product Generation** legend; hover
   a cell to see that cycle's exact timing. Click a cell to set a cycle to **Definite**, **Possible** or cleared.
   Clicking a product name cycles its whole row. Cycles come from the monitoring data. **Uniform cells** gives each
   cycle cell the same fixed size. **Mark late/overdue D** automatically marks cells on schedule/slightly late or
   overdue as Definite; **Mark 0-15m ahead P** marks cells received 0-15 minutes ahead as Possible. These buttons
   follow the same schedule-adjusted categories as the cell colors and apply only to the cells shown by the current
   filters. **Clear all** also only removes the selections of the filtered cells. Cells marked earlier but hidden by a
   filter are kept, but are not counted, used for the recipients or included in the message until shown again.
   When filters leave no visible products, the matrix shows *No matching records found.* and the marking buttons
   are disabled. The layout buttons switch between side-by-side matrix and notice, matrix only, or notice only.
3. The **To** pill shows the number of destinations concerned; click it to list them. They are the destinations
   receiving the selected product/cycle combinations (as in the table at the bottom of `/do/monitoring`), limited to
   those showing in the monitor unless *Include destinations not showing in the monitor* is on. Their contact emails
   are de-duplicated and the list is always recomputed by the Monitor when the notice is registered.
4. Choose a predefined reason or select **Other (enter a reason)** and enter a free-text reason in the adjacent field.
   The reason, ETA and expected delay are added to the message; free-text reasons are saved with the notice and
   restored when it is reopened. Add a note and review the editable subject and message. The message starts from the
   grouped *Products Delay* message managed in **Product Messages** (`/do/admin/productmessages`); `{{PRODUCTS}}` (or
   `{{PRODUCT}}`) becomes the list of selected products, `{{CYCLES}}` the selected cycles and `{{DESCRIPTION}}` the
   product descriptions. The Definite/Possible lists, reason, expected delay, ETA and note are inserted at
   `{{AFFECTED}}` (or after the first paragraph when that placeholder is absent). In **Product Messages**, the grouped
   templates can instead place `{{REASON}}`, `{{ETA}}`, `{{EXPECTED_DELAY}}` and `{{NOTE}}` individually; each value
   placed this way is omitted from the affected block. Resolution messages use the grouped *Products Resumed* message
   and the original notice's saved values for those placeholders. When allowed by the flags in **Product Messages**,
   Bcc recipients (in addition to the selected destination recipients and configured always-in-Bcc list) and Cc
   recipients can each be switched off, edited, and saved as the default. Additional Bcc recipients are added to the
   destination recipients and configured always-in-Bcc list. Both fields accept
   valid email addresses separated by semicolons; invalid addresses block saving, previewing, and sending. The
   clipboard icon beside the recipient count copies the destination-recipient and configured Bcc recipient list and is
   disabled when that list is empty.
   The ETA is picked with hour/minute selectors (UTC) so no invalid time can be entered; the message and subject are
   generated as soon as the page loads. **Compact empty lines** is remembered per browser user and reduces runs of
   blank lines to one blank line in the generated or edited message. **Regenerate** rebuilds the message from the
   template and current values; **Preview** shows the final mail (subject, To/Cc/Bcc counts and body). When the Product
   Messages are written in Markdown (the default), the preview is formatted and a button at the bottom switches to the
   plain-text view used by Outlook (direct sends are HTML). The
   **Configuration Guide** button opens a side panel summarising all of this.
5. Choose **Send with Outlook** to register the notice (`DLY-YYYY-NNNN`) and open an Outlook message with the
   recipients in BCC, or **Send directly** to queue the email through the Master mail service. Direct sends are
   acknowledged automatically when accepted by that service. Outlook sends remain **to confirm** until the operator
   presses **I have sent it** (right after sending, or later from **Sent notices**). Pending messages can also be sent
   directly from **Sent notices**. If direct sending fails, the notice remains registered and the Outlook message opens
   as a fallback. If the BCC list is too long for an Outlook link, the product distribution lists are used and
   **Copy BCC emails** gives the explicit list.

The product summary email buttons offer the same Outlook and direct-send choices. Direct emails use the configured
notification addresses and are queued through the Master mail service.

The selected timing categories, layout, **Uniform cells**, and **Compact empty lines** settings are remembered in the
browser for the signed-in user. Default Cc and additional Bcc recipient addresses, and the flags allowing these fields, are
stored in `SYS_CONFIG` and managed in **Product Messages**.

## Sent notices

The register keeps every notice until an operator deletes it. For an active or partially resolved notice:

- **Inform resolution** opens a form listing the open cells (at least one must stay ticked). The subject and message
  are prefilled from the *Products Resumed* message and follow the ticked cells until edited manually (the message is
  then tinted); **Regenerate** rebuilds them and **Preview** shows the final mail. The resolution can then be
  registered and opened in Outlook, or sent directly. Resolving only some cells leaves the notice *partial* until all
  are resolved, then *resolved*.
- **Update** reopens the form prefilled; destinations that were not in the notice are highlighted and added to the
  recipients.
- **Cancel** closes the notice without resolution.

Resolved and cancelled notices can no longer be updated. **Delete** removes a notice from the register.

Notices are kept in the dedicated `DELAY_NOTICE` table as a permanent history (the page shows the latest 100 and can
load older ones). The Master allocates the references atomically, so several Monitors can register notices at the same
time, and changes are protected by a version check: if someone else modified the same notice, the operation is retried
on the fresh version. Models and the default Cc/Bcc addresses are stored in `SYS_CONFIG`.

The report controls below the table generate a CSV for notices created in the selected inclusive date range, optionally
filtered by status. The on-page summary shows status totals and the most affected products. The CSV includes affected
product/cycle rows; message history and recipient addresses are optional, and addresses are excluded by default.

Deploy the Master and the Monitors together, as the Monitor calls new Master operations.

## From a monitoring page

The **Email Notifications** card of a product page (e.g. `/do/monitoring/summary/GWAVE/18`) has a *Register a notice*
group next to the existing Outlook ones, which are unchanged. The same group is available on the all-cycles page of a
product (`/do/monitoring/summary/GWAVE`, all its cycles) and on the all-products page (`/do/monitoring/summary/`):

- **Register Delay Notice** opens this page with that product and cycle already selected as *Definite*.
- **Register Resumed Notice** opens the *Sent notices* tab with the resolution form of the open notice covering that
  product and cycle (only that cycle ticked). If no open notice covers it, a warning is shown.

In both cases the message is registered and must be confirmed as sent, as usual.
