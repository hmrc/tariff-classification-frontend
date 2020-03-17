/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers

import config.AppConfig
import connector.StrideAuthConnector
import javax.inject.{Inject, Singleton}
import models.request.AuthenticatedRequest
import models.{Operator, Permission, Role}
import play.api.mvc.Results._
import play.api.mvc._
import play.api.{Configuration, Environment, Logger}
import uk.gov.hmrc.auth.core.AuthProvider.PrivilegedApplication
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.{Credentials, Name, Retrievals, ~}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter
import uk.gov.hmrc.play.bootstrap.config.AuthRedirects

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuthenticatedAction @Inject()(
  appConfig: AppConfig,
  parse : BodyParsers.Default,
  override val config: Configuration,
  override val env: Environment,
  override val authConnector: StrideAuthConnector,
  cc: ControllerComponents
) extends ActionBuilder[AuthenticatedRequest, AnyContent] with AuthorisedFunctions with AuthRedirects {

  override protected def executionContext: ExecutionContext = global
  override val parser: BodyParser[AnyContent] = parse

  private lazy val teamEnrolment: String = appConfig.teamEnrolment
  private lazy val managerEnrolment: String = appConfig.managerEnrolment
  private lazy val readOnlyEnrolment: String = appConfig.readOnlyEnrolment
  private lazy val checkEnrolment: Boolean = appConfig.checkEnrolment

  private val uncheckedPredicate = AuthProviders(PrivilegedApplication)
  private val checkedPredicate = (Enrolment(teamEnrolment) or Enrolment(managerEnrolment) or Enrolment(readOnlyEnrolment)) and uncheckedPredicate
  private val predicate = if (checkEnrolment) checkedPredicate else uncheckedPredicate

  override def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]): Future[Result] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(
      request.headers,
      Some(request.session)
    )

    authorised(predicate).retrieve(Retrievals.credentials and Retrievals.name and Retrievals.allEnrolments) {
      case (credentials: Credentials) ~ (name: Name) ~ (roles: Enrolments) =>
        Logger.info(s"User Authenticated with id [${credentials.providerId}], roles [${roles.enrolments.map(_.key).mkString(",")}]")
        val id = credentials.providerId
        val role = roles.enrolments.map(_.key) match {
          case e if e.contains(managerEnrolment) => Role.CLASSIFICATION_MANAGER
          case e if e.contains(teamEnrolment) => Role.CLASSIFICATION_OFFICER
          case _ => Role.READ_ONLY
        }
        val operator = Operator(id, name.name, role = role)
        val permittedOperator = operator.copy(
          permissions = Permission.applyingTo(operator)
        )
        block(AuthenticatedRequest(permittedOperator, request))
    } recover {
      case _: NoActiveSession =>

        val protocol = if (request.secure) "https" else "http"
        toStrideLogin(s"${protocol}://${request.host}${request.uri}")

      case e: AuthorisationException =>
        Logger.info("Auth Failed", e)
        Redirect(routes.SecurityController.unauthorized())
    }
  }
}
