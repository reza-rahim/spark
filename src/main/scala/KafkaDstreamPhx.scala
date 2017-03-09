import org.apache.spark.sql.SQLContext
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe

object KafkaDstreamPhx {
   var zkList ="localhost"

   def getStreamContext(conf : SparkConf, interval: Int, kafkaTopic: String , KafkaBrokerList: String) :StreamingContext = {

       val ssc = new StreamingContext(conf, Seconds(interval))
       ssc.sparkContext.setLogLevel("ERROR")

       val kafkaParams = Map[String, Object](
          "bootstrap.servers" -> KafkaBrokerList,
          "key.deserializer" -> classOf[StringDeserializer],
          "value.deserializer" -> classOf[StringDeserializer],
          "group.id" -> "use_a_separate_group_id_for_each_stream",
          "auto.offset.reset" -> "latest",
          "enable.auto.commit" -> (false: java.lang.Boolean)
       )

       val topics = Array(kafkaTopic)

       val stream = KafkaUtils.createDirectStream[String, String](
         ssc,
         PreferConsistent,
         Subscribe[String, String](topics, kafkaParams)
       )

       //messages = stream.map(record => (record.key, record.value))
       val messages = stream.map(message => (message.value))

       var parsedMessages = messages.mapPartitions {
         rows => {
             rows.map { row => Msg.parse(row) }
         }
       }
 
       parsedMessages.foreachRDD( rdd => {
         rdd.foreachPartition(rows =>{
             rows.foreach(row => Phx.insert(row))
             Phx.commit()
         })

       })

       ssc

   }

   def main(args: Array[String]) : Unit = {
      /*
       * Running from sbt console

         val conf = new org.apache.spark.SparkConf().setMaster("local[2]").setAppName("KafkaDstreamPhx"); val ssc =  KafkaDstreamPhx.getStreamContext(conf, 3, "message", "localhost:9092");ssc.start
       */

      val interval=3
      val kafkaTopic= "message" 
      val KafkaBrokerList = args(0)
      zkList = args(1)

      val conf = new SparkConf().setAppName("KafkaDstreamPhx")
      val ssc = KafkaDstreamPhx.getStreamContext(conf, interval, kafkaTopic, KafkaBrokerList) 

      ssc.start
      ssc.awaitTermination
   }
}
