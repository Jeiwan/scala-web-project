package services

import play.api.libs.ws.WSClient
import scala.concurrent.Future
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global

class WeatherService(wsClient: WSClient) {
  def getTemperature(lat: Double, lon: Double): Future[Double] = {
    val apiKey = sys.env("OWM_API_KEY")
    val weatherResponseF = wsClient.url(s"http://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$lon&units=metric&APPID=$apiKey").get()

    weatherResponseF.map { weatherResponse =>
      val weatherJson = weatherResponse.json
      val temperature = (weatherJson \ "main" \ "temp").as[Double]
      temperature
    }
  }
}
