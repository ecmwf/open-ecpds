#
# Makefile for managing Docker containers using Docker Compose for the OpenECPDS application.
# This file provides commands to:
# - Start Docker containers using specified Docker Compose files.
# - Log in to the running containers for interactive use.
# - Monitor the logs and status of the containers.
# - Initiate a backup of the database.
#
# (c) Copyright ECMWF 2019-2024 - Laurent Gougeon (syi@ecmwf.int)
#

# Use bash by default
SHELL=/bin/bash

# Extract the version and build number from the Maven file
VERSION=$(shell grep '<version>' ../../../pom.xml | head -n 1 | sed 's/.*>\(.*\)<.*/\1/')
BUILD=$(shell grep '<build.number>' ../../../pom.xml | head -n 1 | sed 's/.*>\(.*\)<.*/\1/')

# Set the TAG (export to be available in the compose file)
export TAG=$(VERSION)-$(BUILD)

# Detect container manager (Docker or Podman)
ifeq ($(shell command -v podman 2> /dev/null),)
  DOCKER=docker
  # Is compose part of Docker?
  ifeq ($(shell docker help compose 2> /dev/null),)
    COMPOSE=docker-compose
  else
    COMPOSE=docker compose
  endif
else
  DOCKER=podman
  COMPOSE=podman-compose
endif

# Set the docker or podman version
DOCKER_VERSION = $(shell $(DOCKER) --version)

# Set the run path to be used inside the Makefile
LOCAL_ROOT_PATH = $(realpath ../..)

# Set the run path (export to be available in the compose file)
ifndef DOCKER_HOST_WORKSPACE
	# Not inside the development container
	export ECPDS_ROOT_PATH = $(LOCAL_ROOT_PATH)
else
	# Inside the development container (let's use the guest host path)
	export ECPDS_ROOT_PATH = $(DOCKER_HOST_WORKSPACE)/run
endif

# Set the repository (export to be available in the compose file)
export ECPDS_REPOSITORY ?= ecpds

# Detect which YAML file to use depending on the OS (unless already defined)
ifeq ($(OS),Windows_NT)
	DOCKER_GUEST_OS := Windows
	DOCKER_HOST_OS ?= Windows
else
	DOCKER_GUEST_OS := $(shell uname -s)
	ifeq ($(DOCKER_GUEST_OS),Linux)
		DOCKER_HOST_OS ?= Linux
	endif
	ifeq ($(DOCKER_GUEST_OS),Darwin)
		DOCKER_HOST_OS ?= Darwin
	endif
endif

# Check if the docker-compose.yml file exists
check-yaml:
	@if [ ! -f "$(DOCKER_HOST_OS)-ecpds/docker-compose.yml" ]; then \
		echo "No docker-compose.yml file found for $(DOCKER_HOST_OS)" && exit 1; \
	fi

# Output the help for each task
.PHONY: help

# Display help by default
.DEFAULT_GOAL := help

help: ## Show this help message
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "\033[36m%-12s\033[0m %s\n", $$1, $$2}' $(MAKEFILE_LIST)

up: check-yaml ## Create and start containers (arg: container, default: all)
	@$(COMPOSE) -f ${DOCKER_HOST_OS}-ecpds/docker-compose.yml up -d $(container)

down: check-yaml ## Stop and remove containers, networks, images, and volumes
	@$(COMPOSE) -f ${DOCKER_HOST_OS}-ecpds/docker-compose.yml down

config: check-yaml ## Validate and view the Compose file
	@$(COMPOSE) -f ${DOCKER_HOST_OS}-ecpds/docker-compose.yml config

connect: check-yaml ## Open a bash session in a running container (arg: container)
	@$(COMPOSE) -f ${DOCKER_HOST_OS}-ecpds/docker-compose.yml exec $(container) bash

mysql: check-yaml ## Connect to the database
	@$(COMPOSE) -f ${DOCKER_HOST_OS}-ecpds/docker-compose.yml exec database sh -c 'mysql ecpds -p"$$MYSQL_ROOT_PASSWORD"'

