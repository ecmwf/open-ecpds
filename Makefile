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
DOCKER_HOST_WORKSPACE := $(realpath .)
DOCKER_HOST_OS ?= $(shell uname -s)
DOCKER_GUEST_OS := $(shell uname -s)
IMAGE_NAME := node-$(PROJECT_NAME)-dev
CONTAINER_NAME := running-$(PROJECT_NAME)-dev
WORKDIR := /workspaces/$(PROJECT_NAME)

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

# Common function to check if inside or outside the development container
check-container-state = \
  if [ "$$IN_DEV_CONTAINER" = "$(1)" ]; then \
    printf "$(RED)Error: This target can only be run $(2) the development container$(RESET)\n"; \
    exit 1; \
  fi

# Conditional targets based on the environment
.PHONY: dev .dev-cntnr run login .rm-cntnr rm-dev get-geodb get-licenses build clean info
dev: .dev-cntnr .run login ## Build, run and login into the development container (*)

.dev-cntnr: ## Build the development container (*)
	@$(call check-container-state,true,outside)
	cd .devcontainer && $(DOCKER) build -f Dockerfile -t $(IMAGE_NAME) .

.run: ## Run the development container (*)
	@$(call check-container-state,true,outside)
	@$(DOCKER) run -d \
		-v /var/run/docker.sock:/var/run/docker.sock \
		-v $(WORKSPACE):/workspaces \
		-e DOCKER_HOST_WORKSPACE=$(DOCKER_HOST_WORKSPACE) \
		-e DOCKER_HOST_OS=$(DOCKER_HOST_OS) \
		--name $(CONTAINER_NAME) \
		$(IMAGE_NAME) \
		sleep infinity

login: ## Log in to the running development container (*)
	@$(call check-container-state,true,outside)
	@$(DOCKER) exec -it -w $(WORKDIR) $(CONTAINER_NAME) /bin/bash

.rm-cntnr: ## Stop and remove the development container (*)
	@$(call check-container-state,true,outside)
	@$(DOCKER) stop $(CONTAINER_NAME) || true
	@$(DOCKER) rm $(CONTAINER_NAME) || true

rm-dev: .rm-cntnr ## Stop the development container, then remove both its container and image. (*)
	@$(call check-container-state,true,outside)
	@$(DOCKER) rmi -f $(IMAGE_NAME) || exit 1

get-geodb: ## Fetch latest GeoLite2-City (MaxMind.com) CDN files
	@$(call check-container-state,"",inside)
	wget -qO- https://cdn.jsdelivr.net/npm/geolite2-city/GeoLite2-City.mmdb.gz | \
		gunzip -c > etc/master/conf/GeoLite2-City.mmdb

get-licenses: ## Fetch license information for all dependencies (**)
	@$(call check-container-state,"",inside)
	@mvn license:download-licenses

build: ## Compile java sources into JARs, create RPMs and Docker images (**)
	@$(call check-container-state,"",inside)
	@echo "$(TAG)" > VERSION
	@mvn package
	@cd docker && $(MAKE) all

clean: ## Stop containers, remove images, JARs, RPMs and dependencies (**)
	@$(call check-container-state,"",inside)
	@cd run/bin/ecpds && $(MAKE) -s down clean  || exit 1
	@cd docker && $(MAKE) -s rm-images  || exit 1
	@cd docker && $(MAKE) clean  || exit 1
	@mvn clean  || exit 1

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
