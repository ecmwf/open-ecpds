# PRS (Product Status) Fields

The **PRS** category tracks the status of products as they progress through OpenECPDS. The
system maintains a top-level monitoring display, which can be updated using the
[`ecpds` command](../use-cases/ecpds-cli.md). Events in this category are logged at key
stages, including when a set of files is scheduled for processing, when files start being
registered in OpenECPDS, and when they are fully recorded in the system.

Each PRS event records a specific moment in the processing of a product, capturing key
information about the product's status and related attributes.

## Example

```text
PRS;TimeStamp=2025-02-12 11:00:00.0;StatusCode=EXEC;DataStream=GWAEF;TimeStep=144;TimeBase=2025-02-12 06:00:00.0;Type=EF;ScheduleTime=2025-02-12 13:04:00.0;LastUpdate=2025-02-12 12:28:44.353
```

## Fields

| Field | Description |
|-------|-------------|
| **StatusCode** | The status of the product at a given stage of processing: **expected** (scheduled but not yet started), **started** (processing has begun), or **completed** (processing has finished). Determined by the flags used with the `ecpds` command-line tool: `-expected`, `-started`, or `-completed`. |
| **DataStream** | Identifies the data stream associated with the product. Used to group and identify the family of products being processed together. |
| **TimeStep** | The time step associated with the product, typically representing the temporal resolution of the data (e.g. hourly, daily, or a specific observational period). |
| **TimeBase** | The time base associated with the product — the reference time or baseline from which the time steps are derived, helping maintain synchronisation across time-dependent datasets. |
| **Type** | The type of the product. Helps categorise the product, often reflecting its role or the type of data it represents (e.g. forecast, observation, analysis). |
| **ScheduleTime** | The scheduled time when the data files related to the product are expected to be available in OpenECPDS for dissemination. Typically set based on the timing of data generation or readiness. |
| **LastUpdate** | The last time the status of the product was updated — the most recent timestamp when any change occurred in the processing of the product. |
| **ErrorMessage** | Contains a descriptive message when an error or unexpected condition is encountered during product status processing. Indicates issues such as a missing required option, or a status that is already expected, not expected, or already completed (in which case the notification is ignored). |

## Related

- [Event Logging overview](overview.md)
- [ECPDS command-line Tool](../use-cases/ecpds-cli.md)
- [RET fields](ret-fields.md)
