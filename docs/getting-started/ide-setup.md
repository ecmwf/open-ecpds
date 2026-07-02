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
