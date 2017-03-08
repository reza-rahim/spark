# spark

sbt console 

val conf = new org.apache.spark.SparkConf().setMaster("local[2]").setAppName("KafkaDstreamPhx"); val ssc =  KafkaDstreamPhx.getStreamContext(conf, 3, "message");ssc.start
