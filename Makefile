#
# Makefile for setting up and using a development container for the OpenECPDS application.
# This file provides commands to:
# - Create and log in to the development container.
# - When executed inside the development container, compile the Java sources, 
#   build JAR files, create RPM packages, and generate Docker images.
#
# (c) Copyright ECMWF 2019-2024 - Laurent Gougeon (syi@ecmwf.int)
#

# Use bash by default
SHELL=/bin/bash

# Define variables for paths and container names
PROJECT_NAME := $(shell basename $(PWD))
WORKSPACE := $(realpath ..)
DOCKER_HOST_WORKSPACE ?= $(realpath .)
DOCKER_HOST_OS ?= $(shell uname -s)
DOCKER_GUEST_OS := $(shell uname -s)
IMAGE_NAME := node-$(PROJECT_NAME)-dev
CONTAINER_NAME := running-$(PROJECT_NAME)-dev
WORKDIR := /workspaces/$(PROJECT_NAME)
DB_DATA_DIR := run/var/lib/ecpds/database

# Extract the tag number from the Maven file
VERSION=$(shell grep '<version>' pom.xml | head -n 1 | sed 's/.*>\(.*\)<.*/\1/')
BUILD=$(shell grep '<build.number>' pom.xml | head -n 1 | sed 's/.*>\(.*\)<.*/\1/')
TAG="$(VERSION)-$(BUILD)"

