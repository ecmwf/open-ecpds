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

## File-transfer connection limits

For **Acquisition** and **Dissemination** Hosts, **Max Connections** limits concurrent
file retrievals/uploads separately for each Destination by default. Each Destination's
own **Limits → Max Connections** also applies.
On the Destination page, the information/warning icon beside each acquisition or
dissemination Host explains its limit scope and the upper bound for that Destination.
Acquisition Hosts also show a live `N retrieving` pill for this Destination,
refreshed approximately every four seconds while the page is visible. For shared
limits, its tooltip includes the combined Host slot usage across Destinations.
These are scheduled retrieval counts, separate from the directory-discovery spinner.

To make all Destinations using the same Host record share its maximum, add this in
the Host's **Options → Properties**:

```properties
ectrans.sharedConnectionLimit="true"
```

For example, a Host maximum of 5 with two Destinations each allowing 4 permits up to
8 simultaneous transfers by default, or 5 combined with the option enabled.
Separate Host records remain independent even if they point to the same remote
hostname/IP. The shared limit applies across Data Movers managed by the same Master;
it is not coordinated between independent Masters.

A negative Host maximum means unlimited; zero prevents new file transfers.
Changing a limit does not interrupt transfers already running: new transfers wait
until capacity is available. Counts refer to scheduled file transfers, not physical
TCP sockets, directory listings, Network Weather probes or idle pooled connections.
One indexed retrieval counts as one scheduled retrieval even if it fetches multiple
files internally.

Acquisition also respects the download scheduler's **`[Scheduler] maxDownloadThreads`**
pool limit in the Master configuration. The shipped configuration takes this from
`${retrieval.maxPoolSize}`.

## Related

- [Acquisition Directory](acquisition.md)
- [Dissemination Directory](dissemination.md)
- [Replication, Source, Backup & Proxy Directory](replication.md)
- [Transfer Modules](../transfer-modules/index.md)
