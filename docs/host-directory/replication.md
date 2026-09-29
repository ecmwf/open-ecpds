# Replication, Source, Backup & Proxy Directory

!!! info
    For **Replication**, **Source**, **Backup**, **Proxy**, and unknown host types, the Directory field is a plain, static **base path** used on the DataMover (or remote server) when setting up the ECtrans destination. There is no scripting and no variable substitution for this host type, so **Test on Server** is not available.

## Format

Enter the path exactly as it should be used — plain text only, no `$variables` and no JavaScript/Python.

```
/ecpds/data/store/
```

## Related

- [Host Directory Field](index.md)
- [Acquisition Directory](acquisition.md)
- [Dissemination Directory](dissemination.md)
- [Transfer Modules](../transfer-modules/index.md)
