import org.scalatestplus.play._
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito._
import play.api.libs.json.Json
import play.api.libs.ws.{WSResponse, WSRequest, WSClient}
import scala.concurrent.Future
import services.SunService
import org.scalatest.concurrent._

class SunServiceSpec extends PlaySpec with MockitoSugar with ScalaFutures {
  "SunService" must {
    "retrieve correct sunset and sunrise information" in {
      val wsClientStub = mock[WSClient]
      val wsRequestStub = mock[WSRequest]
      val wsResponseStub = mock[WSResponse]

      val expectedResponse =
        """{
          "results": {
            "sunrise": "2016-04-14T20:18:12+00:00",
            "sunset": "2016-04-15T07:31:52+00:00"
          }
        }"""
      val jsResult = Json.parse(expectedResponse)

      val lat = -33.8830
      val lon = 151.2167
      val url = s"http://api.sunrise-sunset.org/json?lat=$lat&lng=$lon&formatted=0"

      when(wsResponseStub.json).thenReturn(jsResult)
      when(wsRequestStub.get()).thenReturn(
        Future.successful(wsResponseStub)
      )
      when(wsClientStub.url(url)).thenReturn(wsRequestStub)

      val sunService = new SunService(wsClientStub)
      val resultF = sunService.getSunInfo(lat, lon)

      whenReady(resultF) { res =>
        res.sunrise mustBe "06:18:12"
        res.sunset mustBe "17:31:52"
      }
    }
  }
}
