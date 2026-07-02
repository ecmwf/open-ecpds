# ERR (Error) Fields

The **ERR** event is triggered when a data transfer fails in the context of
[dissemination](../use-cases/dissemination.md), providing detailed information about the
failed operation. It records metadata about the transfer, including file details, network
parameters, and user information, along with an error message describing the issue. This
helps diagnose and troubleshoot dissemination failures within OpenECPDS.

## Example

```text
ERR;Monitored=false;TimeStamp=2025-02-12 11:00:00.0;DataTransferId=123456789;DestinationName=MyDestination;DestinationType=Gold;FileName=/my/home/datafile.bin;FileSize=35850;ScheduledTime=2025-02-11 06:04:20.0;StartTime=2025-02-11 09:46:35.586;MetaStream=GOPER;MetaType=FC;MetaTime=00;TimeBase=2025-02-11 00:00:00.0;TimeStep=64;Duration=0;UserId=uid;CountryCode=fr;BytesSent=0;TransferProtocol=sftp;TransferServer=my.mover.name.it;HostAddress=my.host.it;NetworkCode=Internet;Message=Module sftp error <- Failed to connect to X.X.X.X:22 (SSH_MSG_DISCONNECT: 11 Too many bad authentication attempts!) (Trying from DataMover=my.mover.name.it)
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
| **ScheduledTime** | Scheduled time for the data transfer to begin. |
| **StartTime** | Actual time when the transfer attempt started. |
| **MetaStream** | Metadata stream associated with the data file (same as RET). |
| **MetaType** | Metadata type associated with the data file (same as RET). |
| **MetaTime** | Metadata time associated with the data file (same as RET). |
| **TimeBase** | Time base associated with the data file (same as RET). |
| **TimeStep** | Time step associated with the data file (same as RET). |
| **Duration** | Total duration of the failed data transmission attempt, in milliseconds. |
| **UserId** | Identifier of the user responsible for initiating the transfer request. |
| **CountryCode** | Country code assigned to the destination entity. |
| **BytesSent** | Number of bytes successfully sent before the transfer failure. |
| **TransferProtocol** | Protocol used for the transfer (e.g. FTP, SFTP, HTTPS). |
| **TransferServer** | Name of the Data Mover responsible for the transfer. |
| **HostAddress** | IP address or hostname of the remote site for dissemination. |
| **NetworkCode** | The network code as configured in the host entity (e.g. Internet, RMDCN). |
| **Message** | Detailed error message explaining the reason for the failure. |

## Related

- [Event Logging overview](overview.md)
- [Dissemination](../use-cases/dissemination.md)
- [UPH fields](uph-fields.md) — the successful counterpart
- [Lifecycle of a Data Transfer](../architecture/data-transfer-lifecycle.md)
