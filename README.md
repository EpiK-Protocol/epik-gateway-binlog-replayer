# Epik knowledge graph binlog replayer

Spark job that replay [bin-log](https://github.com/EpiK-Protocol/epik-gateway-binlog-encoder/blob/master/binlog.md) of cn_dbpedia to Nebula Graph database.

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

## Log format

Basically every Log is composed of 2 components: 

![Log_format](images/log_format.png)
1. _Mode_ , a bitMap, used to interpret the _Payload_, different bit combination indicate different log type.
2. _Payload_, the actual log content.

_Mode_ is 1 byte long, which is divided into 4 subsections from lowest to the highest bit.
1. The lowest 2 bits are Add/Update/Remove mode.
2. The next 2 bits are GraphDB/Tag/Edge mode.
3. The next 1 bit is Schema/Data(instances) mode.
4. The highest 3 bits are reserved for later usage.

![bitmap_mode](images/bitmap_mode.png)

Combination of those bits, can form any of the following logs.
1. GraphDB/Tag/Edge's Schema Add/Update/Remove Log.
2. Tag/Edge's Data(instances) Add/Update/Remove Log.


### GraphDB Schema Log
GraphDB Schema Log is used to manipulate graph space's defination in schema.
![schema_db](images/schema_db.png)

In order to support multi knowledge graphs, GraphDB is introduced to support conceptual and physical data organization, which has a counterpart in Relational Database Management System(RDBMS).
GraphDB's naming convention follows the identifier naming convention of typical programming language such as c/c++, which should be composed of alphabet、digit、underscore and $, and should not starts with 
digit. GraphDB names that starts with underscore or $ are reserved for internal usage. Since GraphDB's name can be of variable length, name's size should be encoded. 
We use a [Nebula Graph](https://nebula-graph.io/) as our backend graph database. Nebula Graph is a distributed graph database, __partition number__ and __replication factor__ should be specified when creating a graphspace.
which could not be altered after creation., so in order to harvest the power when scale-out, a modest partition number should be used, 1024 or higher is recommended.
Replication is used to maintain fault-tolerance. Graph database should be functional under at most minority of all replicas fails.
But replication has its drawback too, it will cause storage-bloat when set too high, an odd small number such as 3、5 or 7 is recommended, the most common setting is 3.


### Tag Schema Log
Tag Schema Log is used to manipulate Tag's defination in schema.
![schema_tag](images/schema_tag.png)
In order to support multi-graphdb, __GraphDB Name__ and __GraphDB Name Size__ is added as prefix to indicate which graphdb this Tag Schema Log belongs to.
Although usually graph database maintain a internal GraphDB id to uniquely identify it, it also maintains the relationship between GraphDB Name and GraphDB id.
But we could not know the GraphDB id before it actually created in graph database, and got assigned its GraphDB id, we should make our log self-contained.
so we put <GraphDB Name Size，GraphDB Name>, a tuple2 composite field in every log to distinguish which graphdb it belongs to.

Tag is a label which assign to a Vertex, which may usually represent a `concept` a vertex has, is a logical group of properties.
And Vertex is conceptually a tuple2 of <VID,TagList>, which VID is a integer assigned by Nebula Graph to uniquely identify a vertex internally, and TagList is a collection of Tags, which indicates that a vertex can have many concepts.


A Tag is conceptually a tuple2 of <TagName, PropertiesList>, which TagName is Tag' name(variable length, so its name's size should be encoded inside), and PropertiesList is a collection of Property, which defined as:
1.	Property Name Size: length of property name.
2.	Property Name: name of the property.
3.	Property Type：support common data type, including: integer、double、string and timestamp. When of type string, since it is variable length, an extra size field will be encoded to mark its boundary.
4.  HasDefaultValue: whether has default value. when in schema definition and HasDefaultValue=1, then the following 2 fields: __Property Default Value Size__ and __Property Default Value__ must be provided. when in data definition, HasDefaultValue is meaningless, keeping it is just to keep format consistent,
5.	Property [Default/Actual] Value Size: value of the property according to Property Type.
6.	Property [Default/Actual] Value: value of the property according to Property Type.
In order to support __PropertiesList__, an extras __Properties Szie__ field to count how many properties a Tag has.


### Edge Schema Log
Edge Schema Log is used to manipulate Edge's defination in schema. 
![schema_edge](images/schema_edge.png)
It bears much resemblance with Tag Schema Log. The only different is that we change __Tag Name Size__ to __Edge Name Size__, and change __Tag Name__ to __Edge Name__.

###	Vertex Data Log
Vertex Data Log is used to manipulate vertex's data.
![data_vertex](images/data_vertex.png)
As mentioned in __Tag Schema Log__，Vertex is conceptually a tuple2 of <VID, TagList>.
Nebula Graph make a conversion from domain values to integer vertex(VID) id to reduce to maintenance overhead. Nebula Graph also expose native functions to do for application developments.
such as __Hash__ or __Fixed-size String__. We use __Hash__ function, but it is our responsibility to make sure that every domain entity(business entity) is unique.
Vertex Data Log is composed of the following:
1. Vertex Domain Value Size: value's size of the domain entity,to mark its boundary.
2. Vertex Domain Value： domain entity's value，must be of string type.
3. VID Generator Type: the function we used to make the domain entity to VID conversion. we use __Hash__.
4. TagList: a collection of Tag，a Tag is tuple6 of<Property Name Size, Property Name, Property Type, HasDefaultValue, Property Value Size, Property Value>, which every __Size__ field is used to mark the boundary of the corresponding __Value__ field. 

### Edge Data Log
__Edge Data Log__ is used to manipulate edge's data.
![data_edge](images/data_edge.png)
It bears much resemblance with __Vertex Data Log__ with minor differences:
1.	Edge is connected by a __From Vertex__ and a __To Vertex__, which require __From Vertex Domain Value__ and __To Vertex Domain Value__，and corresponding Value Sizes fields.
2.	Edge does not support multi-Tag，so does not need introduce filed designed for multi-Tags，Edge's properties just lay one after another，every property is tuple6 of <Property Name Size, Property Name, Property Type, HasDefaultValue, Property Value Size, Property Value>.
