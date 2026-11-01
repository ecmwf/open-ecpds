# Login Page Content

Use **Administration → Login Page Content** to manage the short phrases and
documentation links shown beneath the **Monitor** and **Data Portal** login forms.
This feature is separate from [System Messages](system-messages.md); it does not
display maintenance notices.

## Managing content

- **Enable login page content** turns the whole area on or off, including its
  persistent **About OpenECPDS** link.
- **Create** opens a message dialog; the row's **Edit** icon opens an existing entry.
  Messages are limited to 160 characters. **Apply to draft** updates the
  table; **Cancel** leaves the draft unchanged.
- **Format** selects Plain text (default) or Restricted Markdown, with the same
  emphasis/list rendering as System Messages. Line breaks require Markdown.
  Markdown links must still point into the OpenECPDS documentation site.
- **Documentation article** is optional. Choose a suggested path or enter a path
  within the OpenECPDS documentation, such as `why-openecpds/` or
  `use-cases/data-portal/`. Paths must end in `/`; external URLs, queries and
  fragments are not accepted.
- **Link label** describes the optional article, with a limit of 80 characters.
- **Show on** selects Monitor, Data Portal or both.
- **Visibility** disables an individual entry without removing it.
- The up/down arrows change rotation order; **Remove** deletes an entry from
  the draft after confirmation. These changes take effect only after **Save**.

Use **Search** and the page-size selector to browse the table. Filtering and
pagination do not change rotation order or exclude messages from saving or preview.
Unsaved draft changes highlight **Save**, with an explanatory tooltip. Leaving the page
prompts for confirmation; reloading or closing the tab uses the browser's warning.
This also protects edits still open in the message dialog. Preview does not clear
the unsaved state. The save confirmation dismisses automatically after four seconds.

The Create/Edit dialog includes a live **single-message preview** of the phrase,
formatting and article link in the active theme. Disabled messages can be inspected
as samples without enabling them. Preview does not apply the dialog changes to the
draft or save them; use **Apply to draft** when ready.

Up to 50 entries are supported. Select **Monitor** or **Data Portal** beside
**Preview** to show unsaved content in the preview panel, using the same rendering as the
login pages. Saving persists the shared configuration in `SYS_CONFIG`, under
`LoginPageContent/configuration`; no additional database table is required.
New login page requests see saved changes without restarting services. Pages
already open retain their existing content until reloaded.

!!! warning "Public content"
    These messages can be read before authentication. Do not include credentials,
    private incident details, personal information or internal-only announcements.
    Administrator-supplied HTML and JavaScript are not supported; raw HTML is escaped.
    Restricted Markdown does not allow headings, tables or images; unsafe and
    non-documentation links are removed.
    Article links open the public documentation in a new tab.

## Behaviour and defaults

### Seeding an existing production database

The standalone script [`login-page-content.sql`](https://github.com/ecmwf/open-ecpds/blob/main/docker/ecpds/database/login-page-content.sql)
adds the same twelve phrases and article links used for new database installations.
It requires the existing `SYS_CONFIG` table and preserves any configuration already
saved, including disabled content. It is safe to rerun; it does not append to or
replace an existing message list.

Back up the database and run this script against the intended database:

```sh
mysql --default-character-set=utf8mb4 -u USER -p DATABASE < docker/ecpds/database/login-page-content.sql
```

The script prints the resulting configuration for verification. Use the
administration editor to change an existing configuration. **Do not run the full
`ecpds.sql` initialization script against production**: it drops existing tables.
Reload the login pages to see the inserted content; no service restart is needed
when the updated services are already deployed.

### Defaults and rotation

When no configuration has been saved, both pages show a small built-in set of
multi-protocol and domain-agnostic phrases, linking to relevant documentation.
Disabling the feature is persistent and does not affect authentication.

With multiple visible entries, one phrase rotates every **8 seconds** with a
brief fade. The message area reserves space so rotation does not move the form.
Users can pause it; rotation also pauses while the area has keyboard focus, while
the pointer is over it, and while the browser tab is hidden. Reduced-motion
preferences start the component paused and suppress transitions. A single
visible phrase is static; with no visible phrases, only the About link remains.

This optional content fails closed to an empty area if configuration retrieval
fails, leaving the login form available. It does not introduce analytics,
tracking, remote article fetching or changes to login validation.

Deploy the updated Master alongside the Monitor and Data Movers: the Data Portal
obtains its configuration through a new Master RMI method. An older Master cannot
provide it; the Portal omits the optional content in that case.
