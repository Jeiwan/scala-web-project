package services

import play.api.mvc.{ Cookie, RequestHeader }
import java.util.concurrent.TimeUnit
import org.apache.commons.codec.binary.Base64
import java.util.UUID
import java.security.MessageDigest
import play.api.cache.CacheApi
import concurrent.duration.Duration
import models.User
import scalikejdbc._
import org.mindrot.jbcrypt.BCrypt

class AuthService(cacheApi: CacheApi) {
  val mda = MessageDigest.getInstance("SHA-512")
  val cookieHeader = "X-Auth-Token"

  def login(userCode: String, password: String): Option[Cookie] = {
    for {
      user <- checkUser(userCode, password)
      cookie <- Some(createCookie(user))
    } yield {
      cookie
    }
  }

  def checkCookie(header: RequestHeader): Option[User] = {
    for {
      cookie <- header.cookies.get(cookieHeader)
      user <- cacheApi.get[User](cookie.value)
    } yield {
      user
    }
  }

  private def checkUser(userCode: String, password: String): Option[User] = {
    DB.readOnly { implicit session =>
      val maybeUser = sql"SELECT * FROM users WHERE user_code = $userCode".map(User.fromRS).single().apply()
      maybeUser.flatMap { user =>
        if (BCrypt.checkpw(password, user.password)) {
          Some(user)
        } else None
      }
    }
  }

  private def createCookie(user: User): Cookie = {
    val randomPart = UUID.randomUUID().toString.toUpperCase
    val userPart = user.userId.toString.toUpperCase()
    val key = s"$randomPart|$userPart"
    val duration = Duration.create(10, TimeUnit.HOURS)
    val token = Base64.encodeBase64String(mda.digest(key.getBytes))
    cacheApi.set(token, user, duration)
    Cookie(cookieHeader, token, maxAge = Some(duration.toSeconds.toInt))
  }
}
