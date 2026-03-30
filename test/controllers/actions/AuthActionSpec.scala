/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.actions

import controllers.{ControllerBaseSpec, routes}
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class AuthActionSpec extends ControllerBaseSpec {

  private def unauthorisedLocation =
    Some(routes.SecurityController.unauthorized().url)

  "Auth Action" when {

    "the user hasn't logged in" should {
      "redirect the user to log in " in {
        val result: Future[Result] = handleAuthError(MissingBearerToken())

        status(result)             shouldBe SEE_OTHER
        redirectLocation(result).get should beTheLoginPage
      }
    }

    "the user's session has expired" should {
      "redirect the user to log in " in {
        val result: Future[Result] = handleAuthError(BearerTokenExpired())

        status(result)             shouldBe SEE_OTHER
        redirectLocation(result).get should beTheLoginPage
      }
    }

    "the user's credentials are invalid" should {
      "redirect the user to log in " in {
        val result: Future[Result] = handleAuthError(InvalidBearerToken())

        status(result)             shouldBe SEE_OTHER
        redirectLocation(result).get should beTheLoginPage
      }
    }

    "the user's session cannot be found" should {
      "redirect the user to log in " in {
        val result: Future[Result] = handleAuthError(SessionRecordNotFound())

        status(result)             shouldBe SEE_OTHER
        redirectLocation(result).get should beTheLoginPage
      }
    }

    "the user doesn't have sufficient enrolments" should {
      "redirect the user to the unauthorised page" in {
        val result: Future[Result] = handleAuthError(InsufficientEnrolments())

        status(result)           shouldBe SEE_OTHER
        redirectLocation(result) shouldBe unauthorisedLocation
      }
    }

    "the user doesn't have sufficient confidence level" should {
      "redirect the user to the unauthorised page" in {
        val result: Future[Result] = handleAuthError(InsufficientConfidenceLevel())

        status(result)           shouldBe SEE_OTHER
        redirectLocation(result) shouldBe unauthorisedLocation
      }
    }

    "the user used an unaccepted auth provider" should {
      "redirect the user to the unauthorised page" in {
        val result: Future[Result] = handleAuthError(UnsupportedAuthProvider())

        status(result)           shouldBe SEE_OTHER
        redirectLocation(result) shouldBe unauthorisedLocation
      }
    }

    "the user has an unsupported affinity group" should {
      "redirect the user to the unauthorised page" in {
        val result: Future[Result] = handleAuthError(UnsupportedAffinityGroup())

        status(result)           shouldBe SEE_OTHER
        redirectLocation(result) shouldBe unauthorisedLocation
      }
    }

    "the user has an unsupported credential role" should {
      "redirect the user to the unauthorised page" in {
        val result: Future[Result] = handleAuthError(UnsupportedCredentialRole())

        status(result)           shouldBe SEE_OTHER
        redirectLocation(result) shouldBe unauthorisedLocation
      }
    }

    "internalId" should {
      "be present in IdentifierAction when it is available from AuthConnector" in {
        val result = handleAuth(Some("internalId"), fakeRequest)

        status(result)             shouldBe OK
        await(bodyOf(result)(mat)) shouldBe "internalId"
      }

      "not be present in IdentifierAction when it is not available from AuthConnector" in
        assertThrows[MissingSessionIdException](await(handleAuth(None, fakeRequest)))

    }

    "sessionId" should {
      "be present if internalId is missing" in {
        val result = handleAuth(None, fakeRequestWithSessionId("sessionID"))

        status(result)             shouldBe OK
        await(bodyOf(result)(mat)) shouldBe "sessionID"
      }
    }
  }

  private def beTheLoginPage =
    startWith("/stride/sign-in")

  private def handleAuthError(exc: AuthorisationException): Future[Result] = {
    val authAction = new AuthenticatedIdentifierAction(
      new FakeFailingAuthConnector(exc),
      cc,
      realAppConfig,
      realConfig,
      realEnv
    )
    val controller = new Harness(authAction)
    controller.onPageLoad()(fakeRequest)
  }

  private def handleAuth(internalId: Option[String], request: FakeRequest[AnyContent]): Future[Result] = {
    val authAction = new AuthenticatedIdentifierAction(
      new FakeAuthConnector(internalId),
      cc,
      realAppConfig,
      realConfig,
      realEnv
    )
    val controller = new Harness(authAction)
    controller.onPageLoadWithInternalId()(request)
  }

  private class Harness(authAction: IdentifierAction) extends BaseController {
    def onPageLoad(): Action[AnyContent] = authAction(_ => Ok)

    override protected def controllerComponents: ControllerComponents = cc

    def onPageLoadWithInternalId(): Action[AnyContent] = authAction(request => Ok(request.internalId))
  }

}

class FakeFailingAuthConnector(exceptionToReturn: Throwable) extends AuthConnector {

  override def authorise[A](
    predicate: Predicate,
    retrieval: Retrieval[A]
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[A] =
    Future.failed(exceptionToReturn)

}

class FakeAuthConnector(internalId: Option[String]) extends AuthConnector {

  override def authorise[A](
    predicate: Predicate,
    retrieval: Retrieval[A]
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[A] =
    Future.successful(internalId.asInstanceOf[A])

}
