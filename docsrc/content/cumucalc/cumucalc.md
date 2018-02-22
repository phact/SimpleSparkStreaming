---
title: Cumulative Streaming Calculations
weight: 20
menu:
  main:
      parent: Cumulative Streaming Calculations
      identifier: cumucalc
      weight: 201
---

## What are Cumulative Streaming Calculations?

Cumulative Streaming Calculations, by definition, span across multiple streaming windows. An example of a cumulative streaming calculation is keeping track of envent counts over time. Because the current count is made up of the new events that just came in during this window, and the events that were already accounted for from previous windows, the application must keep track of this state. Spark operations like `updateStateByKey` keep track of state for you in spark.

{{< note title="Cumulative Calculations are not idempotent!" >}}
updateStateByKey is a stateful operation which means that Spark must keep track of state accross windows, this has performance and failiure tolerance implications.
{{< /note >}}

Cumulative Streaming Calculations are a useful feature of Spark Streaming but also introduce some complexity to streaming applications. They should only be used when necessary.

## Implications of Non-idempotent Spark operations

If you use stateful operations in Spark, like `updateStateByKey` you must be a ble to recover that state if the application goes down unexpectedly, or if an executor is lost.
For this reason, it is required to perform checkpointing against a distributed file system when using `updateStateByKey`.

{{< note title="Checkpointing requires serializability" >}}
In DSE we recommend using DSE's very own file system (DSEFS) for this purpose. For checkpointing to work, all the classes used in the executors (i.e. the code inside `forEachRDD`) must be serializable.
{{< /note >}}

Compared to an idempotent application, a stateful streaming app will use more system resources and will not be as performant since it needs to keep track of state.
In addition, note that the streaming app will store, calculate, and persist the state of each key during each window.
This means that the performance of the streaming job will scale with the size of the total number of keys, not with the amount of data received in a particular window.

Let's take the ebdse yaml used for this asset:

```
bindings:
  franchise_id: compose Mod(<<sources:100>>); ToHashedUUID() -> UUID
  event_name: compose normal(50, 20); HashedLineToString(data/variable_words.txt); ToString() -> String
tags:
  phase: main
statement: "{franchise_id}-{event_name}"
```

Notice that the events that we are generating are a combination of the franchise_id and the event_name.
In this case, we have 100 franchises and about 73 normally distributed event_names.
This means that the total number of keys will be about 7300 and this number remains constant for the lifetime of the streaming job.

This yaml results in a stable `updateStateByKey` job because the total number of keys does not increase.

In the case where your keys are unbounded, there are better, idempotent, approaches that can be used to pre-calculate rollups with streaming applications that do not fall into the category of Cumulative Calculations.

For example, the following configuration would result in an unstable Spark application (one that quickly becomes unable to process the data in the time allotted):

```
bindings:
  franchise_id: compose Mod(<<sources:100>>); ToHashedUUID() -> UUID
  event_name: ToHashedUUID()
tags:
  phase: main
statement: "{franchise_id}-{event_name}"
```

In this case the event_name is a UUID derived from the monotonically increasing cycle and it will quickly result in a state that becomes too large and takes too long to persist.
