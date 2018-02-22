package com.datastax.powertools.analytics

import java.util.Date

import com.datastax.spark.connector._
import com.datastax.spark.connector.cql.CassandraConnector
import com.datastax.spark.connector.writer.SqlRowWriter
import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.minlog.Log
import com.esotericsoftware.minlog.Log.Logger
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Row, SQLContext, SaveMode, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.rdd.RDD
import org.apache.spark.serializer.KryoRegistrator
import org.apache.spark.sql.cassandra._
import org.slf4j.LoggerFactory

// For DSE it is not necessary to set connection parameters for spark.master (since it will be done
// automatically)
object SimpleSparkStreaming {

  def main(args: Array[String]) {
    if (args.length < 5) {
      System.err.println("Usage: SimpleSparkStreaming <hostname> <port> <seconds> <persist> <aggregate>")
      System.exit(1)
    }

    val persist = args(3).toBoolean
    val aggregate = args(4).toBoolean

    System.setProperty("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    System.setProperty("spark.kryo.registrator", "com.datastax.powertools.analytics.MyRegistrator")

    val conf = new SparkConf().setAppName("SimpleSparkStreaming")
    conf.set("spark.locality.wait", "0");
    conf.set("spark.kryoserializer.buffer","64k")
    conf.set("output.concurrent.writes","50")
    conf.set("spark.kryo.registrator", "com.datastax.powertools.analytics.MyRegistrator")


    val sc = SparkContext.getOrCreate(conf)


    //default
    var seconds = 1
    if (args.length > 2) {
      //argument
      seconds = args(2).toInt
    }
    val host = args(0)
    val port = args(1).toInt

    // Create the context with the window size
    val ssc = new StreamingContext(sc, Seconds(seconds))

    if (aggregate) {
      //because of updateStateByKey, this is full data checkpointing not just metadata.
      ssc.checkpoint("dsefs:///checkpoint")
    }

    //for a queue use queue specific connector
    val lines = ssc.socketTextStream(host, port, StorageLevel.MEMORY_AND_DISK_SER)

    //go from event to tuples (event, 1)
    val words = lines.map(x => (x, 1))

    words.foreachRDD { (rdd: RDD[(String, Int)], time: org.apache.spark.streaming.Time) =>
      val epochTime: Long = time.milliseconds

      //WordCount is a Case Class that maps to the DSE table
      val wordCountsRDD = rdd.map((r: (String, Int)) => WordCount(r._1, r._2, epochTime))

      if (persist) {
        //write to the database in each microbatch
        wordCountsRDD.saveToCassandra("wordcount", "wordcount")
      }
    }

    def updateFunction(newValues: Seq[Int], runningCount: Option[Int]): Option[Int] = {
      val newCount = runningCount.getOrElse(0) + newValues.sum
      Some(newCount)
    }

    var stateCount = words.updateStateByKey[Int](updateFunction _).map(x => Row(x._1, x._2))
    if (aggregate) {

      stateCount.foreachRDD { (rdd: RDD[Row], time: org.apache.spark.streaming.Time) =>

        val wordCountsRDD = rdd.map((r: (Row)) => WordCountAggregate(r.getAs[String](0), r.getAs[Int](1)))

        //write to the database in each microbatch
        wordCountsRDD.saveToCassandra("wordcount", "rollups")
      }
    }

    ssc.start()
    ssc.awaitTermination()
  }
}
// scalastyle:on println


//case classes are used to map the data to DSE tables
case class WordCount(word: String, count: Long, time: Long)
case class WordCountAggregate(word: String, count: Long)

//because we are checkpointing the data into DSEFS, we need to register the case classes with kryo
class MyRegistrator extends KryoRegistrator {
  override def registerClasses(kryo: Kryo) {
    kryo.register(classOf[WordCount])
    kryo.register(classOf[WordCountAggregate])
  }
}
