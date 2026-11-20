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

Use the focused regression runners from the repository root:

```bash
bash scripts/tests/rotation-protocols.sh
bash scripts/tests/rotation-database.sh
```

The protocol runner uses authenticated loopback TLS endpoints and synthetic
credentials. The database runner creates disposable MariaDB resources and
covers configuration binding, authorization, draining, activation and
retirement. Neither runner uses live cloud accounts or shared services.

For the standalone OpenBao lifecycle, package the Java core and build matching
standalone/OpenBao images, then run:

```bash
STANDALONE_TEST_IMAGE=<your-local-standalone-tag> \
  OPENBAO_TEST_IMAGE=open-ecpds/openbao:2.7.2 \
  python3 -u scripts/tests/standalone-openbao-lifecycle-test.py
```

This runner covers vault initialization/unseal, scoped token permissions,
persistence across restart, startup with empty and populated data volumes, and vault/SQL retirement
recovery. Its minimal Hibernate fixture disables notification revision fencing
only inside its disposable environment. These are component and integration
regressions; complete service-deployment acceptance also needs the actual
Monitor, Master and final Movers, including direct and Continental routing.
Keep application validation separate from changes to shared running services.

## Database requests

Keep application SQL in `etc/master/conf/sql` and execute it through
`ECpdsBase`/`DataBase` and Broker. Existing `$parameter` templates remain
supported; migrate them incrementally rather than rewriting unrelated requests.

For new value parameters, keep the normal prompt declaration, add its binding
type, and use an **unquoted** `:name` placeholder:

```sql
#menu "ECpdsBase"
#name "lockHostPasswordActive"
#group "select"
#timeout "5"
#prompt "host;Host identifier;;"
#bind "host;LONG"
SELECT 1 FROM HOST_PASSWORD_ACTIVE WHERE HOS_NAME=:host FOR UPDATE
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
Legacy requests retain their existing escaping and substitution behavior.

For dependent operations, use `DataBase.scriptTransaction` and its named-map
`prepare`/`executeBatch` operations so all statements share one Broker
transaction. A standalone `FOR UPDATE` request does **not** make a later,
separate request atomic with it.

SQL identifiers, sort directions and fragments cannot be bound as values.
If a bound request also needs `$parameter` structural substitution, declare
explicit `#prompt` choices and keep those choices in the SQL file; other values
are rejected. Existing conditional directives still work, and bindings in an
inactive branch do not require values.

Bound requests contain one SQL statement; they do not use the legacy
semicolon-splitting, `LOOP`, `CHUNK`, or executable-comment mechanisms. Use an
explicit count request for bound pagination when found-row emulation is enabled.
Native MariaDB `SQL_CALC_FOUND_ROWS` and legacy pagination remain supported.

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
