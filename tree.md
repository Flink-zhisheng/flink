Blink 源码的目录结构

```
.
├── build-target
├── docs
│   ├── _includes
│   ├── _layouts
│   ├── _plugins
│   ├── concepts
│   ├── dev
│   ├── docker
│   ├── examples
│   ├── fig
│   ├── internals
│   ├── monitoring
│   ├── ops
│   ├── page
│   ├── quickstart
│   ├── redirects
│   ├── release-notes
│   ├── start
│   └── tutorials
├── flink-annotations
├── flink-clients
├── flink-connectors
│   ├── flink-connector-cassandra
│   ├── flink-connector-elasticsearch
│   ├── flink-connector-elasticsearch-base
│   ├── flink-connector-elasticsearch2
│   ├── flink-connector-elasticsearch5
│   ├── flink-connector-elasticsearch6
│   ├── flink-connector-filesystem
│   ├── flink-connector-hive
│   ├── flink-connector-kafka-0.10
│   ├── flink-connector-kafka-0.11
│   ├── flink-connector-kafka-0.8
│   ├── flink-connector-kafka-0.9
│   ├── flink-connector-kafka-base
│   ├── flink-connector-kinesis
│   ├── flink-connector-nifi
│   ├── flink-connector-rabbitmq
│   ├── flink-connector-twitter
│   ├── flink-hadoop-compatibility
│   ├── flink-hbase
│   ├── flink-hcatalog
│   ├── flink-jdbc
│   └── flink-orc
├── flink-container
│   ├── docker
│   └── kubernetes
├── flink-contrib
│   ├── docker-flink
│   ├── flink-connector-wikiedits
│   ├── flink-storm
│   └── flink-storm-examples
├── flink-core
├── flink-dist
├── flink-docs
├── flink-end-to-end-tests
│   ├── flink-bucketing-sink-test
│   ├── flink-cli-test
│   ├── flink-confluent-schema-registry
│   ├── flink-dataset-allround-test
│   ├── flink-datastream-allround-test
│   ├── flink-distributed-cache-via-blob-test
│   ├── flink-e2e-test-utils
│   ├── flink-elasticsearch1-test
│   ├── flink-elasticsearch2-test
│   ├── flink-elasticsearch5-test
│   ├── flink-elasticsearch6-test
│   ├── flink-end-to-end-tests-common
│   ├── flink-heavy-deployment-stress-test
│   ├── flink-high-parallelism-iterations-test
│   ├── flink-local-recovery-and-allocation-test
│   ├── flink-metrics-availability-test
│   ├── flink-metrics-reporter-prometheus-test
│   ├── flink-parent-child-classloading-test
│   ├── flink-queryable-state-test
│   ├── flink-quickstart-test
│   ├── flink-sql-client-test
│   ├── flink-state-evolution-test
│   ├── flink-stream-sql-test
│   ├── flink-stream-state-ttl-test
│   ├── flink-stream-stateful-job-upgrade-test
│   ├── flink-streaming-file-sink-test
│   ├── flink-streaming-kafka-test
│   ├── flink-streaming-kafka-test-base
│   ├── flink-streaming-kafka010-test
│   ├── flink-streaming-kafka011-test
│   └── test-scripts
├── flink-examples
│   ├── flink-examples-batch
│   ├── flink-examples-streaming
│   └── flink-examples-table
├── flink-filesystems
│   ├── flink-fs-hadoop-shaded
│   ├── flink-hadoop-fs
│   ├── flink-mapr-fs
│   ├── flink-s3-fs-base
│   ├── flink-s3-fs-hadoop
│   ├── flink-s3-fs-presto
│   └── flink-swift-fs-hadoop
├── flink-formats
│   ├── flink-avro
│   ├── flink-avro-confluent-registry
│   ├── flink-json
│   └── flink-parquet
├── flink-fs-tests
├── flink-java
├── flink-java8
├── flink-kubernetes
├── flink-libraries
│   ├── flink-cep
│   ├── flink-cep-scala
│   ├── flink-gelly
│   ├── flink-gelly-examples
│   ├── flink-gelly-scala
│   ├── flink-ml
│   ├── flink-python
│   ├── flink-sql-client
│   ├── flink-sql-parser
│   ├── flink-streaming-python
│   ├── flink-table
│   └── flink-table-common
├── flink-mesos
├── flink-metrics
│   ├── flink-metrics-core
│   ├── flink-metrics-datadog
│   ├── flink-metrics-dropwizard
│   ├── flink-metrics-ganglia
│   ├── flink-metrics-graphite
│   ├── flink-metrics-jmx
│   ├── flink-metrics-prometheus
│   ├── flink-metrics-slf4j
│   └── flink-metrics-statsd
├── flink-optimizer
├── flink-queryable-state
│   ├── flink-queryable-state-client-java
│   └── flink-queryable-state-runtime
├── flink-quickstart
│   ├── flink-quickstart-java
│   └── flink-quickstart-scala
├── flink-runtime
├── flink-runtime-web
│   └── web-dashboard
├── flink-scala
├── flink-scala-shell
├── flink-service
├── flink-shaded-curator
├── flink-shaded-hadoop
│   ├── flink-shaded-hadoop2
│   ├── flink-shaded-hadoop2-uber
│   └── flink-shaded-yarn-tests
├── flink-state-backends
│   └── flink-statebackend-rocksdb
├── flink-streaming-java
├── flink-streaming-scala
├── flink-test-utils-parent
│   ├── flink-test-utils
│   └── flink-test-utils-junit
├── flink-tests
├── flink-yarn
├── flink-yarn-shuffle
├── flink-yarn-tests
└── tools
    ├── force-shading
    ├── maven
    └── releasing
```
