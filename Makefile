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
AI_DATA_DIR := run/var/lib/ecpds/ia
DOCS_HOST ?= 0.0.0.0
DOCS_PORT ?= 8000
MONITOR_UI_HOST ?= ecpds-mover
MONITOR_UI_PORT ?= 8443
JAVADOC_SRC := ecpds-core/target/site/apidocs
SITE_DIR    := site

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
help: ## Show this help message
	@if [ -n "$(IN_DEV_CONTAINER)" ]; then ctx="inside"; else ctx="outside"; fi; \
	awk -v ctx="$$ctx" \
	    -v green="\033[32m" -v dim="\033[2m" -v cyan="\033[36m" -v reset="\033[0m" \
	'BEGIN { FS = ":.*?## " } \
	/^[a-zA-Z_-]+:.*?## / { \
	    desc = $$2; \
	    if (ctx == "inside") { \
	        gsub(/\(\*\*\)/, green "(**)" reset, desc); \
	        gsub(/\(\*\)/,   dim   "(*)"  reset, desc); \
	    } else { \
	        gsub(/\(\*\)/,   green "(*)"  reset, desc); \
	        gsub(/\(\*\*\)/, dim   "(**)" reset, desc); \
	    } \
	    gsub(/\(~\)/, green "(~)" reset, desc); \
	    printf cyan "%-20s" reset " %s\n", $$1, desc; \
	}' $(MAKEFILE_LIST); \
	printf "\n"; \
	if [ "$$ctx" = "inside" ]; then \
	    printf "  \033[32m(**)\033[0m runnable now (inside dev container)    \033[2m(*)\033[0m  outside only\n"; \
	else \
	    printf "  \033[32m(*)\033[0m  runnable now (outside dev container)   \033[2m(**)\033[0m inside only\n"; \
	fi; \
	printf "  \033[32m(~)\033[0m  requires Docker — runnable inside or outside\n"; \
	printf "  (no marker) — no restrictions\n\n"

# Check if inside or outside the development container
is-dev-container = \
  if [ "$$IN_DEV_CONTAINER" = "$(1)" ]; then \
    printf "$(RED)Error: This target can only be run $(2) the development container$(RESET)\n"; \
    exit 1; \
  fi

# Check that Docker (or Podman) is available
check-docker = \
  if ! command -v $(DOCKER) > /dev/null 2>&1; then \
    printf "$(RED)Error: '$(DOCKER)' is not available. Install Docker (or Podman) to use this target.$(RESET)\n"; \
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
.PHONY: help dev .dev-cntnr .run login rm-dev \
        get-geodb get-licenses build build-standalone \
        start-db stop-db start-ai stop-ai start-backend stop-backend \
        docs docs-screenshots docs-preview docs-publish \
        clean info

# ─── Development container ────────────────────────────────────────────────────
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
		-v $(HOME)/.copilot:/root/.copilot \
		-v $(HOME)/.ssh:/root/.ssh \
		-v $(WORKSPACE):/workspaces \
		-e DOCKER_HOST_WORKSPACE=$(DOCKER_HOST_WORKSPACE) \
		-e DOCKER_HOST_OS=$(DOCKER_HOST_OS) \
		--name $(CONTAINER_NAME) \
		--add-host=ecpds-mover:host-gateway \
		-p $(DOCS_PORT):$(DOCS_PORT) \
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
	@if [ -d "$(DOCKER_HOST_WORKSPACE)/$(DB_DATA_DIR)" ] || [ -d "$(DOCKER_HOST_WORKSPACE)/$(AI_DATA_DIR)" ]; then \
		echo ""; \
		echo "Note: local service data directories still exist:"; \
		[ -d "$(DOCKER_HOST_WORKSPACE)/$(DB_DATA_DIR)" ] && echo "  $(DB_DATA_DIR)"; \
		[ -d "$(DOCKER_HOST_WORKSPACE)/$(AI_DATA_DIR)" ] && echo "  $(AI_DATA_DIR)"; \
		read -p "Delete them too? (y/N) " answer; \
		if [ "$$answer" = "y" ] || [ "$$answer" = "Y" ]; then \
			rm -rf "$(DOCKER_HOST_WORKSPACE)/$(DB_DATA_DIR)" "$(DOCKER_HOST_WORKSPACE)/$(AI_DATA_DIR)"; \
			echo "Data directories removed."; \
		else \
			echo "Data directories kept. They will be reused if you run 'make dev' again."; \
		fi \
	fi

# ─── Build ────────────────────────────────────────────────────────────────────
get-geodb: ## Fetch latest GeoLite2-City (MaxMind.com) CDN files
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

build-standalone: ## Build the standalone all-in-one Docker image (**)
	@$(call is-dev-container,"",inside)
	@echo -n "$(TAG)" > VERSION
	@mvn package -Dcheckstyle.skip=true -Dspotbugs.skip=true
	@cd docker && $(MAKE) get-rpms get-licenses build-java build-standalone

