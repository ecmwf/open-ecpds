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
VERSION=$(shell grep '<revision>' ../../../pom.xml | head -n 1 | sed 's/.*>\(.*\)<.*/\1/')
BUILD=$(shell grep '<build.number>' ../../../pom.xml | head -n 1 | sed 's/.*>\(.*\)<.*/\1/')

# Set the TAG (export to be available in the compose file)
export TAG=$(VERSION)-$(BUILD)

# Define colour codes
CYAN   := \033[36m
YELLOW := \033[33m
RED    := \033[31m
RESET  := \033[0m

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

# Display help by default
.DEFAULT_GOAL := help

.PHONY: help up ps logs connect config down prune mariadb dump-full dump-conf clean info

# ─── Help ─────────────────────────────────────────────────────────────────────

help: ## Show this help message
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / \
	    {printf "$(CYAN)%-16s$(RESET) %s\n", $$1, $$2}' $(MAKEFILE_LIST)
	@printf "\n"

# ─── Helpers ──────────────────────────────────────────────────────────────────

# Abort with an error if the docker-compose.yml file is missing for this OS
check-yaml:
	@if [ ! -f "$(DOCKER_HOST_OS)-ecpds/docker-compose.yml" ]; then \
		printf "$(RED)Error: No docker-compose.yml found for OS '$(DOCKER_HOST_OS)'$(RESET)\n"; \
		exit 1; \
	fi

# Warn (and optionally abort) if active master connections exist before a clean
check-db:
	@running=$$($(COMPOSE) -f $(DOCKER_HOST_OS)-ecpds/docker-compose.yml ps -q database); \
	if [ -z "$$running" ]; then \
		printf "$(YELLOW)Database container is not running — skipping connection check.$(RESET)\n"; \
		exit 0; \
	fi; \
	printf "Database container is running. Checking active 'master' connections...\n"; \
	active_count=$$(echo "SELECT COUNT(*) FROM INFORMATION_SCHEMA.PROCESSLIST WHERE USER='master';" | \
		$(MAKE) mariadb-exec 2>/dev/null); \
	if [ "$$active_count" -gt 0 ]; then \
		printf "$(YELLOW)WARNING: There are $$active_count active connection(s) from user 'master'!$(RESET)\n"; \
		read -p "Are you sure you want to clean the database? (y/N) " answer; \
		if [ "$$answer" != "y" ] && [ "$$answer" != "Y" ]; then \
			printf "$(RED)Aborting clean.$(RESET)\n"; \
			exit 1; \
		fi; \
	else \
		printf "No active connections from user 'master'.\n"; \
	fi

mariadb-exec:
	@$(COMPOSE) -f $(DOCKER_HOST_OS)-ecpds/docker-compose.yml exec -T \
		database sh -c 'env MYSQL_PWD="$$MYSQL_ROOT_PASSWORD" mariadb -N -s ecpds' < /dev/stdin

# ─── Containers ───────────────────────────────────────────────────────────────

up: check-yaml ## Create and start containers (arg: svc, default: all)
	@$(COMPOSE) -f ${DOCKER_HOST_OS}-ecpds/docker-compose.yml up -d $(svc)

ps: check-yaml ## List running containers
	@$(COMPOSE) -f ${DOCKER_HOST_OS}-ecpds/docker-compose.yml ps

logs: check-yaml ## View output from a container (arg: svc)
	@TERM=dumb NO_COLOR=1 $(COMPOSE) -f ${DOCKER_HOST_OS}-ecpds/docker-compose.yml logs --no-color --no-log-prefix $(svc)

connect: check-yaml ## Open a bash session in a running container (arg: svc)
	@$(COMPOSE) -f ${DOCKER_HOST_OS}-ecpds/docker-compose.yml exec $(svc) bash

config: check-yaml ## Validate and view the Compose file
	@$(COMPOSE) -f ${DOCKER_HOST_OS}-ecpds/docker-compose.yml config

down: check-yaml ## Stop and remove containers, networks, images, and volumes (arg: svc, default: all)
	@$(COMPOSE) -f ${DOCKER_HOST_OS}-ecpds/docker-compose.yml down $(svc)

prune: ## Remove unused data (stopped containers, unused networks, and volumes)
	@$(DOCKER) system prune -f
	@$(DOCKER) volume prune -f

# ─── Database ─────────────────────────────────────────────────────────────────

mariadb: check-yaml ## Connect to the database
	@$(COMPOSE) -f ${DOCKER_HOST_OS}-ecpds/docker-compose.yml exec database sh -c 'mariadb ecpds -p"$$MYSQL_ROOT_PASSWORD"'

dump-full: check-yaml ## Perform a full dump of the database to stdout
	@$(COMPOSE) -f ${DOCKER_HOST_OS}-ecpds/docker-compose.yml exec database sh -c 'mariadb-dump -A -p"$$MYSQL_ROOT_PASSWORD"'

dump-conf: check-yaml ## Dump only configuration data from the database to stdout
	@$(COMPOSE) -f ${DOCKER_HOST_OS}-ecpds/docker-compose.yml exec database sh -c \
		'mariadb-dump -p"$$MYSQL_ROOT_PASSWORD" ecpds \
		--ignore-table=ecpds.ACTIVITY \
		--ignore-table=ecpds.BANDWIDTH \
		--ignore-table=ecpds.DATA_FILE \
		--ignore-table=ecpds.DATA_TRANSFER \
		--ignore-table=ecpds.EVENT \
		--ignore-table=ecpds.INCOMING_HISTORY \
		--ignore-table=ecpds.METADATA_ATTRIBUTE \
		--ignore-table=ecpds.METADATA_VALUE \
		--ignore-table=ecpds.MONITORING_VALUE \
		--ignore-table=ecpds.PRODUCT_STATUS \
		--ignore-table=ecpds.PUBLICATION \
		--ignore-table=ecpds.TRANSFER_HISTORY \
		--ignore-table=ecpds.UPLOAD_HISTORY'
	@$(COMPOSE) -f ${DOCKER_HOST_OS}-ecpds/docker-compose.yml exec database sh -c \
		'mariadb-dump -p"$$MYSQL_ROOT_PASSWORD" -d ecpds \
		ACTIVITY BANDWIDTH DATA_FILE DATA_TRANSFER EVENT INCOMING_HISTORY \
		METADATA_ATTRIBUTE METADATA_VALUE MONITORING_VALUE PRODUCT_STATUS \
		PUBLICATION TRANSFER_HISTORY UPLOAD_HISTORY CHANGE_LOG'

# ─── Maintenance ──────────────────────────────────────────────────────────────

clean: check-db down .clean-log .clean-lib .clean-tmp .fix-perms ## Remove all logs, persistent data, and fix database permissions

.clean-log: # Remove all logs (master, monitor and mover)
	cd ${LOCAL_ROOT_PATH}/var/log/ecpds ; rm -f master/* monitor/* mover/*

.clean-lib: # Reset all persistent data (database, data files, output)
	cd ${LOCAL_ROOT_PATH}/var/lib/ecpds ; rm -fr database/* master/*/* mover/volume*/*

.clean-tmp: # Remove all temporary files
	cd ${LOCAL_ROOT_PATH}/var/tmp/ecpds ; rm -fr master/* monitor/* mover/*

.fix-perms: # Fix permissions for the database directories
	cd ${LOCAL_ROOT_PATH}/var/lib/ecpds ; chown -R $(shell id -u):$(shell id -g) database/ ; chmod -R 755 database/
	cd ${LOCAL_ROOT_PATH}/etc/ecpds ; chmod -R 755 database/*

# ─── Info ─────────────────────────────────────────────────────────────────────

info: ## Output the configuration
	@printf "\n"
	@printf "$(CYAN)── Project ──────────────────────────────────$(RESET)\n"
	@printf "  %-24s %s\n" "Version:"         "$(TAG)"
	@printf "  %-24s %s\n" "Root path:"       "$(LOCAL_ROOT_PATH)"
	@printf "  %-24s %s\n" "Repository:"      "$(ECPDS_REPOSITORY)"
	@printf "\n"
	@printf "$(CYAN)── Docker / container engine ────────────────$(RESET)\n"
	@printf "  %-24s %s\n" "Command:"         "$(DOCKER)"
	@printf "  %-24s %s\n" "Version:"         "$(DOCKER_VERSION)"
	@printf "  %-24s %s\n" "Host OS:"         "$(DOCKER_HOST_OS)"
	@printf "  %-24s %s\n" "Guest OS:"        "$(DOCKER_GUEST_OS)"
	@printf "  %-24s %s\n" "Compose file:"    "$(DOCKER_HOST_OS)-ecpds/docker-compose.yml"
	@printf "\n"
	@printf "$(CYAN)── Container status ─────────────────────────$(RESET)\n"
	@if [ ! -f "$(DOCKER_HOST_OS)-ecpds/docker-compose.yml" ]; then \
		printf "  $(YELLOW)No docker-compose.yml found for OS '$(DOCKER_HOST_OS)'$(RESET)\n"; \
	else \
		for svc in $$($(COMPOSE) -f $(DOCKER_HOST_OS)-ecpds/docker-compose.yml config --services 2>/dev/null); do \
			if [ -n "$$($(COMPOSE) -f $(DOCKER_HOST_OS)-ecpds/docker-compose.yml ps -q $$svc 2>/dev/null)" ]; then \
				printf "  %-24s $(CYAN)%s$(RESET)\n" "$$svc:" "running"; \
			else \
				printf "  %-24s %s\n" "$$svc:" "stopped"; \
			fi; \
		done; \
	fi
	@printf "\n"
