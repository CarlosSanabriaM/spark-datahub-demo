/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// scalastyle:off println
package org.example

import org.apache.spark.sql.SparkSession

import java.io.File
import scala.math.random

/** Computes an approximation to pi */
object SparkPi extends App {
  private val outputJsonFilePath = "./open-lineage-output-events.jsonl"
  // Delete file before running the job to avoid appending to the file
  new File(outputJsonFilePath).delete()

  val spark = SparkSession
    .builder()
    .appName("Spark Pi OpenLineage Example")
    // Specify local execution mode
    .master("local[*]")
    // This line is EXTREMELY important
    .config("spark.extraListeners", "io.openlineage.spark.agent.OpenLineageSparkListener")
    // The transport type used for event emit, default type is console
    .config("spark.openlineage.transport.type", "file")
    // The path of the file to write the events to
    .config("spark.openlineage.transport.location", outputJsonFilePath)
    // The default namespace to be applied for any jobs submitted
    .config("spark.openlineage.namespace", "spark_namespace")
    // The job namespace to be used for the parent job facet
    .config("spark.openlineage.parentJobNamespace", "airflow_namespace")
    // The job name to be used for the parent job facet
    .config("spark.openlineage.parentJobName", "airflow_dag.airflow_task")
    // The RunId of the parent job that initiated this Spark job
    .config("spark.openlineage.parentRunId", "xxxx-xxxx-xxxx-xxxx")
    .getOrCreate()

  val slices = if (args.length > 0) args(0).toInt else 2
  val n = math.min(100000L * slices, Int.MaxValue).toInt // avoid overflow
  val count = spark.sparkContext.parallelize(1 until n, slices).map { i =>
    val x = random() * 2 - 1
    val y = random() * 2 - 1
    if (x*x + y*y <= 1) 1 else 0
  }.reduce(_ + _)
  println(s"Pi is roughly ${4.0 * count / (n - 1)}")

  spark.stop()
}
// scalastyle:on println