dump-full: check-yaml ## Perform a full dump of the database to stdout
	@$(COMPOSE) -f ${DOCKER_HOST_OS}-ecpds/docker-compose.yml exec database sh -c 'mysqldump -A -p"$$MYSQL_ROOT_PASSWORD"'

dump-conf: check-yaml ## Dump only configuration data from the database to stdout
	@$(COMPOSE) -f ${DOCKER_HOST_OS}-ecpds/docker-compose.yml exec database sh -c 'mysqldump -p"$$MYSQL_ROOT_PASSWORD" ecpds --ignore-table=ecpds.ACTIVITY --ignore-table=ecpds.BANDWIDTH --ignore-table=ecpds.DATA_FILE --ignore-table=ecpds.DATA_TRANSFER --ignore-table=ecpds.DISK_USAGE --ignore-table=ecpds.EVENT --ignore-table=ecpds.INCOMING_HISTORY --ignore-table=ecpds.METADATA_ATTRIBUTE --ignore-table=ecpds.METADATA_VALUE --ignore-table=ecpds.MONITORING_HISTORY --ignore-table=ecpds.MONITORING_VALUE --ignore-table=ecpds.PRODUCT_STATUS --ignore-table=ecpds.PUBLICATION --ignore-table=ecpds.Q2DISS_REPORT --ignore-table=ecpds.STATISTICS --ignore-table=ecpds.TRANSFER_HISTORY --ignore-table=ecpds.UPLOAD_HISTORY'
	@$(COMPOSE) -f ${DOCKER_HOST_OS}-ecpds/docker-compose.yml exec database sh -c 'mysqldump -p"$$MYSQL_ROOT_PASSWORD" -d ecpds ACTIVITY BANDWIDTH DATA_FILE DATA_TRANSFER DISK_USAGE EVENT INCOMING_HISTORY METADATA_ATTRIBUTE METADATA_VALUE MONITORING_HISTORY MONITORING_VALUE PRODUCT_STATUS PUBLICATION Q2DISS_REPORT STATISTICS TRANSFER_HISTORY UPLOAD_HISTORY CHANGE_LOG'

clean: down .clean-log .clean-lib .clean-tmp .clean-kml .fix-perms ## Remove all logs, persistent data, and fix database permissions

.clean-log: # Remove all logs (master, monitor and mover)
	cd ${LOCAL_ROOT_PATH}/var/log/ecpds ; rm -f master/* monitor/* mover/*

.clean-lib: # Reset all persistent data (database, data files, output)
	cd ${LOCAL_ROOT_PATH}/var/lib/ecpds ; rm -fr database/* master/*/* mover/volume*/*

.clean-tmp: # Remove all temporary files
	cd ${LOCAL_ROOT_PATH}/var/tmp/ecpds ; rm -fr master/* monitor/* mover/*

.clean-kml: # Remove all kml files
	cd ${LOCAL_ROOT_PATH}/../etc/monitor/htdocs/resources/maps ; rm -f *.kml

.fix-perms: # Fix permissions for the database directories
	cd ${LOCAL_ROOT_PATH}/var/lib/ecpds ; chown -R $(shell id -u):$(shell id -g) database/ ; chmod -R 755 database/
	cd ${LOCAL_ROOT_PATH}/etc/ecpds ; chmod -R 755 database/*

logs: check-yaml ## View output from a container (arg: container)
	@$(COMPOSE) -f ${DOCKER_HOST_OS}-ecpds/docker-compose.yml logs $(container)

ps: check-yaml ## List running containers
	@$(COMPOSE) -f ${DOCKER_HOST_OS}-ecpds/docker-compose.yml ps

info: ## Output the configuration
	@echo "Software tag number: $(TAG)"
	@echo "Docker host/current OS: $(DOCKER_HOST_OS)/$(DOCKER_GUEST_OS)"
	@echo "Docker command: $(DOCKER) ($(DOCKER_VERSION))"
	@if [ -n "$(BUILD_OPTS)" ]; then \
		echo "Docker options: $(BUILD_OPTS)"; \
	fi
