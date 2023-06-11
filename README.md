## My Scala versions for Stream Processing With Apache Flink book

Most of the examples are just slightly modified Scala versions from Java examples from the book [Stream Processing With Apache Flink](https://leanpub.com/streamprocessingwithapacheflink) by Giannis Polyzos, so all credits goes to him.
Also, consider buying the book, it's great!

See the book's repository: https://github.com/polyzos/stream-processing-with-apache-flink

## Setup environment

Flink and Kafka cluster:

```bash
docker-compose up
```

Setup topics:

```bash
./redpanda-setup.sh
```

TODO: Run producers


## Run local example

NOTE! These are not running in Flink cluster, but in local JVM.

```bash
sbt run
```


