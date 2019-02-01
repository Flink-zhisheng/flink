Blink 源码各个模块代码统计

项目 Java 代码行数总共 834182 行， Scala 代码行数总共 237335 行，总共：1071517（统计包括各种 example 目录和 test 目录下的代码行数）。

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
├── flink-annotations    代码行数：108
├── flink-clients    代码行数：7838  src 代码行数：4747
├── flink-connectors    代码行数：52165
│   ├── flink-connector-cassandra   代码行数：1992   src 代码行数：1047
│   ├── flink-connector-elasticsearch    代码行数：296   src 代码行数：151
│   ├── flink-connector-elasticsearch-base    代码行数：2047    src 代码行数：1278
│   ├── flink-connector-elasticsearch2    代码行数：249    src 代码行数：155
│   ├── flink-connector-elasticsearch5    代码行数：217    src  代码行数：110
│   ├── flink-connector-elasticsearch6    代码行数：769    src  代码行数：497
│   ├── flink-connector-filesystem      代码行数：3864    src  代码行数：1748
│   ├── flink-connector-hive    代码行数：2126   src  代码行数：1921
│   ├── flink-connector-kafka-0.10    代码行数：2048   src 代码行数：974
│   ├── flink-connector-kafka-0.11    代码行数：3090   src 代码行数：1675
│   ├── flink-connector-kafka-0.8    代码行数：2826   src 代码行数：1913
│   ├── flink-connector-kafka-0.9    代码行数：3320   src 代码行数：1518
│   ├── flink-connector-kafka-base    代码行数：10647   src 代码行数：4198
│   ├── flink-connector-kinesis    代码行数：5527   src 代码行数：2332
│   ├── flink-connector-nifi    代码行数：207   src 代码行数：149
│   ├── flink-connector-rabbitmq    代码行数：975   src 代码行数：507
│   ├── flink-connector-twitter    代码行数：120   src 代码行数：120
│   ├── flink-hadoop-compatibility    代码行数：3890   src 代码行数：1803
│   ├── flink-hbase    代码行数：2615   src 代码行数：1675
│   ├── flink-hcatalog    代码行数：336   src 代码行数：336
│   ├── flink-jdbc    代码行数：1695   src 代码行数：869
│   └── flink-orc    代码行数：3309      src  代码行数：1919
├── flink-container
│   ├── docker
│   └── kubernetes
├── flink-contrib   代码行数：421
│   ├── docker-flink
│   ├── flink-connector-wikiedits
│   ├── flink-storm
│   └── flink-storm-examples
├── flink-core       代码行数：80561   src  代码行数：49920
├── flink-dist
├── flink-docs       代码行数：593
├── flink-end-to-end-tests   代码行数：2039
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
├── flink-examples   代码行数：6018
│   ├── flink-examples-batch
│   ├── flink-examples-streaming
│   └── flink-examples-table
├── flink-filesystems   代码行数：7190
│   ├── flink-fs-hadoop-shaded
│   ├── flink-hadoop-fs
│   ├── flink-mapr-fs
│   ├── flink-s3-fs-base
│   ├── flink-s3-fs-hadoop
│   ├── flink-s3-fs-presto
│   └── flink-swift-fs-hadoop
├── flink-formats   代码行数：9187
│   ├── flink-avro
│   ├── flink-avro-confluent-registry
│   ├── flink-json
│   └── flink-parquet
├── flink-fs-tests   代码行数：1592
├── flink-java   代码行数：30106   src 代码行数：14434
├── flink-java8   代码行数：1100     src   代码行数：209
├── flink-kubernetes   代码行数：4210    src    代码行数：2907
├── flink-libraries   Java 代码行数：175695      scala    代码行数：196195
│   ├── flink-cep   代码行数：16412
│   ├── flink-cep-scala
│   ├── flink-gelly   代码行数：24137
│   ├── flink-gelly-examples
│   ├── flink-gelly-scala   scala 代码行数：1764
│   ├── flink-ml   scala 代码行数：6843
│   ├── flink-python   Java 代码行数：2292
│   ├── flink-sql-client   Java 代码行数：7729
│   ├── flink-sql-parser   Java 代码行数：43456
│   ├── flink-streaming-python   Java 代码行数：1156
│   ├── flink-table     scala    代码行数：186623
│   └── flink-table-common   Java 代码行数：10335
├── flink-mesos   代码行数：5543
├── flink-metrics   代码行数：3295
│   ├── flink-metrics-core
│   ├── flink-metrics-datadog
│   ├── flink-metrics-dropwizard
│   ├── flink-metrics-ganglia
│   ├── flink-metrics-graphite
│   ├── flink-metrics-jmx
│   ├── flink-metrics-prometheus
│   ├── flink-metrics-slf4j
│   └── flink-metrics-statsd
├── flink-optimizer   代码行数：25339   src   代码行数：13050
├── flink-queryable-state   代码行数：6276
│   ├── flink-queryable-state-client-java
│   └── flink-queryable-state-runtime
├── flink-quickstart   代码行数：32
│   ├── flink-quickstart-java
│   └── flink-quickstart-scala
├── flink-runtime   代码行数：270253     src   代码行数：137035
├── flink-runtime-web   代码行数：5302
│   └── web-dashboard
├── flink-scala
├── flink-scala-shell
├── flink-service   代码行数：561
├── flink-shaded-curator
├── flink-shaded-hadoop
│   ├── flink-shaded-hadoop2
│   ├── flink-shaded-hadoop2-uber
│   └── flink-shaded-yarn-tests
├── flink-state-backends   代码行数：7992
│   └── flink-statebackend-rocksdb
├── flink-streaming-java   代码行数：76105   src   代码行数：32073
├── flink-streaming-scala
├── flink-test-utils-parent   代码行数：3374
│   ├── flink-test-utils
│   └── flink-test-utils-junit
├── flink-tests   代码行数：37499
├── flink-yarn   代码行数：8897
├── flink-yarn-shuffle   代码行数：85
├── flink-yarn-tests
└── tools
    ├── force-shading
    ├── maven
    └── releasing
```

