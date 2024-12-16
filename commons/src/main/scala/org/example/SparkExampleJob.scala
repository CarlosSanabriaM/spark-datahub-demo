package org.example

import org.apache.spark.sql.functions.{concat, lit}
import org.apache.spark.sql.SparkSession

/**
 * A trait that provides a method to run a Spark example job.
 *
 * The DAG of the job will be captured and emitted by the corresponding lineage plugin.
 */
trait SparkExampleJob {
  /**
   * Run the Spark job.
   *
   * @param moduleName the name of the Maven module that will execute the job
   */
  def runSparkJob(moduleName: String)(implicit spark: SparkSession): Unit = {
    val peopleCsvFilePath = "people.csv"
    val outputFolderPath = s"$moduleName/target/developers-under-30"

    // Create a DataFrame from the CSV file
    val df = spark.read.option("header", "true").option("delimiter", ";").csv(peopleCsvFilePath)
    df.printSchema()
    df.show()

    // Add a new column to the DataFrame with the full name
    val dfWithFullName = df.withColumn("full_name", concat(df("name"), lit(" "), df("surname")))
    dfWithFullName.printSchema()
    dfWithFullName.show()

    // Filter the DataFrame to only include developers below the age of 30
    val dfDevelopersUnder30 = dfWithFullName
      .filter(dfWithFullName("job") === "Developer" && dfWithFullName("age") < 30)
    dfDevelopersUnder30.show()

    // Save the DataFrame to a CSV file
    dfDevelopersUnder30.write.mode("overwrite").option("header", "true").csv(outputFolderPath)

    spark.stop()
  }
}
