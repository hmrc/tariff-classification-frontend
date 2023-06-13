/*
 * Copyright 2023 HM Revenue & Customs
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
import connector.DataCacheConnector
import models.request._
import models.{Case, Permission, UserAnswers}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Results._
import play.api.mvc.{ActionFilter, ActionRefiner, Result}
import service.CasesService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future.successful
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CheckCasePermissionsAction @Inject() (override implicit val executionContext: ExecutionContext)
    extends ActionRefiner[AuthenticatedCaseRequest, AuthenticatedCaseRequest] {

  override protected def refine[A](
    request: AuthenticatedCaseRequest[A]
  ): Future[Either[Result, AuthenticatedCaseRequest[A]]] =
    successful(
      Right(
        new AuthenticatedCaseRequest(
          operator      = request.operator.addPermissions(Permission.applyingTo(request.`case`, request.operator)),
          request       = request,
          requestedCase = request.`case`
        )
      )
    )
}

@Singleton
class VerifyCaseExistsActionFactory @Inject() (casesService: CasesService)(
  implicit val messagesApi: MessagesApi,
  appConfig: AppConfig,
  val case_not_found: views.html.case_not_found,
  implicit val ec: ExecutionContext
) extends I18nSupport {

  def apply(reference: String): ActionRefiner[AuthenticatedRequest, AuthenticatedCaseRequest] =
    new ActionRefiner[AuthenticatedRequest, AuthenticatedCaseRequest] {
      override protected def refine[A](
        request: AuthenticatedRequest[A]
      ): Future[Either[Result, AuthenticatedCaseRequest[A]]] = {
        implicit val hc: HeaderCarrier =
          HeaderCarrierConverter.fromRequestAndSession(request, request.session)
        implicit val r: AuthenticatedRequest[A] = request

        casesService.getOne(reference).flatMap {
          case Some(c: Case) =>
            successful(
              Right(
                new AuthenticatedCaseRequest(operator = request.operator, request = request, requestedCase = c)
              )
            )

          case _ => successful(Left(NotFound(case_not_found(reference))))
        }
      }

      override protected def executionContext: ExecutionContext = ec
    }
}

@Singleton
class MustHavePermissionActionFactory @Inject() (implicit ec: ExecutionContext) {

  def apply[B[C] <: OperatorRequest[C]](permission: Permission): ActionFilter[B] =
    new ActionFilter[B] {
      override protected def filter[A](request: B[A]): Future[Option[Result]] =
        request match {
          case r if r.hasPermission(permission) => successful(None)
          case _                                => successful(Some(Redirect(routes.SecurityController.unauthorized())))
        }

      override protected def executionContext: ExecutionContext = ec
    }

  def apply[B[C] <: OperatorRequest[C]](permissions: Seq[Permission]): ActionFilter[B] =
    new ActionFilter[B] {
      override protected def filter[A](request: B[A]): Future[Option[Result]] =
        request match {
          case r if permissions.foldLeft[Boolean](false)(_ || r.hasPermission(_)) => successful(None)
          case _                                                                  => successful(Some(Redirect(routes.SecurityController.unauthorized())))
        }

      override protected def executionContext: ExecutionContext = ec
    }
}

@Singleton
class RequireDataActionFactory @Inject() (
  dataCacheConnector: DataCacheConnector
)(implicit ec: ExecutionContext) {
  def apply[B[C] <: OperatorRequest[C]](cacheKey: String): ActionRefiner[B, AuthenticatedDataRequest] =
    new ActionRefiner[B, AuthenticatedDataRequest] {
      override protected def refine[A](
        request: B[A]
      ): Future[Either[Result, AuthenticatedDataRequest[A]]] =
        dataCacheConnector.fetch(cacheKey).map {
          case Some(cacheMap) => Right(new AuthenticatedDataRequest(request.operator, request, UserAnswers(cacheMap)))
          case None           => Left(Redirect(routes.SecurityController.unauthorized()))
        }

      override protected def executionContext: ExecutionContext = ec
    }
}

@Singleton
class RequireCaseDataActionFactory @Inject() (
  casesService: CasesService,
  dataCacheConnector: DataCacheConnector,
  val case_not_found: views.html.case_not_found
)(
  implicit
  val messagesApi: MessagesApi,
  appConfig: AppConfig,
  ec: ExecutionContext
) extends I18nSupport {
  def apply[B[C] <: AuthenticatedRequest[C]](
    reference: String,
    cacheKey: String
  ): ActionRefiner[B, AuthenticatedCaseDataRequest] =
    new ActionRefiner[B, AuthenticatedCaseDataRequest] {
      override protected def refine[A](
        request: B[A]
      ): Future[Either[Result, AuthenticatedCaseDataRequest[A]]] = {
        implicit val hc: HeaderCarrier =
          HeaderCarrierConverter.fromRequestAndSession(request, request.session)
        implicit val authenticatedRequest: AuthenticatedRequest[_] = request

        casesService.getOne(reference).flatMap {
          case Some(cse) =>
            dataCacheConnector.fetch(cacheKey).map {
              case Some(cacheMap) =>
                Right(new AuthenticatedCaseDataRequest(request.operator, request, cse, UserAnswers(cacheMap)))
              case None =>
                Left(Redirect(routes.SecurityController.unauthorized()))
            }
          case None =>
            successful(Left(NotFound(case_not_found(reference))))
        }
      }

      override protected def executionContext: ExecutionContext = ec
    }
}
