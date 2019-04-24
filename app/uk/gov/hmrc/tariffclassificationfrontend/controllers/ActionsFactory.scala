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
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Results._
import play.api.mvc.{ActionFilter, ActionRefiner, Result}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.models.Permission.Permission
import uk.gov.hmrc.tariffclassificationfrontend.models.request.AuthenticatedRequest
import uk.gov.hmrc.tariffclassificationfrontend.models.{Case, Permission}
import uk.gov.hmrc.tariffclassificationfrontend.service.CasesService
import uk.gov.hmrc.tariffclassificationfrontend.views

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful

@Singleton
class CheckPermissionsAction
  extends ActionRefiner[AuthenticatedRequest, AuthenticatedRequest] {

  override protected def refine[A](request: AuthenticatedRequest[A]):
  Future[Either[Result, AuthenticatedRequest[A]]] = {

    if (request.c.exists(_.isAssignedTo(request.operator))) {
      // add case owner permissions
      authCaseRequest(request, Permission.teamCaseOwnerPermissions)
    } else {
      // nothing extra to add
      authCaseRequest(request, Set.empty)
    }
  }

  //TODO: try to remove his function
  private def authCaseRequest[A](request: AuthenticatedRequest[A], additionalPermissions: Set[Permission]) = {
    successful(Right(new AuthenticatedRequest(
      operator = request.operator.addPermissions(additionalPermissions),
      request = request,
      c = request.c)))
  }
}

@Singleton
class VerifyCaseExistsActionFactory @Inject()(casesService: CasesService)(implicit val messagesApi: MessagesApi, appConfig: AppConfig) extends I18nSupport {

  def apply(reference: String): ActionRefiner[AuthenticatedRequest, AuthenticatedRequest] =
    new ActionRefiner[AuthenticatedRequest, AuthenticatedRequest] {
      override protected def refine[A](request: AuthenticatedRequest[A]): Future[Either[Result, AuthenticatedRequest[A]]] = {
        implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))
        implicit val r: AuthenticatedRequest[A] = request

        casesService.getOne(reference).flatMap {
          case Some(c: Case) =>
            successful(
              Right(
                new AuthenticatedRequest(
                  operator = request.operator,
                  request = request,
                  c = Some(c))
              )
            )
          case _ => successful(Left(NotFound(views.html.case_not_found(reference))))
        }
      }
    }
}

@Singleton
class MustHavePermissionActionFactory {

  def apply(permission: Permission): ActionFilter[AuthenticatedRequest] =
    new ActionFilter[AuthenticatedRequest] {
      override protected def filter[A](request: AuthenticatedRequest[A]): Future[Option[Result]] = {
        request.operator match {
          case o if o.hasPermission(permission) => successful(None)
          case _ => successful(Some(Redirect(routes.SecurityController.unauthorized())))
        }
      }
    }
}
