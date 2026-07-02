# Installation

This page covers downloading the OpenECPDS distribution, creating the development
container, and building & configuring the application. Make sure you have met the
[System Requirements](requirements.md) first.

## Download the distribution

To download the latest distribution, run:

```bash
curl -L -o master.zip https://github.com/ecmwf/open-ecpds/archive/refs/heads/master.zip && unzip master.zip
```

A `Makefile` located in the `open-ecpds-master` directory is used to create the
development container that installs all the necessary tools for building the
application. The Java classes are compiled, packaged into RPM files, and used to build
Docker images for each OpenECPDS component.

## Create and log into the development container

The development container includes all tools for compiling source code, building RPM
files, creating container images, and deploying the application.

```bash
make dev
```

If successful, you should be logged into the development container.

## Build and configure OpenECPDS

Once inside the development container, compile the Java classes, package the RPM files,
and build the OpenECPDS Docker images:

```bash
make build
```

!!! warning
    In a production environment, `ENV` should be avoided in Dockerfiles for sensitive
    data like `MYSQL_ROOT_PASSWORD` for the Database, or `KEYSTORE_PASSWORD` for the
    Monitor and Mover. Docker secrets or environment variable files should be used
    instead.

Once the build process is complete, navigate to the directory where another `Makefile`
is available:

```bash
cd run/bin/ecpds
```

The services are started using **Docker Compose**. The `docker-compose.yml` file
contains all the necessary configurations to launch and manage the different components
of OpenECPDS. You can find this file in the appropriate directory for your OS:

- `run/bin/ecpds/Darwin-ecpds/docker-compose.yml` — macOS
- `run/bin/ecpds/Linux-ecpds/docker-compose.yml` — Linux and Windows

To verify the configuration and understand how Docker Compose interprets the settings
before running the services:

```bash
make config
```

For advanced configurations, you can fine-tune the options by modifying the default
values in the Compose file. Each parameter is documented within the file itself to
provide a better understanding of its function and how it impacts the system's
behaviour. By reviewing the Compose file, you can tailor the setup to your environment's
specific requirements.

## Next steps

Continue to [First Run](first-run.md) to start the services and access the interfaces.
For working inside an IDE, see [IDE Setup](ide-setup.md).
