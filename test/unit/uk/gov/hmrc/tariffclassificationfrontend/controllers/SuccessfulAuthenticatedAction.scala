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
import play.api.mvc.{ActionRefiner, Request, Result}
import play.api.{Configuration, Environment}
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.connector.StrideAuthConnector
import uk.gov.hmrc.tariffclassificationfrontend.models.{Case, Operator}
import uk.gov.hmrc.tariffclassificationfrontend.models.request.AccessType._
import uk.gov.hmrc.tariffclassificationfrontend.models.request.{AuthenticatedCaseRequest, AuthenticatedRequest}
import uk.gov.hmrc.tariffclassificationfrontend.service.CasesService
import uk.gov.tariffclassificationfrontend.utils.Cases

import scala.concurrent.Future
import scala.concurrent.Future.successful

class SuccessfulAuthenticatedAction(operator: Operator = Operator("0", Some("name"))) extends AuthenticatedAction(
  appConfig = mock(classOf[AppConfig]),
  config = mock(classOf[Configuration]),
  env = mock(classOf[Environment]),
  authConnector = mock(classOf[StrideAuthConnector])) {

  override def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]): Future[Result] = {
    block(new AuthenticatedRequest(operator, request))
  }
}

class SuccessfulMustHaveWritePermissionAction(operator: Operator = Operator("0", Some("name")), accessType: AccessType = READ_WRITE) extends MustHaveWritePermissionAction {
  protected override def filter[A](request: AuthenticatedCaseRequest[A]): Future[Option[Result]] = successful(None)
}

class SuccessfulCheckPermissionsAction(operator: Operator = Operator("0", Some("name")), accessType: AccessType = READ_WRITE) extends CheckPermissionsAction {
  override def refine[A](request: AuthenticatedCaseRequest[A]): Future[Either[Result, AuthenticatedCaseRequest[A]]] = {
    successful(Right(new AuthenticatedCaseRequest(operator, request, accessType, Cases.btiCaseExample)))
  }
}

class ExistingCaseActionFactory(reference: String, requestCase : Case = Cases.btiCaseExample) extends VerifyCaseExistsActionFactory(casesService = mock(classOf[CasesService])) {

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


class SuccessfulRequestActions(operator: Operator, reference: String = "test-reference", requestedCase : Case)
  extends RequestActions(
    new SuccessfulMustHaveWritePermissionAction(operator),
    new SuccessfulCheckPermissionsAction(operator),
    new SuccessfulAuthenticatedAction(operator),
    new ExistingCaseActionFactory(reference, requestedCase)
  ) {

}