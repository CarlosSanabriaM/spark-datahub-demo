# Spark DataHub integration

Run the `SparkPi` example in IntelliJ. You should see the following messages in the logs:

```
INFO org.apache.spark.SparkContext - Registered listener io.openlineage.spark.agent.OpenLineageSparkListener
INFO io.openlineage.client.transports.ConsoleTransport - {"eventTime": ...
INFO io.openlineage.spark.agent.EventEmitter - Emitting lineage completed successfully with run id: ...
```