/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.tariffclassificationfrontend.controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc.Results._
import play.api.mvc.{ActionBuilder, Request, Result}
import play.api.{Configuration, Environment}
import uk.gov.hmrc.auth.core.AuthProvider.PrivilegedApplication
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.{Credentials, Name, Retrievals, ~}
import uk.gov.hmrc.auth.core.syntax.retrieved
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter
import uk.gov.hmrc.play.bootstrap.config.AuthRedirects
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.connector.StrideAuthConnector
import uk.gov.hmrc.tariffclassificationfrontend.models.Operator
import uk.gov.hmrc.tariffclassificationfrontend.models.request.AuthenticatedRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class AuthenticatedAction @Inject()(appConfig: AppConfig,
                                    override val config: Configuration,
                                    override val env: Environment,
                                    override val authConnector: StrideAuthConnector)
  extends ActionBuilder[AuthenticatedRequest]
    with AuthorisedFunctions
    with AuthRedirects {

  private lazy val enrolment: Option[Enrolment] = appConfig.authEnrolment.map(Enrolment(_))

  override def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]): Future[Result] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(
      request.headers,
      Some(request.session)
    )
    authorise().retrieve(Retrievals.credentials and Retrievals.name) { retrieved: Credentials ~ Name =>
      val id = retrieved.a.providerId
      val name = retrieved.b.name
      block(AuthenticatedRequest(Operator(id, name), request))
    } recover {
      case _: NoActiveSession => toStrideLogin(
        if (appConfig.runningAsDev) s"http://${request.host}${request.uri}"
        else s"${request.uri}"
      )
      case _: AuthorisationException => Redirect(routes.SecurityController.unauthorized())
    }

  }

  private def authorise(): AuthorisedFunction = {
    enrolment match {
      case Some(e) => authorised(e and AuthProviders(PrivilegedApplication))
      case _ => authorised(AuthProviders(PrivilegedApplication))
    }
  }

}
