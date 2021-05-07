# Epik knowledge graph binlog replayer

Spark job that replay binlog of cn_dbpedia to Nebula Graph database.

## Prerequisites  
Same as [epik-gateway-binlog-encoder](https://github.com/EpiK-Protocol/epik-gateway-binlog-encoder)

1. JDK1.8  
Install java-1.8.0-openjdk-devel for you OS. set JAVA_HOME environment variable. and add $JAVA_HOME/bin to your PATH.

2. Maven3.6+   
Install [apache Maven](http://maven.apache.org/install.html), make M2_HOME environment variable point to it, and add $M2_HOME/bin to your PATH.

3. [Hadoop 3.2.2](https://archive.apache.org/dist/hadoop/common/hadoop-3.2.2/hadoop-3.2.2.tar.gz)  
Setup a [pseudo-distributed](http://hadoop.apache.org/docs/r3.2.2/hadoop-project-dist/hadoop-common/SingleCluster.html#Pseudo-Distributed_Operation) hadoop cluster. Or [full-distributed](http://hadoop.apache.org/docs/r3.2.2/hadoop-project-dist/hadoop-common/SingleCluster.html#Fully-Distributed_Operation) if you have spare resources.
Make HADOOP_HOME environment variable point to where you extracted tar.gz files, and add $HADOOP_HOME/bin and $HADOOP_HOME/sbin to your PATH.  
Start hdfs and yarn services.

4. Spark 3.0.1  
Since we use spark-on-yarn as our distributed computing engine, and use a custom version of Hadoop 3.2.2, we need to build spark from source.  
```bash
curl https://codeload.github.com/apache/spark/tar.gz/refs/tags/v3.0.1 -o spark-3.0.1.tar.gz
tar zxvf spark-3.0.1.tar.gz
cd spark-3.0.1
./dev/make-distribution.sh --name epik --tgz -Phive -Phadoop-3.2 -Dhadoop.version=3.2.2 -Pscala-2.12 -Phive-thriftserver -Pyarn -Pkubernetes -DskipTests -X
```
When done, extract `spark-3.0.1-bin-epik-spark-3.0.1.tgz`, make environment variable SPARK_HOME point to it, and add $SPARK_HOME/bin to your PATH.

5. sbt  
Install scala build tool [sbt](https://www.scala-sbt.org/), need it when building epik-gateway-binlog-encoder spark job.  
```bash
curl https://github.com/sbt/sbt/releases/download/v1.4.8/sbt-1.4.8.tgz 
tar zxvf sbt-1.4.8.tgz  -C /opt/
ln -s /opt/sbt-1.4.8 /opt/sbt
export SBT_HOME=/opt/sbt
export PATH=$SBT_HOME/bin:$PATH
```

## Build java [nebula-client](https://github.com/vesoft-inc/nebula-java)   
```bash
git clone https://github.com/vesoft-inc/nebula-java.git
cd nebula-java
mvn clean install -Dcheckstyle.skip=true -DskipTests -Dgpg.skip -X
```
Current version is 2.0.0-SNAPSHOT(note we disable the annoying checks.)

## Submit [epik-gateway-binlog-encoder](https://github.com/EpiK-Protocol/epik-gateway-binlog-encoder) spark job to spark-on-yarn cluster  
So that we have got binlog files on hdfs already. Or you could download binlogs from epik, and put it on hdfs.

## Build epik-gateway-binlog-replayer spark job  
```bash
git clone https://github.com/EpiK-Protocol/epik-gateway-binlog-replayer.git
cd epik-gateway-binlog-replayer
sbt assembly
```

epik-gateway-binlog-replayer spark job jar file should be generated in:

```bash
epik-gateway-binlog-replayer/target/scala-2.12/epik-gateway-binlog-replayer.jar
```

## Start up [Nebula Graph](https://nebula-graph.io/) using docker-compose and init cn_dbpedia graph database.

1. Install docker and docker-compose, make sure the following cmd works.
```bash
$ docker version
$ docker-compose help
```

2. Clone nebula docker compose git repo, and start up nebula graph database cluster with docker-compose.

```bash
$ git clone https://github.com/vesoft-inc/nebula-docker-compose.git
$ cd nebula-docker-compose/
$ docker-compose up -d
```

Check to see whether nebula graph cluster start up successfully through `docker ps` , make sure the output show something like this:

```bash
# docker ps
CONTAINER ID   IMAGE                               COMMAND                  CREATED        STATUS                PORTS                                                                                                  NAMES
ca439dc5e07e   vesoft/nebula-console:v2-nightly    "/bin/sh"                38 hours ago   Up 38 hours                                                                                                                  nostalgic_heyrovsky
5f1a4681882a   vesoft/nebula-console:v2-nightly    "/bin/sh"                2 days ago     Up 2 days                                                                                                                    fervent_hoover
20b16354febf   vesoft/nebula-console:v2-nightly    "/bin/sh"                3 days ago     Up 3 days                                                                                                                    gallant_mayer
ed103e81797c   vesoft/nebula-console:v2-nightly    "/bin/sh"                3 days ago     Up 3 days                                                                                                                    fervent_visvesvaraya
0317bf1d1bf6   vesoft/nebula-graphd:v2-nightly     "/usr/local/nebula/b…"   3 days ago     Up 2 days (healthy)   0.0.0.0:55020->9669/tcp, 0.0.0.0:49317->19669/tcp, 0.0.0.0:49315->19670/tcp                            nebula-docker-composegit_graphd1_1
e4dfacfd87a9   vesoft/nebula-graphd:v2-nightly     "/usr/local/nebula/b…"   3 days ago     Up 2 days (healthy)   0.0.0.0:55022->9669/tcp, 0.0.0.0:49320->19669/tcp, 0.0.0.0:49319->19670/tcp                            nebula-docker-composegit_graphd2_1
4193243df37b   vesoft/nebula-storaged:v2-nightly   "./bin/nebula-storag…"   3 days ago     Up 2 days (healthy)   9777-9778/tcp, 9780/tcp, 0.0.0.0:49318->9779/tcp, 0.0.0.0:49316->19779/tcp, 0.0.0.0:49313->19780/tcp   nebula-docker-composegit_storaged2_1
fa681b1da10b   vesoft/nebula-storaged:v2-nightly   "./bin/nebula-storag…"   3 days ago     Up 2 days (healthy)   9777-9778/tcp, 9780/tcp, 0.0.0.0:49323->9779/tcp, 0.0.0.0:49322->19779/tcp, 0.0.0.0:49321->19780/tcp   nebula-docker-composegit_storaged0_1
e80dd421c455   vesoft/nebula-storaged:v2-nightly   "./bin/nebula-storag…"   3 days ago     Up 2 days (healthy)   9777-9778/tcp, 9780/tcp, 0.0.0.0:49314->9779/tcp, 0.0.0.0:49312->19779/tcp, 0.0.0.0:49311->19780/tcp   nebula-docker-composegit_storaged1_1
7db545994257   vesoft/nebula-graphd:v2-nightly     "/usr/local/nebula/b…"   3 days ago     Up 2 days (healthy)   0.0.0.0:9669->9669/tcp, 0.0.0.0:49309->19669/tcp, 0.0.0.0:49306->19670/tcp                             nebula-docker-composegit_graphd_1
bcb3ecfc1ac9   vesoft/nebula-metad:v2-nightly      "./bin/nebula-metad …"   3 days ago     Up 2 days (healthy)   9560/tcp, 0.0.0.0:49302->9559/tcp, 0.0.0.0:49301->19559/tcp, 0.0.0.0:49300->19560/tcp                  nebula-docker-composegit_metad1_1
3913c7a64c72   vesoft/nebula-metad:v2-nightly      "./bin/nebula-metad …"   3 days ago     Up 2 days (healthy)   9560/tcp, 0.0.0.0:49308->9559/tcp, 0.0.0.0:49305->19559/tcp, 0.0.0.0:49303->19560/tcp                  nebula-docker-composegit_metad2_1
478942d3bc4c   vesoft/nebula-metad:v2-nightly      "./bin/nebula-metad …"   3 days ago     Up 2 days (healthy)   9560/tcp, 0.0.0.0:49310->9559/tcp, 0.0.0.0:49307->19559/tcp, 0.0.0.0:49304->19560/tcp                  nebula-docker-composegit_metad0_1
```

3. Run nebula-console docker image to restore schema and data, which is dump as ngql.

Run nebula-console container first:

```bash
docker run --rm -ti --network nebula-docker-composegit_nebula-net --entrypoint=/bin/sh vesoft/nebula-console:v2-nightly
```

In nebula-console docker container, run the following to restore graph database schema(must before binlog replay):

```bash
# nebula-console -u user -p password --address=graphd --port=9669
```

This will open a nebula-console, where we can type [ngql](https://docs.nebula-graph.io/2.0/3.ngql-guide/1.nGQL-overview/1.overview/) in:

We chosen partition_num=512, which make full utilization of hardware resources.
```sql
CREATE SPACE IF NOT EXISTS cn_dbpedia(partition_num=512, replica_factor=3, vid_type=INT64);
:sleep 5
USE cn_dbpedia;
CREATE TAG IF NOT EXISTS domain_entity(domain_predicate string);
CREATE EDGE IF NOT EXISTS predicate(domain_predicate string);
```

## Submit epik-gateway-binlog-encoder spark job to spark-on-yarn cluster.
```bash
${SPARK_HOME}/bin/spark-submit --class com.epik.kbgateway.LogFileReplayer --master yarn --deploy-mode cluster --driver-memory 256M --driver-java-options "-Dspark.testing.memory=536870912" --executor-memory 4g  --num-executors 4 --executor-cores 2 /root/epik-gateway-binlog-replayer.jar -d cn_dbpedia -h localhost:55020,localhost:55022,localhost:9669 -b 100 -i /epik_log_output -q /ngql_dump -s 1 -t 10000
```
The main class name is `com.epik.kbgateway.LogFileReplayer`. Note We put epik-gateway-binlog-replayer.jar under /root dir, point to where you put it if it is not the case.

Application options:

option name| example |note
:---:|:---|---
-f | /cn_dbpedia_input/baike_triples.txt | hdfs input file, location of baike_cnpedia.txt
-d | cn_dbpedia | nebula database name. 
-h | localhost:55020,localhost:55022,localhost:9669 | comma separated nebula graphd service socket addresses.
-b | 100 | batch size, how many ngqls in a batch submit to nebula graph.
-q | /ngql_output | ngql dumps, by-product while populating nebula graph.
-s | 1 | how many connection in a nebula Session instance. 
-t | 10000 | connection timeout in milliseconds of nebula.

When this spark job is done, Nebula Graph should be populated with triples decoded from log files. BTW, ngql script file will also be generated in the dir `ngql_output`(option value of `-q`) on hdfs.

### Alternative way of log replaying.
Since ngql scripts have been generated, we could download to local dir, and use the following bash script to replay them on Nebula Graph(PS: _this may take hours_).

```bash
for file in $(ls /ngql_output/part-* | sort)
do
    echo "Replaying ${file}..."
    nebula-console -u user -p password --address=graphd --port=9669 -f $file;
    echo "Replay ${file} Done."
done
```

## How to verify data in Nebula Graph.
You could use nebula-console to verify that binlog have been successfully replayed to nebula graph, note we use "书籍" as a starting vertex, use whatever vertex value you like.
```sql
GO 1 STEPS FROM hash("书籍") OVER predicate BIDIRECT YIELD predicate.domain_predicate;
GET SUBGRAPH 1 STEPS FROM hash("书籍") OUT predicate;
```

## How to build using docker

```bash
#Build a docker image using the `Dockerfile` we provided.
docker build -t epik-gateway-binlog-replayer:1.0 .

#Start a container from that image.
docker run -it epik-gateway-binlog-replayer:1.0 /bin/bash

#checkout the conainter's id from the following output, which is b291aa02aeb3 in our case.
docker container ls -a

# Copy the already-built spark job jar from the container to local.The you can sumbit it to a spark cluster.
docker cp b291aa02aeb3:/root/epik-gateway-binlog-replayer/target/scala-2.12/epik-gateway-binlog-replayer.jar .
```
