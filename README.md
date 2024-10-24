<img src="img/OpenECPDS.svg" alt="SVG Image" width="500">

## Introduction

OpenECPDS has been designed as a multi-purpose repository, hereafter referred to as the **Data Store**, delivering three strategic data-related services:

- **Data Acquisition**: the automatic discovery and retrieval of data from data providers.
- **Data Dissemination**: the automatic distribution of data products to remote sites.
- **Data Portal**: the pulling and pushing of data initiated by remote sites.

Data Acquisition and Data Dissemination are active services initiated by OpenECPDS, whereas the Data Portal is a passive service triggered by incoming requests from remote sites. The Data Portal service provides interactive access to the Data Dissemination and Data Acquisition services.

- [Driving Forces](#driving-forces)
- [Data Storage and Retrieval](#data-storage-and-retrieval)
- [Protocols and Connections](#protocols-and-connections)
- [Object Storage](#object-storage)
- [Additional Features](#additional-features)
- [Getting Started](#getting-started)
- [Deployment](#deployment)
- [Concepts for Users](#concepts-for-users)
- [Support Materials](#support-materials)
- [Notes](#notes)

OpenECPDS enhances data services by integrating innovative technologies to streamline the acquisition, dissemination, and storage of data across diverse environments and protocols.

## Driving Forces

To ensure ECMWF's forecasts reach Member and Co-operating States promptly, efficient data collection and dissemination are crucial. To address this, ECMWF has developed the ECMWF Production Data Store (ECPDS) in-house, which has been operational for many years. This mature solution has greatly enhanced the efficiency and productivity of data services, providing a portable and adaptable tool for various environments.

The picture below shows the essential role of ECPDS in the ECMWF Numerical Weather Prediction (NWP) system.

<img src="img/Figure1.svg" alt="ECMWF acquisition and dissemination system" width="450"/>

Recently, ECPDS has been rolled out to some Member States and is now in use within the United Weather Centers (UWC) West consortium. Building on the success of these deployments and following requests from our Member States, OpenECPDS was launched to encourage collaboration with other organizations, strengthen integration efforts, and enhance data service capabilities through community contributions.

OpenECPDS is using container technologies, simplifying application building and operation across different environments. This allows for easy testing on laptops with limited resources or scaling up for large deployments involving hundreds of systems and petabytes of data.

The software is designed for ease of building, using a development container that includes all necessary tools for compiling source code, building RPM files, creating container images, and deploying the application. Comprehensive documentation is available in the [Getting Started](#getting-started) section, guiding users through setup and execution, ensuring a smooth experience even for those new to container technologies.
 
The launch of OpenECPDS signifies a commitment to ongoing maintenance and updates, focusing on long-term sustainability and continuous improvement to meet evolving user needs.

## Data Storage and Retrieval

Unlike a conventional data store, OpenECPDS does not necessarily physically store the data in its persistent repository but rather works like a search engine, crawling and indexing metadata from data providers. However, OpenECPDS can cache data in its Data Store to ensure availability without relying on instant access to data providers.

Data can be fed into the Data Store via:
- The **Data Acquisition** service, discovering and fetching data from data providers.
- Data providers actively pushing data through the **Data Portal**.
- Data providers using the **OpenECPDS API** to register metadata, allowing asynchronous data retrieval.

Data products can be searched by name or metadata and either pushed by the Data Dissemination service or pulled from the Data Portal by users. OpenECPDS streams data on the fly or sends it from the Data Store if it was previously fetched.

## Protocols and Connections

OpenECPDS interacts with a variety of environments and supports multiple standard protocols:

- **Outgoing connections** (Data Acquisition & Dissemination): FTP, SFTP, FTPS, HTTP/S, AmazonS3, Azure and Google Cloud Storage.
- **Incoming connections** (Data Portal): FTP, HTTPS, S3 (SFTP and SCP soon available).

Protocol configurations vary based on authentication and connection methods (e.g., password vs. key-based authentication, parallel vs. serial connections).

The OpenECPDS software is modular, supporting new protocols through extensions.

## Object Storage

OpenECPDS stores data as objects, combining data, metadata, and a globally unique identifier. It employs a file-system-based solution with replication across multiple locations to ensure continuous data availability. For example, data can be replicated across local storage systems and cloud platforms to bring data closer to users and enhance performance.

The object storage system in OpenECPDS is hierarchy-free but can emulate directory structures when necessary, based on metadata provided by data providers. OpenECPDS presents different views of the same data, depending on user preferences.

## Additional Features

- **Notification System**: Provides an embedded MQTT broker to publish notifications and an MQTT client to subscribe to data providers.
- **Data Compression**: Supports various algorithms (lzma, zip, gzip, bzip2, lbzip2, lz4, snappy) to reduce dissemination time and enable faster access to data.
- **Data Checksumming**: Provides MD5 for data integrity checks on the remote sites, and ADLER32 for data integrity checks in the data store.
- **Garbage Collection**: Automatically removes expired data, with no limit on expiry dates.
- **Data Backup**: Can be configured to map data sets in OpenECPDS to existing archiving systems.

## Getting Started

### Building and Running OpenECPDS

OpenECPDS requires Docker to be installed and fully functional, with the default Docker socket enabled (Settings -> Advanced -> "Allow the default Docker socket to be used"). The build and run process has been tested on Linux and macOS (Intel/Apple Silicon) using Docker Desktop v4.34.2. It has also been reported to work on Windows with the WSL 2 backend and the host networking option enabled.

To test the deployment of OpenECPDS containers to a Kubernetes cluster, Kubernetes must be enabled in Docker (Settings -> Kubernetes -> "Enable Kubernetes").

>**Warning:** If Kubernetes is properly installed, your `$HOME/.kube/config` file should point to `https://kubernetes.docker.internal:6443`. If not, you can manually update the file.

The default setup needs a minimum of 3GB of available RAM. The disk space required depends on the size of the data you expect to handle, but at least 15GB is essential for the development and application containers.

To download the latest distribution, run the following command:

```bash
curl -L -o master.zip https://github.com/ecmwf/open-ecpds/archive/refs/heads/master.zip && unzip master.zip
```

A [Makefile](Makefile) located in the `open-ecpds-master` directory can be used to create the development container that installs all the necessary tools for building the application. The Java classes are compiled, packaged into RPM files, and used to build Docker images for each OpenECPDS component.

#### Creating and Logging into the Development Container

To build the development container:

```bash
make dev
```

If successful, you should be logged into the development container.

#### Building and Configuring OpenECPDS

From there, you can run the following command to compile the Java classes, package the RPM files, and build the OpenECPDS Docker images:

```bash
make build
```
>**Warning:** In a production environment, ENV should be avoided in Dockerfiles for sensitive data like MYSQL_ROOT_PASSWORD for the [Database](docker/ecpds/database/Dockerfile) or KEYSTORE_PASSWORD for the [Monitor](docker/ecpds/monitor/Dockerfile) and [Mover](docker/ecpds/mover/Dockerfile). Docker secrets or environment variable files should be used instead.

Once the build process is complete, navigate to the following directory where another [Makefile](run/bin/ecpds/Makefile) is available:

```
cd run/bin/ecpds
```

The services are started using Docker Compose. The `docker-compose.yml` file contains all the necessary configurations to launch and manage the different components of OpenECPDS. You can find this file in the appropriate directory for your OS ([Darwin-ecpds](run/bin/ecpds/Darwin-ecpds/docker-compose.yml) for macOS or [Linux-ecpds](run/bin/ecpds/Linux-ecpds/docker-compose.yml) for Linux and Windows).

To verify the configuration and understand how Docker Compose interprets the settings before running the services, use the following command:

```
make config
```

For advanced configurations, you can fine-tune the options by modifying the default values in the Compose file. Each parameter is documented within the file itself to provide a better understanding of its function and how it impacts the system's behavior. By reviewing the Compose file, you can tailor the setup to your environment’s specific requirements.

#### Starting OpenECPDS

To start the application:

```
make up
```

This will start the OpenECPDS master, monitor, mover, and database services.

It might take a few seconds for all the services to start. Once they are up, you can access the following URLs (please update them if you changed the configuration in the compose files):

>**Warning:** Certificate validation should be disabled when relevant, as the test environment uses a self-signed certificate.

| Interface             | URL                                              | Login Details         |
|-----------------------|--------------------------------------------------|-----------------------|
| Monitoring            | [https://127.0.0.1:3443](https://127.0.0.1:3443) | admin/admin2021       |
| Data Portal           | [https://127.0.0.1:4443](https://127.0.0.1:4443) | test/test2021         |
|                       | [ftp://127.0.0.1:4021](ftp://127.0.0.1:4021)     | test/test2021         |
| MQTT Broker           | [mqtt://127.0.0.1:4883](mqtt://127.0.0.1:4883)   | test/test2021         |
| Virtual FTP Server    | [ftp://127.0.0.1:2021](ftp://127.0.0.1:2021)     | admin/admin2021       |
| JMX Interfaces        | [http://127.0.0.1:2062](http://127.0.0.1:2062)   | master/admin          |
|                       | [http://127.0.0.1:3062](http://127.0.0.1:3062)   | monitor/admin         |
|                       | [http://127.0.0.1:4062](http://127.0.0.1:4062)   | mover/admin           |

#### Checking the Containers and Logs

To verify that the containers are running, use:

```
make ps
```

To view the standard output (stdout) and standard error (stderr) streams generated by the containers, use:

```
make logs
```

To view the logs generated by OpenECPDS, you can browse the following directories mounted to the containers:

```
run/var/log/ecpds/master
run/var/log/ecpds/monitor
run/var/log/ecpds/mover
```

#### Additional Makefile Options

To log in to the database:

```
make mysql
```

To log in to the master container (use the same for monitor, mover, and database):

```
make connect container=master
```

#### Stopping OpenECPDS

To stop the application, run:

```
make down
```

To clean the logs and data:

```
make clean
```

## Deployment

If you have successfully built the OpenECPDS containers and enabled Kubernetes in Docker, navigate to the following directory where a [Makefile](deploy/kubernetes/Makefile) is available:

```
cd deploy/kubernetes
```

To convert the [docker-compose.yml](deploy/kubernetes/docker-compose.yml) file into Kubernetes YAML files in the  `k8s-configs` directory and start the pods, run:

```
make build
```

If successful, use the following command to get the port mappings needed to connect from outside the cluster:

```
make ports
```

If the default port for the monitoring interface has not been updated in the `docker-compose.yml` file, you can find the external port by checking the redirection for port 3443, for example:

3443 -> 32034/TCP

You can then use your browser to access the monitoring interface at:

https://127.0.0.1:32034

To get the YAML files for all pods, PVs, and PVCs (you can redirect the output to capture the full configuration), run:

```
make yaml
```

To delete all Kubernetes resources and stop the pods:

```
make delete
```

## Concepts for Users

Understanding some key OpenECPDS concepts will help users to benefit fully from the tool’s capabilities:

- **data files**
- **data transfers**
- **destinations** and **aliases**
- **dissemination** and **acquisition hosts**

A user connecting to the OpenECPDS web interface will come across each of these entities, which are related to each other through the different services.

A **data file** is a record of a product stored in the OpenECPDS Data Store with a one-to-one mapping between the data file and the product. The data file contains information on the physical specifications of the product, such as its size, type, compression and entity tag (ETag) in the Data Store, as well as the metadata associated with it by the data provider (e.g. meteorological parameters, name or comments concerning the product).

A **data transfer** is linked to a unique data file and represents a transfer request for its content, together with any related information (e.g. schedule, priority, progress, status, rate, errors, history). A single data file can be linked to several data transfers as many remote sites might be interested in obtaining the same products from the Data Store.

A **destination** should be understood as a place where data transfers are queued and processed in order to deliver data to a unique remote place, hence the name ‘destination’. It specifies the information the Data Dissemination service needs to disseminate the content of a data file to a particular remote site.

<img src="img/Figure2.jpg" alt="The OpenECPDS interface for internal and external user" width="600"/>

A breadcrumb trail at the top shows where a user currently is in the tool. In this case, a user has created a destination called EC1. Users with the right credentials can see the status of this destination and can review the progress of data transmission. They can manage the destination by, for example, requesting data transfers, changing priorities and stopping or starting data transmissions.

Each destination implements a transfer scheduler with its own configuration parameters, which can be fine-tuned to meet the remote site’s needs. These settings make it possible to control various things, such as how to organise the data transmission by using data transfer priorities and parallel transmissions, or how to deal with transmission errors with a fully customisable retry mechanism.

In addition, a destination can be associated with a list of **dissemination hosts**, with a primary host indicating the main target system where to deliver the data, and a list of fall-back hosts to switch to if for some reason the primary host is unavailable.

A dissemination host is used to connect and transmit the content of a data file to a target system. It enables users to configure various aspects of the data transmission, including which network and transfer protocol to use, in which target directory to place the data, which passwords, keys or certificates to use to connect to the remote system, and more.

If the data transfers within a destination are retrieved by remote users through the Data Portal service, then there will be no dissemination hosts attached to the destination. In this particular case, the destination can be seen as a ‘bucket’ (in Amazon S3 terms) or a ‘blob container’ (in Microsoft Azure terms). The transfer scheduler will be deactivated, and the data transfers will stay idle in the queue, waiting to be picked up through the Data Portal.

A destination can also be associated with a list of **acquisition hosts**, indicating the source systems where to discover and retrieve files from remote sites. Like their dissemination counterparts, the acquisition hosts contain all the information required to connect to the remote site, including which network, transfer protocol, source directory and credentials to use for the connection. In addition, the acquisition host also contains the information required to select the files at the source. Complex rules can be defined for each source directory, type, name, timestamp and protocol, to name just a few options.

A destination can be a dissemination destination, an acquisition destination or both. It will be a dissemination destination as long as at least one dissemination host is defined, and it will be an acquisition destination as long as at least one acquisition host is defined. When both are defined, then the destination can be used to automatically discover and retrieve data from one place and transmit it to another, with or without storing the data in the Data Store, depending on the destination configuration. This is a popular way of using OpenECPDS. For example, this mechanism is used for the delivery of some regional near-real-time ensemble air quality forecasts produced at ECMWF for the EU-funded Copernicus Atmospheric Monitoring Service implemented by the Centre.

There is also the concept of destination **aliases**, which makes it possible to link two or more destinations together, so that whatever data transfer is queued to one destination is also queued to the others. This mechanism enables processing the same set of data transfers to different sites with different schedules and/or transfer mechanisms defined on a destination basis. Conditional aliasing is also possible in order to alias only a subset of data transfers.

## Software components

OpenECPDS is a distributed application with four main software components:

- the **Master Server** implements the business logic of the application
- the **Monitor** implements the web interface for management and monitoring
- the **MariaDB Database** enables persistent storage for configurations, metadata and history
- **Data Movers** make it possible to store objects and to perform incoming and outgoing data transfers.

The key components are the Master Server, the Monitor, the MariaDB database and the Data Movers.
The MasterServer and MariaDB Database should both run in a highly resilient environment. The Data Movers currently run on physical servers deployed in their own secure environment. There are currently 37 Data Movers available for a total capacity of 1.2 petabytes.

In order to prevent downtime and data loss, which is a critical requirement of ECMWF’s Member and Co-operating States and is needed for the real-time running of the forecasting system at ECMWF, the following mechanisms have been implemented:

The ECPDS Master and MySQL Database are each replicated on three servers in an active/passive mode: one active instance handles requests and two passive instances are on standby. When the active instance fails or requires maintenance, one of the passive instances takes over and the service resumes as normal.
Each Data Mover group includes multiple servers. In order to guarantee the availability of the files in a group, a replication process is run to copy the files across the Data Movers (each file is replicated three times). If relevant, replication is also performed between ECMWF Data Movers and Data Movers located in the cloud on other continents.
A load balancer is configured to distribute incoming requests to the ECPDS Data Portal amongst the Data Movers. The use of multiple Data Movers and load balancing increases availability through redundancy.

ECMWF upgrades its forecasting system on a regular basis, and such upgrades are usually accompanied by a large increase in the volume of data to distribute. The way to scale up ECPDS in this situation is by adding more Data Movers. Each Data Mover adds CPU and I/O capability and disk space resources. More Data Movers can handle a bigger workload and more data.

## Support Materials

You can access the Javadoc API documentation for OpenECPDS at the following link: [Javadocs](https://ecmwf.github.io/open-ecpds/apidocs/). This comprehensive documentation provides detailed information about the classes, methods, and functionalities available, serving as a valuable resource for developers.

Additionally, you can find the OpenECPDS options for various editors at this link: [OpenECPDS Options](Options.md). This documentation outlines the configurable options available in the OpenECPDS editors, helping users to customize their experience and optimize their workflow effectively.

## Notes

![GraalVM](https://img.shields.io/badge/GraalVM-23.0.1-brightgreen)
![Maven](https://img.shields.io/badge/Maven-3.9.8-brightgreen)
![Docker](https://img.shields.io/badge/Docker-27.2.0-blue)
![Kompose](https://img.shields.io/badge/Kompose-1.34.0-blue)
![Kubectl](https://img.shields.io/badge/Kubectl-1.31.1-blue)

This project automates the downloading of specific tools (GraalVM, Maven, Docker, Kompose, Kubectl). Additionally, it uses external APIs that are downloaded via Maven. For licenses and details on these dependencies, please refer to their respective documentation. You can retrieve the licenses from the development container using:

```bash
mvn dependency:copy-dependencies
```
