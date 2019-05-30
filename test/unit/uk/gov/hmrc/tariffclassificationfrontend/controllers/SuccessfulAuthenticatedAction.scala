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

import org.mockito.Mockito.mock
import play.api.i18n.MessagesApi
import play.api.mvc.{ActionFilter, ActionRefiner, Request, Result}
import play.api.{Configuration, Environment}
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.connector.StrideAuthConnector
import uk.gov.hmrc.tariffclassificationfrontend.models.Permission.Permission
import uk.gov.hmrc.tariffclassificationfrontend.models.request.{AuthenticatedCaseRequest, AuthenticatedRequest, OperatorRequest}
import uk.gov.hmrc.tariffclassificationfrontend.models.{Case, Operator, Permission}
import uk.gov.hmrc.tariffclassificationfrontend.service.CasesService
import uk.gov.tariffclassificationfrontend.utils.Cases

import scala.concurrent.Future
import scala.concurrent.Future.successful

class SuccessfulAuthenticatedAction(operator: Operator = Operator("0", Some("name")), permissions : Set[Permission] = Set.empty) extends AuthenticatedAction(
  appConfig = mock(classOf[AppConfig]),
  config = mock(classOf[Configuration]),
  env = mock(classOf[Environment]),
  authConnector = mock(classOf[StrideAuthConnector])) {

  override def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]): Future[Result] = {
    block(new AuthenticatedRequest(operator.copy(permissions = permissions), request))
  }
}

class SuccessfulCheckPermissionsAction(operator: Operator = Operator("0", Some("name"))) extends CheckPermissionsAction {
  override def refine[A](request: AuthenticatedCaseRequest[A]): Future[Either[Result, AuthenticatedCaseRequest[A]]] = {
    successful(Right(new AuthenticatedCaseRequest(operator, request, request.`case`)))
  }
}

class ExistingCaseActionFactory(reference: String, requestCase: Case)
  extends VerifyCaseExistsActionFactory(casesService = mock(classOf[CasesService]))(mock(classOf[MessagesApi]), mock(classOf[AppConfig])) {

  override def apply(reference: String): ActionRefiner[AuthenticatedRequest, AuthenticatedCaseRequest] = {
    new ActionRefiner[AuthenticatedRequest, AuthenticatedCaseRequest] {
      override protected def refine[A](request: AuthenticatedRequest[A]): Future[Either[Result, AuthenticatedCaseRequest[A]]] = {
        successful(
          Right(new AuthenticatedCaseRequest(operator = request.operator, request = request, requestedCase = requestCase)
          )
        )
      }
    }
  }
}

class HaveRightPermissionsActionFactory extends MustHavePermissionActionFactory {
  override def apply[B[C] <: OperatorRequest[_]](permission: Permission): ActionFilter[B] = {
    new ActionFilter[B] {
      override protected def filter[A](request: B[A]): Future[Option[Result]] = {
        successful(None)
      }
    }
  }
}


class SuccessfulRequestActions(operator: Operator, c: Case = Cases.btiCaseExample, reference: String = "test-reference")
  extends RequestActions(
    new SuccessfulCheckPermissionsAction(operator),
    new SuccessfulAuthenticatedAction(operator),
    new ExistingCaseActionFactory(reference, c),
    new HaveRightPermissionsActionFactory
  ) {}



class RequestActionsWithPermissions(permissions : Set[Permission], addViewCasePermission: Boolean = true, reference: String = "test-reference",  c: Case = Cases.btiCaseExample)
  extends RequestActions(
    new CheckPermissionsAction,
    new SuccessfulAuthenticatedAction(permissions = if(addViewCasePermission) permissions ++ Set(Permission.VIEW_CASES) else permissions),
    new ExistingCaseActionFactory(reference, c),
    new MustHavePermissionActionFactory
  ) {}