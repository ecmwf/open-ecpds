# UPH (Upload History) Fields

The **UPH** category records events whenever a file is disseminated from OpenECPDS to a
remote site. Each event captures details about the data transfer, including scheduling,
execution, and completion status.

## Example

```text
UPH;Monitored=false;TimeStamp=2025-02-12 11:00:00.0;DataTransferId=123456789;DestinationName=MyDestination;DestinationType=Gold;FileName=/my/home/datafile-260225.bin;FileSize=131259312;Identity=/my/home/datafile.bin;MetaStream=GENFO;MetaType=EF;MetaTime=00;Priority=69;QueueTime=2025-02-13 06:53:10.0;RequeueCount=0;ScheduledTime=2025-02-13 06:53:10.0;StartTime=2025-02-13 09:00:53.139;PutTime=2025-02-13 09:00:56.015;Duration=16471;DurationOnClose=18534;BytesSent=131259312;StatusCode=DONE;RetrievalTime=2025-02-13 05:42:01.694;TimeBase=2025-02-13 00:00:00.0;TimeStep=79;FinishTime=2025-02-13 09:01:14.584;TransferProtocol=sftp;TransferServer=my.mover.name.it;NetworkCode=Internet;HostAddress=my.host.it;PreSchedule=false;ProxyHost=X.X.X.X;Proxied=true;SocketStatistics=...;Compressed=-;CompressedOnTheFly=false
```

!!! note
    The `SocketStatistics` value above is abbreviated. When enabled, it contains the full
    output of the `ss` command (see field description below).

## Fields

| Field | Description |
|-------|-------------|
| **Monitored** | Indicates whether the destination responsible for the data transfer is tracked in the top monitoring display (same as in RET). |
| **DataTransferId** | Unique identifier of the data transfer request (same as in RET). |
| **DestinationName** | Name of the OpenECPDS destination for this data transfer request (same as in RET). |
| **DestinationType** | Type of the destination (same as in RET). |
| **FileName** | Full name of the file (same as in RET). |
| **FileSize** | Size of the file (same as in RET). |
| **Identity** | A string that allows the identification of recurring data transfers (same as in RET). |
| **MetaStream** | Metadata stream associated with the data file (same as in RET). |
| **MetaType** | Metadata type associated with the data file (same as in RET). |
| **MetaTime** | Metadata time associated with the data file (same as in RET). |
| **Priority** | Priority level assigned to the data transfer request in the destination queue. |
| **QueueTime** | The requested time for starting the dissemination. Initially set to the scheduled time but may change if the file has been re-queued or automatically retried. |
| **RequeueCount** | The number of times the data transfer request has been re-queued. |
| **ScheduledTime** | The scheduled time when the data transfer request is expected to be processed (same as in RET). |
| **StartTime** | The most recent time when the dissemination process started. |
| **PutTime** | The time when the connection to the destination was established, and the input stream was ready to be read. |
| **Duration** | The total time taken for data dissemination, measured from when the input stream started until it completed, in milliseconds. |
| **DurationOnClose** | The total time taken for data dissemination, in milliseconds, from the start until the output stream is fully flushed and closed. Can be longer than Duration if the transfer module uses large output buffers. |
| **BytesSent** | The number of bytes sent during transmission. May differ from the original file size if the transfer was incomplete, the file was compressed, or the transfer was resumed. |
| **StatusCode** | The final status of the transfer request: **Done** (successfully completed), **Stopped** (intentionally stopped), **Failed**, **Requeued** (re-queued for another attempt), or **Interrupted**. |
| **RetrievalTime** | The time when the data retrieval process started. |
| **TimeBase** | The time base associated with the data file (same as in RET). |
| **TimeStep** | The time step associated with the data file (same as in RET). |
| **FinishTime** | The time when the data transmission was completed. |
| **TransferProtocol** | The protocol used for the transfer (e.g. FTP, SFTP, HTTPS, S3, GCS). |
| **TransferServer** | The name of the Data Mover responsible for connecting to the host and processing the data dissemination. |
| **NetworkCode** | The network code as configured in the host entity (e.g. Internet, RMDCN). |
| **HostAddress** | The address or hostname of the target host. |
| **PreSchedule** | If set to true, the scheduled time is ignored, and the file is sent as soon as possible. |
| **ProxyHost** | If the transfer request was handled by a [Continental Data Mover](../architecture/continental-data-movers.md), this field contains the identifier of the proxy host used. On its own, this does not imply the Continental Data Mover delivered the file; it only means the file was staged there for backup/redundancy. |
| **Proxied** | Reliably indicates whether the data file was disseminated through a Continental Data Mover. |
| **SocketStatistics** | If the target host was configured to collect socket statistics before closing the data connection, this field contains the output of the `ss` command. |
| **Compressed** | Specifies whether the file was sent in a compressed format. |
| **CompressedOnTheFly** | Indicates whether the file was pre-compressed before transmission or compressed on the fly during the transfer. |

!!! note
    If a problem occurs during dissemination, an [ERR](err-fields.md) event will be
    triggered instead of a UPH event to provide error details.

## Related

- [Event Logging overview](overview.md)
- [Dissemination](../use-cases/dissemination.md)
- [ERR fields](err-fields.md)
- [Continental Data Movers](../architecture/continental-data-movers.md)
