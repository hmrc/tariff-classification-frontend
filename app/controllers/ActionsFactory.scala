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

import javax.inject.{Inject, Singleton}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Results._
import play.api.mvc.{ActionFilter, ActionRefiner, Result}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter
import config.AppConfig
import models.{Case, Permission}
import models.request.{AuthenticatedCaseRequest, AuthenticatedRequest, OperatorRequest}
import service.CasesService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.Future.successful

@Singleton
class CheckCasePermissionsAction
  extends ActionRefiner[AuthenticatedCaseRequest, AuthenticatedCaseRequest] {

  override protected def refine[A](request: AuthenticatedCaseRequest[A]):
  Future[Either[Result, AuthenticatedCaseRequest[A]]] = {
    println("refine ::::::::::::::")
    println("refine ::::::::::::::")
    println("refine ::::::::::::::" + request.operator)
    successful(
      Right(
        new AuthenticatedCaseRequest(
          operator = request.operator.addPermissions(Permission.applyingTo(request.`case`, request.operator)),
          request = request,
          requestedCase = request.`case`)
      )
    )
  }

  override protected def executionContext: ExecutionContext = global
}

@Singleton
class VerifyCaseExistsActionFactory @Inject()(casesService: CasesService)(implicit val messagesApi: MessagesApi, appConfig: AppConfig) extends I18nSupport {

  def apply(reference: String): ActionRefiner[AuthenticatedRequest, AuthenticatedCaseRequest] =
    new ActionRefiner[AuthenticatedRequest, AuthenticatedCaseRequest] {
      override protected def refine[A](request: AuthenticatedRequest[A]): Future[Either[Result, AuthenticatedCaseRequest[A]]] = {
        implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))
        implicit val r: AuthenticatedRequest[A] = request

        casesService.getOne(reference).flatMap {
          case Some(c: Case) =>{
            println("VerifyCaseExistsActionFactory")
            println("VerifyCaseExistsActionFactory")
            println("VerifyCaseExistsActionFactory" + request.operator)
            successful(
              Right(
                new AuthenticatedCaseRequest(
                  operator = request.operator,
                  request = request,
                  requestedCase = c)
              )
            )
          }

          case _ => successful(Left(NotFound(views.html.case_not_found(reference))))
        }
      }

      override protected def executionContext: ExecutionContext = global
    }
}

@Singleton
class MustHavePermissionActionFactory {

  def apply[B[C] <: OperatorRequest[C]](permission: Permission): ActionFilter[B] =
    new ActionFilter[B] {
      override protected def filter[A](request: B[A]): Future[Option[Result]] = {
        request match {
          case r if r.hasPermission(permission) => successful(None)
          case _ => successful(Some(Redirect(routes.SecurityController.unauthorized())))
        }
      }

      override protected def executionContext: ExecutionContext = global
    }

  def apply[B[C] <: OperatorRequest[C]](permissions: Seq[Permission]): ActionFilter[B] =
    new ActionFilter[B] {
      override protected def filter[A](request: B[A]): Future[Option[Result]] = {
        request match {
          case r if permissions.foldLeft[Boolean](false){_ || r.hasPermission(_)} => successful(None)
          case _ => successful(Some(Redirect(routes.SecurityController.unauthorized())))
        }
      }

      override protected def executionContext: ExecutionContext = global
    }
}