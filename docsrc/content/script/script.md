---
title: Script
weight: 40
menu:
  main:
      parent: Script
      identifier: script
      weight: 401
---

## Overview

This Proofshop consists of two parts:

1. High performance read / write workload
2. Streaming application

## Read / Write Workload

In order to obtain a performance baseline to size a DSE cluster for a particular workload, we can run performance benchmarks with realistic data.
EBDSE's cql activity type allows us to read and write randomly generated data against a real DSE cluster and captures throughput rates and client side latency histograms.

This Proofshop includes an EBDSE cql worlkoad for the raw events that are used by the streaming application described below.
To kick this job off ssh into the cluster and run `./startup ingestebdse`.
Note, this job will not run automatically on `./startup all` because it is marked as optional (`#ingestebdse`) in the `startup.order` file.

**Note:** The performance of this read write job is dependent on the hardware and topology of the DSE cluster it is run against. The script in `.startup/ingestebdse` has some tunable parameters (in the form of bash variables) including `threads` and `target rate` that can be tweaked to reach optimal performance.

## Streaming Application

The streaming application in this Proofshop takes data by the EBDSE tcpserver activity type and performs the following actions:

- Writes the raw events to the `streaming.orders` table in DSE
- Calculates a running count of events per category and persists the running count to the `streaming.rollups` table in DSE.

At this point, an operational analytics application can consult the rollups table and obtain results with single digit millisecond latencies.

**Note:** Similar to the `.startup/ingestebdse`, the `.startup/runebdse` script has parameters that can be tuned based on cluster size, hardware profiles, and dse version.
