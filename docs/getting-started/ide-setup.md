# IDE Setup for OpenECPDS Development

OpenECPDS includes configuration files for both **Visual Studio Code** and **Eclipse**,
so you can choose the development environment you are most comfortable with. Simply
select your preferred IDE, and you will find ready-to-use settings tailored to
streamline your work with OpenECPDS.

## Prerequisite: start the database

Before following the guidelines to start OpenECPDS in your IDE, ensure that the required
Docker network has been created and that an instance of the OpenECPDS database is
running. This can be done either outside or inside the development container if it
already exists. To set it up, run:

```bash
make start-db
```

This command creates the Docker network for OpenECPDS and starts only the database
service, while the Master, Monitor, and Mover services can be launched directly from the
IDE.

To reset a disposable development database, stop all IDE-launched Masters first,
then run `make start-db` and confirm reinitialization. The target stops the
database container before deleting its data, and also detects an incomplete
previous initialization. Merely restarting a container does not rerun the
initialization SQL. Resetting deletes all development database data.

## Visual Studio Code

When working with OpenECPDS in Visual Studio Code, the **Dev Containers extension** is
required and will automatically detect the presence of a `.devcontainer` directory
within the project folder. This directory contains key configuration files, such as
`.devcontainer/devcontainer.json` and a `.devcontainer/Dockerfile`, which collectively
define the development environment for OpenECPDS.

Before opening the project, make sure to edit `.devcontainer/devcontainer.json` to
update the `DOCKER_HOST_OS` environment parameter according to your Docker host
operating system (the default is set to `Darwin` for macOS).

Once you open the OpenECPDS folder, VS Code will prompt you to reopen the project within
a container: **Reopen in Container**. If this option is selected, VS Code uses the
`.devcontainer/Dockerfile` to build the container image, adding necessary tools and
dependencies specified for OpenECPDS. The `.devcontainer/devcontainer.json` file
configures additional settings, such as environment variables and workspace mounting,
ensuring the container is fully tailored to the project. This setup provides a consistent
and fully-equipped development environment from the start.

!!! warning
    If the Docker network has not been created using the `make start-db` target in the
    project's home directory, creating the development container will fail.

For more information on working with development containers in Visual Studio Code, please
visit the [Visual Studio Code website](https://code.visualstudio.com/docs/devcontainers/containers).

### Run and Debug configurations

To access the Debug and Run configurations:

- Open the **Command Palette** by pressing ++ctrl+shift+p++ (Windows/Linux) or
  ++cmd+shift+p++ (Mac).
- Type **Run and Debug** in the **Command Palette** to find the **Run and Debug** view.
- Select **Run and Debug** in the sidebar or access it through the **Debug icon** in the
  Activity Bar on the left side of VS Code.

This view displays the available **Run and Debug** configurations:

- **OpenECPDS Master Server**
- **OpenECPDS Mover Server**
- **OpenECPDS Monitor Server**
- **OpenECPDS Stack** — to start them all at once.

To build the OpenECPDS Docker images, open a terminal in the development container by
selecting **Terminal → New Terminal** from the top menu, and follow the instructions in
[Installation](installation.md).

## Eclipse

To open the OpenECPDS project in Eclipse:

- Go to **File → Import...**
- In the import dialog, select **Existing Maven Projects** under the **Maven** category
  and click **Next**.
- Browse to the location of your OpenECPDS project folder on your system and select it.
  Eclipse will automatically detect the project files.
- Once the project is detected, click **Finish** to complete the import.

After dependency changes, select the root project and `ecpds-core`, then choose
**Maven → Update Project...** (++alt+f5++). The root project's automatically
activated `eclipse-m2e` profile supplies its IDE dependencies; `ecpds-core` uses
its own dependency list. If unresolved imports remain after the update, use
**Project → Clean...**. Do not add individual JARs manually to the build path.

To build the application from the source:

- Select the `pom.xml` file in the **Project Explorer** view.
- In the contextual menu, choose **Run As → OpenECPDS Clean Compile**.

The application can now be started within Eclipse using the preconfigured Debug and Run
options available: **OpenECPDS Master Server**, **OpenECPDS Mover Server**, and
**OpenECPDS Monitor Server**. These configurations are accessible under
**Run → Run Configurations...** and **Run → Debug Configurations...**

After completing these steps, the OpenECPDS project should be ready to work with in
Eclipse, with access to any required dependencies and configurations.

Eclipse does not natively support development containers, and this is not required for
simply running and debugging OpenECPDS within Eclipse. To build the OpenECPDS Docker
images, the development container can be manually created as outlined in
[Installation](installation.md).

### Local OpenTelemetry and journal testing

An optional test Collector accepts OTLP HTTP/protobuf on
`http://127.0.0.1:4318` and prints every decoded log record, trace and metric to
its container logs. It requires Docker/Compose or Podman/Compose, but no local
Collector installation or image build:

```sh
make start-otel
make logs-otel      # Follow captured telemetry; Ctrl-C stops following only.
make stop-otel      # Stop/remove the container and discard its captured logs.
```

The pinned Collector image and service are defined in the development Compose
files; its configuration is `run/etc/ecpds/otel/config.yaml`. It is opt-in and
is not started by `start-db`, `start-backend` or an ordinary Compose `up`.
The published port is bound to the Docker host's loopback interface. A process
inside another container cannot use its own `127.0.0.1` to reach it; on the
macOS development `ecpds-backbone` network, use `http://otel:4318` instead.

For Eclipse, start the Collector manually in a terminal, then use the ordinary
**OpenECPDS Master Server** Run/Debug configuration. This also avoids requiring
Docker on Eclipse's own `PATH`. The launch uses
`run/etc/ecpds/master/argfile`, which supplies the local endpoint:

```text
-Dotel.exporter.otlp.endpoint=http://127.0.0.1:4318
-Dotel.exporter.otlp.protocol=http/protobuf
```

All three signals inherit these settings and append their HTTP paths
(`/v1/logs`, `/v1/traces`, `/v1/metrics`). Remove or override any signal-specific
OTLP endpoint, protocol or authentication settings in your environment if they
would otherwise redirect telemetry or send unwanted credentials to the test
Collector.

The development overrides in `run/etc/ecpds/master/ecmwf.properties` enable
`openTelemetryEnabled=yes` and `journalEnabled=yes` in `[OperationalEvents]`.
Production configuration defaults remain unchanged. Traces and metrics remain
separately controlled by the normal Master settings.

Start the development database first and ensure it has the
`OPERATIONAL_EVENT` journal table. Start only one Master for the development
configuration; start the Monitor separately to inspect
**Admin Tasks → Operational Event Journal**.

Stopping the IDE session does not stop the Collector. Use `make stop-otel`
when finished; Ctrl-C in the log terminal only stops following its logs.

!!! warning
    This is a non-production diagnostic sink, not durable telemetry storage.
    It acknowledges records after printing them, allowing the Master to delete
    journal rows. Captured output contains operational data. Do not point a
    production Master or a production journal database at this test sink.

To exercise recovery, stop the Collector with `make stop-otel`, generate
normal operational events, and watch the journal's pending count grow.
Run `make start-otel` again and confirm that queued events appear in
`make logs-otel` and the queue drains. Collection and delivery can also be
paused independently in the Monitor. See
[Durable database journal](../event-logging/overview.md#durable-database-journal)
for event-generation gates and delivery limits.
