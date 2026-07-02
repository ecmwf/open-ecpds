# Host Options

!!! info
    These options configure behaviour in the **Host editor** **Properties** field.
    They apply to dissemination, acquisition and proxy hosts and use the `ecpds.`,
    `master.`, `proxy.` and `upload.` prefixes.

## `ecpds.*` — Data Mover Allocation

These options select which OpenECPDS Data Movers should handle a given type of transfer.
Each option accepts a value spanning multiple lines. Every line uses this format:

```text
({operator} transferGroupName) mover1,mover2,...
```

Supported operators are `==`, `!=`, `.=` and `=.`.

| Option | Type | Default | Description |
|---|---|---|---|
| `ecpds.moverListForSource` | Multi-line rule set | *none* | Select the list of data movers to use when downloading source files |
| `ecpds.moverListForBackup` | Multi-line rule set | *none* | Select the list of data movers to use when backing up a destination |
| `ecpds.moverListForProcessing` | Multi-line rule set | *none* | Select the list of data movers to use when disseminating, retrieving data through acquisition, and generating network reports |

### Quick-start example

```properties
ecpds.moverListForSource =
  (== internet) ecpds-dm1,ecpds-dm2
  (== rmdcn) ecpds-dm3

ecpds.moverListForProcessing =
  (.= prod) ecpds-dm4,ecpds-dm5
```

## `master.*` — Virtual FTP Access

| Option | Type | Default | Description |
|---|---|---|---|
| `master.homeDir` | Path | *host default* | Home directory exposed when accessing this host through the master's virtual FTP server. This is especially useful for acquisition hosts containing multiple directories |

### Quick-start example

```properties
master.homeDir = "/incoming/research"
```

## `proxy.*` — Continental Data Mover / Proxy

These options apply when a host uses a Continental Data Mover or proxy path.

| Option | Type | Default | Description |
|---|---|---|---|
| `proxy.httpMoverUrl` | URL | *selected mover URL* | Alternative HTTP URL for the data mover used by the Continental Data Mover to report its activity. Useful when only one mover is reachable from the continental side |
| `proxy.httpProxyUrl` | URL | *none* | HTTP proxy URL to use when connecting to the Continental Data Mover |
| `proxy.modulo` | Integer | *none* | After unsuccessful transmissions on the Continental Data Mover, retry locally once every `proxy.modulo` attempts |
| `proxy.timeout` | Duration | *module default* | Connection timeout for the Continental Data Mover HTTP URL |
| `proxy.useDestinationFilter` | Boolean | *disabled* | Reuse the destination's compression method, `ectrans.filterpattern` and `ectrans.filterMinimumSize` settings for replication to Proxy Movers |

### Quick-start example

```properties
proxy.httpMoverUrl = "https://dm-west.example.org:8443/openecpds"
proxy.httpProxyUrl = "http://proxy.example.org:3128"
proxy.modulo = "5"
proxy.timeout = "30s"
proxy.useDestinationFilter = "yes"
```

## `upload.*` — Upload Rate Control (Dissemination Hosts)

These options control dissemination upload rates and slow-transfer handling.

| Option | Type | Default | Description |
|---|---|---|---|
| `upload.interruptSlow` | Boolean | *disabled* | Terminate slow data transmissions when `upload.maximumDuration` is reached or the transfer falls below `upload.minimumRate` |
| `upload.maximumDuration` | Duration | *none* | Maximum duration allowed for a data transmission |
| `upload.minimumDuration` | Duration | *none* | Minimum duration before OpenECPDS starts checking transfer rate and maximum-duration thresholds |
| `upload.minimumRate` | ByteRate | *none* | Minimum rate allowed for a data transmission |
| `upload.rateThrottling` | ByteRate | *none* | Maximum rate allowed for a data transmission |

### Quick-start example

```properties
upload.minimumDuration = "2m"
upload.maximumDuration = "30m"
upload.minimumRate = "500KB"
upload.rateThrottling = "20MB"
upload.interruptSlow = "yes"
```

## Related

- [ECtrans common options](../transfer-modules/ectrans.md)
- [Acquisition options](../use-cases/acquisition-options.md)
- [OpenECPDS entities](../concepts/entities.md)
- [Continental Data Movers](../architecture/continental-data-movers.md)
