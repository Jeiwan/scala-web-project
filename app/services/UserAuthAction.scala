package services

import models.User
import play.api.mvc.{ ActionBuilder, Request, Result, Results, WrappedRequest }
import scala.concurrent.Future
import services.AuthService

case class UserAuthRequest[A](user: User, request: Request[A])
  extends WrappedRequest[A](request)

class UserAuthAction(authService: AuthService) extends ActionBuilder[UserAuthRequest] {
  def invokeBlock[A](request: Request[A],
                     block: (UserAuthRequest[A]) => Future[Result]): Future[Result] = {
    val maybeUser = authService.checkCookie(request)
    maybeUser match {
      case None => Future.successful(Results.Redirect("/login"))
      case Some(user) => block(UserAuthRequest(user, request))
    }
  }
}
