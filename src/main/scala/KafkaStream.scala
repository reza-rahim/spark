import org.apache.spark.sql.SQLContext
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe

object KafkaDstreamPhx {

   def getStreamContext(conf : SparkConf, interval: Int, kafkaTopic: String ) :StreamingContext = {

       val ssc = new StreamingContext(conf, Seconds(interval))
       ssc.sparkContext.setLogLevel("ERROR")

       val kafka=scala.util.Properties.envOrElse("KAFKA_BROKER_LIST", "localhost:9092" )
       val kafkaParams = Map[String, Object](
          "bootstrap.servers" -> kafka,
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

         val conf = new org.apache.spark.SparkConf().setMaster("local[2]").setAppName("KafkaDstreamPhx"); val ssc =  KafkaDstreamPhx.getStreamContext(conf, 3, "message");ssc.start

       */

      val interval=scala.util.Properties.envOrElse("BATCH_INTERVAL", "3" ).toInt
      val kafkaTopic=scala.util.Properties.envOrElse("KAFKA_TOPIC", "message" )

      val conf = new SparkConf().setAppName("KafkaDstreamPhx")
      val ssc = KafkaDstreamPhx.getStreamContext(conf, interval, kafkaTopic) 

      ssc.start
      ssc.awaitTermination
   }
}
