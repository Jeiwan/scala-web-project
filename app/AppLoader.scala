import scala.concurrent.Future

import actors._
import actors.StatsActor.Ping
import akka.actor.Props
import com.softwaremill.macwire._
import controllers.{Application, Assets}
import filters._
import play.api._
import play.api.ApplicationLoader.Context
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.db.evolutions.{EvolutionsComponents, DynamicEvolutions}
import play.api.db.DBComponents
import play.api.db.HikariCPComponents
import play.api.mvc.Filter
import play.api.routing.Router
import play.api.cache.EhCacheComponents
import router.Routes
import services._
import scalikejdbc.config.DBs

class AppApplicationLoader extends ApplicationLoader {
  def load(context: Context) = {
    LoggerConfigurator(context.environment.classLoader).foreach { configurator =>
      configurator.configure(context.environment)
    }
    (new BuiltInComponentsFromContext(context) with AppComponents).application
  }
}

trait AppComponents extends BuiltInComponents with AhcWSComponents
    with EvolutionsComponents with DBComponents with HikariCPComponents
    with EhCacheComponents {
  lazy val assets: Assets = wire[Assets]
  lazy val prefix: String = "/"
  lazy val router: Router = wire[Routes]
  lazy val applicationController = wire[Application]
  lazy val sunService = wire[SunService]
  lazy val weatherService = wire[WeatherService]
  lazy val statsFilter: Filter = wire[StatsFilter]
  lazy val statsActor = actorSystem.actorOf(
    Props(wire[StatsActor]), StatsActor.name
  )
  lazy val dynamicEvolutions = new DynamicEvolutions
  lazy val authService = new AuthService(defaultCacheApi)
  lazy val userAuthAction = wire[UserAuthAction]
  override lazy val httpFilters = Seq(statsFilter)

  applicationLifecycle.addStopHook { () =>
    Logger.info("The app is about to stop!")
    DBs.closeAll()
    Future.successful(Unit)
  }

  val onStart = {
    Logger.info("The app is about to start!")
    applicationEvolutions
    DBs.setupAll()
    statsActor ! Ping
  }
}
