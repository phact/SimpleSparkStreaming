---
title: Streaming Monitoring and Performance Tuning
weight: 30
menu:
  main:
      parent: Streaming Monitoring and Performance Tuning
      identifier: perf
      weight: 301
---

## Monitoring

Like DSE Batch Analyitcs applications, Streaming applications can be monitored directly from the Spark UI.
The Spark <a target="_blank" href="https://github.com/datastax/spark-cassandra-connector/blob/master/doc/11_metrics.md">Metrics</a> can still be used for further troubleshooting.
However, the Spark UI has a Streaming tab that is specific to Streaming applications.

![streaming tab](/perf/streamingtab.png)

Notice the dotted line marked `stable` in the Processing Time section of the Streaming UI.
This line signifies the maximum processing time beyont which the streaming job will start falling behind.
This is the key performance indicator of a streaming application.

## Performance Tuning

If you find that your Streaming job is `unstable` you may consider some of the same options you might use to improve performance on a DSE Batch application.
DSE Analytics apps can be tuned by using the read / write <a href="https://github.com/datastax/spark-cassandra-connector/blob/240f3b4ec995d0e62c45530251e86ef894feb54f/doc/reference.md#read-tuning-parameters" target="_blank">performance settings</a> available in the spark-cassandra-connector.

Specific to streaming applications, the main lever that can be tuned is the size of your streaming window. The larger the window, the more throughput an application can achieve since it is not expending as much time in overhead operations that repeat every window.
