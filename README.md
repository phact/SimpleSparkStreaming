## Streaming Analytics Proofshop

This is a guide for how to use the Streaming Analytics Proofshop asset brought to you by the Vanguard team. The Proofshop consists of demonstrating real time data processing and analytics with the DSE paltform at high throughput, low latency, and scale.

### Motivation

DSE is often seen as a serving layer for the output of analytical applications on large datasets. Often the analytics themselves are performed in slow batch data layers and persisted to DSE only when they are ready to be exposed to a front end or downstream system. In many cases, as we look to satisfy end user requirements, it makes more sense to perform some of these operational analytics in real time using DSE's streaming analytics capabilities.

### What is included?

This field asset includes sample usage of DSE Streaming Analytics in the following contexts:

* Data generation using EBDSE served via TCP
* Spark streaming application that persists raw events into DSE and rollups into summary tables
* Tuning capabilities for throughput and latencies
* Checkpointing into DSEFS

### Business Take Aways

In the right now economy businesses need analytics that are up to date. Monthly, weekly, and hourly reports are often too old to be actionable. DSE Streaming analytics allows businesses to operationalize structured analytics to obtain real time insights to power their decision making.

Out of the Five Dimensions, this asset focuses on Relevancy and Responsiveness without ignoring the remaining dimensions (Availability, Accessibility, Engagement).

If discussing this asset with a business stakeholder it may be relevant to walk them through <a target="_blank" href="https://docs.google.com/presentation/d/1z_wGENm2RNX1oqwUkSzDg3P03xPG2P9lKEjj8UiZOQM/edit?usp=sharing">The DataStax Story</a>

### Technical Take Aways

DSE Streaming Analytics is DataStax's version of Apache Spark (TM) which ships with DSE and is modified to match the design principles that our engineering team has always focused on when building C* and DSE (CARDS). DSE Spark is optimized for high availability and operational simplicity by removing its dependency on zookeeper and using LWTs for leader elections. Furthermore, DSE Spark is optimized for performance against the DSE backend with features that include Contiunous Paging and Direct Joins.

Building Streaming Analytics applications on DSE requires:

- Having a streaming source - in this case we use EBDSE's tcpserver functionality and data generation capabilities as our streaming source
- A Spark Streaming Application - sources for the app included as part of the asset

For more general information on how to use Spark Streaming check out the <a href="https://spark.apache.org/docs/latest/streaming-programming-guide.html" target="_blank">Programming Guide</a> in the Spark Docs.

These docs will dive deeper on the following functionality:

- Cumulative Streaming Calculations
- Monitoring and Tuning Streaming Jobs
