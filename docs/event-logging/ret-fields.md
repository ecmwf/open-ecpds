# RET (Retrieval) Fields

The **RET** category corresponds to the retrieval process. Once a file is registered in
OpenECPDS, it is handled by the [Retrieval Scheduler](../use-cases/acquisition.md#retrieval),
which attempts to retrieve the data. Upon completion — whether successful or not — a RET
event is recorded to reflect the retrieval outcome.

## Example

```text
RET;Monitored=true;TimeStamp=2025-02-12 11:00:00.0;DataTransferId=123456789;DestinationName=MyDestination;DestinationType=Gold;FileName=/my/home/datafile-260225.bin;FileSize=9426076326;Identity=/my/home/datafile.bin;ScheduledTime=2025-02-12 13:04:00.0;StartTime=2025-02-12 12:29:43.98;MetaStream=GENFO;MetaType=EF;MetaTime=06;TimeBase=2025-02-12 06:00:00.0;TimeStep=144;Duration=40781;HostId=123456;HostLogin=login;HostAddress=my.host.it;TransferProtocol=sftp;MoverName=my.mover.name.it;UserId=uid;RequestAddress=X.X.X.X;DataOnlyDuration=40189;StandBy=false;RemoteHost=my.remote.host.it
```

## Fields

| Field | Description |
|-------|-------------|
| **Monitored** | A flag indicating whether the destination responsible for processing the data transfer is monitored in the top monitoring display. |
| **DataTransferId** | A unique identifier for the data transfer request. Used to track and manage the progress of the transfer. |
| **DestinationName** | The name of the OpenECPDS destination for this data transfer request. |
| **DestinationType** | The type of destination for the data transfer. |
| **FileName** | The full name of the file being transferred, including its path and extension. |
| **FileSize** | The size of the file in bytes, which helps assess the scope of the transfer. |
| **Identity** | A string that allows the identification of recurring data transfers. It enables ECPDS to track the same data transfer that runs periodically and monitor the sending time over multiple days. Typically, if the target name includes a date and time, the identity would be the target name without them. |
| **ScheduledTime** | The scheduled time when the data transfer request is expected to start processing, based on the transfer schedule. |
| **StartTime** | The actual time when the retrieval of the data file started, helping monitor delays or discrepancies compared to the scheduled time. |
| **MetaStream** | The meta stream associated with the data file, used for categorising or identifying a specific subset of data. |
| **MetaType** | The meta type associated with the data file, providing additional classification or type information. |
| **MetaTime** | The meta time associated with the data file — a specific timestamp or point in time relevant to the metadata. |
| **TimeBase** | The time base associated with the data file. |
| **TimeStep** | The time step associated with the data file. |
| **Duration** | The total duration of the data retrieval process, in milliseconds, from initiation to completion. |
| **HostId** | The identifier of the acquisition or source host used to retrieve the data file. Identifies where the file originated. |
| **HostLogin** | The user ID associated with the host system used to retrieve the file. |
| **HostAddress** | The address or hostname of the host from which the file is being retrieved. |
| **TransferProtocol** | The protocol used to connect to the host for the data retrieval (e.g. FTP, SFTP, HTTPS, S3, or GCS). |
| **MoverName** | The name of the Data Mover responsible for connecting to the host and processing the data retrieval. |
| **UserId** | The user identifier associated with the destination of the data transfer. |
| **RequestAddress** | The address of the host from where the registration of the data transfer was made — typically the host where the `ecpds` command was issued. If registration was done through the Acquisition System, this is the address of the host used for discovery. |
| **DataOnlyDuration** | The duration of the data retrieval process, in milliseconds, excluding protocol overhead such as authentication or directory navigation. Helps separate actual data transfer time from protocol-related delays. |
| **StandBy** | A flag indicating whether the transfer request is in **standby** mode after retrieval has completed. If set, the file remains inactive until further processing instructions; otherwise it is in **queued** mode, awaiting dissemination. |
| **RemoteHost** | Can be the same as the HostAddress or different, particularly in scenarios where the host entity is configured for load balancing across a cluster. In such cases, RemoteHost specifies the actual host used to process the retrieval. |

## Error fields

In the case of a problem during retrieval, the following additional fields are set to
provide error details:

| Field | Description |
|-------|-------------|
| **Status** | Set to `false` if the transfer was unsuccessful, indicating an error in the retrieval process. |
| **Message** | A description of the error or problem that occurred during the transfer, providing information for troubleshooting. |

## Related

- [Event Logging overview](overview.md)
- [Acquisition](../use-cases/acquisition.md)
- [UPH fields](uph-fields.md)
