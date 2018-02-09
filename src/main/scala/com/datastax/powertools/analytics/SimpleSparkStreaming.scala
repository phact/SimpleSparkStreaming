package com.datastax.powertools.analytics

import com.datastax.spark.connector._
import com.datastax.spark.connector.cql.CassandraConnector
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{SQLContext, SaveMode, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.{Seconds, StreamingContext}

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{SQLContext, SaveMode}
import org.apache.spark.sql.cassandra._

// For DSE it is not necessary to set connection parameters for spark.master (since it will be done
// automatically)

/**
 * Counts words in UTF8 encoded, '\n' delimited text received from the network every second.
 */
object SimpleSparkStreaming {

  def main(args: Array[String]) {
    if (args.length < 2) {
      System.err.println("Usage: SimpleSparkStreaming <hostname> <port> <seconds>")
      System.exit(1)
    }

    val conf = new SparkConf().setAppName("SimpleSparkStreaming")
    val sc = SparkContext.getOrCreate(conf)


    // Create the context with a 1 second batch size
    val ssc = new StreamingContext(sc, Seconds(seconds))

    // Create a socket stream on target ip:port and count the
    // words in input stream of \n delimited text (eg. generated by 'nc')
    // Note that no duplication in storage level only for running locally.
    // Replication necessary in distributed scenario for fault tolerance.
    val lines = ssc.socketTextStream(args(0), args(1).toInt, StorageLevel.MEMORY_AND_DISK_SER)

    var seconds = 1
    if (args.length > 2){
      seconds = args(2).toInt
    }
    val words = lines.flatMap(_.split(" "))
    val wordCounts = words.map(x => (x, 1)).reduceByKey(_ + _)

    wordCounts.foreachRDD{ (rdd : RDD[(String , Int)], time: org.apache.spark.streaming.Time) =>

      val spark = SparkSessionSingleton.getInstance(rdd.sparkContext.getConf)
      import spark.implicits._

      val wordCountsDS= rdd.map((r:(String, Int)) => WordCount(r._1, r._2)).toDS()
      wordCountsDS.show()
      wordCountsDS.write.cassandraFormat("wordcount", "wordcount").mode(SaveMode.Append).save
    }

    ssc.start()
    ssc.awaitTermination()
  }
}
// scalastyle:on println

/** Lazily instantiated singleton instance of SparkSession */
object SparkSessionSingleton {

  @transient  private var instance: SparkSession = _

  def getInstance(sparkConf: SparkConf): SparkSession = {
    if (instance == null) {
      instance = SparkSession
        .builder
        .config(sparkConf)
        .getOrCreate()
    }
    instance
  }
}
case class WordCount(word: String, count: Long)
