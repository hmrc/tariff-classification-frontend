/*
 * Copyright 2024 HM Revenue & Customs
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
import connector.{BindingTariffClassificationConnector, StrideAuthConnector}
import controllers.auth.AuthRedirects
import models.request.AuthenticatedRequest
import models.{Operator, Permission, Role}
import play.api.mvc.Results._
import play.api.mvc._
import play.api.{Configuration, Environment, Logging}
import uk.gov.hmrc.auth.core.AuthProvider.PrivilegedApplication
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuthenticatedAction @Inject() (
  appConfig: AppConfig,
  parse: PlayBodyParsers,
  override val config: Configuration,
  override val env: Environment,
  override val authConnector: StrideAuthConnector,
  userConnector: BindingTariffClassificationConnector
)(override implicit val executionContext: ExecutionContext)
    extends ActionBuilder[AuthenticatedRequest, AnyContent]
    with AuthorisedFunctions
    with AuthRedirects
    with Logging {

  override val parser: BodyParser[AnyContent] = parse.default

  private lazy val teamEnrolment: String     = appConfig.teamEnrolment
  private lazy val managerEnrolment: String  = appConfig.managerEnrolment
  private lazy val readOnlyEnrolment: String = appConfig.readOnlyEnrolment
  private lazy val checkEnrolment: Boolean   = appConfig.checkEnrolment

  private val uncheckedPredicate = AuthProviders(PrivilegedApplication)
  private val checkedPredicate =
    (Enrolment(teamEnrolment) or Enrolment(managerEnrolment) or Enrolment(readOnlyEnrolment)) and uncheckedPredicate
  private val predicate = if (checkEnrolment) checkedPredicate else uncheckedPredicate

  override def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]): Future[Result] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(
      request,
      request.session
    )

    authorised(predicate).retrieve(
      Retrievals.credentials and Retrievals.name and Retrievals.email and Retrievals.allEnrolments
    ) {
      case Some(credentials) ~ name ~ email ~ roles =>
        val pid = credentials.providerId

        val role = roles.enrolments.map(_.key) match {
          case e if e.contains(managerEnrolment) => Role.CLASSIFICATION_MANAGER
          case e if e.contains(teamEnrolment)    => Role.CLASSIFICATION_OFFICER
          case _                                 => Role.READ_ONLY
        }

        val userFromAuth = Operator(pid, name.flatMap(_.name), email, role)

        for {
          userWithTeams <- userConnector.getUserDetails(pid)
          updatedUser <- userWithTeams match {
                           case None =>
                             userConnector.createUser(userFromAuth)
                           case Some(existingUser) =>
                             if (existingUser.withoutTeams != userFromAuth) {
                               userConnector.updateUser(userFromAuth.withTeamsFrom(existingUser))
                             } else {
                               Future(existingUser)
                             }
                         }

          permittedUser = updatedUser.copy(permissions = Permission.applyingTo(updatedUser))
          result <- block(AuthenticatedRequest(permittedUser, request))
        } yield result
      case _ =>
        throw InternalError("Unable to retrieve user credentials")
    } recover {
      case _: NoActiveSession =>
        toStrideLogin(
          if (appConfig.runningAsDev) s"http://${request.host}${request.uri}"
          else s"${request.uri}"
        )
      case e: AuthorisationException =>
        logger.info("Auth Failed", e)
        Redirect(routes.SecurityController.unauthorized())
    }
  }
}
