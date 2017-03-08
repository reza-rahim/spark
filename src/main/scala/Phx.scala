import org.apache.spark.sql.SQLContext
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe

import java.sql.DriverManager
import java.sql.Connection

object Phx extends Serializable {
   Class.forName("org.apache.phoenix.jdbc.PhoenixDriver")
   val zk = scala.util.Properties.envOrElse("ZK", "localhost" );
   val connection:Connection=DriverManager.getConnection(s"jdbc:phoenix:$zk");
   val sql = "UPSERT INTO SENSOR (FT, ID, M) VALUES (to_date(?, 'ssssssssssSSS'), ?, ?)"

   def insert( option: Option[Msg])={
        option.map{ row =>
                     val statement = Phx.connection.prepareStatement(Phx.sql)
                     statement.setString(1,row.date.toString)
                     statement.setInt(2,row.id)
                     statement.setInt(3,row.msg)
                     statement.executeUpdate()

                  }
   }

   def commit()={  connection.commit() }
}
