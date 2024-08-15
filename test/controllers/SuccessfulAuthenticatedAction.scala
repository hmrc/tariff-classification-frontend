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
import models.request._
import models.{Case, Operator, Permission, UserAnswers}
import org.mockito.Mockito.mock
import play.api.i18n.MessagesApi
import play.api.mvc.Results.Redirect
import play.api.mvc._
import play.api.{Configuration, Environment}
import service.{CasesService, FakeDataCacheService}
import utils.Cases
import views.html.case_not_found

import javax.inject.Inject
import scala.concurrent.Future.successful
import scala.concurrent.{ExecutionContext, Future}

class SuccessfulAuthenticatedAction(
  parse: PlayBodyParsers,
  operator: Operator = Operator("0", Some("name")),
  permissions: Set[Permission] = Set.empty
)(implicit ec: ExecutionContext)
    extends AuthenticatedAction(
      appConfig = mock(classOf[AppConfig]),
      config = mock(classOf[Configuration]),
      env = mock(classOf[Environment]),
      authConnector = mock(classOf[StrideAuthConnector]),
      parse = parse,
      userConnector = mock(classOf[BindingTariffClassificationConnector])
    ) {

  override def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]): Future[Result] =
    block(new AuthenticatedRequest(operator.copy(permissions = permissions), request))
}

class SuccessfulCasePermissionsAction(
  operator: Operator = Operator("0", Some("name")),
  permissions: Set[Permission] = Set.empty
)(implicit val ec: ExecutionContext)
    extends CheckCasePermissionsAction {
  override def refine[A](request: AuthenticatedCaseRequest[A]): Future[Either[Result, AuthenticatedCaseRequest[A]]] =
    successful(Right(new AuthenticatedCaseRequest(operator.copy(permissions = permissions), request, request.`case`)))
}

class ExistingCaseActionFactory(requestCase: Case)
    extends VerifyCaseExistsActionFactory(casesService = mock(classOf[CasesService]))(
      mock(classOf[MessagesApi]),
      case_not_found = mock(classOf[case_not_found]),
      mock(classOf[ExecutionContext])
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

class HaveRightPermissionsActionFactory @Inject() (implicit ec: ExecutionContext)
    extends MustHavePermissionActionFactory {

  override def apply[B[C] <: OperatorRequest[_]](permission: Permission): ActionFilter[B] =
    new ActionFilter[B] {
      override protected def filter[A](request: B[A]): Future[Option[Result]] =
        successful(None)

      override protected def executionContext: ExecutionContext = ec
    }
  override def apply[B[C] <: OperatorRequest[_]](permissions: Seq[Permission]): ActionFilter[B] =
    new ActionFilter[B] {
      override protected def filter[A](request: B[A]): Future[Option[Result]] =
        successful(None)

      override protected def executionContext: ExecutionContext = ec
    }
}

class MustHaveDataActionFactory @Inject() (userAnswers: UserAnswers)(implicit ec: ExecutionContext)
    extends RequireDataActionFactory(dataCacheService = FakeDataCacheService) {
  override def apply[B[C] <: OperatorRequest[C]](cacheKey: String): ActionRefiner[B, AuthenticatedDataRequest] =
    new ActionRefiner[B, AuthenticatedDataRequest] {
      override protected def refine[A](
        request: B[A]
      ): Future[Either[Result, AuthenticatedDataRequest[A]]] =
        successful(Right(new AuthenticatedDataRequest(request.operator, request, userAnswers)))

      override protected def executionContext: ExecutionContext = ec
    }

}

class HaveExistingCaseDataActionFactory(requestCase: Case)
    extends RequireCaseDataActionFactory(
      casesService = mock(classOf[CasesService]),
      dataCacheService = FakeDataCacheService,
      case_not_found = mock(classOf[case_not_found])
    )(
      mock(classOf[MessagesApi]),
      mock(classOf[ExecutionContext])
    ) {

  override def apply[B[C] <: AuthenticatedRequest[C]](
    reference: String,
    cacheKey: String
  ): ActionRefiner[B, AuthenticatedCaseDataRequest] =
    new ActionRefiner[B, AuthenticatedCaseDataRequest] {
      override protected def refine[A](
        request: B[A]
      ): Future[Either[Result, AuthenticatedCaseDataRequest[A]]] =
        FakeDataCacheService
          .fetch(cacheKey)
          .map {
            case Some(cacheMap) =>
              Right(new AuthenticatedCaseDataRequest(request.operator, request, requestCase, UserAnswers(cacheMap)))
            case None =>
              Left(Redirect(routes.SecurityController.unauthorized()))
          }(ExecutionContext.Implicits.global)

      override protected def executionContext: ExecutionContext = ExecutionContext.Implicits.global
    }
}

class SuccessfulRequestActions(
  parse: PlayBodyParsers,
  operator: Operator,
  c: Case = Cases.btiCaseExample
)(implicit ec: ExecutionContext)
    extends RequestActions(
      new SuccessfulCasePermissionsAction(operator),
      new SuccessfulAuthenticatedAction(parse, operator),
      new ExistingCaseActionFactory(c),
      new HaveRightPermissionsActionFactory,
      new RequireDataActionFactory(FakeDataCacheService),
      new HaveExistingCaseDataActionFactory(c)
    ) {}

class RequestActionsWithPermissions(
  parse: PlayBodyParsers,
  permissions: Set[Permission],
  addViewCasePermission: Boolean = true,
  c: Case = Cases.btiCaseExample,
  op: Operator = Operator("0", Some("name"))
)(implicit ec: ExecutionContext)
    extends RequestActions(
      new SuccessfulCasePermissionsAction(
        operator = op,
        permissions = if (addViewCasePermission) permissions ++ Set(Permission.VIEW_CASES) else permissions
      ),
      new SuccessfulAuthenticatedAction(
        parse,
        operator = op,
        permissions = if (addViewCasePermission) permissions ++ Set(Permission.VIEW_CASES) else permissions
      ),
      new ExistingCaseActionFactory(c),
      new MustHavePermissionActionFactory,
      new RequireDataActionFactory(FakeDataCacheService),
      new HaveExistingCaseDataActionFactory(c)
    ) {}

class RequestActionsWithPermissionsAndData(
  parse: PlayBodyParsers,
  permissions: Set[Permission],
  userAnswers: UserAnswers,
  addViewCasePermission: Boolean = true,
  c: Case = Cases.btiCaseExample,
  op: Operator = Operator("0", Some("name"))
)(implicit ec: ExecutionContext)
    extends RequestActions(
      new SuccessfulCasePermissionsAction(
        operator = op,
        permissions = if (addViewCasePermission) permissions ++ Set(Permission.VIEW_CASES) else permissions
      ),
      new SuccessfulAuthenticatedAction(
        parse,
        operator = op,
        permissions = if (addViewCasePermission) permissions ++ Set(Permission.VIEW_CASES) else permissions
      ),
      new ExistingCaseActionFactory(c),
      new MustHavePermissionActionFactory,
      new MustHaveDataActionFactory(userAnswers),
      new HaveExistingCaseDataActionFactory(c)
    ) {}
