package filters

import akka.actor.ActorSystem
import play.api.mvc.{Filter, RequestHeader, Result}
import concurrent.Future
import play.api.Logger
import akka.stream.Materializer
import actors.StatsActor

class StatsFilter(actorSystem: ActorSystem,
  implicit val mat: Materializer) extends Filter {
  override def apply(nextFilter: (RequestHeader) => Future[Result])
                    (header: RequestHeader): Future[Result] = {
    Logger.info(s"Serving another request: ${header.path}")
    actorSystem.actorSelection(StatsActor.path) ! StatsActor.RequestReceived
    nextFilter(header)
  }
}
