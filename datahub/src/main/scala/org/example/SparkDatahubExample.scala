package org.example

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{concat, lit}

import java.io.File

object SparkDatahubExample extends App {
  private val peopleCsvFilePath = "people.csv"
  private val outputFolderPath = "datahub/target/developers-under-30"
  private val datahubOutputEventsJsonFilePath = "datahub/target/datahub-output-events.jsonl"
  // Change this value to modify where the events will be emitted:
  // * file: writes the events to a file
  // * rest: sends the events to DataHub using its REST API
  private val emitterType = EmitterType.file

  // Delete file before running the job to avoid appending to the file
  new File(datahubOutputEventsJsonFilePath).delete()

  // Create Spark session with OpenLineage configuration
  private val sparkSessionBuilder = SparkSession
    .builder()
    .appName("Spark DataHub example")
    // Specify local execution mode
    .master("local[*]")
    // This line is EXTREMELY important
    .config("spark.extraListeners", "datahub.spark.DatahubSparkListener")
    // Include scheme from the path URI (e.g. hdfs://, s3://) in the dataset URN.
    // We recommend setting this value to false. Default is true for backwards compatibility with previous versions.
    .config("spark.datahub.metadata.include_scheme", "false")
    // Specify the ways to emit metadata. By default, it sends to DataHub using REST emitter.
    // Valid options are rest, kafka or file
    .config("spark.datahub.emitter", emitterType.toString)

  private val spark = (emitterType match {
    case EmitterType.file =>
      sparkSessionBuilder
        // The file where metadata will be written if file emitter is set
        .config("spark.datahub.file.filename", datahubOutputEventsJsonFilePath)
    case EmitterType.rest =>
      sparkSessionBuilder
        // The DataHub REST server URL
        .config("spark.datahub.rest.server", "http://localhost:8080")
  }).getOrCreate()

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

  // Save the DataFrame to a CSV file
  dfDevelopersUnder30.write.mode("overwrite").option("header", "true").csv(outputFolderPath)

  spark.stop()
}

/**
 * Define the emitter type to use (either file or rest).
 *
 * Unlike OpenLineage, DataHub Spark listener does not have a composite emitter
 * that allows sending events to multiple destinations.
 */
object EmitterType extends Enumeration {
  type EmitterType = Value
  val file, rest = Value
}
