# Pipeline

## Kafka
kubectl  exec -it kafka-0 bash

kafka-topics.sh --create --zookeeper zk-0.zk:2181 --replication-factor 1 --partitions 1 --topic message

### kafka producer
kafka-console-producer.sh --broker-list kafka-0.kafka:9092 --topic message

### kafka consumer
kafka-console-consumer.sh --topic message --from-beginning --zookeeper zk-0.zk:2181


## Phx
kubectl exec -it hm-0 bash

sqlline.py zk-0.zk

CREATE TABLE SENSOR (

FT UNSIGNED_TIME NOT NULl

,ID UNSIGNED_INT NOT null

,M UNSIGNED_INT

CONSTRAINT PK PRIMARY KEY (FT DESC,ID)

) SALT_BUCKETS=2,UPDATE_CACHE_FREQUENCY='NEVER';

## Zeppline

default.driver : org.apache.phoenix.jdbc.PhoenixDriver

default.url : jdbc:phoenix:zk-0.zk:/hbase


## Spark

kubectl exec -it sm-0 bash

git clone https://github.com/reza-rahim/spark.git

cd spark 

sbt console 

val conf = new org.apache.spark.SparkConf().setMaster("local[2]").setAppName("KafkaDstreamPhx"); val ssc =  KafkaDstreamPhx.getStreamContext(conf, 3, "message");ssc.start

/opt/spark/bin/spark-submit --class KafkaDstreamPhx --master spark://sm-0.sm:7077 /path/to/jar
/opt/spark/bin/spark-submit --class KafkaDstreamPhx --master spark://sm-0.sm:7077 -deploy-mode cluster hdfs://nn-0.nn:9000/path/to/jar


/opt/spark/bin/spark-submit --class KafkaDstreamPhx --master spark://sm-0.sm:7077 --jars /opt/phoenix/phoenix-4.9.0-HBase-1.2-client.jar --conf spark.executorEnv.	="zk-0.zk"  /pipline/target/scala-2.11/spark-stream-assembly-0.0.1-SNAPSHOT.jar kafka-0.kafka:9092


## port forwarding

ssh vagrant@10.0.15.10 -L 50070:localhost:50070 -L 16010:localhost:16010 -L 8080:localhost:8080 -L 8081:localhost:8081  -L 4040:localhost:4040 -L 30001:10.0.15.11:30001

kubectl port-forward nn-0 50070:50070 &

kubectl  port-forward hm-0 16010:16010 &

kubectl  port-forward sm-0 8080:8080 &

kubectl port-forward sm-0 4040:4040 &

kubectl  port-forward sw-0 8081:8081 &

## Message
### from kafka client

2017-03-08 13:50:19.365,3,56

### to nodecli

curl -H "Content-Type: application/json" -X POST -d '{"msg":"2017-03-08 13:50:19.365,3,56"}'  http://10.0.15.11:32000/api

### to haproxy
curl -H "Content-Type: application/json" -X POST -d '{"msg":"2017-03-08 13:50:19.365,3,56"}'  http://10.0.15.10/api


