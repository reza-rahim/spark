import org.apache.spark.sql.SQLContext
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe

case class Msg ( date:Long, id: Int, msg: Int )

object Msg extends Serializable {
   val dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
   {
      println("Initializing ... Msg object")
   }
   def parse(st:String): Option[Msg] = {
      val matchmsg = "(.+),(.+),(.+)".r
      val msg = st match {
         case matchmsg(date,id,msg)=> {
            try{
               //println(s"good message $st")
               Some(Msg(Msg.dateFormat.parse(date).getTime,id.toInt,msg.toInt)) ;
            } catch {
               case e: Exception => {
                                      //println(s"bad message $st")
                                      None
				    }
            }
         }
            case _ => None
      }
      return msg
   }
}
