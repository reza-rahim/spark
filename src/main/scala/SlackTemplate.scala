import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity

object SlackTemplate extends Serializable {
    val httpClient = HttpClientBuilder.create().build();

    def post(msg: String ) = {
       try {
          val request = new HttpPost("https://hooks.slack.com/services/XXXXX/XXXXX/XXXXXXXXXXXXXXXXXX")
          request.addHeader("content-type", "application/x-www-form-urlencoded");

          val data ="payload={\"channel\": \"#notify\", \"username\": \"webhookbot\" , \"text\": \"" + msg +"\" }"

          val  params =new StringEntity(data,"UTF-8")
          params.setContentType("application/json")

          request.setEntity(params)
 
         val response = httpClient.execute(request)
       } catch {
               case e: Exception => {
                        println("Counldn't send message to Slack")
               }
       }
    }
}
