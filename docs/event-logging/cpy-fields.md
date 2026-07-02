# CPY (Copy / Replication) Fields

The **CPY** event records details about file replication within OpenECPDS. It tracks when
a file is duplicated either internally within an OpenECPDS Data Mover or transferred from
an internal Data Mover to a [Continental Data Mover](../architecture/continental-data-movers.md).
This event captures key metadata, transfer details, and any errors encountered during
replication, ensuring traceability and monitoring of data movement across the system.

## Example

```text
CPY;Monitored=true;TimeStamp=2025-02-12 11:00:00.0;DataTransferId=123456789;DestinationName=MyDestination;DestinationType=Gold;FileName=/my/home/datafile.bin;FileSize=196445574;ScheduledTime=2025-02-13 08:07:46.13;StartTime=2025-02-13 08:07:51.022;MetaStream=-;MetaType=-;MetaTime=00;TimeBase=2025-02-13 00:00:00.0;TimeStep=-1;Duration=1040;CountryCode=ec;Target=my.target.mover.name.it;TransferServer=my.source.mover.name.it;Caller=/my/home/datafile.output;ExpiryTime=2025-02-15 08:07:46.13;FileSystem=18;Status=true;Message=-;Action=replicate
```

## Fields

| Field | Description |
|-------|-------------|
| **Monitored** | Indicates whether the destination responsible for the data transfer is tracked in the top-level monitoring display. |
| **DataTransferId** | Unique identifier of the data transfer request. |
| **DestinationName** | Name of the OpenECPDS destination handling the data transfer request. |
| **DestinationType** | Type of the destination. |
| **FileName** | Full name of the transferred file. |
| **FileSize** | Size of the transferred file. |
| **ScheduledTime** | Scheduled time for processing the data transfer request. |
| **StartTime** | Timestamp when the data transfer began. |
| **MetaStream** | Metadata stream associated with the data file (same as RET). |
| **MetaType** | Metadata type associated with the data file (same as RET). |
| **MetaTime** | Metadata time associated with the data file (same as RET). |
| **TimeBase** | Time base associated with the data file (same as RET). |
| **TimeStep** | Time step associated with the data file (same as RET). |
| **Duration** | Total duration of the data transmission, in milliseconds. |
| **CountryCode** | Country code assigned to the destination entity. |
| **Target** | File name on the target site after dissemination. |
| **TransferServer** | Name of the server handling the data transfer (same as RET). |
| **Caller** | Identifies the caller initiating the transfer (same as INH). |
| **ExpiryTime** | Expiry time associated with the data transfer request. |
| **FileSystem** | Identifier of the file system allocated for storing the data file. |
| **Status** | Indicates whether the replication was successful (`true`) or not (`false`). |
| **Message** | Error message providing details in case of a replication failure. |
| **Action** | Specifies whether the replication was internal within the OpenECPDS Data Mover (`replicate`) or between an internal Data Mover and a Continental Data Mover (`proxy`). |

## Related

- [Event Logging overview](overview.md)
- [Continental Data Movers](../architecture/continental-data-movers.md)
- [Replication, Source, Backup & Proxy Directory](../host-directory/replication.md)
