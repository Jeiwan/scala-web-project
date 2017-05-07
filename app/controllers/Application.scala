package controllers

import akka.util.Timeout
import java.util.concurrent.TimeUnit
import play.api.libs.json.Json
import scala.concurrent.ExecutionContext.Implicits.global

import akka.actor.ActorSystem
import play.api._
import play.api.mvc._
import services._
import actors.StatsActor
import akka.pattern.ask
import models.CombinedData
import play.api.data.Form
import play.api.data.Forms._

case class UserLoginData(username: String, password: String)

class Application(sunService: SunService,
                  weatherService: WeatherService,
                  actorSystem: ActorSystem,
                  authService: AuthService,
                  userAuthAction: UserAuthAction) extends Controller {
  val userDataForm = Form {
    mapping(
      "username" -> nonEmptyText,
      "password" -> nonEmptyText
    )(UserLoginData.apply)(UserLoginData.unapply)
  }

  def index = Action {
    Ok(views.html.index())
  }

  def data = Action.async {
    implicit val timeout = Timeout(5, TimeUnit.SECONDS)
    val lat = -33.8830
    val lon = 151.2167
    val sunInfoF = sunService.getSunInfo(lat, lon)
    val temperatureF = weatherService.getTemperature(lat, lon)
    val requestsF = (actorSystem.actorSelection(StatsActor.path) ? StatsActor.GetStats).mapTo[Int]
    for {
      sunInfo <- sunInfoF
      temperature <- temperatureF
      requests <- requestsF
    } yield {
      // Ok(views.html.index(sunInfo, temperature, requests))
      Ok(Json.toJson(CombinedData(sunInfo, temperature, requests)))
    }
  }

  def login = Action {
    Ok(views.html.login(None))
  }

  def doLogin = Action(parse.anyContent) { implicit request =>
    userDataForm.bindFromRequest.fold(
      formWithErrors => Ok(views.html.login(Some("Wrong data"))),
      userData => {
        val maybeCookie = authService.login(
          userData.username, userData.password
        )
        maybeCookie match {
          case Some(cookie) =>
            Redirect("/").withCookies(cookie)
          case None =>
            Ok(views.html.login(Some("Login failed")))
        }
      }
    )
  }

  def restricted = userAuthAction { userAuthRequest =>
    Ok(views.html.restricted(userAuthRequest.user))
  }
}
