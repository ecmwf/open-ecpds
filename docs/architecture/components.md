# Components

OpenECPDS is composed of several cooperating services. This page describes the role of
each. For how they fit together, see the [Architecture Overview](overview.md).

## Master Server

The **Master Server** is the central coordinator of OpenECPDS. It:

- Authenticates clients (for example, the [`ecpds` command-line tool](../use-cases/ecpds-cli.md)).
- Registers metadata and allocates a **Data File ID** for each file.
- Assigns a **Data Mover** to receive or serve content, balancing load across available
  movers and source hosts.
- Runs the schedulers that drive processing: the **Data Transfer Scheduler**, the **Data
  Retrieval Scheduler**, and the **Acquisition Scheduler**.
- Records the requests and tracks their [lifecycle](data-transfer-lifecycle.md).

Communication with the Master Server occurs via secure HTTPS requests using a REST/JSON
interface, ensuring robust and scalable data handling.

## Mover Server (Data Mover)

The **Mover Server**, or **Data Mover**, is responsible for moving bytes. It:

- Connects to remote systems using the appropriate [transfer module](../transfer-modules/index.md)
  based on the protocol defined in the host configuration.
- Receives file content (push), retrieves it (acquisition/fetch), or streams it for
  dissemination.
- Stores content in the Data Store and replicates it across file systems and locations.

In the Data Portal workflows, two roles are distinguished:

- The **User Data Mover** is the server where the customer connects (FTP, SFTP, SCP,
  HTTPS or S3) to upload or download a file. In a multi-mover setup it is selected by a
  Load Balancer.
- The **Target Data Mover** is the server where the file is stored. It is allocated by
  the Master Server, considering available storage and system load.

The User Data Mover and Target Data Mover may not be the same. See the
[Data Portal use case](../use-cases/data-portal.md).

A [Continental Data Mover](continental-data-movers.md) is essentially a standard Data
Mover with limited functionalities and an alternative communication module designed to
receive instructions from the Master Server.

## Monitor Server

The **Monitor Server** provides the web-based monitoring and management interface
(available on port 3443 in the default setup). Through it, authorised users can:

- View the status of destinations and review the progress of data transmission.
- Manage destinations — request data transfers, change priorities, and start or stop
  transmissions.
- Inspect hosts, transfer methods, and data transfers.

## Data Portal

The **Data Portal** is a passive service that provides interactive, incoming access to
the Dissemination and Acquisition services. Remote sites connect to it using `ftp`,
`sftp`, `scp`, `s3`, and `wget`/`curl` to push or pull data. Incoming connections
support FTP, HTTPS, and S3 (SFTP and SCP through a Commercial API). See the
[Data Portal use case](../use-cases/data-portal.md).

## Database

The database persists the system's metadata: [destinations and aliases](../concepts/entities.md#destinations-and-aliases),
[hosts](../concepts/entities.md#dissemination-and-acquisition-hosts),
[data files and data transfers](../concepts/entities.md#data-files-and-data-transfers),
schedules, and history. In the default development setup it runs as a MariaDB container
(`make mariadb` opens a session).

## Related

- [Architecture Overview](overview.md)
- [Failover Mechanism](failover.md)
- [Continental Data Movers](continental-data-movers.md)
- [OpenECPDS Entities](../concepts/entities.md)
