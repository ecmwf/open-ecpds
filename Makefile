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

# Extract the tag number from the Java class
VERSION_CLASS="src/ecmwf/common/version/Version.java"
VERSION=$(shell grep 'String VERSION =' $(VERSION_CLASS) | sed -e 's/.*VERSION = "\(.*\)";/\1/')
BUILD=$(shell grep 'String BUILD =' $(VERSION_CLASS) | sed -e 's/.*BUILD = "\(.*\)";/\1/')
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
help: ## Show this help message (*=outside **=inside the dev-container)
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "\033[36m%-12s\033[0m %s\n", $$1, $$2}' $(MAKEFILE_LIST)

# Common function to check if inside or outside the dev-container
check-container-state = \
  if [ "$$IN_DEV_CONTAINER" = "$(1)" ]; then \
    printf "$(RED)Error: This target can only be run $(2) the dev-container$(RESET)\n"; \
    exit 1; \
  fi

# Conditional targets based on the environment
.PHONY: all build run login rm-cntnr rm-image inst-libs make clean info
all: build run login ## Build, run and login into the dev-container (*)

build: ## Build the dev-container (*)
	@$(call check-container-state,true,outside)
	cd .devcontainer && $(DOCKER) build -f Dockerfile -t $(IMAGE_NAME) .

run: ## Run the dev-container (*)
	@$(call check-container-state,true,outside)
	@$(DOCKER) run -d \
		-v /var/run/docker.sock:/var/run/docker.sock \
		-v $(WORKSPACE):/workspaces \
		-e DOCKER_HOST_WORKSPACE=$(DOCKER_HOST_WORKSPACE) \
		-e DOCKER_HOST_OS=$(DOCKER_HOST_OS) \
		--name $(CONTAINER_NAME) \
		$(IMAGE_NAME) \
		sleep infinity

login: ## Login into the running dev-container (*)
	@$(call check-container-state,true,outside)
	@$(DOCKER) exec -it -w $(WORKDIR) $(CONTAINER_NAME) /bin/bash

rm-cntnr: ## Stop and remove the dev-container (*)
	@$(call check-container-state,true,outside)
	@$(DOCKER) stop $(CONTAINER_NAME) || true
	@$(DOCKER) rm $(CONTAINER_NAME) || true

rm-image: ## Remove the dev-image (*)
	@$(call check-container-state,true,outside)
	@$(DOCKER) rmi $(IMAGE_NAME) || exit 1

inst-libs: ## Install jars in libs into the local Maven repository (**)
	@$(call check-container-state,"",inside)
	@mvn install:install-file -Dfile=libs/ecaccess.jar -DgroupId=ecaccess -DartifactId=ecaccess -Dversion=1.0.0 -Dpackaging=jar
	@mvn install:install-file -Dfile=libs/ecmwf-webgrp.jar -DgroupId=ecaccess -DartifactId=ecmwf-webgrp -Dversion=1.0.0 -Dpackaging=jar
	@mvn install:install-file -Dfile=libs/jmxtools.jar -DgroupId=ecaccess -DartifactId=jmxtools -Dversion=1.0.0 -Dpackaging=jar
	@mvn install:install-file -Dfile=libs/ftp4j-1.7.2.jar -DgroupId=it.sauronsoftware -DartifactId=ftp4j -Dversion=1.7.2 -Dpackaging=jar
	@mvn install:install-file -Dfile=libs/jackson-all-1.9.11.jar -DgroupId=org.codehaus.jackson -DartifactId=jackson-all -Dversion=1.9.11 -Dpackaging=jar

make: ## Compile java sources into JARs, create RPMs and Docker images (**)
	@$(call check-container-state,"",inside)
	@mvn package
	@cd docker && $(MAKE) all

clean: ## Stop containers, remove images, JARs, RPMs and dependencies (**)
	@$(call check-container-state,"",inside)
	@cd run/bin/ecpds && $(MAKE) -s down clean  || exit 1
	@cd docker && $(MAKE) -s rm-images  || exit 1
	@cd docker && $(MAKE) clean  || exit 1
	@cd rpmbuild && $(MAKE) clean  || exit 1
	@cd ant && ant clean  || exit 1
	@cd rpmbuild/SOURCES/ecpds && rm -f \
		master/lib/ext/* monitor/lib/ext/* mover/lib/ext/*  || exit 1

info: ## Output the configuration
	@if [ -n "$(IN_DEV_CONTAINER)" ]; then \
		printf "$(GREEN)** You are INSIDE the dev-container **$(RESET)\n"; \
	else \
		printf "$(RED)** You are OUTSIDE the dev-container **$(RESET)\n"; \
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
