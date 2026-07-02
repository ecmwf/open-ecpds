# Event Logging in OpenECPDS

OpenECPDS generates structured event logs that capture key activities and transitions
within the system, including data retrieval, replication, and dissemination processes.
These logs are designed to provide insight into system operations and facilitate
troubleshooting by recording events at different stages of the workflow.

At ECMWF, **Splunk** is used to collect, index, and analyse these event logs, enabling
efficient monitoring and alerting. However, OpenECPDS does not rely on Splunk specifically
— these logs follow a format that can be parsed by any log management or analysis tool,
such as ELK (Elasticsearch, Logstash, Kibana), Graylog, or Fluentd. This flexibility
allows different users and organisations to integrate OpenECPDS logs into their own
monitoring and analytics ecosystems.

The logs can be found in the directories mounted to the containers — see
[Checking the Containers and Logs](../getting-started/first-run.md#checking-the-containers-and-logs).

## Event Categories

OpenECPDS generates various types of events, each categorised based on the nature of the
operation being recorded:

| Code | Category | Description | Fields |
|------|----------|-------------|--------|
| **PRS** | PRoduct Status | Tracks the status of products as they progress through OpenECPDS. The system maintains a top-level monitoring display, which can be updated using the `ecpds` command. Events are logged at key stages, including when a set of files is scheduled, when files start being registered, and when they are fully recorded. | [PRS fields](prs-fields.md) |
| **RET** | RETrieval | Corresponds to the retrieval process. Once a file is registered, it is handled by the Retrieval Scheduler. Upon completion (successful or not), a RET event records the outcome. | [RET fields](ret-fields.md) |
| **UPH** | UPload History | Records events whenever a file is successfully disseminated from OpenECPDS to a remote site. Tracks outbound file transfers. | [UPH fields](uph-fields.md) |
| **INH** | INcoming History | Logs incoming data transfers — files pushed to OpenECPDS or retrieved by users via the data portal. | [INH fields](inh-fields.md) |
| **ERR** | ERRor | Captures error events, specifically when a file fails to be disseminated successfully. | [ERR fields](err-fields.md) |
| **CPY** | CoPY / Replication | Tracks file replication between different data movers. Records whether the replication was successful. | [CPY fields](cpy-fields.md) |
| **DEA** | DEnied Access | Captures unauthorised access attempts, helping administrators identify security threats, misconfigurations, or unexpected behaviour. | [DEA fields](dea-fields.md) |

## Field formats

Each event logged by OpenECPDS follows a structured format, with fields that provide
detailed information about the recorded operation. The fields vary depending on the event
category, capturing relevant attributes such as timestamps, file identifiers, processing
statuses, and transfer details.

!!! note
    All duration values are measured in **milliseconds**. All timestamps are formatted as
    `yyyy-MM-dd HH:mm:ss.fffffffff`, where `yyyy` is the 4-digit year, `MM` is the
    2-digit month, `dd` is the 2-digit day, `HH` is the 2-digit hour (24-hour clock),
    `mm` is minutes, `ss` is seconds, and `fffffffff` represents fractional seconds in
    nanoseconds.

!!! note
    A `TimeStamp` field has been added to all Splunk log entries to ensure log rotation is
    correctly detected. This prevents Splunk from misidentifying rotated files due to
    identical file headers and avoids missing or duplicated log events.

## Field references by category

- [PRS (Product Status)](prs-fields.md)
- [RET (Retrieval)](ret-fields.md)
- [UPH (Upload History)](uph-fields.md)
- [INH (Incoming History)](inh-fields.md)
- [ERR (Error)](err-fields.md)
- [CPY (Copy / Replication)](cpy-fields.md)
- [DEA (Denied Access)](dea-fields.md)

## Related

- [Lifecycle of a Data Transfer](../architecture/data-transfer-lifecycle.md)
- [ECPDS command-line Tool](../use-cases/ecpds-cli.md)
