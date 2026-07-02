# Test Transfer Module

!!! info
    The **Test** module is a *simulation module* used for benchmarking and testing
    transfer pipelines without sending data to any real remote host. All operations
    (connect, put, del, copy) are faked — no actual network connections are made. All
    options use the `test.` prefix.

## How it works

- **connect** — resolves the local hostname and sets status. No TCP connection is made.
- **put (stream)** — writes bytes to a `NullOutputStream` throttled to
  `test.bytesPerSec`, simulating network throughput.
- **put (OutputStream)** — returns a throttled `NullOutputStream` directly to the caller.
- **copy** — sleeps for the time it would take to transfer *size* bytes at the
  configured rate.
- **size** — returns the number of bytes written to the null stream.
- **get** — not supported.

## Options

| Option | Default | Description |
|--------|---------|-------------|
| `test.bytesPerSec` | `10MB` | Simulated transfer rate. Controls how quickly bytes are consumed by the null stream and how long a copy operation sleeps. Accepts byte-size notation, e.g. `50MB`, `1GB`. |
| `test.delay` | `500ms` | Fixed delay injected before every operation (connect, del, put, copy, size, close). Accepts duration notation, e.g. `1s`, `200ms`. |
| `test.errorsFrequency` | `1000` | Inject a simulated `IOException` once every N status-changing operations. Set to `0` to disable error injection entirely. Useful for testing retry and error-handling behaviour. |

## Examples

Simulate a slow 1 MB/s link with a 1-second connect delay:

```properties
# Simulate a slow 1 MB/s link with a 1-second connect delay
test.bytesPerSec=1MB
test.delay=1s
test.errorsFrequency=0
```

Fast throughput test with occasional errors (1 in every 50 ops):

```properties
# Fast throughput test with occasional errors (1 in every 50 ops)
test.bytesPerSec=500MB
test.delay=0ms
test.errorsFrequency=50
```

## Related

- [Transfer Modules overview](index.md)
- [Continental Data Movers](../architecture/continental-data-movers.md)
- [Hosts & Transfer Methods](../concepts/entities.md)
