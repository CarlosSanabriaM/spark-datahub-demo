package org.example

import org.apache.commons.io.FileUtils
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{concat, lit}

import java.io.File

object SparkDatahubExample extends App {
  private val peopleCsvFilePath = "people.csv"
  private val outputFolderPath = "datahub/target/developers-under-30"

  // Create Spark session with OpenLineage configuration
  private val spark = SparkSession
    .builder()
    .appName("Spark DataHub example")
    // Specify local execution mode
    .master("local[*]")
    // This line is EXTREMELY important
    .config("spark.extraListeners", "datahub.spark.DatahubSparkListener")
    // Specify the ways to emit metadata. By default, it sends to DataHub using REST emitter.
    // Valid options are rest, kafka or file
    .config("spark.datahub.emitter", "rest")
    // The DataHub REST server URL
    .config("spark.datahub.rest.server", "http://localhost:8080")
    // Include scheme from the path URI (e.g. hdfs://, s3://) in the dataset URN.
    // We recommend setting this value to false. Default is true for backwards compatibility with previous versions.
    .config("spark.datahub.metadata.include_scheme", "false")
    // config("spark.jars.packages","io.acryl:acryl-spark-lineage:0.2.16")
    //
    // By default, datahub assigns Hive-like tables to the Hive platform.
    // If you are using Glue as your Hive metastore, set this config flag to glue
    // config("spark.datahub.metadata.dataset.hivePlatformAlias", "glue")
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
