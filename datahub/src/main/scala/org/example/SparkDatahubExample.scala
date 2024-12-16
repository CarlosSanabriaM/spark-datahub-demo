package org.example

import org.apache.spark.sql.SparkSession
import org.example.EmitterType.EmitterType
import org.rogach.scallop.{ScallopConf, ScallopOption}

import java.io.File

object SparkDatahubExample extends App with SparkExampleJob {
  private val datahubOutputEventsJsonFilePath = "datahub/target/datahub-output-events.json"

  // Create a new instance of the AppConf class to parse the program arguments
  private val conf = AppConf(args)

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
    .config("spark.datahub.emitter", conf.emitterType().toString)

  private val spark = (conf.emitterType() match {
    case EmitterType.file =>
      sparkSessionBuilder
        // The file where metadata will be written if file emitter is set
        .config("spark.datahub.file.filename", datahubOutputEventsJsonFilePath)
    case EmitterType.rest =>
      sparkSessionBuilder
        // The DataHub REST server URL
        .config("spark.datahub.rest.server", "http://localhost:8080")
  }).getOrCreate()

  // Run the Spark job
  runSparkJob("datahub")(spark)
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

case class AppConf(arguments: Seq[String]) extends ScallopConf(arguments) {
  /**
   * Emitter type.
   *
   * Possible values are specified in [[EmitterType]].
   */
  val emitterType: ScallopOption[EmitterType] =
    choice(
      choices = EmitterType.values.toSeq.map(_.toString),
      required = true,
      descr = "Emitter type to use: file (writes the events to a file) or " +
        "rest (sends the events to DataHub using its REST API). " +
        "Unlike OpenLineage, DataHub Spark listener does not have a composite emitter " +
        "that allows sending events to multiple destinations.",
      noshort = true)
      // Transform the string to the enum value
      .map(EmitterType.withName)

  verify()
}
