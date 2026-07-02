# Illustrative Physical Infrastructure

This page provides a high-level overview of the physical infrastructure supporting
OpenECPDS at ECMWF. The deployment consists of three distinct services: **Acquisition
(ACQ)**, **Dissemination (DISS)**, and **Auxiliary (AUX)**. While this serves as an
example of a possible physical infrastructure, the actual setup may vary depending on the
specific requirements of each site.

## Service separation

Each service is dedicated to a specific function but retains the same core capabilities.
This separation primarily helps distribute workloads based on peak usage periods. For
instance:

- The **Acquisition** service operates continuously.
- The **Dissemination** and **Auxiliary** services experience peak activity three times
  a day, particularly during forecast deliveries.

## Scale

The current infrastructure comprises **80 bare-metal systems** with a total storage
capacity of **2 petabytes**. Storage capacity is regularly reassessed, especially when
new processing cycles are introduced.

## Data flows

The main data flows for OpenECPDS and its various processes are:

1. **Data Submission** from the HPC to OpenECPDS.
2. **Data Replication** between the Data Movers.
3. **Data Dissemination** to the Internet and RMDCN networks over the WAN.

This refers to **Data Dissemination** in a broad sense, including both uploads to remote
sites from OpenECPDS and downloads by customers through the OpenECPDS data portal.

The HPC and OpenECPDS are each within their respective security zones, and the traffic
between these zones passes through a firewall.

## Related

- [Architecture Overview](../architecture/overview.md)
- [Continental Data Movers](../architecture/continental-data-movers.md)
- [Global Reach](../global-reach.md)
