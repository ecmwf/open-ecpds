#
# Makefile for managing Kubernetes resources and operations
# 
# This Makefile provides a set of targets to help with the creation, management,
# and cleanup of Kubernetes resources. It includes commands for converting Docker
# Compose files to Kubernetes manifests, creating persistent volumes, starting and
# stopping pods, and retrieving configuration details.
#
# Targets:
#   help    - Show this help message
#   build   - Create volumes, YAML files and start pod
#   volumes - Create volumes for the database, master, monitor, and mover pods
#   kompose - Convert docker-compose.yml into Kubernetes files
#   start   - Start pods
#   ports   - Get the ports translation to connect to from outside the cluster
#   yaml    - Get the YAML files for all pods, PV, and PVC
#   delete  - Delete all pods, PV, PVC and YAML files
#   info    - Output the configuration
#
# Usage:
#   make <target>
#
# Example:
#   make build
#
# (c) Copyright ECMWF 2019-2024 - Laurent Gougeon (syi@ecmwf.int)
#

# These are the sizes for the various volumes. Please update them as required.
VAR_LIB_ECPDS_DATABASE_SIZE=8Gi
ETC_ECPDS_DATABASE_SIZE=50Mi
VAR_LOG_ECPDS_MASTER_SIZE=5Gi
VAR_TMP_ECPDS_MASTER_SIZE=50Mi
VAR_LIB_ECPDS_MASTER_SIZE=1Gi
ETC_ECPDS_MASTER_SIZE=50Mi
VAR_LOG_ECPDS_MONITOR_SIZE=5Gi
VAR_TMP_ECPDS_MONITOR_SIZE=50Mi
ETC_ECPDS_MONITOR_SIZE=50Mi
VAR_LOG_ECPDS_MOVER_SIZE=5Gi
VAR_TMP_ECPDS_MOVER_SIZE=50Mi
VAR_LIB_ECPDS_MOVER_SIZE=10Gi
ETC_ECPDS_MOVER_SIZE=50Mi

# Use bash by default
SHELL=/bin/bash

# Extract the version and build number from the Maven file
VERSION=$(shell grep '<version>' ../../../../pom.xml | head -n 1 | sed 's/.*>\(.*\)<.*/\1/')
BUILD=$(shell grep '<build.number>' ../../../../pom.xml | head -n 1 | sed 's/.*>\(.*\)<.*/\1/')

# Set the TAG (export to be available in the compose file)
export TAG=$(VERSION)-$(BUILD)

# Set the run path to be used inside the Makefile
LOCAL_ROOT_PATH = $(realpath ../../..)

# Output the help for each task
.PHONY: help build .volume volumes kompose start yaml ports clean info

# Display help by default
.DEFAULT_GOAL := help

help: ## Show this help message
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "\033[36m%-12s\033[0m %s\n", $$1, $$2}' $(MAKEFILE_LIST)

build: volumes kompose start ## Create volumes, YAML files and start pods

.volume: ## Define the init-volume target
	@if [ -z "$(PV_NAME)" ] || [ -z "$(LOCAL_DIR)" ] || [ -z "$(SIZE)" ]; then \
		echo "Usage: make init-volume PV_NAME=<persistent-volume-name> LOCAL_DIR=<local-directory> SIZE=<size>"; \
		exit 1; \
	fi
	printf "apiVersion: v1\nkind: PersistentVolume\nmetadata:\n  name: $(PV_NAME)\nspec:\n  capacity:\n    storage: $(SIZE)\n  accessModes:\n    - ReadWriteOnce\n  hostPath:\n    path: /mnt/data/$(PV_NAME)\n" | kubectl apply -f -
	printf "apiVersion: v1\nkind: PersistentVolumeClaim\nmetadata:\n  name: $(PV_NAME)\nspec:\n  accessModes:\n    - ReadWriteOnce\n  resources:\n    requests:\n      storage: $(SIZE)\n" | kubectl apply -f -
	printf "apiVersion: v1\nkind: Pod\nmetadata:\n  name: init-ecpds-pod\nspec:\n  containers:\n  - name: init-container\n    image: busybox\n    command: ['sh', '-c', 'sleep 600']\n    volumeMounts:\n    - mountPath: /mnt/data\n      name: init-volume\n  volumes:\n  - name: init-volume\n    persistentVolumeClaim:\n      claimName: $(PV_NAME)\n" | kubectl apply -f -
	# Wait for the pod to be running
	@while [ "$$(kubectl get pod init-ecpds-pod -o jsonpath='{.status.phase}')" != "Running" ]; do \
		echo "Waiting for pod to be running..." && sleep 2; \
	done
	kubectl cp $(LOCAL_DIR) init-ecpds-pod:/mnt/data
	kubectl delete pod init-ecpds-pod --grace-period=2
	echo "Data from $(LOCAL_DIR) has been copied to the persistent volume $(PV_NAME)."

