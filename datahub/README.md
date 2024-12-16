# Spark DataHub example

Unlike OpenLineage, DataHub Spark listener does not have a composite emitter that allows
sending events to multiple destinations. Therefore, you must choose the emitter type to use:

* `file`: Writes the events to a file. Run `SparkDatahubExample (file)` in IntelliJ.
* `rest`: Sends the events to DataHub using its REST API. Run `SparkDatahubExample (rest)` in IntelliJ.

You can execute one after the other.

You should see the following messages in the logs:

```
DEBUG datahub.spark.DatahubSparkListener - loadDatahubConfig completed successfully
INFO datahub.spark.DatahubSparkListener - onApplicationStart completed successfully
```
