/*
 * Copyright 2022 HM Revenue & Customs
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
import models.Permission
import models.request._
import play.api.mvc.ActionFunction

@Singleton
class RequestActions @Inject() (
  checkPermissionsAction: CheckCasePermissionsAction,
  authenticatedAction: AuthenticatedAction,
  caseExistsActionFactory: VerifyCaseExistsActionFactory,
  mustHavePermissionActionFactory: MustHavePermissionActionFactory,
  requireDataActionFactory: RequireDataActionFactory,
  requireCaseDataActionFactory: RequireCaseDataActionFactory
) {

  val authenticated: AuthenticatedAction = authenticatedAction

  def casePermissions(reference: String): ActionFunction[AuthenticatedRequest, AuthenticatedCaseRequest] =
    mustHave(Permission.VIEW_CASES) andThen caseExistsActionFactory(reference) andThen checkPermissionsAction

  def mustHave[B[A] <: OperatorRequest[A]](permission: Permission): ActionFunction[B, B] =
    mustHavePermissionActionFactory[B](permission)

  def mustHaveOneOf[B[A] <: OperatorRequest[A]](permissions: Seq[Permission]): ActionFunction[B, B] =
    mustHavePermissionActionFactory[B](permissions)

  def requireData[B[A] <: OperatorRequest[A]](cacheKey: String): ActionFunction[B, AuthenticatedDataRequest] =
    requireDataActionFactory(cacheKey)

  def requireCaseData[B[A] <: AuthenticatedRequest[A]](reference: String, cacheKey: String): ActionFunction[B, AuthenticatedCaseDataRequest] =
    requireCaseDataActionFactory(reference, cacheKey)
}
