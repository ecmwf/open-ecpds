# Delay Notification

**Monitoring → Delay Notification** (`/do/monitoring/delaynotification`) lets operators send a single, consistent
notice for all the products and cycles affected by a delay, then tell the same users when it is resolved. It is
available to the users who can access `/do/monitoring`.

## Send a notice

1. Filter the products by name (same syntax as the monitoring page: comma-separated, `*`/`?` wildcards, `!` or `-` to
   exclude) and/or by [model](../administration/product-descriptions.md#product-models).
2. Click cells of the matrix to set a cycle to **Definite**, **Possible** or cleared. Clicking a product name cycles
   its whole row. Cycles come from the monitoring data.
3. The **To** pill shows the number of destinations concerned; click it to list them. They are the destinations
   receiving the selected product/cycle combinations (as in the table at the bottom of `/do/monitoring`), limited to
   those showing in the monitor unless *Include destinations not showing in the monitor* is on. Their contact emails
   are de-duplicated and the list is always recomputed by the Monitor when the notice is registered.
4. Choose the reason, ETA and expected delay, add a note and review the editable subject and message. The message starts from the grouped *Products Delay* message managed in
   **Product Messages** (`/do/admin/productmessages`); `{{PRODUCTS}}` (or `{{PRODUCT}}`) becomes the list of selected products, `{{CYCLES}}` the
   selected cycles and `{{DESCRIPTION}}` the product descriptions. The Definite/Possible lists, reason, ETA and note are
   inserted at `{{AFFECTED}}` (or after the first paragraph when that placeholder is absent). Resolution messages start from
   the grouped *Products Resumed* message. The internal Cc
   can be switched off, edited, and saved as the default.
   The ETA is picked with hour/minute selectors (UTC) so no invalid time can be entered; the message and subject are generated as
   soon as the page loads, and **Preview** shows the final mail (subject, To/Cc/Bcc counts and body). The **Configuration Guide**
   button opens a side panel summarising all of this.
5. **Send notice** registers the notice (`DLY-YYYY-NNNN`) and opens an Outlook message with the recipients in BCC, as the
   *Products Delay* button does. ECPDS cannot see whether the mail is then sent from Outlook, so the notice is flagged
   **to confirm** until the operator presses **I have sent it** (right after sending, or later from **Sent notices**,
   where **Open Outlook again** reopens the message if needed). The confirmation records who confirmed and when. If the
   BCC list is too long for a link, the product distribution lists are used and **Copy BCC emails** gives the explicit
   list.

## Sent notices

The register keeps every notice until an operator deletes it. For an active or partially resolved notice:

- **Inform resolution** sends the resolution for all or only some cells (the notice becomes *partial* until all are
  resolved, then *resolved*).
- **Update** reopens the form prefilled; destinations that were not in the notice are highlighted and added to the
  recipients.
- **Cancel** closes the notice without resolution.

Resolved and cancelled notices can no longer be updated. **Delete** removes a notice from the register.

Notices are kept in the dedicated `DELAY_NOTICE` table as a permanent history (the page shows the latest 100 and can
load older ones). The Master allocates the references atomically, so several Monitors can register notices at the same
time, and changes are protected by a version check: if someone else modified the same notice, the operation is retried
on the fresh version. When upgrading an existing database, create the table once with `docker/ecpds/database/delay-notice.sql` (new
databases get it from `ecpds.sql`). Models and
the default Cc are stored in `SYS_CONFIG`.

Deploy the Master and the Monitors together, as the Monitor calls new Master operations.

## From a monitoring page

The **Email Notifications** card of a product page (e.g. `/do/monitoring/summary/GWAVE/18`) has a *Register a notice*
group next to the existing Outlook ones, which are unchanged. The same group is available on the all-cycles page of a
product (`/do/monitoring/summary/GWAVE`, all its cycles) and on the all-products page (`/do/monitoring/summary/`):

- **Register Delay Notice** opens this page with that product and cycle already selected as *Definite*.
- **Register Resumed Notice** opens the *Sent notices* tab with the resolution form of the open notice covering that
  product and cycle (only that cycle ticked). If no open notice covers it, a warning is shown.

In both cases the message is registered and must be confirmed as sent, as usual.
