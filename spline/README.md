# Spark Spline example

## Context

__This module has a different purpose__ than the other modules in this repository.

Instead of trying to emit lineage to DataHub, this module shows __how to emit lineage in [Spline](https://absaoss.github.io/spline/)
format to the Spline server__, and how to __visualize it in the Spline UI__.

The initial idea was to compare the format of the jsons emitted by the OpenLineage Spark listener with the format of
the jsons emitted by the Spline QueryExecution listener. But that idea evolved into a full example of how to use Spline,
which serves to easily analyze which information is captured by Spline, and also compare how DataHub and Spline visualize lineage.

## How to run

You must stop DataHub before running this example. To do so, run the following command:

```shell
datahub docker quickstart --stop
```

Then, you have to start Spline server, by running the following command in the `spline` directory:

```shell
docker compose up
```

After all the services are up, you can run the `SparkSplineExample` in IntelliJ.

You should see the following message in the logs:

```
INFO SparkLineageInitializer: Spline successfully initialized. Spark Lineage tracking is ENABLED.
```

Then, navigate to the DataHub UI at http://localhost:9090 in your browser.

You should see the `Spark Spline example` execution event.

When you are done, you can stop Spline by running the following command in the `spline` directory:

```shell
docker compose down
```

## Results

| 😐 | Better at showing the Spark job execution plan but worst at global lineage |
|----|:---------------------------------------------------------------------------|

Spline __excels at showing the Spark job execution plan__, with a lot of detail about the Spark job internal operations:

<img width="1500" src="docs/images/spline-execution-events.png" alt="Spline executions events in Spline UI">

<img width="1500" src="docs/images/spline-spark-job-overview-1.png" alt="Spline Spark job overview 1 in Spline UI">

<img width="1500" src="docs/images/spline-spark-job-overview-2.png" alt="Spline Spark job overview 2 in Spline UI">

<img width="1500" src="docs/images/spline-spark-job-overview-3.png" alt="Spline Spark job overview 3 in Spline UI">

<img width="1500" src="docs/images/spline-spark-job-overview-4.png" alt="Spline Spark job overview 4 in Spline UI">

<img width="1500" src="docs/images/spline-spark-job-overview-5.png" alt="Spline Spark job overview 5 in Spline UI">

<img width="1500" src="docs/images/spline-spark-job-overview-6.png" alt="Spline Spark job overview 6 in Spline UI">

But __it is not able to show column-level-lineage at the global visualization__:

<img width="1500" src="docs/images/spline-execution-event-overview.png" alt="Spline execution event overview in Spline UI">

__It can only show attribute lineage for a single field__ when you select it in the Spline UI:

<img width="1500" src="docs/images/spline-spark-attribute-lineage-1.png" alt="Spline Spark attribute lineage 1 in Spline UI">

<img width="1500" src="docs/images/spline-spark-attribute-lineage-2.png" alt="Spline Spark attribute lineage 2 in Spline UI">

Also, __it is only able to show dataset schema for the output dataset__:

<img width="1500" src="docs/images/spline-datasets.png" alt="Spline datasets in Spline UI">

<img width="1500" src="docs/images/spline-input-dataset.png" alt="Spline input dataset in Spline UI">

<img width="1500" src="docs/images/spline-output-dataset.png" alt="Spline output dataset in Spline UI">
