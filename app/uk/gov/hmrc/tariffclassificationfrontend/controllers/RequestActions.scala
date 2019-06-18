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
import play.api.mvc.ActionFunction
import uk.gov.hmrc.tariffclassificationfrontend.models.Permission
import uk.gov.hmrc.tariffclassificationfrontend.models.request.{AuthenticatedCaseRequest, AuthenticatedRequest, OperatorRequest}

@Singleton
class RequestActions @Inject()(checkPermissionsAction: CheckCasePermissionsAction,
                               authenticatedAction: AuthenticatedAction,
                               caseExistsActionFactory: VerifyCaseExistsActionFactory,
                               mustHavePermissionActionFactory: MustHavePermissionActionFactory) {

  val authenticated: AuthenticatedAction = authenticatedAction

  def casePermissions(reference: String): ActionFunction[AuthenticatedRequest, AuthenticatedCaseRequest] =
    mustHave(Permission.VIEW_CASES) andThen caseExistsActionFactory(reference) andThen checkPermissionsAction

  def mustHave[B[A] <: OperatorRequest[A]](permission: Permission): ActionFunction[B, B] = mustHavePermissionActionFactory[B](permission)
}
