# spark

####
kubectl -s api:8080 exec -it kafka-0 bash
kafka-topics.sh --create --zookeeper zk-0.zk:2181 --replication-factor 1 --partitions 1 --topic message

kubectl -s api:8080 exec -it sm-0 bash

####
kubectl -s api:8080 exec -it hm-0 bash

CREATE TABLE SENSOR (
FT UNSIGNED_TIME NOT NULl
,ID UNSIGNED_INT NOT null
,M UNSIGNED_INT
CONSTRAINT PK PRIMARY KEY (FT DESC,ID)
) SALT_BUCKETS=2,UPDATE_CACHE_FREQUENCY='NEVER';

####

sbt console 

val conf = new org.apache.spark.SparkConf().setMaster("local[2]").setAppName("KafkaDstreamPhx"); val ssc =  KafkaDstreamPhx.getStreamContext(conf, 3, "message");ssc.start


####

curl -H "Content-Type: application/json" -X POST -d '{"msg":"2017-03-08 13:50:19.365,3,56"}'  http://10.0.15.11:32000/api
