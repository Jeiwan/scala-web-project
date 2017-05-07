package services

import play.api.libs.ws.WSClient
import play.api.Play.current
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import org.joda.time.{DateTime, DateTimeZone}
import org.joda.time.format.DateTimeFormat
import models.SunInfo

class SunService(wsClient: WSClient) {
  def getSunInfo(lat: Double, lon: Double): Future[SunInfo] = {
    val responseF = wsClient.url(s"http://api.sunrise-sunset.org/json?lat=$lat&lng=$lon&formatted=0").get()
    responseF.map { response =>
      val json = response.json
      val sunriseTimeStr = (json \ "results" \ "sunrise").as[String]
      val sunsetTimeStr = (json \ "results" \ "sunset").as[String]
      val sunriseTime = DateTime.parse(sunriseTimeStr)
      val sunsetTime = DateTime.parse(sunsetTimeStr)
      val formatter = DateTimeFormat.forPattern("HH:mm:ss").
        withZone(DateTimeZone.forID("Australia/Sydney"))
      val sunInfo = SunInfo(formatter.print(sunriseTime), formatter.print(sunsetTime))
      sunInfo
    }
  }
}
