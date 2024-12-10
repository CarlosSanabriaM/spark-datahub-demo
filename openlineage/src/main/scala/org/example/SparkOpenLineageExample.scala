package org.example

import org.apache.commons.io.FileUtils
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{concat, lit}

import java.io.File

object SparkOpenLineageExample extends App {
  private val peopleCsvFilePath = "commons/src/main/resources/people.csv"
  private val outputFolderPath = "./developers-under-30"
  private val openLineageOutputEventsJsonFilePath = "./open-lineage-output-events.jsonl"

  // Delete file before running the job to avoid appending to the file
  new File(openLineageOutputEventsJsonFilePath).delete()

  // Create Spark session with OpenLineage configuration
  val spark = SparkSession
    .builder()
    .appName("Spark OpenLineage example")
    // Specify local execution mode
    .master("local[*]")
    // This line is EXTREMELY important
    .config("spark.extraListeners", "io.openlineage.spark.agent.OpenLineageSparkListener")
    // The transport type used for event emit, default type is console
    // The file transport type is pretty useless on Spark/Flink applications deployed to Yarn or Kubernetes cluster
    .config("spark.openlineage.transport.type", "file")
    // The path of the file to write the events to
    .config("spark.openlineage.transport.location", openLineageOutputEventsJsonFilePath)
    // The default namespace to be applied for any jobs submitted
    .config("spark.openlineage.namespace", "spark_namespace")
    // The job namespace to be used for the parent job facet
    .config("spark.openlineage.parentJobNamespace", "airflow_namespace")
    // The job name to be used for the parent job facet
    .config("spark.openlineage.parentJobName", "airflow_dag.airflow_task")
    // The RunId of the parent job that initiated this Spark job
    .config("spark.openlineage.parentRunId", "xxxx-xxxx-xxxx-xxxx")
    .getOrCreate()

  // Create a DataFrame from the CSV file
  private val df = spark.read.option("header", "true").option("delimiter", ";").csv(peopleCsvFilePath)
  df.printSchema()
  df.show()

  // Add a new column to the DataFrame with the full name
  private val dfWithFullName = df.withColumn("full_name", concat(df("name"), lit(" "), df("surname")))
  dfWithFullName.printSchema()
  dfWithFullName.show()

  // Filter the DataFrame to only include developers below the age of 30
  private val dfDevelopersUnder30 = dfWithFullName
    .filter(dfWithFullName("job") === "Developer" && dfWithFullName("age") < 30)
  dfDevelopersUnder30.show()

  // Delete the output folder if it exists
  FileUtils.deleteDirectory(new File(outputFolderPath))
  // Save the DataFrame to a CSV file
  dfDevelopersUnder30.write.option("header", "true").csv(outputFolderPath)

  spark.stop()
}
