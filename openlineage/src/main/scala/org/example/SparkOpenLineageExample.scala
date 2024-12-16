package org.example

import org.apache.spark.sql.SparkSession

import java.io.File

object SparkOpenLineageExample extends App with SparkExampleJob {
  private val openLineageOutputEventsJsonFilePath = "openlineage/target/open-lineage-output-events.jsonl"

  // Delete file before running the job to avoid appending to the file
  new File(openLineageOutputEventsJsonFilePath).delete()

  // Create Spark session with OpenLineage configuration
  private val spark = SparkSession
    .builder()
    .appName("Spark OpenLineage example")
    // Specify local execution mode
    .master("local[*]")
    // This line is EXTREMELY important
    .config("spark.extraListeners", "io.openlineage.spark.agent.OpenLineageSparkListener")
    // The transport type used for event emit, default type is console
    // The CompositeTransport is designed to combine multiple transports, allowing event emission to several destinations
    .config("spark.openlineage.transport.type", "composite")
    // Determines if the process should continue even when one of the transports fails. Default is true.
    .config("spark.openlineage.transport.continueOnFailure", "false")
    // The file transport type is pretty useless on Spark/Flink applications deployed to Yarn or Kubernetes cluster
    .config("spark.openlineage.transport.transports.file.type", "file")
    // The path of the file to write the events to
    .config("spark.openlineage.transport.transports.file.location", openLineageOutputEventsJsonFilePath)
    // Allows sending events to HTTP endpoint
    .config("spark.openlineage.transport.transports.http.type", "http")
    // Base url for HTTP requests
    .config("spark.openlineage.transport.transports.http.url", "http://localhost:8080")
    // String specifying the endpoint to which events are sent, appended to url. Optional, default: /api/v1/lineage.
    .config("spark.openlineage.transport.transports.http.endpoint", "/openapi/openlineage/api/v1/lineage")
    // The default namespace to be applied for any jobs submitted
    .config("spark.openlineage.namespace", "spark_namespace")
    // The job namespace to be used for the parent job facet
    .config("spark.openlineage.parentJobNamespace", "airflow_namespace")
    // The job name to be used for the parent job facet
    .config("spark.openlineage.parentJobName", "airflow_dag.airflow_task")
    // The RunId of the parent job that initiated this Spark job
    .config("spark.openlineage.parentRunId", "xxxx-xxxx-xxxx-xxxx")
    .getOrCreate()

  // Run the Spark job
  runSparkJob("openlineage")(spark)
}