# Define color codes
GREEN := \033[32m
RED := \033[31m
RESET := \033[0m

# Detect container manager (Docker or Podman)
ifeq ($(shell command -v podman 2> /dev/null),)
  DOCKER=docker
  BUILD_OPTS=
else
  DOCKER=podman
  BUILD_OPTS=--format docker --cap-add all
endif

# Set the docker or podman version
DOCKER_VERSION = $(shell $(DOCKER) --version)

# Default target
.PHONY: help
help: ## Show this help message (*=outside **=inside the development container)
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "\033[36m%-12s\033[0m %s\n", $$1, $$2}' $(MAKEFILE_LIST)

# Check if inside or outside the development container
is-dev-container = \
  if [ "$$IN_DEV_CONTAINER" = "$(1)" ]; then \
    printf "$(RED)Error: This target can only be run $(2) the development container$(RESET)\n"; \
    exit 1; \
  fi

# Check if the development container exists and is running
check-dev-container = \
  @if [ -z "$(shell $(DOCKER) ps -a -q -f name=$(CONTAINER_NAME))" ]; then \
    printf "$(RED)Error: The development container '$(CONTAINER_NAME)' does not exist.$(RESET)\n"; \
    exit 1; \
  elif [ -z "$(shell $(DOCKER) ps -q -f name=$(CONTAINER_NAME))" ]; then \
    printf "$(RED)Error: The development container '$(CONTAINER_NAME)' is not running.$(RESET)\n"; \
    exit 1; \
  fi

# Check if the development container exists
dev-container-exists = \
  @if [ "$(shell $(DOCKER) ps -a -q -f name=$(CONTAINER_NAME))" ]; then \
      printf "$(RED)Error: The development container '$(CONTAINER_NAME)' already exists.$(RESET)\n"; \
      exit 1; \
  fi

# Conditional targets based on the environment
.PHONY: dev .dev-cntnr .run login rm-dev get-geodb get-licenses build clean info
dev: .dev-cntnr .run login ## Build, run and login into the development container (*)

.dev-cntnr: ## Build the development container (*)
	@$(call is-dev-container,true,outside)
	@$(call dev-container-exists)
	cd .devcontainer && $(DOCKER) build -f Dockerfile -t $(IMAGE_NAME) .

.run: ## Run the development container (*)
	@$(call is-dev-container,true,outside)
	@$(DOCKER) run -d \
		-v /var/run/docker.sock:/var/run/docker.sock \
		-v $(HOME)/.kube:/root/.kube \
		-v $(WORKSPACE):/workspaces \
		-e DOCKER_HOST_WORKSPACE=$(DOCKER_HOST_WORKSPACE) \
		-e DOCKER_HOST_OS=$(DOCKER_HOST_OS) \
		--name $(CONTAINER_NAME) \
		$(IMAGE_NAME) \
		sleep infinity

login: ## Log in to the running development container (*) with GitHub Copilot token
	@$(call is-dev-container,true,outside)
	@$(call check-dev-container)
	@[ -n "$$GH_TOKEN" ] && TOKEN="$$GH_TOKEN" || TOKEN="$$GITHUB_TOKEN"; \
	if [ -n "$$TOKEN" ]; then \
		$(DOCKER) exec -it -w $(WORKDIR) $(CONTAINER_NAME) env GH_TOKEN=$$TOKEN /bin/bash; \
	else \
		$(DOCKER) exec -it -w $(WORKDIR) $(CONTAINER_NAME) /bin/bash; \
	fi

rm-dev: ## Stop the development container, then remove both its container and image. (*)
	@$(call is-dev-container,true,outside)
	@$(call check-dev-container)
	@$(DOCKER) stop $(CONTAINER_NAME) || true
	@$(DOCKER) rm $(CONTAINER_NAME) || true
	@$(DOCKER) rmi -f $(IMAGE_NAME) || exit 1

get-geodb: ## Fetch latest GeoLite2-City (MaxMind.com) CDN files
	@$(call is-dev-container,"",inside)
	wget -qO- https://cdn.jsdelivr.net/npm/geolite2-city/GeoLite2-City.mmdb.gz | \
		gunzip -c > etc/master/conf/GeoLite2-City.mmdb

get-licenses: ## Fetch license information for all dependencies (**)
	@$(call is-dev-container,"",inside)
	@mvn license:download-licenses

build: ## Compile java sources into JARs, create RPMs and Docker images (**)
	@$(call is-dev-container,"",inside)
	@echo -n "$(TAG)" > VERSION
	@mvn package
	@cd docker && $(MAKE) all

start-db: ## Build and run the database for VS Code and Eclipse debugging/running
	@if [ -d $(DB_DATA_DIR)/mysql ] && [ -d $(DB_DATA_DIR)/ecpds ]; then \
		echo "WARNING: A database already exists in '$(DB_DATA_DIR)'."; \
		echo "If you keep it, the SQL initial script will NOT be executed."; \
		read -p "Delete existing database data and reinitialize? (y/N) " answer; \
		if [ "$$answer" = "y" ] || [ "$$answer" = "Y" ]; then \
			echo "Deleting existing database data..."; \
			rm -rf $(DB_DATA_DIR)/*; \
	else \
		echo "Keeping existing database data..."; \
		fi \
	fi
	@cd docker && $(MAKE) build-db
	@cd run/bin/ecpds && $(MAKE) up container=database

stop-db: ## Stop the database
	@cd run/bin/ecpds && $(MAKE) down

clean: ## Stop containers, remove images, JARs, RPMs and dependencies (**)
	@$(call is-dev-container,"",inside)
	@cd run/bin/ecpds && $(MAKE) -s down clean  || exit 1
	@cd docker && $(MAKE) -s rm-images  || exit 1
	@cd docker && $(MAKE) clean  || exit 1
	@mvn clean  || exit 1
	@rm -f lib/*.jar lib/*.pom || exit 1

info: ## Output the configuration
	@if [ -n "$(IN_DEV_CONTAINER)" ]; then \
		printf "$(GREEN)** You are INSIDE the development container **$(RESET)\n"; \
	else \
		printf "$(RED)** You are OUTSIDE the development container **$(RESET)\n"; \
	fi
	@echo "Software tag number: $(TAG)"
	@echo "Docker host/current OS: $(DOCKER_HOST_OS)/$(DOCKER_GUEST_OS)"
	@echo "Docker command: $(DOCKER) ($(DOCKER_VERSION))"
	@if [ -n "$(BUILD_OPTS)" ]; then \
		echo "Docker options: $(BUILD_OPTS)"; \
	fi
	@echo "Workspace directory: $(DOCKER_HOST_WORKSPACE)"
	@echo "Image name: $(IMAGE_NAME)"
	@echo "Container name: $(CONTAINER_NAME)"
