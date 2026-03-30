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

import com.google.inject.Inject
import config.AppConfig
import controllers.routes
import models.request.IdentifierRequest
import play.api.mvc.Results.Redirect
import play.api.mvc._
import play.api.{Configuration, Environment, Logging}
import uk.gov.hmrc.auth.core.AuthProvider.PrivilegedApplication
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.http.HeaderCarrier
import controllers.auth.AuthRedirects
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

trait IdentifierAction
    extends ActionBuilder[IdentifierRequest, AnyContent]
    with ActionFunction[Request, IdentifierRequest]

class AuthenticatedIdentifierAction @Inject() (
  override val authConnector: AuthConnector,
  cc: ControllerComponents,
  appConfig: AppConfig,
  override val config: Configuration,
  override val env: Environment
)(implicit ec: ExecutionContext)
    extends IdentifierAction
    with AuthorisedFunctions
    with AuthRedirects
    with Logging {

  override val parser: BodyParser[AnyContent]               = cc.parsers.defaultBodyParser
  override protected val executionContext: ExecutionContext = cc.executionContext

  private def getSessionFromHeaderCarrier(hc: HeaderCarrier): String =
    hc.sessionId match {
      case Some(value) =>
        value.value
      case _ =>
        throw new MissingSessionIdException("Unable to retrieve session ID")
    }

  private def authorise(): AuthorisedFunction = authorised(AuthProviders(PrivilegedApplication))

  override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] = {

    implicit val hc: HeaderCarrier =
      HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    authorise().retrieve(Retrievals.internalId) {
      case Some(internalId: String) =>
        block(IdentifierRequest(request, internalId))
      case _ =>
        block(IdentifierRequest(request, getSessionFromHeaderCarrier(hc)))
    } recover {
      case _: NoActiveSession =>
        toStrideLogin(
          if (appConfig.runningAsDev) s"http://${request.host}${request.uri}"
          else s"${request.uri}"
        )
      case e: AuthorisationException =>
        logger.info("[AuthenticatedIdentifierAction][invokeBlock] Auth Failed", e)
        Redirect(routes.SecurityController.unauthorized())
    }
  }
}
