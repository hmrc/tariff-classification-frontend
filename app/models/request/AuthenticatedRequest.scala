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

package models.request

import models.{Case, Operator, Permission, UserAnswers}
import play.api.mvc.{Request, WrappedRequest}

abstract class OperatorRequest[A](_operator: Operator, _request: Request[A]) extends WrappedRequest[A](_request) {
  val operator: Operator
  val request: Request[A] = _request

  def hasPermission(permission: Permission*): Boolean = _operator.hasPermissions(permission.toSet)
}

class AuthenticatedRequest[A](_operator: Operator, _request: Request[A])
    extends OperatorRequest[A](_operator, _request) {

  val operator: Operator = _operator
}

object AuthenticatedRequest {
  def apply[A](_operator: Operator, _request: Request[A]) = new AuthenticatedRequest(_operator, _request)
}

class AuthenticatedCaseRequest[A](operator: Operator, request: Request[A], requestedCase: Case)
    extends AuthenticatedRequest[A](operator, request) {
  val `case`: Case = requestedCase
}

class AuthenticatedDataRequest[A](operator: Operator, request: Request[A], val userAnswers: UserAnswers)
    extends AuthenticatedRequest[A](operator, request)

class AuthenticatedCaseDataRequest[A](
  operator: Operator,
  request: Request[A],
  requestedCase: Case,
  val userAnswers: UserAnswers
) extends AuthenticatedRequest[A](operator, request) {
  val `case`: Case = requestedCase
}
