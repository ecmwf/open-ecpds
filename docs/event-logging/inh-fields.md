# INH (Incoming History) Fields

The **INH** event records details whenever a new file is pushed to or retrieved from
OpenECPDS via the [data portal](../use-cases/data-portal.md), or pushed using the
[`ecpds` command-line tool](../use-cases/ecpds-cli.md). It captures information about the
data transfer, including metadata, user details, and network parameters.

## Example

```text
INH;Monitored=true;TimeStamp=2025-02-12 11:00:00.0;DataTransferId=123456789;DestinationName=MyDestination;DestinationType=Gold;FileName=/my/home/datafile.bin;FileSize=162392;ScheduledTime=2025-02-13 07:41:00.0;StartTime=2025-02-13 09:05:32.616;MetaStream=GENFO-GTS;MetaType=EP;MetaTime=00;TimeBase=2025-02-13 00:00:00.0;TimeStep=240;Duration=1;UserId=uid;CountryCode=uk;UserDescription=User description;BytesSent=162392;TransferProtocol=ftp;TransferServer=my.mover.name.it;HostAddress=my.host.it;Caller=/my/home/datafile.output;ExpiryTime=2025-03-15 07:41:00.0;FileSystem=20;Action=download
```

## Fields

| Field | Description |
|-------|-------------|
| **Monitored** | Indicates whether the destination responsible for the data transfer is monitored in the top-level display (same as RET). |
| **DataTransferId** | Unique identifier of the data transfer request (same as RET). |
| **DestinationName** | Name of the OpenECPDS destination handling the data transfer request (same as RET). |
| **DestinationType** | Type of the destination (same as RET). |
| **FileName** | Full name of the transferred file (same as RET). |
| **FileSize** | Size of the transferred file (same as RET). |
| **ScheduledTime** | Scheduled time for processing the data transfer request (same as RET). |
| **StartTime** | Timestamp when the data transfer began (same as RET). |
| **MetaStream** | Meta stream associated with the data file (same as RET). |
| **MetaType** | Meta type associated with the data file (same as RET). |
| **MetaTime** | Meta time associated with the data file (same as RET). |
| **TimeBase** | Time base associated with the data file (same as RET). |
| **TimeStep** | Time step associated with the data file (same as RET). |
| **Duration** | The total duration of the data transmission, in milliseconds, whether inbound or outbound. |
| **UserId** | If the request originates from the `ecpds` command, this field contains the user who initiated the command. Otherwise, it holds the identifier of the underlying data user. |
| **CountryCode** | Country code associated with the underlying data user. |
| **UserDescription** | Description field associated with the underlying data user. |
| **BytesSent** | Total number of bytes sent or received during the transfer. |
| **TransferProtocol** | If the request originates from the `ecpds` command, this field is set to **ecpds**. Otherwise, it records the protocol used to connect to the data portal (e.g. FTP, SFTP, HTTPS). |
| **TransferServer** | If the request was registered via the `ecpds` command-line tool, this field contains the Master Server name. Otherwise, it holds the name of the Data Mover handling the data processing. |
| **HostAddress** | IP address of the client submitting the request. If the request was initiated via `ecpds`, this is the IP of the host where the command was executed. If submitted through the data portal, this is the address of the Master Server. |
| **Caller** | If the request comes from the `ecpds` command-line, this field can be set using the `-caller` flag. If the flag is not provided, `ecpds` attempts to retrieve the value from the system variable `EC_job_stdout`. If not found, or if the request originates from the data portal, this field remains empty. |
| **ExpiryTime** | Expiry time associated with the data transfer request. |
| **FileSystem** | Identifier of the file system allocated for storing the data file. |
| **Action** | Specifies whether the transfer request was related to an **upload** or a **download**. |

## Related

- [Event Logging overview](overview.md)
- [Data Portal](../use-cases/data-portal.md)
- [ECPDS command-line Tool](../use-cases/ecpds-cli.md)
- [DEA fields](dea-fields.md)
