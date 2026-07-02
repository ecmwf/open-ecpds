# Replication, Source, Backup & Proxy Directory

!!! info
    For **Replication**, **Source**, **Backup**, **Proxy**, and unknown host types, the Directory field specifies the **base path** used on the DataMover (or remote server) when setting up the ECtrans destination. Only the **first line** is used; the path is truncated at the first `$` character. Script mode is not supported for this host type.

## Format

Enter a plain path. Only the first line is read. If the path contains a `$` token the path is truncated there (the part before `$` is used as `realDir`). Dissemination-style selector blocks starting with `(` are skipped entirely.

```
/ecpds/data/store/
```

## Variables

Because the path is truncated at the first `$`, substitution variables are **not** generally useful here. The raw string before the first `$` is taken as the directory.

## Internal usage

This value is passed to the ECtrans module as the `dir` / `realDir` when the DataMover opens a connection for file transfer. The exact interpretation depends on the ECtrans module configured for the host's transfer method.

## Related

- [Host Directory Field](index.md)
- [Acquisition Directory](acquisition.md)
- [Dissemination Directory](dissemination.md)
- [Transfer Modules](../transfer-modules/index.md)
