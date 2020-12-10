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
import models.request.{AuthenticatedCaseRequest, AuthenticatedRequest, OperatorRequest}
import models.{Case, Operator, Permission}
import org.mockito.Mockito.mock
import play.api.i18n.MessagesApi
import play.api.mvc._
import play.api.{Configuration, Environment}
import service.CasesService
import utils.Cases

import scala.concurrent.Future.successful
import scala.concurrent.{ExecutionContext, Future}

class SuccessfulAuthenticatedAction(
  parse: PlayBodyParsers,
  operator: Operator           = Operator("0", Some("name")),
  permissions: Set[Permission] = Set.empty
)(implicit ec: ExecutionContext)
    extends AuthenticatedAction(
      appConfig     = mock(classOf[AppConfig]),
      config        = mock(classOf[Configuration]),
      env           = mock(classOf[Environment]),
      authConnector = mock(classOf[StrideAuthConnector]),
      parse         = parse,
      cc            = mock(classOf[ControllerComponents])
    ) {

  override def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]): Future[Result] =
    block(new AuthenticatedRequest(operator.copy(permissions = permissions), request))
}

class SuccessfulCasePermissionsAction(
  operator: Operator           = Operator("0", Some("name")),
  permissions: Set[Permission] = Set.empty
) extends CheckCasePermissionsAction {
  override def refine[A](request: AuthenticatedCaseRequest[A]): Future[Either[Result, AuthenticatedCaseRequest[A]]] =
    successful(Right(new AuthenticatedCaseRequest(operator.copy(permissions = permissions), request, request.`case`)))
}

class ExistingCaseActionFactory(reference: String, requestCase: Case)
    extends VerifyCaseExistsActionFactory(casesService = mock(classOf[CasesService]))(
      mock(classOf[MessagesApi]),
      mock(classOf[AppConfig])
    ) {

  override def apply(reference: String): ActionRefiner[AuthenticatedRequest, AuthenticatedCaseRequest] =
    new ActionRefiner[AuthenticatedRequest, AuthenticatedCaseRequest] {
      override protected def refine[A](
        request: AuthenticatedRequest[A]
      ): Future[Either[Result, AuthenticatedCaseRequest[A]]] =
        successful(
          Right(
            new AuthenticatedCaseRequest(operator = request.operator, request = request, requestedCase = requestCase)
          )
        )

      override protected def executionContext: ExecutionContext = ExecutionContext.Implicits.global
    }
}

class HaveRightPermissionsActionFactory extends MustHavePermissionActionFactory {

  override def apply[B[C] <: OperatorRequest[_]](permission: Permission): ActionFilter[B] =
    new ActionFilter[B] {
      override protected def filter[A](request: B[A]): Future[Option[Result]] =
        successful(None)

      override protected def executionContext: ExecutionContext = ExecutionContext.Implicits.global
    }
  override def apply[B[C] <: OperatorRequest[_]](ppermissions: Seq[Permission]): ActionFilter[B] =
    new ActionFilter[B] {
      override protected def filter[A](request: B[A]): Future[Option[Result]] =
        successful(None)

      override protected def executionContext: ExecutionContext = ExecutionContext.Implicits.global
    }
}

class SuccessfulRequestActions(
  parse: PlayBodyParsers,
  operator: Operator,
  c: Case           = Cases.btiCaseExample,
  reference: String = "test-reference"
)(implicit ec: ExecutionContext)
    extends RequestActions(
      new SuccessfulCasePermissionsAction(operator),
      new SuccessfulAuthenticatedAction(parse, operator),
      new ExistingCaseActionFactory(reference, c),
      new HaveRightPermissionsActionFactory
    ) {}

class RequestActionsWithPermissions(
  parse: PlayBodyParsers,
  permissions: Set[Permission],
  addViewCasePermission: Boolean = true,
  reference: String              = "test-reference",
  c: Case                        = Cases.btiCaseExample,
  op: Operator                   = Operator("0", Some("name"))
)(implicit ec: ExecutionContext)
    extends RequestActions(
      new SuccessfulCasePermissionsAction(
        operator    = op,
        permissions = if (addViewCasePermission) permissions ++ Set(Permission.VIEW_CASES) else permissions
      ),
      new SuccessfulAuthenticatedAction(
        parse,
        operator    = op,
        permissions = if (addViewCasePermission) permissions ++ Set(Permission.VIEW_CASES) else permissions
      ),
      new ExistingCaseActionFactory(reference, c),
      new MustHavePermissionActionFactory
    ) {}
