#
# Makefile for building Docker images for the OpenECPDS application.
# This file provides commands to streamline the process of creating
# and managing Docker images for deployment and testing.
#
# (c) Copyright ECMWF 2019-2024 - Laurent Gougeon (syi@ecmwf.int)
#

# Use bash by default
SHELL=/bin/bash

# Gets repository credentials for login/push targets (CR_URL, CR_UID & CR_PWD)
-include ../.settings/.cr-credentials

# Extract the tag number from the Maven file
VERSION=$(shell grep '<version>' ../pom.xml | head -n 1 | sed 's/.*>\(.*\)<.*/\1/')
BUILD=$(shell grep '<build.number>' ../pom.xml | head -n 1 | sed 's/.*>\(.*\)<.*/\1/')
TAG="$(VERSION)-$(BUILD)"

# Define color codes
GREEN := \033[32m
RED := \033[31m
RESET := \033[0m

# The docker host and current OS
DOCKER_HOST_OS ?= $(shell uname -s)
DOCKER_GUEST_OS := $(shell uname -s)

# How to get the rpms?
COPY := cp ../target/rpm

# Detect container manager (Docker or Podman)
ifeq ($(shell command -v podman 2> /dev/null),)
  DOCKER=docker
  BUILD_OPTS=
else
  DOCKER=podman
  BUILD_OPTS=--format docker --cap-add all
endif

# Set the docker or podman version
DOCKER_VERSION := $(shell $(DOCKER) --version)

# Check HTTPS access (required to build images)
HTTPS_ACCESS := $(shell curl -s --max-time 2 -I https://google.com | sed 's/^[^ ]*  *\([0-9]\).*/\1/; 1q')

# Output the help for each task
.PHONY: help all get-rpms build images bk-images rm-images clean .clean info

help: ## Show this help message
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "\033[36m%-15s\033[0m %s\n", $$1, $$2}' $(MAKEFILE_LIST)

# Display help by default
.DEFAULT_GOAL := help

all: ## Get RPMs, and build images
	@$(MAKE) get-rpms
	@$(MAKE) build

.https-access: ## Check HTTPS access (required to build images)
	@if [ "$(HTTPS_ACCESS)" != "2" -a "$(HTTPS_ACCESS)" != "3" ]; then \
		printf "$(RED)** HTTPS connectivity is down **$(RESET)\n" && exit 1; \
	fi

get-rpms: ## Get RPMs from the RPMS directory in rpmbuild
	@$(MAKE) .get-rpm app=mover rpm=mover arch=noarch
	@$(MAKE) .get-rpm app=master rpm=master arch=noarch
	@$(MAKE) .get-rpm app=monitor rpm=monitor arch=noarch

.get-rpm: # Create the target directory and get the specified RPM (args: app, rpm, arch)
	mkdir -p ecpds/$(app)/rpms && $(COPY)/ecpds-$(rpm)/RPMS/$(arch)/ecpds-$(rpm)-$(TAG).$(arch).rpm ecpds/$(app)/rpms/.

build: .https-access ## Build database, Java, and service images
	@$(MAKE) .build app=database tag=$(TAG)
	@$(MAKE) .build app=java tag=graalvm
	@$(MAKE) .build app=mover tag=$(TAG)
	@$(MAKE) .build app=master tag=$(TAG)
	@$(MAKE) .build app=monitor tag=$(TAG)

.build: # Build the image (args: app, tag)
	$(DOCKER) build $(BUILD_OPTS) --build-arg SDKMAN_JDK=$(SDKMAN_JDK) --build-arg TAG=$(tag) --label "app=$(app)" --label "tag=$(tag)" -t ecpds/$(app):$(tag) ecpds/$(app)

images: ## List images
	@$(DOCKER) images --format "{{.ID}}@{{.Repository}}:{{.Tag}}" | grep 'ecpds/$(app)' | grep ':$(tag)' | awk -F'[@/]' '{print $$1 "@" $$(NF-1)":"$$NF}' | sed 's/:/-/g' | egrep -v '\-latest$$' | uniq

bk-images: ## Backup images (args: app, tag)
	@for line in $$($(MAKE) -s images tag=$(tag) app=$(app)); do \
		id=$$(echo $$line | cut -d'@' -f1); \
		name=$$(echo $$line | cut -d'@' -f2); \
		echo "Backing up image $$id with name $$name ..."; \
		$(DOCKER) save $$id | gzip > $$name.tar.gz; \
	done

rm-images: ## Remove images (args: app, tag)
	@for line in $$($(MAKE) -s images tag=$(tag) app=$(app)); do \
		id=$$(echo $$line | cut -d'@' -f1); \
		echo "Removing image $$id ..."; \
		$(DOCKER) rmi -f $$id; \
	done

.checkcr: ## Check if the credentials are configured
	@if [ "$(CR_URL)" == "" -o "$(CR_UID)" == "" -o "$(CR_PWD)" == "" ]; then \
		printf "$(RED)** No CR credentials found (CR_URL, CR_UID & CR_PWD) **$(RESET)\n" && exit 1; \
	fi

login: .checkcr ## Log in to the CR registry (according to '../.settings/.cr-credential')
	@echo -n "$(CR_PWD)" | $(DOCKER) login -u '$(CR_UID)' --password-stdin $(CR_URL); \

push: .checkcr ## Push images to CR
	@$(MAKE) .push app=database tag=$(TAG)
	@$(MAKE) .push app=mover tag=$(TAG)
	@$(MAKE) .push app=master tag=$(TAG)
	@$(MAKE) .push app=monitor tag=$(TAG)

.push: ## Push image and tag as latest (args: from, to, app, tag)
	$(DOCKER) tag ecpds/$(app):$(tag) $(CR_URL)/$(app):$(tag)
	$(DOCKER) tag ecpds/$(app):$(tag) $(CR_URL)/$(app)
	$(DOCKER) push $(CR_URL)/$(app):$(tag)
	$(DOCKER) push $(CR_URL)/$(app)

clean: ## Remove all RPMs
	@$(MAKE) .clean app=mover
	@$(MAKE) .clean app=master
	@$(MAKE) .clean app=monitor

.clean: # Remove RPMs for the specified app (arg: app)
	rm -f ecpds/$(app)/rpms/ecpds-*-*.*.rpm

info: ## Output the configuration
	@echo "Software tag number: $(TAG)"
	@echo "Docker host/current OS: $(DOCKER_HOST_OS)/$(DOCKER_GUEST_OS)"
	@echo "Docker command: $(DOCKER) ($(DOCKER_VERSION))"
	@if [ -n "$(BUILD_OPTS)" ]; then \
		echo "Docker options: $(BUILD_OPTS)"; \
	fi
