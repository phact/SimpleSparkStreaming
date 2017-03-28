#SimpleSparkStreaming

Counts words in UTF8 encoded, '\n' delimited text received from the network every second.

##Usage:

To run this on your local machine, you need to first run a Netcat server

    $ nc -lk 9999

and then run the example:

    $ dse spark-submit --class com.datastax.powertools.analytics.SimpleSparkStreaming ./target/SimpleSparkStreaming-0.1.jar localhost 9999

optionally provide a spark master

    $ dse spark-submit --master spark://10.200.178.62:7077 --class com.datastax.powertools.analytics.SimpleSparkStreaming ./target/SimpleSparkStreaming-0.1.jar localhost 9999

optionally run with cluster mode and superivise

    $ dse spark-submit --deploy-mode cluster --supervise --master spark://10.200.178.62:7077 --class com.datastax.powertools.analytics.SimpleSparkStreaming ./target/SimpleSparkStreaming-0.1.jar localhost 9999

better to pick an ip than localhost 

dse spark-submit --deploy-mode cluster --supervise --master
spark://10.200.178.67:7077 --class
com.datastax.powertools.analytics.SimpleSparkStreaming
http//:10.200.178.70:8000/SimpleSparkStreaming-0.1.jar 10.200.178.67 9999
