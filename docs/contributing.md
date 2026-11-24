# Contributing

OpenECPDS is an open-source project, and contributions from the community are welcome.
Ongoing maintenance and updates focus on long-term sustainability and continuous
improvement to meet evolving user needs.

!!! note
    Parts of this page are a starting point for contributors. Project-specific policies
    (such as a formal Contributor License Agreement or detailed coding standards) should
    be confirmed against the repository's own governance files where available.

## Where development happens

The main development of OpenECPDS is done at the **European Centre for Medium-Range
Weather Forecasts (ECMWF)**. The project is hosted on GitHub:

- Repository: [github.com/ecmwf/open-ecpds](https://github.com/ecmwf/open-ecpds)

OpenECPDS was launched to encourage collaboration with other organisations, strengthen
integration efforts, and enhance data service capabilities through community
contributions.

## Setting up a development environment

1. Review the [System Requirements](getting-started/requirements.md).
2. Follow the [Installation](getting-started/installation.md) guide to download the
   distribution and build the development container (`make dev`, `make build`).
3. Configure your IDE using the [IDE Setup](getting-started/ide-setup.md) guide
   (VS Code Dev Containers or Eclipse).
4. Start the database and services and verify your build with the
   [First Run](getting-started/first-run.md) guide.

## Building and testing

Use the provided `Makefile` targets to build and run the application:

```bash
make dev      # create / log into the development container
make build    # compile, package RPMs, build Docker images
make start-db # create the Docker network and start the database
make up       # start master, monitor, mover and database
make ps       # check running containers
make logs     # view container stdout/stderr
make down     # stop the application
make clean    # clean logs and data
```

### Credential rotation checks

Local credential-rotation regression fixtures are maintained separately and are
not distributed in this repository. Component and integration checks do not
replace deployment acceptance with the actual Monitor, Master and Movers.
Keep application validation separate from changes to shared running services.

## Documentation conventions

Describe configuration and operations through the Monitor UI, command-line tools
and supported APIs. Do not instruct users, including administrators, to edit
application records directly in the database.
Physical database table names belong only in the [database reference](architecture/database.md)
and [changelog](changelog.md), not in other documentation or user-facing option help.

## Regenerating documentation screenshots

Use a disposable instance, not production: screenshots and recordings can expose
hostnames, user names, file names and configuration. The examples below use
the standalone image and keep its destinations stopped. The
[standalone access instructions](getting-started/standalone.md#access) explain
how to obtain its generated passwords.

Upload a synthetic file to the stopped `aviso_demo` destination through the Data
Portal. Repeat with other names to populate the table; include `forecast.txt`
for the optional recording:

```sh
printf 'Synthetic forecast for documentation\n' |
  curl --insecure --user test --upload-file - \
    https://localhost:7443/webdav/aviso_demo/forecast.txt
```

`--insecure` is for the standalone's self-signed certificate only. Do not start
the other example destinations merely to create screenshots: acquisition may
contact external providers.

From the development container, set `MONITOR_UI_BASE` to the reachable Monitor
URL and run:

```sh
MONITOR_UI_BASE=https://localhost:8443 make docs-screenshots \
  SCREENSHOT_FLAGS="--insecure --video --globe-demo"
```

The tool prompts privately for the generated `admin` and `monitor` passwords.
For automated use, `MONITOR_ADMIN_PASS` and `MONITOR_MON_PASS` can be supplied
through the environment; avoid exposing them in shell history or logs. Omit
`--insecure` when the certificate is trusted. Dark theme is the default;
`--theme light` selects the light theme instead. `--video` is optional and
records queue filtering, MQTT publication details and the Live Earth views.
`--globe-demo` supplies sample transfer events in the capture browser only;
it does not send transfers to the Master or contact remote hosts.
The capture excludes the product monitoring dashboard, which has no product
records in a fresh standalone.
Use `--destination NAME` to
choose another destination or `--output-dir PATH` to review captures separately.
Use `--api-client ID` if the existing client is named something other than `api`.
Run `python3 scripts/crawl_monitor_ui.py --help` for all options.
All selected pages must load successfully before generated files are replaced.
Review the resulting media for private information before committing it.

For populated traffic charts, use an evaluation instance with recorded transfer
and Data Portal activity. Do not edit database counters to populate screenshots.
Use the [MQTT upload example](getting-started/standalone.md#try-mqtt-upload-notifications)
to populate MQTT Activity and wait for broker outcomes before capture. The
capture tool itself does not insert statistics, upload files or modify accounts.

## Database requests

Keep application SQL in `etc/master/conf/sql` and execute it through
`ECpdsBase`/`DataBase` and Broker. Both typed bindings and `$parameter`
templates are supported.

For new value parameters, keep the normal prompt declaration, add its binding
type, and use an **unquoted** `:name` placeholder:

```sql
#menu "ECpdsBase"
#name "boundHostExample"
#group "select"
#timeout "5"
#prompt "host;Host identifier;;"
#bind "host;LONG"
SELECT :host
```

`#prompt` remains available to existing request-discovery and prompting tools.
`#bind` opts the request into JDBC value binding. Names can repeat in SQL and
are matched by name, not Java argument order. Placeholders inside SQL quoted
strings, identifiers and ordinary comments are left alone.

Supported binding types are `STRING`, `LONG`, `INTEGER`, `BOOLEAN`, `DECIMAL`,
`TIMESTAMP`, `DATE` and `TIME`. Types are checked before execution; boolean text
accepts `true`, `false`, `1` and `0`. Typed-map callers can supply Java `null` for
SQL `NULL`; the string `"null"` is not a null marker.

Database-layer methods can use the normal `executeSelect`, `executeQuery` and
`executeUpdate` APIs with a map of raw values, for example
`Map.of("host", hostId)`. Existing `String[]` arguments such as
`new String[] { "host=" + hostId }` also work. Do not SQL-escape bound values.
Requests using `$parameter` retain their escaping and substitution behavior.

For dependent operations, use `DataBase.scriptTransaction` and its named-map
`prepare`/`executeBatch` operations so all statements share one Broker
transaction. A standalone `FOR UPDATE` request does **not** make a later,
separate request atomic with it.

SQL identifiers, sort directions and fragments cannot be bound as values.
If a bound request also needs `$parameter` structural substitution, declare
explicit `#prompt` choices and keep those choices in the SQL file; other values
are rejected. Existing conditional directives still work, and bindings in an
inactive branch do not require values.

Bound requests contain one SQL statement; they do not use the
semicolon-splitting, `LOOP`, `CHUNK`, or executable-comment mechanisms. Use an
explicit count request for bound pagination when found-row emulation is enabled.
Native MariaDB `SQL_CALC_FOUND_ROWS` and pagination without bindings are supported.

## Suggested contribution workflow

*To be completed with the project's confirmed process.* A typical open-source workflow is:

1. **Fork** the repository and create a feature branch.
2. Make your changes, keeping them focused and well-described.
3. Ensure the project still builds (`make build`) and runs (`make up`).
4. Open a **pull request** against the `master` branch with a clear description of the
   change and its motivation.
5. Respond to review feedback.

## Reporting issues

*To be completed.* Use the GitHub issue tracker on the
[OpenECPDS repository](https://github.com/ecmwf/open-ecpds) to report bugs or request
features. When reporting a bug, include:

- The OpenECPDS version (see [Changelog](changelog.md) for version format).
- Steps to reproduce.
- Relevant log output (see [Event Logging](event-logging/overview.md) and the log
  directories described in [First Run](getting-started/first-run.md#checking-the-containers-and-logs)).

## Licensing

OpenECPDS is distributed under the **Apache License, Version 2.0**. By contributing, you
agree that your contributions will be licensed under the same terms. See the `LICENSE.txt`,
`AUTHORS`, and `NOTICE` files in the repository.

## Related

- [Support Materials](support.md)
- [Changelog](changelog.md)
- [Glossary](glossary.md)