volumes: ## Create volumes for the database, master, monitor and mover pods
	$(MAKE) .volume PV_NAME=var-lib-ecpds-database LOCAL_DIR=$(LOCAL_ROOT_PATH)/var/lib/ecpds/database/. SIZE=$(VAR_LIB_ECPDS_DATABASE_SIZE)
	$(MAKE) .volume PV_NAME=etc-ecpds-database LOCAL_DIR=$(LOCAL_ROOT_PATH)/etc/ecpds/database/. SIZE=$(ETC_ECPDS_DATABASE_SIZE)
	$(MAKE) .volume PV_NAME=var-log-ecpds-master LOCAL_DIR=$(LOCAL_ROOT_PATH)/var/log/ecpds/master SIZE=$(VAR_LOG_ECPDS_MASTER_SIZE)
	$(MAKE) .volume PV_NAME=var-tmp-ecpds-master LOCAL_DIR=$(LOCAL_ROOT_PATH)/var/tmp/ecpds/master SIZE=$(VAR_TMP_ECPDS_MASTER_SIZE)
	$(MAKE) .volume PV_NAME=var-lib-ecpds-master LOCAL_DIR=$(LOCAL_ROOT_PATH)/var/lib/ecpds/master SIZE=$(VAR_LIB_ECPDS_MASTER_SIZE)
	$(MAKE) .volume PV_NAME=etc-ecpds-master LOCAL_DIR=$(LOCAL_ROOT_PATH)/etc/ecpds/master SIZE=$(ETC_ECPDS_MASTER_SIZE)
	$(MAKE) .volume PV_NAME=var-log-ecpds-monitor LOCAL_DIR=$(LOCAL_ROOT_PATH)/var/log/ecpds/monitor SIZE=$(VAR_LOG_ECPDS_MONITOR_SIZE)
	$(MAKE) .volume PV_NAME=var-tmp-ecpds-monitor LOCAL_DIR=$(LOCAL_ROOT_PATH)/var/tmp/ecpds/monitor SIZE=$(VAR_TMP_ECPDS_MONITOR_SIZE)
	$(MAKE) .volume PV_NAME=etc-ecpds-monitor LOCAL_DIR=$(LOCAL_ROOT_PATH)/etc/ecpds/monitor SIZE=$(ETC_ECPDS_MONITOR_SIZE)
	$(MAKE) .volume PV_NAME=var-log-ecpds-mover LOCAL_DIR=$(LOCAL_ROOT_PATH)/var/log/ecpds/mover SIZE=$(VAR_LOG_ECPDS_MOVER_SIZE)
	$(MAKE) .volume PV_NAME=var-tmp-ecpds-mover LOCAL_DIR=$(LOCAL_ROOT_PATH)/var/tmp/ecpds/mover SIZE=$(VAR_TMP_ECPDS_MOVER_SIZE)
	$(MAKE) .volume PV_NAME=var-lib-ecpds-mover LOCAL_DIR=$(LOCAL_ROOT_PATH)/var/lib/ecpds/mover SIZE=$(VAR_LIB_ECPDS_MOVER_SIZE)
	$(MAKE) .volume PV_NAME=etc-ecpds-mover LOCAL_DIR=$(LOCAL_ROOT_PATH)/etc/ecpds/mover SIZE=$(ETC_ECPDS_MOVER_SIZE)

kompose: ## Convert docker-compose.yml into kubernetes files
	@command -v kompose >/dev/null 2>&1 || { echo >&2 "Kompose installation skipped due to unsupported platform."; exit 1; }
	cd k8s-configs && ECPDS_ROOT_PATH=$(LOCAL_ROOT_PATH) kompose -f ../docker-compose.yml convert
	# Remove unused persistent volume claim files (already created)
	rm -f k8s-configs/*-persistentvolumeclaim.yaml
	# Update pod/service names
	sed -i 's/: database/: ecpds-database/g' k8s-configs/database-*.yaml
	sed -i 's/: master/: ecpds-master/g' k8s-configs/master-*.yaml
	sed -i 's/: monitor/: ecpds-monitor/g' k8s-configs/monitor-*.yaml
	sed -i 's/: mover/: ecpds-mover/g' k8s-configs/mover-*.yaml

start: ## Start pods
	cd k8s-configs && kubectl apply -f .

ports: ## Get the ports translation to connect to from outside the cluster
	@kubectl describe svc | grep 'NodePort:' | awk '{printf("%s -> %s\n",$$2,$$3)}' | sort

yaml: ## Get the YAML files for all pods, PV and PVC
	@kubectl get pv --no-headers | grep '-ecpds-' | awk '{print $$1}' | xargs -r kubectl get pv -o yaml
	@echo "---"
	@kubectl get pvc --no-headers | grep '-ecpds-' | awk '{print $$1}' | xargs -r kubectl get pvc -o yaml
	@echo "---"
	@kubectl get svc --no-headers | egrep "database|master|monitor|mover" | awk '{print $$1}' | xargs -r kubectl get svc -o yaml
	@echo "---"
	@kubectl get pods --no-headers | egrep "init-ecpds-pod|database|master|monitor|mover" | awk '{print $$1}' | xargs -r kubectl get pods -o yaml

delete: ## Delete all pods, PV, PVC and YAML files
	kubectl get pods --no-headers | egrep "init-ecpds-pod|database|master|monitor|mover" | awk '{print $$1}' | xargs -r kubectl delete pods
	kubectl get svc --no-headers | egrep "database|master|monitor|mover" | awk '{print $$1}' | xargs -r kubectl delete svc
	kubectl get pvc --no-headers | grep '-ecpds-' | awk '{print $$1}' | xargs -r kubectl delete pvc
	kubectl get pv --no-headers | grep '-ecpds-' | awk '{print $$1}' | xargs -r kubectl delete pv
	rm -f k8s-configs/*

info: ## Output the configuration
	@echo "Software tag number: $(TAG)"
	@echo "Kompose command: $(shell kompose version)"
	@echo "Kubectl command: $(shell kubectl version)"
