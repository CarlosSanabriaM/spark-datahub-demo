# Spark DataHub integration

* https://datahubproject.io/docs/lineage/openlineage
* https://datahubproject.io/docs/metadata-integration/java/acryl-spark-lineage
* https://www.restack.io/docs/datahub-knowledge-datahub-spark-column-lineage
* https://www.youtube.com/watch?v=FjkNySWkghY&t=
* https://openlineage.io/docs/integrations/spark/spark_column_lineage/

To add column-level lineage, one can use the Python code snippet provided in the DataHub documentation. After enabling the 'Show Columns' feature in DataHub, users can visualize the column-level lineage between datasets, as depicted in the official guide's images.

The integration process involves ensuring that the targeted datasets are already present in DataHub. Attempting to add lineage to non-existent entities will result in failure.

In summary, integrating Spark with DataHub for lineage capture involves utilizing DataHub's advanced SQL parser, adding lineage through provided code snippets, and leveraging the Airflow plugin for automatic lineage extraction.

## Python script

```python
# Sample Python script to emit column-level lineage
from datahub.emitter.mce_builder import make_lineage_mce
from datahub.emitter.rest_emitter import DatahubRestEmitter

# Initialize the REST emitter
datahub_emitter = DatahubRestEmitter(server='http://localhost:8080')

# Define the lineage information
lineage_mce = make_lineage_mce(
    upstream_urns=["urn:li:dataset:(urn:li:dataPlatform:bigquery,myproject.mydataset.mysource_table,PROD)"],
    downstream_urn='urn:li:dataset:(urn:li:dataPlatform:bigquery,myproject.mydataset.mydestination_table,PROD)',
    actor='urn:li:corpuser:datahub'
)

# Emit the lineage
datahub_emitter.emit_mce(lineage_mce)
```

## Release Spark column-level lineage
* https://github.com/datahub-project/datahub/releases/tag/v0.13.1

Abril 2024: v0.13.1

Openlineage endpoint and Spark Lineage Beta Plugin: https://github.com/datahub-project/datahub/pull/9870

Better Data Lineage: This release introduced support for Openlineage in service of the Spark Lineage Beta Plugin; additionally, we now support incremental Column-Level Lineage, improving the accuracy of detecting column-level relationships during ingestion.9870, 9967, 10090

## OpenLineage
* https://openlineage.io/docs/integrations/spark/spark_column_lineage/
* https://openlineage.io/docs/integrations/spark/

> Column-level lineage for Spark is turned on by default and requires no additional work to be done. The following documentation describes its internals.

Collected information is sent in OpenLineage event within columnLineage dataset facet described [here](https://openlineage.io/docs/spec/facets/dataset-facets/column_lineage_facet/).

Column-level lineage has been implemented separately from the rest of builders and visitors extracting lineage information from Spark logical plans. As a result the codebase is stored in io.openlineage.spark3.agent.lifecycle.plan.columnLineage package within classes responsible only for this feature.

Class ColumnLevelLineageUtils.java is an entry point to run the mechanism and is used within OpenLineageRunEventBuilder.

### Collecting Lineage in Spark
Collecting lineage requires hooking into Spark's ListenerBus in the driver application and collecting and analyzing execution events as they happen.

Both raw RDD and Dataframe jobs post events to the listener bus during execution. These events expose the structure of the job, including the optimized query plan, allowing the Spark integration to analyze the job for datasets consumed and produced, including attributes about the storage, such as location in GCS or S3, table names in a relational database or warehouse, such as Redshift or Bigquery, and schemas.

In addition to dataset and job lineage, Spark SQL jobs also report logical plans, which can be compared across job runs to track important changes in query plans, which may affect the correctness or speed of a job.

A single Spark application may execute multiple jobs. The Spark OpenLineage integration maps one Spark job to a single OpenLineage Job. The application will be assigned a Run id at startup and each job that executes will report the application's Run id as its parent job run.

### Installation
Bundle the package with your Apache Spark application project:

```xml
<dependency>
  <groupId>io.openlineage</groupId>
  <artifactId>openlineage-spark_${SCALA_BINARY_VERSION}</artifactId>
  <version>1.25.0</version>
</dependency>
```

### Configuration
Configuring the OpenLineage Spark integration is straightforward. It uses built-in Spark configuration mechanisms.

The below example demonstrates how to set the properties directly in your application when constructing a SparkSession.

> The setting config("spark.extraListeners", "io.openlineage.spark.agent.OpenLineageSparkListener") is extremely important. Without it, the OpenLineage Spark integration will not be invoked, rendering the integration ineffective.

```scala
import org.apache.spark.sql.SparkSession

object OpenLineageExample extends App {
  val spark = SparkSession.builder()
    .appName("OpenLineageExample")
    // This line is EXTREMELY important
    .config("spark.extraListeners", "io.openlineage.spark.agent.OpenLineageSparkListener")
    .config("spark.openlineage.transport.type", "http")
    .config("spark.openlineage.transport.url", "http://localhost:5000")
    .config("spark.openlineage.namespace", "spark_namespace")
    .config("spark.openlineage.parentJobNamespace", "airflow_namespace")
    .config("spark.openlineage.parentJobName", "airflow_dag.airflow_task")
    .config("spark.openlineage.parentRunId", "xxxx-xxxx-xxxx-xxxx")
    .getOrCreate()

  // ... your code

  spark.stop()
}
```
