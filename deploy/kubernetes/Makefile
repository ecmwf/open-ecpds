SHELL := /bin/bash
.DEFAULT_GOAL := help

PYTHON ?= python3
PROFILE ?= cluster
VERSION := $(shell sed -n 's/.*<revision>\(.*\)<\/revision>.*/\1/p' ../../pom.xml | head -n 1)
BUILD := $(shell sed -n 's/.*<build.number>\(.*\)<\/build.number>.*/\1/p' ../../pom.xml | head -n 1)
TAG ?= $(VERSION)-$(BUILD)

export PROFILE TAG ECPDS_REPOSITORY OPENBAO_IMAGE LOCAL_NODE STORAGE_CLASS
export CONTEXT NAMESPACE CLAIM SOURCE CONFIRM_DATA

help: ## Show native Kubernetes deployment operations
	@awk 'BEGIN {FS = ":.*## "} /^[a-z-]+:.*## / {printf "  %-18s %s\n", $$1, $$2}' $(MAKEFILE_LIST)

render: ## Render the selected PROFILE without contacting a cluster
check: ## Check resource structure and references offline
diff: ## Compare workload resources (CONTEXT and NAMESPACE required)
validate: ## Server-side dry run of workload resources (prerequisites must exist)
storage: ## Provision claims explicitly for a fresh local or cluster profile
bootstrap: ## Import SOURCE into a single empty CLAIM before launching workloads
apply: ## Check prerequisites and apply workloads without touching storage
status: ## Show the selected workloads, services and claims
wait: ## Wait for the selected workloads with bounded timeouts
ports: ## Show Service ports for the selected profile
delete-workloads: ## Remove selected controllers, retaining storage, services and policies
purge-data: ## Delete one unused CLAIM after explicit CONFIRM_DATA=context/namespace/claim

operations := render check diff validate storage bootstrap apply status wait ports delete-workloads purge-data
.PHONY: help $(operations) build start delete
$(operations):
	@$(PYTHON) deploy.py $@

build start: apply ## Compatibility aliases; storage is now a separate operation
delete: delete-workloads ## Compatibility alias; no data is deleted