# ─── Local services (database + AI) ───────────────────────────────────────────
start-db: ## Build and start the database service (~)
	@$(call check-docker)
	@if [ -d "$(DB_DATA_DIR)/mysql" ] && [ -d "$(DB_DATA_DIR)/ecpds" ]; then \
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
	@cd run/bin/ecpds && $(MAKE) up svc=database

stop-db: ## Stop the database service (~)
	@$(call check-docker)
	@cd run/bin/ecpds && $(MAKE) down svc=database

start-ai: ## Build and start the AI service (~)
	@$(call check-docker)
	@if [ -d "$(AI_DATA_DIR)" ] && find "$(AI_DATA_DIR)" -mindepth 1 -print -quit | grep -q .; then \
		echo "WARNING: AI data already exists in '$(AI_DATA_DIR)'."; \
		echo "If you keep it, preloaded models will NOT be pulled again."; \
		read -p "Delete existing AI data and reinitialize? (y/N) " answer; \
		if [ "$$answer" = "y" ] || [ "$$answer" = "Y" ]; then \
			echo "Deleting existing AI data..."; \
			rm -rf $(AI_DATA_DIR)/*; \
		else \
			echo "Keeping existing AI data..."; \
		fi \
	fi
	@cd docker && $(MAKE) build-ai
	@cd run/bin/ecpds && $(MAKE) up svc=ai

stop-ai: ## Stop the AI service (~)
	@$(call check-docker)
	@cd run/bin/ecpds && $(MAKE) down svc=ai

start-backend: ## Build and start both database and AI services (~)
	@$(MAKE) start-db
	@$(MAKE) start-ai

stop-backend: ## Stop both database and AI services (~)
	@$(MAKE) stop-ai
	@$(MAKE) stop-db

# ─── Documentation ────────────────────────────────────────────────────────────
docs: ## Build the documentation site (MkDocs + JavaDocs) into site/ (**)
	@$(call is-dev-container,"",inside)
	@echo "Generating JavaDocs..."
	@mvn javadoc:javadoc -pl ecpds-core -Dcheckstyle.skip=true -Dspotbugs.skip=true -Dmaven.javadoc.failOnError=false -q
	@echo "Building MkDocs static site..."
	@mkdocs build --strict
	@echo "Copying JavaDocs into $(SITE_DIR)/javadoc/ ..."
	@mkdir -p $(SITE_DIR)/javadoc
	@cp -r $(JAVADOC_SRC)/. $(SITE_DIR)/javadoc/
	@echo "Site ready at: $(CURDIR)/$(SITE_DIR)/"

docs-screenshots: ## Regenerate Monitor UI screenshots from a running standalone container (**)
	@$(call is-dev-container,"",inside)
	@echo ""
	@echo "=== Monitor UI Screenshot Generator ==="
	@read -p "Monitor UI URL       [https://$(MONITOR_UI_HOST):$(MONITOR_UI_PORT)]: " ui_base; \
	 ui_base=$${ui_base:-"https://$(MONITOR_UI_HOST):$(MONITOR_UI_PORT)"}; \
	 read -p "Admin username       [admin]: " admin_user; \
	 admin_user=$${admin_user:-admin}; \
	 read -s -p "Admin password       [admin2021]: " admin_pass; echo; \
	 admin_pass=$${admin_pass:-admin2021}; \
	 read -p "Monitor username     [monitor]: " mon_user; \
	 mon_user=$${mon_user:-monitor}; \
	 read -s -p "Monitor password     [monitor2021]: " mon_pass; echo; \
	 mon_pass=$${mon_pass:-monitor2021}; \
	 echo "Capturing screenshots from $$ui_base ..."; \
	 MONITOR_UI_BASE="$$ui_base" \
	 MONITOR_ADMIN_USER="$$admin_user" \
	 MONITOR_ADMIN_PASS="$$admin_pass" \
	 MONITOR_MON_USER="$$mon_user" \
	 MONITOR_MON_PASS="$$mon_pass" \
	 python3 scripts/crawl_monitor_ui.py
	@echo "Screenshots updated in docs/monitor-ui/img/"

docs-preview: ## Serve the documentation locally for testing, accessible from the Docker host (**)
	@$(call is-dev-container,"",inside)
	@echo "Generating JavaDocs..."
	@mvn javadoc:javadoc -pl ecpds-core -Dcheckstyle.skip=true -Dspotbugs.skip=true -Dmaven.javadoc.failOnError=false -q
	@echo "Copying JavaDocs into docs/javadoc/ for local preview..."
	@mkdir -p docs/javadoc
	@cp -r $(JAVADOC_SRC)/. docs/javadoc/
	@trap 'echo "Cleaning up docs/javadoc/..."; rm -rf docs/javadoc' EXIT INT TERM; \
	 echo "Starting MkDocs on http://$(DOCS_HOST):$(DOCS_PORT) ..."; \
	 mkdocs serve --dev-addr $(DOCS_HOST):$(DOCS_PORT)

docs-publish: docs ## Build and publish the documentation to GitHub Pages (**)
	@$(call is-dev-container,"",inside)
	@echo "Deploying to GitHub Pages (gh-pages branch)..."
	@ghp-import -n -p -f $(SITE_DIR)
	@echo "Published at https://ecmwf.github.io/open-ecpds/"
	@rm -rf $(SITE_DIR)

# ─── Utilities ────────────────────────────────────────────────────────────────
clean: ## Stop containers, remove images, JARs, RPMs and dependencies (**)
	@$(call is-dev-container,"",inside)
	@cd run/bin/ecpds && $(MAKE) -s down clean  || exit 1
	@cd docker && $(MAKE) -s rm-images  || exit 1
	@cd docker && $(MAKE) clean  || exit 1
	@mvn clean  || exit 1
	@rm -f lib/*.jar lib/*.pom || exit 1
	@rm -rf $(SITE_DIR)

info: ## Output the configuration
	@printf "\n"
	@if [ -n "$(IN_DEV_CONTAINER)" ]; then \
		printf "$(GREEN)╔══════════════════════════════════════════╗\n"; \
		printf "║   ✔  Inside the development container    ║\n"; \
		printf "╚══════════════════════════════════════════╝$(RESET)\n"; \
	else \
		printf "$(RED)╔══════════════════════════════════════════╗\n"; \
		printf "║   ✘  Outside the development container   ║\n"; \
		printf "╚══════════════════════════════════════════╝$(RESET)\n"; \
	fi
	@printf "\n"
	@printf "$(GREEN)── Project ──────────────────────────────────$(RESET)\n"
	@printf "  %-24s %s\n" "Name:"       "$(PROJECT_NAME)"
	@printf "  %-24s %s\n" "Version:"    "$(TAG)"
	@printf "  %-24s %s\n" "Workspace:"  "$(DOCKER_HOST_WORKSPACE)"
	@printf "\n"
	@printf "$(GREEN)── Development container ────────────────────$(RESET)\n"
	@printf "  %-24s %s\n" "Image:"      "$(IMAGE_NAME)"
	@printf "  %-24s %s\n" "Container:"  "$(CONTAINER_NAME)"
	@if [ -n "$(shell $(DOCKER) ps -q -f name=$(CONTAINER_NAME) 2>/dev/null)" ]; then \
		printf "  %-24s $(GREEN)%s$(RESET)\n" "Status:" "running"; \
	elif [ -n "$(shell $(DOCKER) ps -a -q -f name=$(CONTAINER_NAME) 2>/dev/null)" ]; then \
		printf "  %-24s $(RED)%s$(RESET)\n" "Status:" "stopped"; \
	else \
		printf "  %-24s %s\n" "Status:" "not created"; \
	fi
	@printf "  %-24s %s\n" "Docs port:"  "$(DOCS_PORT)"
	@printf "\n"
	@printf "$(GREEN)── Docker / container engine ────────────────$(RESET)\n"
	@printf "  %-24s %s\n" "Command:"    "$(DOCKER)"
	@printf "  %-24s %s\n" "Version:"    "$(DOCKER_VERSION)"
	@printf "  %-24s %s\n" "Host OS:"    "$(DOCKER_HOST_OS)"
	@printf "  %-24s %s\n" "Guest OS:"   "$(DOCKER_GUEST_OS)"
	@if [ -n "$(BUILD_OPTS)" ]; then \
		printf "  %-24s %s\n" "Build options:" "$(BUILD_OPTS)"; \
	fi
	@printf "\n"
	@printf "$(GREEN)── Local services ───────────────────────────$(RESET)\n"
	@printf "  %-24s %s\n" "DB data dir:" \
		"$(if $(wildcard $(DB_DATA_DIR)/ecpds),$(DB_DATA_DIR) (exists),$(DB_DATA_DIR) (not initialised))"
	@printf "  %-24s %s\n" "AI data dir:" \
		"$(if $(wildcard $(AI_DATA_DIR)/*),$(AI_DATA_DIR) (exists),$(AI_DATA_DIR) (empty or absent))"
	@printf "\n"
	@printf "$(GREEN)── Toolchain (inside container only) ────────$(RESET)\n"
	@if [ -n "$(IN_DEV_CONTAINER)" ]; then \
		printf "  %-24s %s\n" "Java:" "$$(java -version 2>&1 | head -1)"; \
		printf "  %-24s %s\n" "Maven:" "$$(mvn -version 2>/dev/null | head -1)"; \
		printf "  %-24s %s\n" "MkDocs:" "$$(mkdocs --version 2>/dev/null)"; \
		printf "  %-24s %s\n" "Python:" "$$(python3 --version 2>/dev/null)"; \
	else \
		printf "  (run from inside the dev container to see toolchain versions)\n"; \
	fi
	@printf "\n"
	@printf "$(GREEN)── Monitor UI screenshots ───────────────────$(RESET)\n"
	@printf "  %-24s %s\n" "Default URL:" "https://$(MONITOR_UI_HOST):$(MONITOR_UI_PORT)"
	@printf "\n"
