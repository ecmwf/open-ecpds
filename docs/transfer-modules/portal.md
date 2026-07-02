# Portal Transfer Module

!!! info
    The **Portal** module is a *no-operation transfer module*. Instead of sending a
    file to a remote host, it consumes the data stream locally and marks the file as
    available for retrieval through the [Data Portal](../use-cases/data-portal.md). No
    connection is made to any external system.

## How it works

1. `connect()` immediately marks the module as available — no network call is made.
2. `put()` reads and discards all bytes from the input stream, recording the byte
   count. This ensures the file is staged and available in the Data Portal for
   authorised users to pull.
3. `size()` returns the number of bytes consumed, satisfying the transfer framework's
   post-upload size check.
4. `del()` and `get()` are not supported and will throw an error if called.

## Configuration options

This module has **no configurable options**. All behaviour is determined by the host
record (type, directory, credentials) and the Data Portal configuration on the
[Mover Server](../architecture/components.md). There are no `portal.*` properties to
set.

## Typical use

Assign this module to a **Dissemination** host when the intention is not to push data
to a remote server but to make it available for authorised users to pull via the
integrated Data Portal interface. The transfer completes successfully as soon as all
bytes have been consumed — no remote connection is required.

## Related

- [Transfer Modules overview](index.md)
- [Data Portal use case](../use-cases/data-portal.md)
- [Hosts & Transfer Methods](../concepts/entities.md)
