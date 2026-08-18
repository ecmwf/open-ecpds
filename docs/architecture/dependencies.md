# Java Dependencies

This page lists the key third-party Java libraries bundled with OpenECPDS, grouped by functional area. Only **direct** runtime dependencies are shown; transitive dependencies pulled in by these libraries are not listed.

## Runtime Platform

| Requirement | Version | Purpose |
|---|---|---|
| [OpenJDK](https://openjdk.org/) | 25+ | Java runtime — required to run all OpenECPDS components |

---

## Web & HTTP Server

| Library | Version | License | Purpose |
|---|---|---|---|
| [Eclipse Jetty](https://eclipse.dev/jetty/) | 12.1.x | Apache 2.0 | Embedded HTTP/HTTPS server (EE8 for Monitor UI, EE10 for REST API and Data Portal) |
| [Jakarta Servlet API (EE8)](https://jakarta.ee/) | 4.0.1 | EPL 2.0 | Servlet API for the Monitor UI and WebDAV handler |

---

## REST API

| Library | Version | License | Purpose |
|---|---|---|---|
| [Eclipse Jersey](https://eclipse-ee4j.github.io/jersey/) | 3.1.x | EPL 2.0 | JAX-RS implementation for the master REST API (`/ecpds/v1/`) and mover REST service |
| [Jakarta Annotation API](https://jakarta.ee/) | 2.1.1 | EPL 2.0 | Jakarta annotations used by Jersey |
| [Jackson Databind](https://github.com/FasterXML/jackson) | 2.18.x | Apache 2.0 | JSON serialisation / deserialisation for REST responses |

---

## Database & ORM

| Library | Version | License | Purpose |
|---|---|---|---|
| [Hibernate ORM](https://hibernate.org/orm/) | 7.0.x | LGPL 2.1 | JPA/ORM layer for all master server database access |
| [Hibernate JCache](https://hibernate.org/orm/) | 7.0.x | LGPL 2.1 | Second-level cache integration between Hibernate and EhCache |
| [Hibernate HikariCP](https://hibernate.org/orm/) | 7.0.x | LGPL 2.1 | HikariCP connection pool integration for Hibernate |
| [HikariCP](https://github.com/brettwooldridge/HikariCP) | 6.3.x | Apache 2.0 | High-performance JDBC connection pool |
| [MariaDB JDBC Client](https://mariadb.com/kb/en/mariadb-connector-j/) | 3.5.x | LGPL 2.1 | Primary JDBC driver for MariaDB databases |
| [MySQL Connector/J](https://dev.mysql.com/downloads/connector/j/) | 9.3.x | GPL 2.0 | JDBC driver for MySQL databases |

---

## Caching

| Library | Version | License | Purpose |
|---|---|---|---|
| [EhCache](https://www.ehcache.org/) | 3.10.x | Apache 2.0 | JCache (JSR-107) provider used as Hibernate's second-level cache |
| [JAXB API](https://javaee.github.io/jaxb-v2/) | 2.3.1 | CDDL 1.1 | `javax.xml.bind` API required by EhCache XML config parsing (removed from JDK 9+) |
| [JAXB Impl](https://eclipse-ee4j.github.io/jaxb-ri/) | 2.3.9 | CDDL 1.1 | JAXB 2.x runtime implementation required by EhCache |

---

## Security & Cryptography

| Library | Version | License | Purpose |
|---|---|---|---|
| [Bouncy Castle Provider](https://www.bouncycastle.org/) | 1.84+ | MIT | JCA/JCE cryptography provider (TLS, certificates, encryption) |
| [Bouncy Castle PKIX](https://www.bouncycastle.org/) | 1.84+ | MIT | X.509 certificate handling, PKIX path validation, CMS/SMIME |

---

## SSH & SFTP

| Library | Version | License | Purpose |
|---|---|---|---|
| [Apache MINA SSHD (SFTP)](https://mina.apache.org/sshd-project/) | 2.19.x | Apache 2.0 | Embedded SFTP server for the Data Portal |
| [Apache MINA SSHD (SCP)](https://mina.apache.org/sshd-project/) | 2.19.x | Apache 2.0 | SCP transfer module support |
| [JSch (mwiede fork)](https://github.com/mwiede/jsch) | 2.27.x | BSD | SSH/SFTP client used for outbound SFTP transfers to remote hosts |
| [EdDSA (ed25519)](https://github.com/str4d/ed25519-java) | 0.3.0 | CC0 | Ed25519 key support for SSH authentication |

---

## File Transfer Protocols

| Library | Version | License | Purpose |
|---|---|---|---|
| [Apache Commons Net](https://commons.apache.org/proper/commons-net/) | 3.11.x | Apache 2.0 | FTP/FTPS client for outbound FTP transfers |
| [ftp4j](http://www.sauronsoftware.it/projects/ftp4j/) | 1.7.2 | LGPL | Alternative FTP client library |
| [Jackrabbit WebDAV](https://jackrabbit.apache.org/jcr/components/jackrabbit-webdav-library.html) | 2.22.x | Apache 2.0 | WebDAV server (Data Portal) and client support |
| [LZ4 Java](https://github.com/lz4/lz4-java) | 1.8.1+ | Apache 2.0 | LZ4 fast compression for data transfer optimisation |

---

## Cloud Storage

| Library | Version | License | Purpose |
|---|---|---|---|
| [AWS SDK S3](https://aws.amazon.com/sdk-for-java/) | 2.46.x | Apache 2.0 | Amazon S3 transfer module and S3-compatible storage support |
| [AWS SDK STS](https://aws.amazon.com/sdk-for-java/) | 2.46.x | Apache 2.0 | AWS Security Token Service for temporary credential handling |
| [AWS SDK Apache Client](https://aws.amazon.com/sdk-for-java/) | 2.46.x | Apache 2.0 | Apache HTTP client backend for the AWS SDK |
| [Azure Core](https://github.com/Azure/azure-sdk-for-java) | 1.55.x | MIT | Azure SDK foundation |
| [Azure Core HTTP Netty](https://github.com/Azure/azure-sdk-for-java) | 1.15.x | MIT | Netty-based HTTP transport for the Azure SDK |
| [Azure Storage Blob](https://github.com/Azure/azure-sdk-for-java) | 12.30.x | MIT | Azure Blob Storage transfer module |
| [Azure Storage Common](https://github.com/Azure/azure-sdk-for-java) | 12.29.x | MIT | Shared Azure Storage primitives |
| [Azure Identity](https://github.com/Azure/azure-sdk-for-java) | 1.16.x | MIT | Azure managed identity and credential providers |
| [Google Cloud Storage](https://github.com/googleapis/java-storage) | (BOM) | Apache 2.0 | Google Cloud Storage transfer module |

---

## Messaging & MQTT

| Library | Version | License | Purpose |
|---|---|---|---|
| [HiveMQ Community Edition (Embedded)](https://github.com/hivemq/hivemq-community-edition) | 2025.3 | Apache 2.0 | Embedded MQTT broker for push notifications |
| [Eclipse Paho MQTT v5 Client](https://github.com/eclipse/paho.mqtt.java) | 1.2.5 | EPL 2.0 | MQTT v5 client for outbound notifications and subscriptions |

---

## Logging

| Library | Version | License | Purpose |
|---|---|---|---|
| [Apache Log4j 2 Core](https://logging.apache.org/log4j/2.x/) | 2.26.x | Apache 2.0 | Logging implementation |
| [Apache Log4j 2 API](https://logging.apache.org/log4j/2.x/) | 2.26.x | Apache 2.0 | Logging API |
| [Log4j SLF4J Bridge](https://logging.apache.org/log4j/2.x/) | 2.26.x | Apache 2.0 | Routes SLF4J log calls to Log4j 2 |
| [Log4j 1.2 API Bridge](https://logging.apache.org/log4j/2.x/) | 2.26.x | Apache 2.0 | Compatibility bridge for legacy Log4j 1.x calls |
| [LMAX Disruptor](https://github.com/LMAX-Exchange/disruptor) | 4.0.0 | Apache 2.0 | High-throughput async logging queue used by Log4j 2 |
| [Apache Sling Commons Threads](https://sling.apache.org/) | 3.3.0 | Apache 2.0 | Thread pool management used by the logging subsystem |

---

## Scripting & Polyglot

| Library | Version | License | Purpose |
|---|---|---|---|
| [GraalVM Polyglot](https://www.graalvm.org/) | 25.0.x | UPL 1.0 | Polyglot scripting host for user-defined JavaScript/Python scripts |
| [GraalVM JavaScript](https://www.graalvm.org/) | 25.0.x | UPL 1.0 | JavaScript engine (metadata transformation scripts, rule evaluation) |
| [GraalVM Python](https://www.graalvm.org/) | 25.0.x | UPL 1.0 | Python engine (user-defined data processing scripts) |

---

## Search & Indexing

| Library | Version | License | Purpose |
|---|---|---|---|
| [Apache Lucene Core](https://lucene.apache.org/) | 9.10.x | Apache 2.0 | Full-text search index for transfers and metadata |
| [Apache Lucene Query Parser](https://lucene.apache.org/) | 9.10.x | Apache 2.0 | Lucene query parsing |
| [Apache Lucene Analysis](https://lucene.apache.org/) | 9.10.x | Apache 2.0 | Text analysis and tokenisation for Lucene |

---

## AI / LLM Integration

| Library | Version | License | Purpose |
|---|---|---|---|
| [LangChain4j](https://github.com/langchain4j/langchain4j) | 0.35.x | Apache 2.0 | LLM integration framework |
| [LangChain4j Core](https://github.com/langchain4j/langchain4j) | 0.35.x | Apache 2.0 | LangChain4j core API |
| [LangChain4j Ollama](https://github.com/langchain4j/langchain4j) | 0.35.x | Apache 2.0 | Ollama local LLM backend integration |

---

## Monitor UI (Web Framework)

| Library | Version | License | Purpose |
|---|---|---|---|
| [Struts](https://struts.apache.org/) | 1.2.9 | Apache 2.0 | MVC web framework for the Monitor UI (JSP/Struts actions) |
| [Apache Taglibs Standard (Impl)](https://tomcat.apache.org/taglibs/) | 1.2.5 | Apache 2.0 | JSTL tag library implementation for JSP pages |
| [Apache Taglibs Standard (Spec)](https://tomcat.apache.org/taglibs/) | 1.2.5 | Apache 2.0 | JSTL specification API |

---

## Utilities

| Library | Version | License | Purpose |
|---|---|---|---|
| [Google Guava](https://github.com/google/guava) | (BOM) | Apache 2.0 | General-purpose utility library (collections, I/O, hashing) |
| [Jackson Databind (Codehaus, legacy)](https://github.com/codehaus/jackson) | 1.9.11 | Apache 2.0 | Legacy Jackson 1.x used by a small number of modules (S3, OpsView) |
| [Apache Commons IO](https://commons.apache.org/proper/commons-io/) | 2.20.x | Apache 2.0 | File and stream I/O utilities |
| [Apache Commons Lang](https://commons.apache.org/proper/commons-lang/) | 2.6 | Apache 2.0 | String and object utilities |
| [Apache Commons Compress](https://commons.apache.org/proper/commons-compress/) | 1.27.x | Apache 2.0 | Archive and compression formats (tar, gz, bz2, zstd) |
| [Apache Commons CSV](https://commons.apache.org/proper/commons-csv/) | 1.14.x | Apache 2.0 | CSV parsing and formatting |
| [Apache Commons Collections](https://commons.apache.org/proper/commons-collections/) | 3.2.2 | Apache 2.0 | Extended Java collections framework |
| [Apache Commons Net](https://commons.apache.org/proper/commons-net/) | 3.11.x | Apache 2.0 | FTP, SMTP and other network protocol clients |
| [Apache HttpClient 5](https://hc.apache.org/httpcomponents-client-5.3.x/) | 5.3.x | Apache 2.0 | HTTP client used for outbound REST/web service calls |
| [Jsoup](https://jsoup.org/) | 1.20.x | MIT | HTML parsing and sanitisation |
| [Java Diff Utils](https://github.com/java-diff-utils/java-diff-utils) | 4.12 | Apache 2.0 | Diff / patch computation for configuration change tracking |
| [MaxMind GeoIP2](https://github.com/maxmind/GeoIP2-java) | 4.2.x | Apache 2.0 | IP geolocation for country/region resolution |
| [JavaMail (Sun)](https://javaee.github.io/javamail/) | 1.6.2 | CDDL 1.1 | E-mail notification delivery |
| [JDOM](http://www.jdom.org/) | 1.1.3 | Apache-style | XML document processing |
| [Kotlin stdlib](https://kotlinlang.org/) | 1.9.x | Apache 2.0 | Kotlin standard library (transitive, pulled in by HiveMQ) |
