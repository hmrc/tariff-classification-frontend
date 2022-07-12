package integration

import play.api.libs.ws.WSCookie
import play.api.mvc.{Cookie, Session, SessionCookieBaker}
import play.api.test.Injecting
import uk.gov.hmrc.crypto.PlainText
import uk.gov.hmrc.http.SessionKeys
import uk.gov.hmrc.play.bootstrap.frontend.filters.crypto.SessionCookieCrypto


trait MockSessionCookie extends Injecting {
  this: IntegrationTest =>

  lazy val cookieCrypto: SessionCookieCrypto = inject[SessionCookieCrypto]
  lazy val cookieBaker: SessionCookieBaker = inject[SessionCookieBaker]

  def mockSessionCookie(sessionId: String) = {

    def makeSessionCookie(session: Session): Cookie = {
      val sessionCookie = cookieBaker.encodeAsCookie(session)
      val encryptedValue = cookieCrypto.crypto.encrypt(PlainText(sessionCookie.value))
      sessionCookie.copy(value = encryptedValue.value)
    }

    val mockSession = Session(Map(
      SessionKeys.lastRequestTimestamp -> System.currentTimeMillis().toString,
      SessionKeys.authToken -> "mock-bearer-token",
      SessionKeys.sessionId -> sessionId
    ))

    val cookie = makeSessionCookie(mockSession)

    new WSCookie() {
      override def name: String = cookie.name
      override def value: String = cookie.value
      override def domain: Option[String] = cookie.domain
      override def path: Option[String] = Some(cookie.path)
      override def maxAge: Option[Long] = cookie.maxAge.map(_.toLong)
      override def secure: Boolean = cookie.secure
      override def httpOnly: Boolean = cookie.httpOnly
    }
  }
}