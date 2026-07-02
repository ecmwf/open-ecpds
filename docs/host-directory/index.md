# Host Directory Field

The **Directory** field is interpreted differently depending on the host type. It is not a single generic setting: acquisition hosts use it as a remote listing specification, dissemination hosts use it as a target-path template, and replication/source/backup/proxy hosts use it as a base path for the ECtrans destination.

## Host-type behaviour

| Host type | Directory field meaning | Script support | Main behaviour |
|---|---|---|---|
| Acquisition | Listing specification | Yes | Each non-empty line describes one remote directory to scan for files to retrieve. The entire field may also be a **JavaScript** or **Python** script that returns listing lines dynamically at runtime. The script runs on a DataMover via `TransferScheduler.execution()`. |
| Dissemination | Target-path template | Yes | The template is applied to every transfer. Variable tokens such as `$host[…]`, `$dataFile[…]`, `$dataTransfer[…]`, and others are substituted at transfer time by `TransferManagement.getTargetName()`. Selector syntax can choose different paths based on conditions. |
| Replication, Source, Backup, Proxy, and unknown types | Base path | No | Only the first line is used. The path is truncated at the first `$` character. Dissemination-style selector blocks starting with `(` are skipped entirely. |

## Pages

- [Acquisition Directory](acquisition.md) — full acquisition listing specification, script mode, variables, selection rules, scheduling, and advanced options.
- [Dissemination Directory](dissemination.md) — target path templates, selector syntax, script behaviour, and available variables.
- [Replication, Source, Backup & Proxy Directory](replication.md) — base-path handling for other host types.

## Related

- [Acquisition Directory](acquisition.md)
- [Dissemination Directory](dissemination.md)
- [Replication, Source, Backup & Proxy Directory](replication.md)
- [Transfer Modules](../transfer-modules/index.md)
