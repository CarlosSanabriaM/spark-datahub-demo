package org.example

import org.apache.spark.sql.SparkSession

object SparkSplineExample extends App with SparkExampleJob {
  // Create Spark session with Spline configuration
  private val spark = SparkSession
    .builder()
    .appName("Spark Spline example")
    // Specify local execution mode
    .master("local[*]")
    // This line is EXTREMELY important
    .config("spark.sql.queryExecutionListeners", "za.co.absa.spline.harvester.listener.SplineQueryExecutionListener")
    // The logical name of the root lineage dispatcher
    // The CompositeDispatcher is a proxy dispatcher that forwards lineage data to multiple dispatchers
    .config("spline.lineageDispatcher", "composite")
    // We are using the http and console dispatchers
    .config("spline.lineageDispatcher.composite.dispatchers", "http,console")
    // The url of the producer API rest endpoint
    .config("spline.lineageDispatcher.http.producer.url", "http://localhost:8080/producer")
    .getOrCreate()

  // Run the Spark job
  runSparkJob("spline")(spark)
}
